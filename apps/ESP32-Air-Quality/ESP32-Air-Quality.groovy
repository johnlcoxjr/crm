/*
*   1.0.0   7/11/20     initial
*/

import hubitat.helper.InterfaceUtils

metadata {
	definition (
    name: "ESP32 Air Quality",
    version: "1.0.0",
    namespace: "johnlcox", 
    author: "john.l.cox@live.com",
    importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/ESP32-Air-Quality.groovy")
    {
		capability "Sensor"
        
        attribute "pm010","number"
        attribute "pm025","number"
        attribute "pm100","number"
        attribute "part003","number"
        attribute "part005","number"
        attribute "part010","number"
        attribute "part025","number"
        attribute "part050","number"
        attribute "part100","number"
        attribute "gas_co2","number"
        attribute "gas_voc","number"

        command "getVersion"
        command "getData"
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

        else if (msg.contains("Version")) {
            sendEvent(name: "msg", value: "${msg}")

            if (showLogs) log.info("version = ${msg}")
        }

        else if (msg.contains("REBOOT")) {
            sendEvent(name: "msg", value: "${msg}")

            if (showLogs) log.info("${msg}")
        }

        else {   
            def parts = msg.tokenize('|')
            
            sendEvent(name: 'pm010', value: parts[0])
            sendEvent(name: 'pm025', value: parts[1])
            sendEvent(name: 'pm100', value: parts[2])
            sendEvent(name: 'part003', value: parts[3])
            sendEvent(name: 'part005', value: parts[4])
            sendEvent(name: 'part010', value: parts[5])
            sendEvent(name: 'part025', value: parts[6])
            sendEvent(name: 'part050', value: parts[7])
            sendEvent(name: 'part100', value: parts[8])
            sendEvent(name: 'gas_co2', value: parts[9])
            sendEvent(name: 'gas_voc', value: parts[10])
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

def getData() {
	if (showLogs) log.info("Sent to device:  data")
    
    sendEthernet("data")
}

def reboot() {
	if (showLogs) log.info("Sent to device:  reboot")
    
    sendEthernet("reboot")
}