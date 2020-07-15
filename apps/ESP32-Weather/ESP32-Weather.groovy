/*
*   1.0.0   7/11/20     initial
*/

import hubitat.helper.InterfaceUtils

metadata {
	definition (
    name: "ESP32 Weather",
    version: "1.0.0",
    namespace: "johnlcox", 
    author: "john.l.cox@live.com",
    importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/ESP32-Weather.groovy")
    {
		capability "Sensor"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Illuminance Measurement"       
        
        attribute "temperature","number"
        attribute "humidity","number"
        attribute "dewpoint","number"
        attribute "windchill","number"
        attribute "heatindex","number"
        attribute "soil_temperature","number"
        attribute "soil_moisture","number"
        attribute "leaf","number"
        attribute "uv","number"
        attribute "solar","number"
        attribute "illuminance","number"
        attribute "rain_rate","number"
        attribute "rain_total","number"
        attribute "rain_day","number"
        
        attribute "wind_dir","number"
        attribute "wind_speed","number"
        attribute "wind_average","number"
        attribute "wind_gust","number"
        attribute "in_temperature","number"
        attribute "in_humidity","number"
        //attribute "in_dewpoint","number"
        //attribute "act_pressure","number"
        attribute "sea_pressure","number"
        
        /*attribute "system_load","number"
        attribute "system_uptime","number"
        attribute "system_storage","number"
        attribute "system_ram","number"
        attribute "system_processes","number"
        attribute "system_last","number"*/

        command "getVersion"
        command "getPCVersion"
        command "reboot"
        command "initialize"

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
    
    main(["weather"])
    details(["weather", "msg", "version"])    
}

def ping() {
    if (showLogs) log.info("ping")
    
    sendEthernet("ping")
}

def initialize() {
    state.clear()
    unschedule()
    
    schedule("0/${settings.freq} * * * * ? *", ping)
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

        else if (msg.contains("Part1")) {   
            //log.debug(">>>part1")
            def part1 = msg.tokenize('|')
            
            sendEvent(name: 'temperature', value: part1[1])
            sendEvent(name: 'humidity', value: part1[2])
            sendEvent(name: 'dewpoint', value: part1[3])
            sendEvent(name: 'windchill', value: part1[4])
            sendEvent(name: 'heatindex', value: part1[5])
            sendEvent(name: 'soil_temperature', value: part1[6])
            sendEvent(name: 'soil_moisture', value: part1[7])
            sendEvent(name: 'leaf', value: part1[8])
            sendEvent(name: 'uv', value: part1[9])
            sendEvent(name: 'wind_dir', value: part1[10])
            sendEvent(name: 'wind_speed', value: part1[11])
            sendEvent(name: 'wind_average', value: part1[12])
            sendEvent(name: 'wind_gust', value: part1[13])
        }
        else if (msg.contains("Part2")) {       
            //log.debug(">>>part2")
            def part2 = msg.tokenize('|')
            
            sendEvent(name: 'solar', value: part2[1]) 
            sendEvent(name: 'illuminance', value: part2[2])
            sendEvent(name: 'in_temperature', value: part2[3])
            sendEvent(name: 'in_humidity', value: part2[4])
            //sendEvent(name: 'in_dewpoint', value: part2[5)
            //sendEvent(name: 'act_pressure', value: part2[6])
            sendEvent(name: 'sea_pressure', value: part2[7])
            sendEvent(name: 'rain_rate', value: part2[8])
            sendEvent(name: 'rain_total', value: part2[9])
            sendEvent(name: 'rain_day', value: part2[10])
        }
        /*else if (msg.contains("Part3"))   {   
            log.debug(">>>part3")
            def part3 = msg.tokenize('|')
            
            sendEvent(name: 'system_load', value: part3[1])
            sendEvent(name: 'system_uptime', value: part3[2])
            sendEvent(name: 'system_storage', value: part3[3])
            sendEvent(name: 'system_ram', value: part3[4])
            sendEvent(name: 'system_processes', value: part3[5])
            sendEvent(name: 'system_last', value: part3[6])
        }*/
        else if (msg.contains("Version")) {
            sendEvent(name: "msg", value: "${msg}")

            if (showLogs) log.info("version = ${msg}")
        }
        
        else if (msg.contains("PCVersion")) {
            sendEvent(name: "msg", value: "${msg}")

            if (showLogs) log.info("pc version = ${msg}")
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

def getVersion() {
	if (showLogs) log.info("Sent to device:  version")
    
    sendEthernet("version")
}

def getPCVersion() {
	if (showLogs) log.info("Sent to device:  pcversion")
    
    sendEthernet("pcversion")
}

def reboot() {
	if (showLogs) log.info("Sent to device:  reboot")
    
    sendEthernet("reboot")
}