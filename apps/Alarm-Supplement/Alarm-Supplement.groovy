/*  Alarm Supplement
 *
 *  1.0.0 	12/19/18   initial
 *  1.1.0   1/3/19	    Updated to match latest Arduino build (1.1.x)
 *  2.0.0   10/2/19    ported to Hubitat
 *  2.0.1   11/10/19	added debug
 *  2.0.2	12/22/19	fixed backlight bug
 *
 *  Copyright 2016 John Cox
 *
 *  Developer retains all right, title, copyright, and interest, including all copyright, patent rights, trade secret in the Background technology.
 *  May be subject to consulting fees under the Agreement between the Developer and the Customer. Developer grants a non-exclusive perpetual license
 *  to use the Background technology in the Software developed for and delivered to Customer under this Agreement. However, the Customer shall make
 *  no commercial use of the Background technology without Developer's written consent.
 *  
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied. 
 *
 *  Software Distribution is restricted and shall be done only with Developer's written approval.
 */

import hubitat.helper.InterfaceUtils
import java.text.DecimalFormat 

metadata {
	preferences {
    	section("Preferences") {
        	input "showLogs", "bool", required: false, title: "Show Debug Logs?", defaultValue: false
    	}
	}
	
	definition (name: "Alarm Supplement", version: "2.0.2", namespace: "johnlcox", author: "john.l.cox@live.com",importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/Alarm-Supplement.groovy") {
		//capability "Alarm"
        capability "Sensor"
        capability "Actuator"
        capability "Contact Sensor"
        capability "Water Sensor"
        capability "Switch"
          
        attribute "water", "string"
        attribute "contact", "string"
        attribute "version", "string"
        attribute "msg", "string"
        attribute "relay1", "string"
        attribute "relay2", "string"
        attribute "door1", "string"
        attribute "door2", "string"
        attribute "window1", "string"
        attribute "window2", "string"
        attribute "refresh", "string"
        
        command "version"
        command "pushRelay1"
        command "pushRelay2"
        command "setBacklight"
        command "setReset"
        command "setRefresh"
		command "off"
        command "on"
	}
	   
	main(["water"])
	details(["water", "version", "window1", "window2", "reset", "relay1", "relay2", "backlight", "door1", "door2", "msg"])
}

def parse(String description) {
    def msg = zigbee.parse(description)?.text   

    if (msg != null) { 
	    //ping
    	if (msg.contains("ping")) {
    		sendEvent(name: "msg", value: "Ready")
		}
    
	    //water sensor
    	if (msg.contains("WWT")) {   
        	sendEvent(name: "water", value: "wet", displayed: true, isStateChange: true, isPhysical: true) 
	        sendEvent(name: "msg", value: "Sump is Wet")
    	}
    	if (msg.contains("WDY")) {
        	sendEvent(name: "water", value: "dry", displayed: true, isStateChange: true, isPhysical: true)   
        	sendEvent(name: "msg", value: "Sump is Dry")
    	}
         	
    	//window1 sensor
		if (msg.contains("W1O")) {
    	    sendEvent(name: "window1", value: "open", displayed: true, isStateChange: true, isPhysical: true)  
        	sendEvent(name: "contact", value: "open", displayed: true, isStateChange: true, isPhysical: true)      
        	sendEvent(name: "msg", value: "East window is open")            
   		}
    	if (msg.contains("W1C")) {
        	sendEvent(name: "window1", value: "closed", displayed: true, isStateChange: true, isPhysical: true)      
        	sendEvent(name: "contact", value: "closed", displayed: true, isStateChange: true, isPhysical: true)      
        	sendEvent(name: "msg", value: "East window is closed")    
   		}

    	//window2 sensor
		if (msg.contains("W2O")) {
        	sendEvent(name: "window2", value: "open", displayed: true, isStateChange: true, isPhysical: true)  
	        sendEvent(name: "contact", value: "open", displayed: true, isStateChange: true, isPhysical: true)      
    	    sendEvent(name: "msg", value: "South window is open")            
    	}
	    if (msg.contains("W2C")) {
    	    sendEvent(name: "window2", value: "closed", displayed: true, isStateChange: true, isPhysical: true)      
        	sendEvent(name: "contact", value: "closed", displayed: true, isStateChange: true, isPhysical: true)      
	        sendEvent(name: "msg", value: "South window is closed")    
    	}
     
	    //door1 sensor
		if (msg.contains("D1O")) {
	        sendEvent(name: "door1", value: "open", displayed: true, isStateChange: true, isPhysical: true)  
	        sendEvent(name: "msg", value: "East overhead door is open")             	
	    }
	    if (msg.contains("D1C")) {
	        sendEvent(name: "door1", value: "closed", displayed: true, isStateChange: true, isPhysical: true)      
	        sendEvent(name: "msg", value: "East overhead door is closed")             	
	    }

	    //door2 sensor
		if (msg.contains("D2O")) {
	        sendEvent(name: "door2", value: "open", displayed: true, isStateChange: true, isPhysical: true)  
	        sendEvent(name: "msg", value: "West overhead door is open")             	
	    }
		if (msg.contains("D2C")) {
	        sendEvent(name: "door2", value: "closed", displayed: true, isStateChange: true, isPhysical: true)      
    	    sendEvent(name: "msg", value: "West overhead door is closed")             	
	     }

    	//relays/actuators
	    if (msg.contains("Relay East")) {        
    		sendEvent(name: "msg", value: "East opener activated") 
		}
	    if (msg.contains("Relay West")) {
    	    sendEvent(name: "msg", value: "West opener activated")     
		}
    
    	//backlight
    	if (msg.contains("BON")) {
        	sendEvent(name: "backlight", value: "on", displayed: true, isStateChange: true, isPhysical: true)   
    	}
    	if (msg.contains("BOF")) {
        	sendEvent(name: "backlight", value: "off", displayed: true, isStateChange: true, isPhysical: true)   
	    }

    	//message
    	if (msg.contains("Version")) {
 	   		sendEvent(name: "msg", value: "${msg}")
    	}

    	//ready
    	if (msg.contains("Ready")) {
    		sendEvent(name: "msg", value: "Ready") 
    	}
    
    	//reset
    	if (msg.contains("Reset")) {
        	sendEvent(name: "msg", value: "Reset complete")         
    	}
	}
	else {
		sendEvent(name: "msg", value: "Ready") 	
	}
}

def pushRelay1() {   
    if (showLogs) log.info("Sent to device:  relayEast ")
    
    sendThingShield("[RELAY-EAST]")
}

def pushRelay2() {
    if (showLogs) log.info("Sent to device:  relayWest")

    sendThingShield("[RELAY-WEST]") 
}

def version() {
	if (showLogs) log.info("Sent to device:  version")
    
    sendThingShield("[VERSION]")
}

def setBacklight() {
	if (showLogs) log.info("Sent to device:  backlight")
    
    sendThingShield("[BACKLIGHT]")
}

def setReset() {
	if (showLogs) log.info("Sent to device:  reset")
    
    sendThingShield("[RESET]")
}

def on() {
    pushRelay1()
}

def off() {
    pushRelay1()
}

def sendThingShield(String message) {
    def encodedString = DataType.pack(message, DataType.STRING_CHAR).substring(2)
	return "he raw 0x${device.deviceNetworkId} 1 0x${device.endpointId} 0x0 {00000a0a" + encodedString + "}"
}
