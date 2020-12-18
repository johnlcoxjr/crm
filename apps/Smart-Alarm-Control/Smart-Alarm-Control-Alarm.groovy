/*  Smart Alarm Control - ADT/SimpliSafe (Child)
 *
 *  Version 
 *	2.0.0 	1/1/2016
 *	2.0.1	2/26/2017	removed unnecessary code
 *	3.0.0	12/2/2018	adapted for SimpliSafe
 *	4.0.0	10/5/2019	ported to Hubitat  
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
    name: "Smart Alarm Control (Alarm)",
    namespace: "johnlcox",
    author: "john.l.cox@live.com",
    parent: "johnlcox:Smart Alarm Control",
    description: "Provides your ADT system with the ability to text it's status.",
    category: "Safety & Security",
    iconUrl: "",
    iconX2Url: "",
importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Alarm-Control-Alarm.groovy"
)


private def about() {
    return "Smart Alarm Control - Alarm (Child) controls all how your alarm responds and sends messages when armed, disarmed, or alarmed. All aspects of this app are customizable. \n ${updatedDate()} ${version()} ${copyright()}"
}

private def paypal() {
    return "You can contribute to the development of this app by making a PayPal donation to https://www.paypal.me/johnlcox. I appreciate your support."
}

private def updatedDate() {
	return "Updated 10/11/2019"
}

private def version() {
    return "Version 4.0.0"
}

private def copyright() {
    return "Copyright © 2017-2019 John Cox"
}

preferences {
	section() {
		paragraph "Smart Alarm Control (Alarm) ${updatedDate()} ${version()}  ${copyright()}"
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
	
	section ("Alarm condition...") {
		paragraph "In an alarm condition, activate the following lights and strobes."
		input "lights", "capability.switch", title: "Lights?", required: false, multiple: true
		input "strobe", "capability.alarm", title: "Strobe?", required: false, multiple: false
		input "timeLights", "number", title: "Lights on time (minutes)?", required: true, multiple: false, defaultValue: 15
		input "timeStrobe", "number", title: "Strobe on time (minutes)?", required: true, multiple: false, defaultValue: 1
	}
}

def installed() {
	initialize()	
}

def updated() {
	unsubscribe()
    state.clear()
    initialize()
}

def initialize() {
    subscribe(parent.alarm, "status", alarmSystem)   
}

def alarmSystem(evt) {
	def status = evt.value
	def alarmState = parent.alarm.currentValue("alarm")    
    
    if (status != "error") { //added if to avoid sending false statuses when error is cleared
        sendEvent(name:"Alarm", value: "${status}", descriptionText:"Status")
    }
	
    if (enableDebug) log.info("status = ${status}")
    
	if (status.contains("off")) {
		parent.aggregateMsg("Alarm is off.")

		parent.activationLight("disarm")     

		app.updateLabel("Alarm <span style=\"color:green\"> Off</span>")
	} 
	else if (status.contains("home")) {
		parent.aggregateMsg("Alarm is on.")

		parent.activationLight("arm")            

		app.updateLabel("Alarm <span style=\"color:red\"> Home (Stay)</span>")
	} 
	else if (status.contains("away")) {
		parent.aggregateMsg("Alarm is on (away).")

		parent.activationLight("arm")

		app.updateLabel("Alarm <span style=\"color:red\"> Home (Away)</span>")
	} 
	else if (status.contains("alarm")) {
		if (alarmState != "OFF") {
			parent.send("ALARM (ALERT - ALARM).")

			alarmCondition()

			app.updateLabel("Alarm <span style=\"color:blue\"> Alarm</span>")
		}
	} 
	else if (status.contains("alert")) {
		def message = parent.alarm.currentValue("messages")

		parent.aggregateMsg("Alarm has an alert message.  ${message}")

		parent.activationLight("alert")        

		app.updateLabel("Alarm <span style=\"color:purple\"> Alert</span>")
	} 
	else {
		if (enableDebug) log.info ("unknown status = ${status}")
	}
}

private alarmCondition() {
    if (lights != null) {
    	if (enableDebug) log.info("Turning lights on")

        lights.on()
 	}
     
  	if (strobe != null) {
      	if (enableDebug) log.info("Turning strobe on")

        strobe.both()
  	}
    
    if ((lights != null) || (strobe != null)) {
        runIn(timeLights * 60, "turnOff")         
    }
}

def turnOff() {
	if (lights != null) {
    	if (enableDebug) log.info("Turning lights off")
        
		lights.off()
	}

	if (strobe != null) {
    	if (enableDebug) log.info("Turning strobe off")
        
		strobe.off()
	}
}