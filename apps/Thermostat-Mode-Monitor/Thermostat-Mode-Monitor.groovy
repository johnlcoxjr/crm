/*  Thermostat Mode Monitor
 *
 *  Version 
 *	1.0.0 	12/11/2019
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
    name: "Thermostat Mode Monitor",
    namespace: "johnlcox",
    author: "john.l.cox@live.com",
    description: "Monitor status of Presence",
    category: "Safety & Security",
    iconUrl: "",
    iconX2Url: "",
importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Thermostat-Mode-Monitor.groovy"
)


private def about() {
    return "Thermostat Mode Monitor \n ${updatedDate()} ${version()} ${copyright()}"
}

private def paypal() {
    return "You can contribute to the development of this app by making a PayPal donation to https://www.paypal.me/johnlcox. I appreciate your support."
}

private def updatedDate() {
	return "Updated 12/9/2019"
}

private def version() {
    return "Version 1.0.0"
}

private def copyright() {
    return "Copyright © 2019 John Cox"
}

preferences {
	section() {
		paragraph "Thermostat Mode Monitor ${updatedDate()} ${version()}  ${copyright()}"
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
	
	section("Settings") {
		input "modeAway", "mode", title: "Modes to set away:", required: true, multiple: true
		input "modeResume", "mode", title: "Modes to resume:", required: true, multiple: true
		input "thermostats", "capability.thermostat", title: "Thermostat?", multiple: true, required: true
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
	subscribe(location, "mode", modeChange)
}

def modeChange(evt) {
	sendEvent(name:"Mode", value: "${evt.value}", descriptionText:"Mode")

    if (evt.value == "Day") {
    	thermostats.resumeProgram()
    } 
    else if (evt.value == "Night") {
    	thermostats.resumeProgram()
    }
    else if (evt.value == "Away") {
    	thermostats.setAway()
    }
    else if (evt.value == "Vacation") {
        thermostats.setAway()
    }
    else if (evt.value == "Stealth") {
        //do nothing
	}
    
    if (enableDebug) log.info("Mode change to ${evt.value}")
}