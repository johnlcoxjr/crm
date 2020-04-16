/*  ComEd Monitor
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
    name: "ComEd Monitor",
    namespace: "johnlcox",
    author: "john.l.cox@live.com",
    description: "Monitor status of Rainforest Eagle",
    category: "Safety & Security",
    iconUrl: "",
    iconX2Url: "",
importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Alarm-Control-Alarm.groovy"
)


private def about() {
    return "ComEd Monitor \n ${updatedDate()} ${version()} ${copyright()}"
}

private def paypal() {
    return "You can contribute to the development of this app by making a PayPal donation to https://www.paypal.me/johnlcox. I appreciate your support."
}

private def updatedDate() {
	return "Updated 12/27/2019"
}

private def version() {
    return "Version 1.0.1"
}

private def copyright() {
    return "Copyright © 2019 John Cox"
}

preferences {
	section() {
		paragraph "ComEd Monitor ${updatedDate()} ${version()}  ${copyright()}"
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
		input "meter", "capability.powerMeter", title: "ComEd?", required: false, multiple: false
		input "reboot", "capability.switch", title: "Reboot?", required: false, multiple: false
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
	state.reboot = false
	runEvery1Minute(process)
	process()
}

def process() {
	def lastActivity = meter.getLastActivity()	
	
	if (enableDebug) {log.info(lastActivity)}
	
	long timeDiff
	def now = new Date()
	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity}".replace("+00:00","+0000"))
	long unxNow = now.getTime()
	long unxPrev = prev.getTime()
	unxNow = unxNow/1000
	unxPrev = unxPrev/1000
	timeDiff = Math.abs(unxNow-unxPrev)
	int min = Math.round(timeDiff/60) //timediff is now in minutes
	int hour = Math.round(timeDiff/60/60) //now in hours
	
	if (enableDebug) {log.info("${min} minutes")}
	
	if ((min > 15) && (!state.reboot)) {
		if (reboot != null) {
			state.reboot = true
			
			if (enableDebug) {log.info("rebooting")}
			
			reboot.off()
			
			runIn(30, turnOn)
		}
	}
}

def turnOn() {
	reboot.on()
	
	runIn(300, clearReboot)
}

def clearReboot() {
	state.reboot = false
	
	if (enableDebug) {log.info("reboot cleared")}
}