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
 *  5.0.0   12/14/20    added air velocity, removed thermostats
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
    return "Smart Damper works behind the scenes and monitors your heating and cooling zones and adjusts your automated bypass damper based on airflow to a problem damper.  All aspets of this app are customizable. ${updatedDate()} ${version()} ${copyright()}"
}

private def paypal() {
    return "You can contribute to the development of this app by making a PayPal donation to https://www.paypal.me/johnlcox. I appreciate your support."
}

private def updatedDate() {
	return "Updated 12/14/2020"
}

private def version() {
    return "Version 5.0.0"
}

private def copyright() {
    return "Copyright © 2017-20 John Cox"
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
	
    section("Problem Damper") {
        input "problemDamper", "capability.contactSensor", title: "Problem Damper", required: true, multiple: false
        input "maxVelocity", "string", title: "Maximum velocity", required: true, defaultValue:"4.2"
        input "minVelocity", "string", title: "Minimum velocity", required: true, defaultValue:"3.2"
	}   
    
    section("Air Velocity") {
        input "airVelocity", "capability.sensor", title: "Air velocity sensor", required: true, multiple: false
	}   

	section("Automated Bypass Damper") {
		input "bypassDamper","capability.actuator", title: "ABD?", multiple: false, required:true     
        input "reliefPosition", "number", title: "Relief position", required: true, defaultValue:7
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
    if (bypassDamper != null) {
        subscribe(bypassDamper, "msg", bypassDamperMsgHandler)    
        subscribe(bypassDamper, "setpoint", bypassDamperSetpointHandler)    
        
        state.bypassDamper = bypassDamper.currentValue("setpoint").toInteger()
    }
    
    if (problemDamper != null) {
        subscribe(problemDamper, "contact", problemDamperHandler)
        
        state.problemDamper = problemDamper.currentValue("contact")
    }
    
    if (airVelocity != null) {
        subscribe(airVelocity, "velocity", airVelocityHandler)
        
        state.airVelocity = airVelocity.currentValue("velocity").toFloat()
    }    
}

def airVelocityHandler(evt) {
    state.airVelocity = evt.value.toFloat()
       
    statusCheck()
}

def problemDamperHandler(evt) {
    state.problemDamper = evt.value
    
    statusCheck()
}

def bypassDamperMsgHandler(evt) {
	def msg = evt.value
       
    if ((state.powerCycled == null) && (msg.contains("Calibrate"))) {
        send("${bypassDamper} needs to be calibrated.  It was either updated, power cycled, or suffered a power loss.")
    
        state.powerCycled = now()
       
        runIn(60, resetBypassDamper) //reset msg handler so it delays one minute before sending another message
    }
}

def bypassDamperSetpointHandler(evt) {   
    state.bypassDamper = evt.value.toInteger()
}

def resetBypassDamper() {
	state.powerCycled = null
}

def zlSetDamper(int openLevel) {
    sendEvent(name:"Setpoint", value: "${openLevel}", descriptionText:"Damper")
    
    if (openLevel == 0) {
        bypassDamper.set0()
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 0%</span>")
    }
    else if (openLevel == 1) {
        bypassDamper.set1()
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 10%</span>")
    }
    else if (openLevel == 2) {
        bypassDamper.set2()
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 20%</span>")
    }
    else if (openLevel == 3) {
        bypassDamper.set3()
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 30%</span>")
    }
    else if (openLevel == 4) {
        bypassDamper.set4()
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 40%</span>")
    }
    else if (openLevel == 5) {
        bypassDamper.set5() 
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 50%</span>")
    }
    else if (openLevel == 6) {
        bypassDamper.set6()
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 60%</span>")
    }
    else if (openLevel == 7) {
        bypassDamper.set7() 
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 70%</span>")
    }
    else if (openLevel == 8) {
        bypassDamper.set8() 
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 80%</span>")
    }
    else if (openLevel == 9) {
        bypassDamper.set9() 
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 90%</span>")
    }    
    else {
        bypassDamper.set10()  
        app.updateLabel("Smart Damper <span style=\"color:blue\"> 100%</span>")
    }  
}

def statusCheck() {       

    if (state.problemDamper == "open") {
        if (state.bypassDamper != 0) {
            if (enableDebug) log.info("Bypass damper = ${state.bypassDamper}, Problem damper = ${state.problemDamper}, Air velocity = ${state.airVelocity}, Bypass damper setting --> 0")         

            zlSetDamper(0)
        }
        else {
            if (enableDebug) log.info("Bypass damper = ${state.bypassDamper}, Problem damper = ${state.problemDamper}, Air velocity = ${state.airVelocity}, Bypass damper setting --> no change")   
        }
    }
    else {
        if (state.airVelocity > maxVelocity.toFloat()) {    
            if (state.bypassDamper != reliefPosition.toInteger()) {
                if (enableDebug) log.info("Bypass damper = ${state.bypassDamper}, Problem damper = ${state.problemDamper}, Air velocity = ${state.airVelocity}, Bypass damper setting --> ${reliefPosition}") 
            
                zlSetDamper(reliefPosition.toInteger()) 
            }
            else {
                if (enableDebug) log.info("Bypass damper = ${state.bypassDamper}, Problem damper = ${state.problemDamper}, Air velocity = ${state.airVelocity}, Bypass damper setting --> no change")   
            }
        }          
        else if (state.airVelocity < minVelocity.toFloat()) {      
            if (state.bypassDamper != 0) {
                if (enableDebug) log.info("Bypass damper = ${state.bypassDamper}, Problem damper = ${state.problemDamper}, Air velocity = ${state.airVelocity}, Bypass damper setting --> 0") 
            
                zlSetDamper(0) 
            }
            else {
                if (enableDebug) log.info("Bypass damper = ${state.bypassDamper}, Problem damper = ${state.problemDamper}, Air velocity = ${state.airVelocity}, Bypass damper setting --> no change")   
            }
        }   
        else {
            if (enableDebug) log.info("Bypass damper = ${state.bypassDamper}, Problem damper = ${state.problemDamper}, Air velocity = ${state.airVelocity}, Bypass damper setting --> no change (${minVelocity}-${maxVelocity})")           
        }
    }
}

def send(msg) {
    sendPushMessage.deviceNotification(msg)
}