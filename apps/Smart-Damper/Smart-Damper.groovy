/**
 *  Smart Damper
 *
 *  Version 
 *	2.0.0	1/1/2016
 *	2.0.1	2/26/2017	removed unnecessary code, removed kickstart
 *  2.1.0	3/21/2017	added the ability to text cailbrate when the damper is power cycled, reducung the need to manually release and enegae
 *	2.1.1	3/22/2017	added the ability to refresh thermostats
 *	3.0.0	11/11/2017	minimized value checking, switched to thermostatOperatingState
 *	3.1.0	7/10/2018	add offline monitor
 *  4.0.0   10/3/2019   ported to Hubitat
 *  4.0.1   12/11/19 removed setAway and resumeProgramming
 *
 *  Copyright 2017 John Cox
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

definition(
    name: "Smart Damper",
    namespace: "johnlcox",
    author: "john.l.cox@live.com",
    description: "Control the Automated Bypass Damper (ABD).",
    category: "Convenience",
    singleInstance: true,
    iconUrl: "",
    iconX2Url: "",
importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Damper.groovy"
)

private def about() {
    return "Smart Damper works behind the scenes and monitors your heating and cooling zones and adjusts your automated bypass damper.  It monitors the theromstats attached to your HVAC system.  You assign relative points (1 to 10) for each zone (1 to 6) based upon the amount of air flow the zone can handle.  The points are evaluated and calculated into a zone level where you set the open level of the damper based upon the calculated zone level.  You also set the open level for open default and determine what occurs at the end of the cycle with the damper (leave open, close, or open default).  All aspects of this app are customizable. ${updatedDate()} ${version()} ${copyright()}"
}

private def paypal() {
    return "You can contribute to the development of this app by making a PayPal donation to https://www.paypal.me/johnlcox. I appreciate your support."
}

private def updatedDate() {
	return "Updated 12/11/2019"
}

private def version() {
    return "Version 4.0.1"
}

private def copyright() {
    return "Copyright © 2017-19 John Cox"
}

preferences {
	section() {
		paragraph "Smart Damper ${updatedDate()} ${version()} ${copyright()}"
	}

	section("About", hideable:true, hidden:true) {
		def hrefAbout = [url: "http://www.coxscience.org/", style: "embedded", title: "Tap for more information...", description:"http://www.coxscience.org/", required: false]

		paragraph about()
		href hrefAbout
	}  

	section("Paypal", hideable:true, hidden:true) {
		def hrefPaypal = [url: "https://www.paypal.me/johnlcox", style: "embedded", title: "Tap for Paypal...", description:"https://www.paypal.me/johnlcox", required: false]

		paragraph paypal()
		href hrefPaypal           
	}  

	section("-= <b>Debug Menu</b> =-") {
		input "enableDebug", "bool", title: "Enable debug output?", required: false, defaultValue: false
	}
	
	section("Thermostats") {
		input "stats","capability.thermostat", title: "Thermostats?", multiple: true, required:false
	}	

	section("Check Thermostats") {
		input "refreshFrequency", "enum", title: "Check (minutes)?", required: true, options: ["1","5","10","15","20","30","45","60"], defaultValue: "15"
	}   

	section("Zones") {
		input "zone1","capability.thermostat", title: "Zone 1 thermostat?", multiple: false, required:false
		input "zone2","capability.thermostat", title: "Zone 2 thermostat?", multiple: false, required:false
		input "zone3","capability.thermostat", title: "Zone 3 thermostat?", multiple: false, required:false
		input "zone4","capability.thermostat", title: "Zone 4 thermostat?", multiple: false, required:false
		input "zone5","capability.thermostat", title: "Zone 5 thermostat?", multiple: false, required:false            
		input "zone6","capability.thermostat", title: "Zone 6 thermostat?", multiple: false, required:false            
	}

	section("Zone Level Points") {
		paragraph "Vibration/noise significantly affects Zone 1 (MBR).  Points assigned to Zone 1 is likely in the 4 range based upon size.  " +
			"However bumping it to 10 will essentially force the damper closed (or close to closed) maximizing air flow.  " +
			"Zone 1 is only a problem when it is not open, so forcing the damper closed when it's is calling is more efficient.  "

		input "zp1","enum", title: "Zone 1 points?", options: ['1','2','3','4','5','6','7','8','9','10'], required: true, defaultValue: '10'
		input "zp2","enum", title: "Zone 2 points?", options: ['1','2','3','4','5','6','7','8','9','10'], required: true, defaultValue: '2'
		input "zp3","enum", title: "Zone 3 points?", options: ['1','2','3','4','5','6','7','8','9','10'], required: true, defaultValue: '5'
		input "zp4","enum", title: "Zone 4 points?", options: ['1','2','3','4','5','6','7','8','9','10'], required: true, defaultValue: '3'
		input "zp5","enum", title: "Zone 5 points?", options: ['1','2','3','4','5','6','7','8','9','10'], required: true, defaultValue: '1'            
		input "zp6","enum", title: "Zone 6 points?", options: ['1','2','3','4','5','6','7','8','9','10'], required: true, defaultValue: '1'           
	}

	section("Automated Bypass Damper") {
		input "bypassDamper","capability.actuator", title: "ABD?", multiple: false, required:true        	
		input "leaveOpen", "enum", title: "Leave damper open?", options: ['Yes','No'], required: true, defaultValue: 'Yes'
		input "openLevel", "enum", title: "Default open (%)?", required: false, options: ["0", "10", "20", "30", "40", "50","60", "70", "80", "90", "100"], defaultValue: "50"
		input "leavePosition", "enum", title: "Damper position?", options: ['Default Open','Last Position'], required: true, defaultValue: 'Default Open'
	}

	section("Zone Level Settings") {
		input "zl1","enum", title: "Zone level 1?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '100'
		input "zl2","enum", title: "Zone level 2?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '90'
		input "zl3","enum", title: "Zone level 3?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '80'
		input "zl4","enum", title: "Zone level 4?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '70'
		input "zl5","enum", title: "Zone level 5?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '60'
		input "zl6","enum", title: "Zone level 6?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '50'
		input "zl7","enum", title: "Zone level 7?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '40'
		input "zl8","enum", title: "Zone level 8?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '30'
		input "zl9","enum", title: "Zone level 9?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '20'
		input "zl10","enum", title: "Zone level 10?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '10'
		input "zl11","enum", title: "Zone level 11?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '0'
		input "zl12","enum", title: "Zone level 12?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '0'
		input "zl13","enum", title: "Zone level 13?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '0'
		input "zl14","enum", title: "Zone level 14?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '0'
		input "zl15","enum", title: "Zone level 15?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '0'
		input "zl16","enum", title: "Zone level 16?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '0'
		input "zl17","enum", title: "Zone level 17?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '0'
		input "zl18","enum", title: "Zone level 18?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '0'
		input "zl19","enum", title: "Zone level 19?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '0'
		input "zl20","enum", title: "Zone level 20?", options: ['0','10','20','30','40','50','60','70','80','90','100'], required: true, defaultValue: '0'
	}           

	section( "Notifications" ) {
		input "sendPushMessage", "capability.notification", title: "Notification?", required:false
	}
}

def installed() {
    initialize()
}

def updated() {
	unsubscribe()
    unschedule()
    state.clear()
    initialize()
}

def initialize() {
    state.zonesOpen = 0
    state.zoneLevel = 0
    state.zonesOpenOld = 0
    state.zoneLevelOld = 0
    
    def damperPosition = getBypassDamperPosition().toInteger()
    
	if (stats != null) {
        subscribe(stats, "thermostat", zoneHandler)
	}
    
	if (stats != null) {
        scheduleChecks()
	}
    
    if (bypassDamper != null) {
        subscribe(bypassDamper, "msg", msgHandler)    
    }
	
	subscribe(location, "mode", modeChange)
}

def modeChange(evt) {
	sendEvent(name:"Mode", value: "${evt.value}", descriptionText:"Mode")
    
    if (evt.value == "Day") {
		//stats.resumeProgram()
	} 
    else if (evt.value == "Night") {
		//stats.resumeProgram()
	} 
	else if (evt.value == "Stealth") {
		//do nothing
	}
	else {
		//stats.setAway()
	} 
}

def msgHandler(evt) {
	def msg = evt.value
    
    if ((state.powerCycled == null) && (msg.contains("Calibrate"))) {
        send("${bypassDamper} needs to be calibrated.  It was either updated, power cycled, or suffered a power loss.")
    
        state.powerCycled = now()
       
        runIn(60, resetMsgHandler) //reset msg handler so it delays one minute before sending another message
    }
}

def resetMsgHandler() {
	state.powerCycled = null
}

def scheduleChecks() {
    if (refreshFrequency == "60") {
        schedule("0 8 */1 * * ?", statusCheck) //changed 0 to 8 to hopefully avoid time out
	}	
    else {
        schedule("0 8/${refreshFrequency} * * * ?", statusCheck) //changed 0 to 8 to hopefully avoid time out  
	}
}

def zlSetDamper(openLevel) {
    sendEvent(name:"Setpoint", value: "${openLevel}", descriptionText:"Damper")
    
    if (openLevel == "0") {
        bypassDamper.set0()
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 0%</span>")
    }
    else if (openLevel == "10") {
        bypassDamper.set10()
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 10%</span>")
    }
    else if (openLevel == "20") {
        bypassDamper.set20()
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 20%</span>")
    }
    else if (openLevel == "30") {
        bypassDamper.set30()
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 30%</span>")
    }
    else if (openLevel == "40") {
        bypassDamper.set40()
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 40%</span>")
    }
    else if (openLevel == "50") {
        bypassDamper.set50() 
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 50%</span>")
    }
    else if (openLevel == "60") {
        bypassDamper.set60()
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 60%</span>")
    }
    else if (openLevel == "70") {
        bypassDamper.set70() 
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 70%</span>")
    }
    else if (openLevel == "80") {
        bypassDamper.set80() 
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 80%</span>")
    }
    else if (openLevel == "90") {
        bypassDamper.set90() 
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 90%</span>")
    }    
    else {
        bypassDamper.set100()  
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 100%</span>")
    }  
    
    if (enableDebug) log.info("Open: ${openLevel}% (zone level)")
}

def getConnectivity(zone) {
	/*if (zone != null) {
        def status = zone.currentValue("DeviceWatch-DeviceStatus")

        if (status.contains("offline")) {
			if (enableDebug) log.info("${zone} status = ${status}")
            
            sendEvent(name:"Thermostat", value: "OFFLINE", descriptionText:"${zone}")
			
            return "offline"
        }
        else {
            //sendEvent(name:"Thermostat", value: "ONLINE", descriptionText:"${zone}")

            return "online"
        }
  	}*/
	return "online"
}

def getStatus(zone) {
	if (zone != null) {
        def status = zone.currentValue("thermostatOperatingState")
        
        if (status.contains("idle")) {
            //do nothing
        }
        else {
            if (enableDebug) log.info("${zone} status = ${status}")
        }

        if ((status.contains("idle")) || (status.contains("off"))) {
            return "closed"
        }
        else {
            return "opened"
        }
  	}
}

def getBypassDamperPosition() {
	def damper = bypassDamper.currentState("setpoint")
      
    def damperPosition = damper.value.toInteger() * 10
    
    state.damperPosition = damperPosition
    
    app.updateLabel("Smart Damper <span style=\"color:blue\"> ${damperPosition}%</span>")
         
    return damperPosition
}

def getZonesConnectivty() {
	def zonesConnected = 0
    
    if (zone1 != null) {
        if (getConnectivity(zone1) == "offline") {
        	zonesConnected = zonesConnected - 1
        }
        else {
            zonesConnected = zonesConnected + 1
        }
   	}
    
    if (zone2 != null) {
        if (getConnectivity(zone2) == "offline") {
        	zonesConnected = zonesConnected - 1
        }
        else {
            zonesConnected = zonesConnected + 1
        }
   	}
    
    if (zone3 != null) {
        if (getConnectivity(zone3) == "offline") {
        	zonesConnected = zonesConnected - 1
        }
        else {
            zonesConnected = zonesConnected + 1
        }
   	}

    if (zone4 != null) {
        if (getConnectivity(zone4) == "offline") {
        	zonesConnected = zonesConnected - 1
        }
        else {
            zonesConnected = zonesConnected + 1
        }
   	}
    
    if (zone5 != null) {
        if (getConnectivity(zone5) == "offline") {
        	zonesConnected = zonesConnected - 1
        }
        else {
            zonesConnected = zonesConnected + 1
        }
   	}
    
    if (zone6 != null) {
        if (getConnectivity(zone6) == "offline") {
        	zonesConnected = zonesConnected - 1
        }
        else {
            zonesConnected = zonesConnected + 1
        }
   	}
       
	return zonesConnected
}

def getZonesOpen() {
	def zonesOpen = 0
    
    state.zonesOK = true
     
    if (zone1 != null) {
        if (getStatus(zone1) == "opened") {
            zonesOpen = zonesOpen + 1
            
            state.zoneLevel = state.zoneLevel + zp1.toInteger()
        }
   	}
    
    if (zone2 != null) {
        if (getStatus(zone2) == "opened") {
            zonesOpen = zonesOpen + 1
            
            state.zoneLevel = state.zoneLevel + zp2.toInteger()
        }
  	}
    
    if (zone3 != null) {
        if (getStatus(zone3) == "opened") {
            zonesOpen = zonesOpen + 1
            
            state.zoneLevel = state.zoneLevel + zp3.toInteger()
        }
   	}    

	if (zone4 != null) {
        if (getStatus(zone4) == "opened") {
            zonesOpen = zonesOpen + 1
        
	        state.zoneLevel = state.zoneLevel + zp4.toInteger()
      	}
  	}
    
    if (zone5 != null) {
        if (getStatus(zone5) == "opened") {
            zonesOpen = zonesOpen + 1
            
            state.zoneLevel = state.zoneLevel + zp5.toInteger()
        }
   	}
    
    if (zone6 != null) {
        if (getStatus(zone6) == "opened") {
            zonesOpen = zonesOpen + 1
            
            state.zoneLevel = state.zoneLevel + zp6.toInteger()
        }
  	}
    
	state.zonesOpen = zonesOpen
    
	return zonesOpen
}

def statusCheck() {
    def damperPosition = getBypassDamperPosition().toInteger()
         
    state.zoneLevel = 0
      
    def zonesOpen = getZonesOpen()
    
    def zonesConnected = getZonesConnectivty()
    
    if (zonesConnected < 3) {//was 1, set to 3 because that means some are still working, so likely a temporary issue
    	send("Ecobee's are offline.  You must re-enter your credentials.  Smart Damper")
    }
    
    if (state.zonesOK) {
        if (state.zonesOpenOld == 1) {
            if (enableDebug) log.info("Current: ${damperPosition}% (${state.zonesOpenOld} open zone) - zone level = ${state.zoneLevelOld}")  
        }
        else {
            if (enableDebug) log.info("Current: ${damperPosition}% (${state.zonesOpenOld} open zones) - zone level = ${state.zoneLevelOld}")  
        }

        def zoneMsg = ""
        if (zonesOpen == 1) {
            zoneMsg = "(${zonesOpen} open zone) - zone level = ${state.zoneLevel}"
        }
        else {
            zoneMsg = "(${zonesOpen} open zones) - zone level = ${state.zoneLevel}"    
        }

        if (state.zoneLevel >= 20) {
            if (damperPosition.toInteger() != zl20.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl20}% ${zoneMsg}")  
                zlSetDamper(zl20)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl20}% ${zoneMsg}")  
            }
        }
        else if (state.zoneLevel >= 19) {
            if (damperPosition.toInteger() != zl19.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl19}% ${zoneMsg}")  
                zlSetDamper(zl19)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl19}% ${zoneMsg}")  
            }
        }	
        else if (state.zoneLevel >= 18) {
            if (damperPosition.toInteger() != zl18.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl18}% ${zoneMsg}")  
                zlSetDamper(zl18)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl18}% ${zoneMsg}")  
            }        
        }	
        else if (state.zoneLevel >= 17) {
            if (damperPosition.toInteger() != zl17.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl17}% ${zoneMsg}")  
                zlSetDamper(zl17)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl17}% ${zoneMsg}")  
            }
        }	
        else if (state.zoneLevel >= 16) {
            if (damperPosition.toInteger() != zl16.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl16}% ${zoneMsg}")  
                zlSetDamper(zl16)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl16}% ${zoneMsg}")  
            }      
        }
        else if (state.zoneLevel >= 15) {
            if (damperPosition.toInteger() != zl15.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl15}% ${zoneMsg}")  
                zlSetDamper(zl15)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl15}% ${zoneMsg}")  
            }
        }	
        else if (state.zoneLevel >= 14) {
            if (damperPosition.toInteger() != zl14.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl14}% ${zoneMsg}")  
                zlSetDamper(zl14)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl14}% ${zoneMsg}")  
            }
        }	
        else if (state.zoneLevel >= 13) {
            if (damperPosition.toInteger() != zl13.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl13}% ${zoneMsg}")  
                zlSetDamper(zl13)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl13}% ${zoneMsg}")  
            }
        }	
        else if (state.zoneLevel >= 12) {
            if (damperPosition.toInteger() != zl12.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl12}% ${zoneMsg}")  
                zlSetDamper(zl12)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl12}% ${zoneMsg}")  
            }
        }
        else if (state.zoneLevel >= 11) {
            if (damperPosition.toInteger() != zl11.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl11}% ${zoneMsg}")  
                zlSetDamper(zl11)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl11}% ${zoneMsg}")  
            }
        }
        else if (state.zoneLevel >= 10) {
            if (damperPosition.toInteger() != zl10.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl10}% ${zoneMsg}")  
                zlSetDamper(zl10)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl10}% ${zoneMsg}")  
            }
        }
        else if (state.zoneLevel >= 9) {
            if (damperPosition.toInteger() != zl9.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl9}% ${zoneMsg}")  
                zlSetDamper(zl9)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl9}% ${zoneMsg}")  
            }
        }
        else if (state.zoneLevel >= 8) {
            if (damperPosition.toInteger() != zl8.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl8}% ${zoneMsg}")  
                zlSetDamper(zl8)
            }        
            else {
                if (enableDebug) log.info("Leaving: ${zl8}% ${zoneMsg}")  
            }
        }
        else if (state.zoneLevel >= 7) {
            if (damperPosition.toInteger() != zl7.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl7}% ${zoneMsg}")  
                zlSetDamper(zl7)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl7}% ${zoneMsg}")  
            }
        }
        else if (state.zoneLevel >= 6) {
            if (damperPosition.toInteger() != zl6.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl6}% ${zoneMsg}")  
                zlSetDamper(zl6)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl6}% ${zoneMsg}")  
            }
        }
        else if (state.zoneLevel >= 5) {
            if (damperPosition.toInteger() != zl5.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl5}% ${zoneMsg}")  
                zlSetDamper(zl5)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl5}% ${zoneMsg}")  
            }
        }
        else if (state.zoneLevel >= 4) {
            if (damperPosition.toInteger() != zl4.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl4}% ${zoneMsg}")  
                zlSetDamper(zl4)
            }       
            else {
                if (enableDebug) log.info("Leaving: ${zl4}% ${zoneMsg}")  
            }
        }
        else if (state.zoneLevel >= 3) {
            if (damperPosition.toInteger() != zl3.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl3}% ${zoneMsg}")  
                zlSetDamper(zl3)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl3}% ${zoneMsg}")  
            }
        }
        else if (state.zoneLevel >= 2) {
            if (damperPosition.toInteger() != zl2.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl2}% ${zoneMsg}")  
                zlSetDamper(zl2)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl2}% ${zoneMsg}")  
            }
        }    
        else if (state.zoneLevel >= 1) {
            if (damperPosition.toInteger() != zl1.toInteger()) {
                if (enableDebug) log.info("Setting: ${zl1}% ${zoneMsg}")  
                zlSetDamper(zl1)
            }
            else {
                if (enableDebug) log.info("Leaving: ${zl1}% ${zoneMsg}")  
            }
        }    
        else { //open default
            if (damperPosition != "0") {
                if (leaveOpen == "Yes") {
                    if (leavePosition == "Default Open") {                   
                        if (damperPosition.toInteger() != openLevel.toInteger()) {
                            if (zonesOpen == 1) {
                                if (enableDebug) log.info("Setting: ${openLevel}% (${zonesOpen} open zone) - zone level = ${state.zoneLevel} reposition (open default)")
                            }
                            else {
                                if (enableDebug) log.info("Setting: ${openLevel}% (${zonesOpen} open zones) - zone level = ${state.zoneLevel} reposition (open default)")
                            }
                        }
                        else {
                            if (zonesOpen == 1) {
                                if (enableDebug) log.info("Leaving: ${openLevel}% (${zonesOpen} open zone) - zone level = ${state.zoneLevel} reposition (open default)")
                            }
                            else {
                                if (enableDebug) log.info("Leaving: ${openLevel}% (${zonesOpen} open zones) - zone level = ${state.zoneLevel} reposition (open default)")                        
                            }
                        }
                    }
                    else { //last position
                        if (zonesOpen == 1) {
                            if (enableDebug) log.info("Leaving: ${damperPosition}% (${zonesOpen} open zone) - zone level = ${state.zoneLevel} reposition (last position)")
                        }
                        else {
                            if (enableDebug) log.info("Leaving: ${damperPosition}% (${zonesOpen} open zones) - zone level = ${state.zoneLevel} reposition (last position)")                        
                        }
                    }  
                }
                else {
                    if (zonesOpen == 1) {
                        if (enableDebug) log.info("Setting: 0% (${zonesOpen} open zone) - zone level = ${state.zoneLevel} reposition(close)")  
                    }
                    else {
                        if (enableDebug) log.info("Setting: 0% (${zonesOpen} open zones) - zone level = ${state.zoneLevel} reposition(close)")
                    }                
                    zlSetDamper("0")
                }
            }              
            else {
                if (enableDebug) log.info("Leaving: ${damperPosition}% ${zoneMsg}")
            }
        }

        state.zonesOpenOld = state.zonesOpen
        state.zoneLevelOld = state.zoneLevel
	}
}

def send(msg) {
    sendPushMessage.deviceNotification(msg)
}

def zoneHandler(evt) {
  	statusCheck()
}