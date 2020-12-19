/*  Alarm Supplement
 *
 *  1.0.0 	12/19/18   initial
 *  1.1.0   1/3/19	    Updated to match latest Arduino build (1.1.x)
 *  2.0.0   10/2/19    ported to Hubitat
 *  2.0.1   11/10/19	added debug
 *  2.0.2	12/22/19	fixed backlight bug
 *  3.0.0   9/2/20    removed relays and garage doors - new doors do not support the functionality
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
        capability "Contact Sensor"
        capability "Water Sensor"
          
        attribute "water", "string"
        attribute "contact", "string"
        attribute "version", "string"
        attribute "msg", "string"
        attribute "window1", "string"
        attribute "window2", "string"
        attribute "refresh", "string"
        
        command "version"
        command "Backlight"
        command "Reset"
        command "Refresh"

	}
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

def version() {
	if (showLogs) log.info("Sent to device:  version")
    
    sendThingShield("version")
}

def Backlight() {
	if (showLogs) log.info("Sent to device:  backlight")
    
    sendThingShield("[BACKLIGHT]")
}

def Reset() {
	if (showLogs) log.info("Sent to device:  reset")
    
    sendThingShield("[RESET]")
}

def sendThingShield(String message) {
    def encodedString = DataType.pack(message, DataType.STRING_CHAR).substring(2)
	return "he raw 0x${device.deviceNetworkId} 1 0x${device.endpointId} 0x0 {00000a0a" + encodedString + "}"
}