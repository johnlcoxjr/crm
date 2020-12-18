/*  Smart Alarm Control
*
* 4.0.0     10/3/19   	updated to Hubitat
*
* Copyright 2017-20 John Cox
*
* Developer retains all right, title, copyright, and interest, including all copyright, patent rights, trade secret in the Background technology.
* May be subject to consulting fees under the Agreement between the Developer and the Customer. Developer grants a non-exclusive perpetual license
* to use the Background technology in the Software developed for and delivered to Customer under this Agreement. However, the Customer shall make
* no commercial use of the Background technology without Developer's written consent.
* 
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
* WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied. 
*
* Software Distribution is restricted and shall be done only with Developer's written approval.
*/

import hubitat.helper.InterfaceUtils

definition(
    name: "Smart Alarm Control",
    version: "4.0.0",
    namespace: "johnlcox",
    author: "john.l.cox@live.com",
    description: "Smart Alarm Control",
    category: "Safety & Security",
    singleInstance: true,
    iconUrl: "",
    iconX2Url: "",
    importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Alarm-Control.groovy"
)

private def about() {
    return "Smart Alarm Control (Parent) controls all aspects of your alarm (turning on, turning off, presence, mode change, and notifications).  All aspects of this app are customizable. \n ${updatedDate()} ${version()} ${copyright()}"
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
		paragraph "Smart Alarm Control ${updatedDate()} ${version()} ${copyright()}" 
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
		input "enableDebug", "bool", title: "Enable debug logging?", required: false, defaultValue: false
	}
	
	section() {
		paragraph "Select the alarm you want to control."
	}

	section("Control this alarm...") {
		input "alarm", "capability.alarm", title:"Select alarm:",multiple: false, required: true
	}      

	section("Color") {
		section("Select the hues to indicate arm and disarm.") {
			input "hues", "capability.colorControl", title: "Which Hue bulbs (internal 1)?", required:false, multiple:true
		}

		section("Select the color and intensity for arming.") {
			input "colorArm","enum", title: "Arming color?", required: true, options: ["Brick Red", "Safety Orange", "Dark Orange", "Amber", "Gold", "Yellow", "Electric Lime", "Lawn Green", "Bright Green", "Lime", "Spring Green", "Turquoise", "Aqua", "Sky Blue", "Dodger Blue",
																					   "Navy Blue", "Blue", "Han Purple", "Electric Indigo", "Electric Purple", "Orchid Purple", "Magenta", "Hot Pink", "Deep Pink", "Raspberry", "Crimson", "Red"], defaultValue: "Red"
			input "intensityArm","enum", title: "Arming intensity?", required: true, options: ["1", "3", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60", "65", "70", "75", "80", "85", "90", "95", "100"], defaultValue: "100"
		}

		section("Select the color and intensity for disarming.") {
			input "colorDisarm","enum", title: "Disarming color?", required: true, options: ["Brick Red", "Safety Orange", "Dark Orange", "Amber", "Gold", "Yellow", "Electric Lime", "Lawn Green", "Bright Green", "Lime", "Spring Green", "Turquoise", "Aqua", "Sky Blue", "Dodger Blue",
																							 "Navy Blue", "Blue", "Han Purple", "Electric Indigo", "Electric Purple", "Orchid Purple", "Magenta", "Hot Pink", "Deep Pink", "Raspberry", "Crimson", "Red"], defaultValue: "Bright Green"
			input "intensityDisarm","enum", title: "Disarming intensity?", required: true, options: ["1", "3", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60", "65", "70", "75", "80", "85", "90", "95", "100"], defaultValue: "100"
		}

		section("Select the color and intensity for alert.") {
			input "colorAlert","enum", title: "Alert color?", required: true, options: ["Brick Red", "Safety Orange", "Dark Orange", "Amber", "Gold", "Yellow", "Electric Lime", "Lawn Green", "Bright Green", "Lime", "Spring Green", "Turquoise", "Aqua", "Sky Blue", "Dodger Blue",
																						"Navy Blue", "Blue", "Han Purple", "Electric Indigo", "Electric Purple", "Orchid Purple", "Magenta", "Hot Pink", "Deep Pink", "Raspberry", "Crimson", "Red"], defaultValue: "Bright Green"
			input "intensityAlert","enum", title: "Alert intensity?", required: true, options: ["1", "3", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60", "65", "70", "75", "80", "85", "90", "95", "100"], defaultValue: "100"
		}
	}        

	section() {
		paragraph "Depending upon which Smart Alarm Control features are activated:\n The alarm will be activated if the following people are home.\n The alarm will be deactivate and re-activated if the following people arrive.\n The alarm will send messages related to the arrival and departure of the following people."
	}

	section("If all of these people are home...") {
		input name: "people", type: "capability.presenceSensor", title:"Select people:", multiple: true, required: true
	}

	section {
		app(name: "Alarm", appName: "Smart Alarm Control (Alarm)", namespace: "johnlcox", title: "Alarm", multiple: false)
		app(name: "Mode", appName: "Smart Alarm Control (Mode)", namespace: "johnlcox", title: "Mode", multiple: false)
		app(name: "Presence", appName: "Smart Alarm Control (Presence)", namespace: "johnlcox", title: "Presence", multiple: false)
		app(name: "People", appName: "Smart Alarm Control (People)", namespace: "johnlcox", title: "People", multiple: false)
	}

	section("Notification (push/SMS)") {
		paragraph "Select whether you want push notifications and/or text messages.  Text messages will not be sent if the person is home at the time.  Text messages will be sent to anyone not at home."

		input name: "notification", type: "capability.notification", title: "Notification", required: true, multiple: true
	}   

	section("Messages will be aggregated to try and minimize the number of text messages...") {
		paragraph "Each message will be delayed by this threshold pending any additional messages sent within this window.  These messages will be aggregated and sent as one large message.  Each message received will increase the delay to the maximum set.  After the threshold has expired, all messages will be sent."

		input name: "msgThreshold", type: "number", title: "Aggregate message (seconds):", required: true, multiple: false, defaultValue: 20
	}                
}

def bulbFollowup() {
    if (hues != null) {
        hues.setLevel(intensityArm.toInteger())
    }
}

def activationLight(status) {      
    if (status == "arm") {
        sendColor(hues, colorArm, intensityArm)  
    }
    else if (status == "alert") {
        sendColor(hues, colorAlert, intensityAlert)  
    }
    else {
        sendColor(hues, colorDisarm, intensityDisarm)
    }

    runIn(5, bulbFollowup)

    runIn(15, activationLightOff)
    runIn(30, activationLightOff2)
}

def activationLightOff() {
	if (hues != null) {
     	hues.off()
    } 
}

def activationLightOff2() {
	activationLightOff()
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
	runEvery1Minute("refreshAlarm")

    childApps.each {child ->
        if (enableDebug) log.info "iterating child app: ${child.label}"
    }
    
    atomicState.sacState = ""
}

def refreshAlarm() {
	alarm.poll()
}

def aggregateMsg(msg) {
	def initMsg = atomicState.msgAggregate
    
    if ((initMsg == "") || (initMsg == null)) {
    	initMsg = msg
  	}
    else {
    	initMsg = initMsg + " " + msg
    }
    
    atomicState.msgAggregate = initMsg
    
    if (msgThreshold > 0) { //if there is a delay, let's wait and see
	    runIn(msgThreshold, processMsg)
 	}
    else { //if no delay, then let's fire it now.
    	processMsg()
    }
}

def setArrival() {
	atomicState.arrival = now()
}

def getArrival() {
	if (atomicState.arrival == null) {
		atomicState.arrival = now()
	}
	
	return atomicState.arrival
}

def processMsg() {
	def initMsg = atomicState.msgAggregate
    
    if ((initMsg == "") || (initMsg == null)) {
    	//do nothing, no message
  	}
    else {
    	send(initMsg)
            
    	atomicState.msgAggregate = ""
    }
}

def send(msg) {
	def duplicate = false

	if (atomicState.msg == msg) { //if equal, check time frame
	  	duplicate = true
    } //if not equal, assume not duplicate

	if (!duplicate) {
		atomicState.msg = msg
        atomicState.success = now()
        
        notification.deviceNotification(msg)       
    }
    
    if (enableDebug) log.info(msg)
}

def sendColor(bulbs, color, intensity) {
	if (bulbs != null) {
        if (enableDebug) log.info("Sendcolor = ${color}")

        def hueColor = 0
        def saturation = 100

        switch(color) {
            case "White":
                hueColor = 50
                saturation = 25
                break;
            case "Daylight":
                hueColor = 53
                saturation = 50
                break;
            case "Soft White":
                hueColor = 19
                saturation = 50
                break;
            case "Warm White":
                hueColor = 25
                saturation = 30 //83
                break;
            case "Brick Red":
                hueColor = 4
                saturation = 100
                break;
            case "Safety Orange":
                hueColor = 7
                saturation = 100
                break;
            case "Dark Orange":
                hueColor = 10
                saturation = 100
                break;             
            case "Amber":
                hueColor = 13
                saturation = 100
                break;             
            case "Gold":
                hueColor = 15
                saturation = 100
                break;             
            case "Yellow":
                hueColor = 17
                saturation = 100
                break;
            case "Electric Lime":
                hueColor = 21
                saturation = 100
                break;             
            case "Lawn Green":
                hueColor = 25
                saturation = 100
                break;             
            case "Bright Green":
                hueColor = 28
                saturation = 100
                break;             
            case "Lime":
                hueColor = 34
                saturation = 100
                break;             
            case "Spring Green":
                hueColor = 42
                saturation = 100
                break;             
            case "Turquoise":
                hueColor = 47
                saturation = 100
                break;             
            case "Aqua":
                hueColor = 50
                saturation = 100
                break;             
            case "Sky Blue":
                hueColor = 54
                saturation = 100
                break;             
            case "Dodger Blue":
                hueColor = 59
                saturation = 100
                break;             
            case "Navy Blue":
                hueColor = 61
                saturation = 100
                break;             
            case "Blue":
                hueColor = 63
                saturation = 100
                break;             
            case "Han Purple":
                hueColor = 71
                saturation = 100
                break;             
            case "Electric Indigo":
                hueColor = 74
                saturation = 100
                break;
            case "Electric Purple":
                hueColor = 78
                saturation = 100
                break;             
            case "Orchid Purple":
                hueColor = 82
                saturation = 100
                break; 
            case "Magenta":
                hueColor = 86
                saturation = 100
                break;             
            case "Hot Pink":
                hueColor = 89
                saturation = 100
                break;             
            case "Deep Pink":
                hueColor = 92
                saturation = 100
                break;             
            case "Raspberry":
                hueColor = 94
                saturation = 100
                break;             
            case "Crimson":
                hueColor = 96
                saturation = 100
                break;     	
            case "Red":
                hueColor = 100
                saturation = 100
                break;
        }

        def lightLevel = 50

        if (intensity != null) {
            lightLevel = intensity.toInteger()
        }

        def newValue = [hue: hueColor, saturation: saturation, level: lightLevel]  

        if (enableDebug) log.info("new value = ${newValue}")

        bulbs*.setColor(newValue)
  	}
}