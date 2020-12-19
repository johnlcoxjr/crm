/*
*   3.0.0   10/2/2019   ported to Hubitat    
*	3.0.1	11/10/19	added debug
*/

import hubitat.helper.InterfaceUtils

metadata {
	preferences {
    	section("Preferences") {
        	input "showLogs", "bool", required: false, title: "Show Debug Logs?", defaultValue: false
    	}
	}

	definition (name: "Radiation Sensor", version: "3.0.1", namespace: "johnlcox", author: "john.l.cox@live.com",importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/Radiation.groovy") {
        capability "Sensor"

        command "debug"
        command "version"
        command "getRadiation"
        command "setBacklight"

        attribute "radiation","string"
        attribute "system", "string"
        attribute "msg", "string"
        attribute "version", "string"
 	}
}

def parse(String description) {  
 	def msg = zigbee.parse(description)?.text
    
    if (msg != null) {
    	//ping
        if (msg.contains("ping") || msg.equals("")) {
            sendEvent(name: "msg", value: "Ready")
        }
        //version
        else if (msg.contains("Version")) {
            sendEvent(name: "version", value: "${msg}")
        }
		//radiation
        else if (msg.contains("pCi")) {
            int vernierValue = getValue(msg).toFloat()

            if (msg.contains(":")) {
                state.radiation = vernierValue
                                              
				def value = vernierValue + " pCi"
                
                sendEvent(name: "radiation", value: "${value}")
                
                sendEvent(name: "msg", value: "Rad: ${value}")                   
 			}
        } 
        //backlight
        else if (msg.contains("BON")) {
            sendEvent(name: "backlight", value: "on", displayed: true, isStateChange: true, isPhysical: true)   
        }
        else if (msg.contains("BOF")) {
            sendEvent(name: "backlight", value: "off", displayed: true, isStateChange: true, isPhysical: true)   
        }        
        
        else {  
            sendEvent(name: "msg", value: "Ready")
        }
 	}
	else {  
		sendEvent(name: "msg", value: "Ready")
	}	
}

def getValue(msg) {
  	def parts = msg.tokenize(':')
    int value = parts[0].toInteger()
	def parameter = parts[1]

	return value
}

def getRadiation() {
	if (showLogs) log.info("Sent to device:  radiation")
    
    zsendThingShield("radiation")
}

def debug() {
	if (showLogs) log.debug("Sent to device:  debug")
    
    sendThingShield("debug")
}

def version() {
	if (showLogs) log.info("Sent to device:  version")
    
    sendThingShield("version") 
}

def setBacklight() {
	if (showLogs) log.info("Sent to device:  backlight")
    
    sendThingShield("[BACKLIGHT]")
}

def sendThingShield(String message) {
    def encodedString = DataType.pack(message, DataType.STRING_CHAR).substring(2)
	return "he raw 0x${device.deviceNetworkId} 1 0x${device.endpointId} 0x0 {00000a0a" + encodedString + "}"
}