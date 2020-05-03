/*  Door Monitor
 *
 *  Version 
 *	1.0.0 	12/5/2019
 *  1.0.1	12/27/19 	fixed min, hr bug
 *
 *  Copyright 2019 John Cox
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
    name: "Door Monitor",
    namespace: "johnlcox",
    author: "john.l.cox@live.com",
    description: "Monitor door status",
    category: "Safety & Security",
    iconUrl: "",
    iconX2Url: "",
importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Alarm-Control-Alarm.groovy"
)


private def about() {
    return "Door Monitor \n ${updatedDate()} ${version()} ${copyright()}"
}

private def paypal() {
    return "You can contribute to the development of this app by making a PayPal donation to https://www.paypal.me/johnlcox. I appreciate your support."
}

private def updatedDate() {
	return "Updated 2/4/2020"
}

private def version() {
    return "Version 1.0.0"
}

private def copyright() {
    return "Copyright © 2020 John Cox"
}

preferences {
	section() {
		paragraph "Door Monitor ${updatedDate()} ${version()}  ${copyright()}"
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
		input "door", "capability.contactSensor", title: "Door", required: true, multiple: false
		input "motion", "capability.motionSensor", title: "Motion", required: true, multiple: false
		input "light", "capability.switch", title: "Light?", required: true, multiple: false
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
    subscribe(door, "contact", doorHandler)   
    subscribe(motion, "motion", motionHandler)   
    state.doorOpen = now()
    state.doorClose = now()    
}

def doorHandler(evt) {
    if (evt.value == "open") {
        state.doorOpen = now()
    }
    else {
        state.doorClose = now()
    }
}

def motionHandler(evt) {
    if (evt.value == "active") {
        if (checkMinutes()) {
            light.on()    
        }
    }
}

def checkMinutes() {
    def delay = 2 * 60 * 1000

	def ret = false

    if (((now() - delay) > atomicState.doorOpen) && ((now() - delay) > atomicState.doorClose) && (door.currentValue("contact") == "open")) {    
        ret = true
	}

	return ret
}