/*  Presence Monitor
 *
 *  Version 
 *	1.0.0 	12/9/2019
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
    name: "Presence Monitor",
    namespace: "johnlcox",
    author: "john.l.cox@live.com",
    description: "Monitor status of Presence",
    category: "Safety & Security",
    iconUrl: "",
    iconX2Url: "",
importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Presence-Monitor.groovy"
)


private def about() {
    return "Presence Monitor \n ${updatedDate()} ${version()} ${copyright()}"
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
		paragraph "Presence Monitor ${updatedDate()} ${version()}  ${copyright()}"
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
		input "presence1", "capability.presenceSensor", title: "Presence 1?", required: false, multiple: false
        input "presence2", "capability.presenceSensor", title: "Presence 2?", required: false, multiple: false
		input "refresh", "capability.pushableButton", title: "Refresh?", required: false, multiple: false
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
	schedule("0 0 */1 * * ?", "process") 
	//runEvery1Hour(process)
}

def process() {
    if (presence1 != null) {
        checkDevice(presence1)
    }
    if (presence2 != null) {
        checkDevice(presence2)
    }
}

def checkDevice(device) {
	def lastActivity = device.getLastActivity()	
	
	//log.info(lastActivity)
	
	long timeDiff
	def now = new Date()
	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity}".replace("+00:00","+0000"))
	long unxNow = now.getTime()
	long unxPrev = prev.getTime()
	unxNow = unxNow/1000
	unxPrev = unxPrev/1000
	timeDiff = Math.abs(unxNow-unxPrev)
	timeDiff = Math.round(timeDiff/60)
	hourDiff = timeDiff / 60
	int hour = Math.floor(timeDiff / 60)
	
	//log.info(timeDiff)
	
	if (hour >= 24) {
		if (refresh != null) {
			refresh.push()
		}
	}
}