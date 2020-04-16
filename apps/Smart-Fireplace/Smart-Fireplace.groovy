/*  Version 
 *	1.0.0 	12/20/2016
 *	1.0.1	2/26/2017	removed unnecessary code
 *  2.0.0    9/28/2019 updated to Hubitat
 *  2.0.1    11/17/19    switched to allowing for alert switch
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
	name: "Smart Fireplace",
    	namespace: "johnlcox",
    	author: "john.l.cox@live.com",
    	description: "Monitors temperature.",
    	category: "Convenience",
    	iconUrl: "",
    	iconX2Url: "",
	importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Fireplace.groovy"
)

private def about() {
    return "Smart Fireplace monitors a temperature sensor and alerts you if it appears that the fireplce is on and you have left or have gone to bed.\n${updatedDate()} ${version()} ${copyright()}"
}

private def paypal() {
    return "You can contribute to the development of this app by making a PayPal donation to https://www.paypal.me/johnlcox. I appreciate your support."
}

private def updatedDate() {
	return "Updated 10/11/2019"
}

private def version() {
    return "Version 2.0.1"
}

private def copyright() {
    return "Copyright © 2017-19 John Cox"
}

preferences {
   	section() {
        paragraph "Smart Fireplace ${updatedDate()} ${version()} ${copyright()}" 
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

  	section("Temperature monitor?") {
    	input name: "temp1", type: "capability.temperatureMeasurement", title: "Fireplace temp Sensor?", multiple: false, required: true
    	input name: "maxTemp", type: "number", title: "Max temp (assume on)?",   required: true, defaultValue: 125
    	input name: "minTemp", type: "number", title: "Min temp (assume off)?",   required: true, defaultValue: 80
    	input name: "diff", type: "number", title: "Temp increase to trigger (°F)?", required: false, defaultValue: 2
  	}
    
  	section("Switch monitor?") {
    	input name: "switches", type: "capability.switch", title: "Switch(es)?", multiple: true, required: true
  	}    
    
    section("Switch monitor?") {
    	input name: "alert", type: "capability.switch", title: "Alert switch(es)?", multiple: true, required: true
  	}  
    
    section("Show notification light only in these mode(s)...") {
        input "modes", "mode",title:"Mode(s)?", required: true, multiple: true
    } 

  	section("Notifications?") {
    	input name: "sendPushMessage", type: "capability.notification", title: "Notification", required: false, defaultValue: "No"
  	}

  	section("Message interval?") {
    	input name: "messageDelay", type: "number", title: "Minutes (0 equals every message)?", required: false, defaultValue: 10
  	}
}

def installed() {
  	initialize()
}

def updated() {
  	unsubscribe()
  	initialize()
}

def initialize() {
  	subscribe(temp1, "temperature", triggerTemp)
    subscribe(switches, "switch.off", triggerTemp)
    state.temp = 0
}

def triggerTemp(evt) {
    def displayName = evt.displayName
    def temperature
    
    if (displayName.contains("Light")) {
         temperature = evt.doubleValue.toInteger()
    }
    else {
  	    temperature = temp1.currentValue("temperature").toInteger()
    }
    
    process(temperature)
}

def process(temperature) {   
    def tempMessage = "The temperature is ${temperature}°F and is "
    
    app.updateLabel("Smart Fireplace <span style=\"color:red\"> ${temperature}°F</span>")
    
    if (temperature < 0) {	
    	temperature = 141
        
        if (enableDebug) log.warn("temperature is over the maximum")
    }
    
    def tempDiff = temperature - state.temp  	
    
    if (temperature > state.temp) {
    	tempMessage = tempMessage + "going up."
        
        state.action = "up"
    }
    else if (temperature < state.temp) {
    	tempMessage = tempMessage + "going down."
        
        state.action = "down"
    }
    else {
    	tempMessage = tempMessage + "holding."
        
        state.action = "hold"
    }	
    
    if (enableDebug) log.info("process current = ${temperature}, previous = ${state.temp}, difference = ${tempDiff}")
        
    state.temp = temperature    
    
    if (enableDebug) log.info("${tempMessage}")

  	if (temperature > maxTemp) { //temp is over the max, must be on or was on
        if (enableDebug) log.info("firepace is either on or was recently turned off, either way, still really hot")

		evalSwitches(tempMessage, tempDiff)
  	}
  	else if (temperature < minTemp) { //temp is less than the min, must be off or cooling way down
        if (enableDebug) log.info("firepace is off or is set really low, so it appears off")
  	}
    else { //temp is somewhere in the middle
    	if (state.action == "up") { //temp is going up, let's assume it was on
        	if (tempDiff >= diff) {
	        	if (enableDebug) log.info("firepace is on, temp going up")

				evalSwitches(tempMessage, tempDiff)
            }
            else {
            	if (enableDebug) log.info("firepace could be on, temp going up, but within the variance of normal temp changes")
        	}
        }
        else if (state.action == "down") { //temp is going down, let's assume off for now, but if it goes up, it will trigger message
            if (enableDebug) log.info("firepace could be on, temp going down, will get caught if it goes up")
        }
        else { //temp is holding, it's either max'ed or min'ed out, but if it goes uo, it will trigger message
            if (enableDebug) log.info("firepace could be on, temp holding, will get caught if it changes")
    	}
    }
}

def evalSwitches(tempMessage, tempDiff) {
    def switchesOff = checkSwitch()

    if (switchesOff) {
        if (enableDebug) log.info("switches are off, so send message")
        
        if (tempDiff >= diff) {
        	send("Fireplace is on.  ${tempMessage}")
        }
        else {
        	send("Fireplace appears to be on.  ${tempMessage}")
        }
        
        alert.on()
    }
    else {
        if (enableDebug) log.info("switches are on, so don't send message")
    }
}

def allOn(switches){
    def allOn = true
	switches.each {eachSwitch->
        if (eachSwitch.currentValue("switch") == "off") {
            allOn = false
        }
	}
	return allOn
}

def allOff(switches){
    def allOff = true
	switches.each {eachSwitch->
        if (eachSwitch.currentValue("switch") == "on") {
            allOff = false
        }
	}
	return allOff
}


def validSwitch(testSwitch) {
	def validSwitches = allOn(testSwitch)
}

def checkSwitch() {
	def switchesOn = allOff(switches)
    
    if (enableDebug) log.info("switches - ${switchesOn} (${switches})")
       
    return switchesOn
}

private send(msg) {
  	def delay = (messageDelay != null && messageDelay != "") ? messageDelay * 60 * 1000 : 0

  	if (now() - delay > state.lastMessage) {
    	state.lastMessage = now()
    
   	    sendPushMessage.deviceNotification(msg)
        
        sendEvent(name:"Fireplace", value: "WARNING", descriptionText:"${msg}")
  	}
}