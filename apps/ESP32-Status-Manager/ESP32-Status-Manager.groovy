/*
*   1.0.0   7/11/20     initial
*/

import hubitat.helper.InterfaceUtils

metadata {
	definition (
    name: "ESP32 Status Manager",
    version: "1.0.0",
    namespace: "johnlcox", 
    author: "john.l.cox@live.com",
    importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/ESP32-Status-Manager.groovy")
    {
		capability "Sensor"
        
        attribute "app_status_manager","string"
        attribute "app_air_quality","string"
        attribute "app_pinger","string"
        attribute "app_scheduler","string"
        attribute "app_sensor","string"
        attribute "app_weather","string"
        attribute "time_status_manager","number"
        attribute "time_air_quality","number"
        attribute "time_pinger","number"
        attribute "time_scheduler","number"
        attribute "time_sensor","number"
        attribute "time_weather","number"

        command "getVersion"
        command "reboot"
        command "initialize"
        command "rebootAll"
        command "versionAll"
        command "dataAll"

	}
    
	preferences {
	    section("Arduino") {
    	    input "ip", "text", title: "Arduino IP Address", description: "ip", required: true, displayDuringSetup: true
	        input "port", "text", title: "Arduino Port", description: "port", required: true, displayDuringSetup: true
            input "mac", "text", title: "Arduino MAC Addr", description: "mac", required: true, displayDuringSetup: true
            input "freq", "text", title: "Ping Frequency", description: "freq", required: true, displayDuringSetup: true, defaultValue: "15"
	    }
	    section("Preferences") {
            input "showLogs", "bool", required: false, title: "Show Debug Logs?", defaultValue: false
    	}
	} 
}

def ping() {
    if (showLogs) log.info("ping")
    
    sendEthernet("ping")
}

def initialize() {
    state.clear()
    unschedule()
    
    state.app_status_manager = now()
    state.app_air_quality = now()
    state.app_pinger = now()
    state.app_scheduler = now()
    state.app_sensor = now()
    state.app_weather = now()
        
    schedule("0/${settings.freq} * * * * ? *", ping)
    schedule("7/20 * * * * ? *",reportTimes)
}

// parse events into attributes
def parse(String description) {
   def msg = parseLanMessage(description).payload
          
    if (showLogs) log.info("description = ${description}")
    
    if (showLogs) log.info("payload = ${msg}")
	    
    msg = hexToASCII(msg);
    if (showLogs) log.info("message = ${msg}") 
   
    if (msg != null) {
        if (msg.contains("ping") || msg.equals("")) {
            sendEvent(name: "msg", value: "Ready")
        }

        else if (msg.contains("Version")) {
            sendEvent(name: "msg", value: "${msg}")

            if (showLogs) log.info("version = ${msg}")
        }

        else {
            def part = msg.tokenize('|')
 
            switch (part[0]) {
                case "0":
                    sendEvent(name: 'app_status_manager', value: part[1])
                    state.app_status_manager = now()
                    break
                case "1":
                    sendEvent(name: 'app_air_quality', value: part[1])
                    state.app_air_quality = now()
                    break
                case "2":
                    sendEvent(name: 'app_pinger', value: part[1])
                    state.app_pinger = now()
                    break                
                case "3":
                    sendEvent(name: 'app_scheduler', value: part[1])
                    state.app_scheduler = now()
                    break            
                case "4":
                    sendEvent(name: 'app_sensor', value: part[1])
                    state.app_sensor = now()
                    break            
                case "5":
                    sendEvent(name: 'app_weather', value: part[1])
                    state.app_weather = now()
                    break
            }    
        }
	}
}

private static String hexToASCII(String hexValue) {
    StringBuilder output = new StringBuilder("");
    for (int i = 0; i < hexValue.length(); i += 2) {
        String str = hexValue.substring(i, i + 2);
        output.append((char) Integer.parseInt(str, 16));
    }
    return output.toString();
}

private getHostAddress() {
    def ip = settings.ip
    def port = settings.port

	if (showLogs) log.info("Using ip: ${ip} and port: ${port} for device: ${device.id}")
    return ip + ":" + port
}

def sendEthernet(message) {
	if (showLogs) log.info("Executing 'sendEthernet' ${message}")
     
    def myHubAction = new hubitat.device.HubAction(message, 
								hubitat.device.Protocol.LAN, 
                                settings.mac,
								[type: hubitat.device.HubAction.Type.LAN_TYPE_UDPCLIENT, 
		                        destinationAddress: getHostAddress(),
                                callback: parse])
    sendHubCommand(myHubAction)
}

def reportTimes() {
    sendEvent(name: 'time_status_manager', value: getElapsedTime(state.app_status_manager))
    sendEvent(name: 'time_air_quality', value: getElapsedTime(state.app_air_quality))
    sendEvent(name: 'time_pinger', value: getElapsedTime(state.app_pinger))
    sendEvent(name: 'time_scheduler', value: getElapsedTime(state.app_scheduler))
    sendEvent(name: 'time_sensor', value: getElapsedTime(state.app_sensor))
    sendEvent(name: 'time_weather', value: getElapsedTime(state.app_weather))
}

def getElapsedTime(lastTime) {
    long elapsed = 0
      
    elapsed = now() - lastTime

    elapsed = elapsed/1000 //seconds
    
    return elapsed  
}    

def getVersion() {
	if (showLogs) log.info("Sent to device:  version")
    
    sendEthernet("version")
}

def reboot() {
	if (showLogs) log.info("Sent to device:  reboot")
    
    sendEthernet("reboot")
}

def rebootAll() {
	if (showLogs) log.info("Sent to device:  rebootAll")
    
    sendEthernet("rebootAll")
}

def versionAll() {
	if (showLogs) log.info("Sent to device:  versionAll")
    
    sendEthernet("versionAll")
}

def dataAll() {
	if (showLogs) log.info("Sent to device:  dataAll")
    
    sendEthernet("dataAll")
}