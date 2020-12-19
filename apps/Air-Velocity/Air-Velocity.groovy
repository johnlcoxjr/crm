/*
*   1.0.0   12/14/20   initial    
*/

import hubitat.helper.InterfaceUtils

metadata {
	preferences {
    	section("Preferences") {
        	input "showLogs", "bool", required: false, title: "Show Debug Logs?", defaultValue: false
    	}
	}

	definition (name: "Air Velocity", version: "0.0.0", namespace: "johnlcox", author: "john.l.cox@live.com",importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/Air-Velocity.groovy") {
        capability "Sensor"

        command "debug"
        command "version"
        command "getVelocity"
        command "setBacklight"
        command "initialize"

        attribute "velocity","string"
        attribute "system", "string"
        attribute "msg", "string"
        attribute "version", "string"
 	}
}

def initialize() {
    state.clear()
    unschedule()
}

def parse(String description) {  
 	def msg = zigbee.parse(description)?.text
    
    if (showLogs) log.debug(msg)
    
    if (msg != null) {
    	//ping
        if (msg.contains("ping") || msg.equals("")) {
            sendEvent(name: "msg", value: "Ready")
        }
        //version
        else if (msg.contains("Version")) {
            sendEvent(name: "version", value: "${msg}")
        }
		//velocity
        else if (msg.contains("m/s")) {
            float vernierValue = getValue(msg).toFloat()

            if (msg.contains(":")) {                                             
				def value = vernierValue //+ " m/s"
                
                sendEvent(name: "velocity", value: "${value}")
                
                //sendEvent(name: "msg", value: "Velocity: ${value}")                   
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
    float value = parts[0].toFloat()
	def parameter = parts[1]

	return value
}

def getVelocity() {
	if (showLogs) log.info("Sent to device:  velocity")
    
    sendThingShield("getVelocity")
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