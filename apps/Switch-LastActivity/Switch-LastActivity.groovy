/*  Switch LastActivity
 *
 *  Version
 *	1.0.0 	 11/17/19
 *  1.0.1    12/30/19    fixed typos
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
    name: "Switch LastActivity",
    namespace: "johnlcox",
    author: "john.l.cox@live.com",
    description: "Updates Outlet LastActicity",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
	importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Switch-LastActivity.groovy"
)

private def about() {
    return "Switch LastActivity - uses indicator to artifically update last activity. \n${updatedDate()} ${version()} ${copyright()}"
}

private def paypal() {
    return "You can contribute to the development of this app by making a PayPal donation to https://www.paypal.me/johnlcox. I appreciate your support."
}

private def updatedDate() {
	return "Updated 12/30/2019"
}

private def version() {
    return "Version 1.0.1"
}

private def copyright() {
    return "Copyright © 2019 John Cox"
}

preferences {
	section() {
		paragraph "Switch LastActivity ${updatedDate()} ${version()} ${copyright()}" 
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

	section("Switch (Indicator Never)") {
		input "switchNever", "capability.switch", title: "Switches (with failing last activity)?", multiple: true, required: false
	}    
	section("Switch (Indicator On)") {
		input "switchOn", "capability.switch", title: "Switches (with failing last activity)?", multiple: true, required: false
	}    
	section("Switch (Indicator Off)") {
		input "switchOff", "capability.switch", title: "Switches (with failing last activity)?", multiple: true, required: false
	}        
}

def installed() {
    initialize()
}

def updated() {
    unschedule()
    state.clear()
    initialize()
}

def initialize() {  
    updateLastActicity()
    
    schedule("0 0/5 * * * ?", updateLastActicity)
}

def updateLastActicity() {
    if (switchNever != null) {
        if (enableDebug) log.info("updating switch ($switchNever) lastActivity")
    
        switchNever.indicatorNever()
    }
    if (switchOn != null) {
        if (enableDebug) log.info("updating switch ($switchOn) lastActivity")
        
        switchOn.indicatorWhenOn()
    }
    if (switchOff != null) {
        if (enableDebug) log.info("updating switch ($switchOff) lastActivity")
        
        switchOff.indicatorWhenOff()       
    }
}