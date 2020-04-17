/**
*  AR Device Type
*	John Cox 
*
* 	3.0.0    10/2/2019    ported to Hubitat
*	3.0.1	11/10/19	added debug
*/
import hubitat.helper.InterfaceUtils
import groovy.time.*
import java.text.DecimalFormat 
metadata {
	preferences {
    	section("Preferences") {
        	input "showLogs", "bool", required: false, title: "Show Debug Logs?", defaultValue: false
    	}
	}
	
	definition (name: "Sump Sensor", version: "3.0.1", namespace: "johnlcox", author: "johnlcox@live.com",importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/Sump-Sensor.groovy") {
        capability "Sensor"
        capability "Acceleration Sensor"

        command "debug"
        command "version"
        command "setBacklight"
        command "data"
        command "reset"

        attribute "acceleration", "string"
        attribute "gpm", "string"
        attribute "act", "string"
        attribute "gal", "string"      
        attribute "depth", "string"
        attribute "flow","string"
        attribute "discharge", "string"
        attribute "bucket", "string"
        attribute "last", "string"
        attribute "system", "string"
        attribute "msg", "string"
 	}

        main(["gpm"])
        details(["gpm","act","gal","depth","flow","acceleration","bucket","discharge", "min", "backlight","msg","version","debug","reset"])
}

def parse(String description) {  
 	def msg = zigbee.parse(description)?.text
    
	boolean process = true
  	if (state.msg != null) {
    	if (state.msg == msg) {
        	process = false
            
            if (showLogs) log.warn("Ignore message")
      	}
    }

    if ((msg != null) && process) {
        state.msg = msg

		if (msg.contains("ping") || msg.equals("")) {
            sendEvent(name: "msg", value: "Ready")
        }

		else if (msg.contains("|")) {
            def parts = msg.tokenize('|')
            def act = parts[0].toString().toFloat()
            def gal = parts[1].toString().toFloat()
            def gpm = parts[2].toString().toFloat()      
            def depth = parts[3].toString().toFloat()
            def flow = parts[4].toString().toFloat()
            def min = parts[5].toString().toFloat()
            def diff = parts[6].toString().toFloat()
            def bucket = parts[7].toString().toFloat()
           
            if (msg.contains("Vibrate")) {
	            sendEvent(name: "acceleration", value: "active")
    	        sendEvent(name: "acceleration", value: "inactive")
                
                def currentDate = new Date().format('EEE MMM d, h:mm:ss a',location.timeZone)
                                
		        sendEvent(name:"last", value: sprintf("%s", currentDate))
        	}
            
            def str = String.format("%1.0f",act) + " act"
            sendEvent(name: 'act', value: "${str}")
            
            str = String.format("%1.1f",gal) + " gal"
            sendEvent(name: 'gal', value: "${str}")
            
            if (gpm < 1) {
            	str = String.format("%1.2f",gpm) + " gpm"
            }
            else {
            	str = String.format("%1.1f",gpm) + " gpm"
			}
			sendEvent(name: 'gpm', value: "${str}")
            
            str = String.format("%1.1f",depth) + " in"
            sendEvent(name: 'depth', value: "${str}")
            
            if (flow < 1) {
            	str = String.format("%1.2f",flow) + " gpm"
			}
            else {
                str = String.format("%1.1f",flow) + " gpm"
			}
			sendEvent(name: 'flow', value: "${str}")
            
            str = String.format("%1.1f",min) + " min"
            sendEvent(name: 'min', value: "${str}")

            if (diff < 1) {
            	str = String.format("%1.1f",diff * 60) + " sec"
           	}
            else if (diff >= 1440) {
				str = String.format("%1.1f",diff / 1440) + " day"
            }
            else if (diff >= 100) {
				str = String.format("%1.1f",diff / 60) + " hr"
            }
          	else {   
            	str = String.format("%1.1f",diff) + " min"
            }
            sendEvent(name: 'discharge', value: "${str}")

            str = String.format("%1.1f",bucket) + " gal"
            sendEvent(name: 'bucket', value: "${str}")

            sendEvent(name: "msg", value: msg)
 		}
        //version
        else if (msg.contains("Version")) {
            sendEvent(name: "msg", value: "${msg}")
        }
        //vibrate
        else if (msg.equals("Vibrate")) {
            sendEvent(name: "acceleration", value: "active")
            sendEvent(name: "acceleration", value: "inactive")
            
            sendEvent(name: "msg", value: msg)
        }
        //backlight
        else if (msg.contains("BON")) {
            sendEvent(name: "backlight", value: "on", displayed: true, isStateChange: true, isPhysical: true)   
        }
        else if (msg.contains("BOF")) {
            sendEvent(name: "backlight", value: "off", displayed: true, isStateChange: true, isPhysical: true)   
        }        
        
        else {  
            sendEvent(name: "msg", value: msg)
        }
 	}
}

def debug() {
	if (showLogs) log.debug("Sent to device:  debug")
    
    sendThingShield("debug")
}

def version() {
	if (showLogs) log.info("Sent to device:  version")
    
    sendThingShield("version")
}

def data() {
	if (showLogs) log.info("Sent to device:  data")
    
    sendThingShield("data") 
}

def reset() {
	if (showLogs) log.info("Sent to device:  reset")
    
    sendThingShield("reset") 
}

def setBacklight() {
	if (showLogs) log.info("Sent to device:  backlight")
    
    sendThingShield("[BACKLIGHT]")
}

def sendThingShield(String message) {
    def encodedString = DataType.pack(message, DataType.STRING_CHAR).substring(2)
	return "he raw 0x${device.deviceNetworkId} 1 0x${device.endpointId} 0x0 {00000a0a" + encodedString + "}"
}