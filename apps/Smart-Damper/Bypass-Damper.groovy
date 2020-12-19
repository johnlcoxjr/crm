/**
*  Automatic Bypass Damper Device Type
*	John Cox 
*
*   2.0.0   	10/2/19     	ported to Hubitat
*   2.0.1	11/10/19	added debug
*/
 
import hubitat.helper.InterfaceUtils

metadata {
	preferences {
    	section("Preferences") {
        	input "showLogs", "bool", required: false, title: "Show Debug Logs?", defaultValue: false
    	}
	}
	
	definition (name: "Bypass Damper", version: "2.0.1", namespace: "johnlcox", author: "john.l.cox@live.com",importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/Bypass-Damper.groovy") {
        capability "Sensor"
        capability "Actuator"
        
        command "open"
        command "close"
        command "damperOpen"
        command "sync"
        command "release"
        command "engage"
        command "debug"
        command "version"
        command "set0"
        command "set1"
        command "set2"
        command "set3"
        command "set4"
        command "set5"
        command "set6"
        command "set7"
        command "set8"
        command "set9"
        command "set10"
        command "setBacklight"
        
        attribute "setpoint", "string"
        attribute "system", "string"
        attribute "msg", "string"
    }
}

def parse(String description) {  
	def msg = zigbee.parse(description)?.text    

    if (msg != null) { 
        if (msg.contains("ping") || msg.equals("")) {
            sendEvent(name: "msg", value: "Ready")
        }
        else if (msg.contains("Version")) {
            sendEvent(name: "msg", value: "${msg}")
        }
        else if (msg.contains("Calibrate")) {
            sendEvent(name: "msg", value: "${msg}")
        }
        else if (msg.contains("0") || msg.contains("1") || msg.contains("2") || msg.contains("3") || msg.contains("4") || msg.contains("5") || msg.contains("6") || msg.contains("7") || msg.contains("8") || msg.contains("9")) {  
            def position = msg.substring(3).toInteger()

            sendEvent(name: "setpoint", value: "${position}")

            sendEvent(name: "msg", value: "Damper open ${position}/10")
        }
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

def damperOpen(int damperValue) {
	if (damperValue == 0) {
    	set0()
    }
    else if (damperValue == 1) {
    	set1()
    }
    else if (damperValue == 2) {
    	set2()
    }
    else if (damperValue == 3) {
    	set3()
    }
    else if (damperValue == 4) {
    	set4()
    }    
    else if (damperValue == 5) {
    	set5()
    }
    else if (damperValue == 6) {
    	set6()
    }    
    else if (damperValue == 7) {
    	set7()
    }
    else if (damperValue == 8) {
    	set8()
    }    
    else if (damperValue == 9) {
    	set9()
    }    
    else {
    	set10()
    }
}

def open() {
    if (showLogs) log.info("Sent to device: open")

    sendEvent(name: "msg", value: "Open")
    
    sendThingShield("open") 
}

def close() {
    if (showLogs) log.info("Sent to device: close")
    
    sendEvent(name: "msg", value: "Close")    
    
    sendThingShield("close") 
}

def set0() {
    if (showLogs) log.info("Sent to device: set0")
    
    sendThingShield("set0") 
}

def set1() {
    if (showLogs) log.info("Sent to device: set1")
    
    sendThingShield("set1") 
}

def set2() {
    if (showLogs) log.info("Sent to device: set2")
    
    sendThingShield("set2") 
}

def set3() {
    if (showLogs) log.info("Sent to device: set3")
    
    sendThingShield("set3")
}

def set4() {
    if (showLogs) log.info("Sent to device: set4")
    
    sendThingShield("set4") 
}

def set5() {
    if (showLogs) log.info("Sent to device: set5")
    
    sendThingShield("set5") 
}

def set6() {
    if (showLogs) log.info("Sent to device: set6")
    
    sendThingShield("set6") 
}

def set7() {
    if (showLogs) log.info("Sent to device: set7")
    
    sendThingShield("set7") 
}

def set8() {
    if (showLogs) log.info("Sent to device: set8")
    
    sendThingShield("set8") 
}

def set9() {
    if (showLogs) log.info("Sent to device: set9")
    
    sendThingShield("set9") 
}

def set10() {
    if (showLogs) log.info("Sent to device: set10")
    
    sendThingShield("set10")
}

def sync() {
	if (showLogs) log.info("Sent to device:  sync")
    
    sendThingShield("sync")
}

def debug() {
	if (showLogs) log.debug("Sent to device:  debug")
    
    sendThingShield("debug") 
}

def version() {
	if (showLogs) log.info("Sent to device:  version")
    
    sendThingShield("version") 
}

def release() {
	if (showLogs) log.info("Sent to device:  release")
    
    sendThingShield("release") 
}

def engage() {
	if (showLogs) log.info("Sent to device:  engage")
    
    sendThingShield("engage")
}

def setBacklight() {
	if (showLogs) log.info("Sent to device:  backlight")
    
    sendThingShield("[BACKLIGHT]")
}

def sendThingShield(String message) {
    def encodedString = DataType.pack(message, DataType.STRING_CHAR).substring(2)
	return "he raw 0x${device.deviceNetworkId} 1 0x${device.endpointId} 0x0 {00000a0a" + encodedString + "}"
}