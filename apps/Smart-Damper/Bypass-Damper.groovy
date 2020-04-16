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
        command "set10"
        command "set20"
        command "set30"
        command "set40"
        command "set50"
        command "set60"
        command "set70"
        command "set80"
        command "set90"
        command "set100"
        command "set1"
        command "set2"
        command "set3"
        command "set4"
        command "set5"
        command "set6"
        command "set7"
        command "set8"
        command "set9"
        command "set10a"
        command "setBacklight"

        
        attribute "setpoint", "string"
        attribute "system", "string"
        attribute "msg", "string"
    }
    
    main(["setpoint"])
    details(["setpoint", "damperOpen", 
             "sync", "release", "engage", "debug", "version", "set0", "set10", "set20", "set30", "set40", "set50", "set60", "set70", "set80", "set90", "set100", "set1", "set2", "set3", "set4", "set5", "set6", "set7", "set8", "set9", "set10a", "backlight","msg"])
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

        else if (msg.contains("set") || msg.contains("set0") || msg.contains("set10") || msg.contains("set20") || msg.contains("set30") || msg.contains("set40") || msg.contains("set50") || msg.contains("set60") || msg.contains("set70") || msg.contains("set80") || msg.contains("set90") || msg.contains("set100")) {
            sendEvent(name: "msg", value: "Processed ${msg}")
        }
        else if (msg.contains("00")) {
            def position = msg.toInteger()

            sendEvent(name: "setpoint", value: "${position}")

            sendEvent(name: "msg", value: "Sync processed ${msg}")
        } 
        else if (msg.contains("0") || msg.contains("1") || msg.contains("2") || msg.contains("3") || msg.contains("4") || msg.contains("5") || msg.contains("6") || msg.contains("7") || msg.contains("8") || msg.contains("9") || msg.contains("10")) {  
            def position = msg.toInteger()

            sendEvent(name: "setpoint", value: "${position}")

            sendEvent(name: "msg", value: "Damper open ${msg}/10")
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
    	set10()
    }
    else if (damperValue == 2) {
    	set20()
    }
    else if (damperValue == 3) {
    	set30()
    }
    else if (damperValue == 4) {
    	set40()
    }    
    else if (damperValue == 5) {
    	set50()
    }
    else if (damperValue == 6) {
    	set60()
    }    
    else if (damperValue == 7) {
    	set70()
    }
    else if (damperValue == 8) {
    	set80()
    }    
    else if (damperValue == 9) {
    	set90()
    }    
    else {
    	set100()
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

def set10() {
    if (showLogs) log.info("Sent to device: set10")
    
    sendThingShield("set10") 
}

def set20() {
    if (showLogs) log.info("Sent to device: set20")
    
    sendThingShield("set20") 
}

def set30() {
    if (showLogs) log.info("Sent to device: set30")
    
    sendThingShield("set30")
}

def set40() {
    if (showLogs) log.info("Sent to device: set40")
    
    sendThingShield("set40") 
}

def set50() {
    if (showLogs) log.info("Sent to device: set50")
    
    sendThingShield("set50") 
}

def set60() {
    if (showLogs) log.info("Sent to device: set60")
    
    sendThingShield("set60") 
}

def set70() {
    if (showLogs) log.info("Sent to device: set70")
    
    sendThingShield("set70") 
}

def set80() {
    if (showLogs) log.info("Sent to device: set80")
    
    sendThingShield("set80") 
}

def set90() {
    if (showLogs) log.info("Sent to device: set90")
    
    sendThingShield("set90") 
}

def set100() {
    if (showLogs) log.info("Sent to device: set100")
    
    sendThingShield("set100")
}

def set1() {
    set10()
}

def set2() {
    set20()
}

def set3() {
    set30()
}

def set4() {
    set40()
}

def set5() {
    set50()
}

def set6() {
    set60()
}

def set7() {
    set70()
}

def set8() {
    set80()
}

def set9() {
    set90()
}

def set10a() {
    set100()
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