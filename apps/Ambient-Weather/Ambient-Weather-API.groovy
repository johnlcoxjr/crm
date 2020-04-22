import hubitat.helper.InterfaceUtils

definition(name: "Ambient Weather API", namespace: "CordMaster", author: "Alden Howard", description: "A simple api for providing ambient weather access", iconUrl: "", iconX2Url: "",importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Ambient-Weather-API.groovy") 

preferences {
    page(name: "page1", title: "Log In", nextPage: "page2", uninstall: true) {
        section {
            input(name: "apiKey", title: "API Key", type: "text", required: true);
            input(name: "applicationKey", title: "Application Key", type: "text", required: true);
        }
		
		section("-= <b>Debug Menu</b> =-") {
			input "enableDebug", "bool", title: "Enable debug output?", required: false, defaultValue: false
		}		
    }
    
    page(name: "page2");
    page(name: "page3");
}

def page2() {
    def stations = [];
    def stationMacs = [];
    try {
        stations = getStations();
        
        stations.each { stationMacs << it.macAddress };
    } catch(groovyx.net.http.HttpResponseException e) {
        //then unauthorized
        return dynamicPage(name: "page2", title: "Error", nextPage: "page1", uninstall: true) {
            section {
                paragraph("There was an error authorizing you. Please try again.");
            }
        }
    }
    
	if (enableDebug) {log.debug("Got stations: " + stations)}
    
	return dynamicPage(name: "page2", title: "Select Station", nextPage: "page3", uninstall: true) {
		section {
			input(name: "station", title: "Station", type: "enum", options: stationMacs, required: true);
            input(name: "refreshInterval", title: "Refresh Interval (in minutes)", type: "number", range: "5..3600", defaultValue: 5, required: true);
		}
	}
}

def page3() {
    dynamicPage(name: "page3", title: "Confirm Settings", install: true, uninstall: true) {
        section {
            paragraph("Selected station: $station");
            paragraph("Refresh interval: $refreshInterval minute(s)");
        }
        
        section {
            paragraph("Press done to finish");
        }
    }
}

//lifecycle functions
def installed() {
	if (enableDebug) {log.debug("Installed")}
        
    //addDevice(); //moved to end, otherwise won't schedule when API loses the schedule due to device already existing

    initialize();
    
    runEvery5Minutes(fetchNewWeather);
	
	addDevice(); //moved to end, otherwise won't schedule when API loses the schedule due to device already existing
}

def updated() {
	if (enableDebug) {log.debug("Updated")}
    
    unsubscribe();
    unschedule();
    installed();
    initialize(); 
}

def initialize() {
    fetchNewWeather();
    
    //chron schedule, refreshInterval is int
    def m = refreshInterval;
    def h = Math.floor(m / 60);
    m -= h * 60;
    
    m = m == 0 ? "*" : "0/" + m.toInteger();
    h = h == 0 ? "*" : "0/" + h.toInteger();
    
	if (enableDebug) {log.debug("Set CHRON schedule with m: $m and h: $h")}
    
    schedule("0 $m $h * * ? *", fetchNewWeather);
}

//children
def addDevice() {
	try {
		addChildDevice("CordMaster", "Ambient Weather", "AWTILE-$station", null, [completedSetup: true]);
	}
	catch (errorException) {
		log.error "... Unable to create device: ${errorException}."
	}
}

def shouldProcess() {
    def elapsed = 0
    
    if (atomicState.success == null) { //need to process, never run before
        elapsed = 4 //minutes
    }
    else {
        elapsed = now() - atomicState.success
		
        elapsed = elapsed/1000/60 //convert to minutes
    }

    if (elapsed < 4.75) { //success just recently happened, so we should bail out of process routine
    	if (atomicState.success != null) {
    		log.warn("shouldProcess: false, elapsed minutes ${elapsed}, last success (${new Date(atomicState.success).format("EEE MMM dd yyyy HH:mm z", location.timeZone)})")
      	}
        else {
    		log.warn("shouldProcess: false, elapsed minutes NULL")
        }
        
    	return false
    }
    else {
    	if (atomicState.success != null) {
	    	if (enableDebug) {log.info("shouldProcess: true, elapsed minutes ${elapsed}, last success (${new Date(atomicState.success).format("EEE MMM dd yyyy HH:mm z", location.timeZone)})")}
    	}
        else {
			if (enableDebug) {log.info("shouldProcess: true, elapsed minutes NULL")}
        }
    	
        return true
   	}	
}

//fetch functions
def getStations() throws groovyx.net.http.HttpResponseException {
    def data = [];
    
    def params = [
        uri: "https://api.ambientweather.net/",
        path: "/v1/devices",
        query: [applicationKey: applicationKey, apiKey: apiKey]
    ];
    
    requestData("/v1/devices", [applicationKey: applicationKey, apiKey: apiKey]) { response ->
        data = response.data;
    };
        
    return data;
}

def getWeather() throws groovyx.net.http.HttpResponseException {
    def data = [];
    
    requestData("/v1/devices/$station", [applicationKey: applicationKey, apiKey: apiKey, limit: 1]) { response ->
        data = response.data;
    };
        
	return data[0];
}

def requestData(path, query, code) {
	def current = now()
	
	def params = [
		uri: "https://api.ambientweather.net/",
		path: path,
		query: query
	];

	try {
		httpGet(params) { response ->
			code(response);
		};

		atomicState.success = current
	}
	catch (errorException) {
		log.error "... Unable retrieve data: ${errorException}."
	}
}

//loop
def fetchNewWeather() {
    if (shouldProcess()) {   
		def weather = getWeather();

		if (enableDebug) {log.debug("Weather: " + weather)}

		childDevices[0].setWeather(weather);
	}
	else {
		log.warn("too many calls, suppressing fetchNewWeather")
	}
}
