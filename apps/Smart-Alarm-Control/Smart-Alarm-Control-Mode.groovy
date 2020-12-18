/*  Smart Alarm Control - Mode
*
* 4.0.0		10/3/19    	updated to Hubitat
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
    name: "Smart Alarm Control (Mode)",
    version: "4.0.0",
    namespace: "johnlcox",
    parent: "johnlcox:Smart Alarm Control",
    author: "john.l.cox@live.com",
    description: "Provides your ADT system with the ability to arm/disarm itself based on people coming/leaving home.",
    category: "Safety & Security",
    iconUrl: "",
    iconX2Url: "",
    importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Alarm-Control-Mode.groovy"
)

private def about() {
    return "Smart Alarm Control - Mode (Child) controls how your alarm responds to mode changes. All aspects of this app are customizable. \n ${updatedDate()} ${version()} ${copyright()}"
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
		paragraph "Smart Alarm Control (Mode) ${updatedDate()} ${version()} ${copyright()}"
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
		paragraph "When the mode changes to away, home, night, or vacation, determine whether the alarm mode should change and if so, to what state."
	}

	section("Changes to Away mode...") {
		input name: "modeAway", type: "mode", title:"When this mode becomes active:", required: true, multiple: false
		input name: "modeAwayActivate", type: "enum", title: "Activate the alarm?", options: ['Yes','No'], required: true, defaultValue: 'Yes'
		input name: "modeAwayAlarm", type: "enum", title:"In this mode:", required: true, multiple:false, options: ['Arm Stay','Arm Away','Disarm'], defaultValue: 'Arm Stay'
	}
	section("Changes to Day/Night mode...") {
		paragraph "Tranisition between home/night and night/home are generally triggered by surise and sunset events and are not normally intended or used to trigger alarm events.  " +
			"Selecting Yes will cause these mode changes to be ignored, othersie they will be acted upon accordingly."

		input name: "modeNormal", type: "enum", title: "Ignore home/night and night/home transitions?", options: ['Yes','No'], required: true, defaultValue: 'Yes'
	}
	section("Changes to Day mode...") {       
		input name: "modeHome", type: "mode", title:"When this mode becomes active:", required: true, multiple: false
		input name: "modeHomeActivate", type: "enum", title: "Activate the alarm?", options: ['Yes','No'], required: true, defaultValue: 'No'
		input name: "modeHomeAlarm", type: "enum", title:"In this mode:", required: true, multiple:false, options: ['Arm Stay','Arm Away','Disarm'], defaultValue: 'Disarm'
	}
	section("Changes to Night mode...") {       
		input name: "modeNight", type: "mode", title:"When this mode becomes active:", required: true, multiple: false
		input name: "modeNightActivate", type: "enum", title: "Activate the alarm?", options: ['Yes','No'], required: true, defaultValue: 'No'
		input name: "modeNightAlarm", type: "enum", title:"In this mode:", required: true, multiple:false, options: ['Arm Stay','Arm Away','Disarm'], defaultValue: 'Disarm'
	}
	section("Changes to Vacation mode...") {
		input name: "modeVac", type: "mode", title:"When this mode becomes active:", required: true, multiple: false
		input name: "modeVacActivate", type: "enum", title: "Activate the alarm?", options: ['Yes','No'], required: true, defaultValue: 'Yes'
		input name: "modeVacAlarm", type: "enum", title:"In this mode:", required: true, multiple:false, options: ['Arm Stay','Arm Away','Disarm'], defaultValue: 'Arm Stay'
	}        
	section("Changes to Stealth mode...") {
		input name: "modeStealth", type: "mode", title:"When this mode becomes active:", required: true, multiple: false
		input name: "modeStealthActivate", type: "enum", title: "Activate the alarm?", options: ['Yes','No'], required: true, defaultValue: 'Yes'
		input name: "modeStealthAlarm", type: "enum", title:"In this mode:", required: true, multiple:false, options: ['Arm Stay','Arm Away','Disarm'], defaultValue: 'Arm Stay'
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
	if (location != null) {
    	subscribe(location, "mode", modeChange)
  	}
    
	state.lastMode = location.mode
}

def modeChange(evt) {
	sendEvent(name:"Mode", value: "${evt.value}", descriptionText:"Mode")

    if (evt.value == "Day") {
    	if (modeHome != null) {
    	    if ((modeHomeActivate == "Yes") || ((state.lastMode != "Night") && (state.lastMode != "Day") && (state.lastMode != "Stealth"))) {
                if (location.mode == modeHome) {
                    if (enableDebug) log.info("Mode change to Day:  location.mode ${location.mode}, lastMode ${state.lastMode}, modeNormal ${modeNormal}")

                    if (((state.lastMode != "Night") && (state.lastMode != "Day")) || (modeNormal != "Yes")) { //this means that it is NOT sunrise, someone must have come home
                        activateAlarm(modeHomeAlarm)
                    }
                }
            }
    	}
        app.updateLabel("Mode <span style=\"color:green\"> Day</span>")
    } 
    else if (evt.value == "Night") {
    	if (modeNight != null) {
            if ((modeNightActivate == "Yes") || ((state.lastMode != "Night") && (state.lastMode != "Day") && (state.lastMode != "Stealth"))) {
                if (location.mode == modeNight) {
                    if (enableDebug) log.info("Mode change to Night:  location.mode ${location.mode}, lastMode ${state.lastMode}, modeNormal ${modeNormal}")

                    if (((state.lastMode != "Day") && (state.lastMode != "Night")) || (modeNormal != "Yes")) { //this means that it is NOT sunset, someone must have come home
                        activateAlarm(modeNightAlarm)
                    }
                }
            }
     	}
        app.updateLabel("Mode <span style=\"color:green\"> Night</span>")       
    }
    else if (evt.value == "Away") {
    	if (modeAway != null) {
            if (modeAwayActivate == "Yes") {
                if (location.mode == modeAway) {
                    if (enableDebug) log.info("Mode change to Away:  location.mode ${location.mode}, lastMode ${state.lastMode}, modeNormal ${modeNormal}")

                    activateAlarm(modeAwayAlarm)
                }
            }
     	}
        app.updateLabel("Mode <span style=\"color:red\"> Away</span>")
    }
    else if (evt.value == "Vacation") {
        if (modeVac != null) {
        	if (modeVacActivate == "Yes") {
            	if (location.mode == modeVac) {
            		if (enableDebug) log.info("Mode change to Vacation:  location.mode ${location.mode}, lastMode ${state.lastMode}, modeNormal ${modeNormal}")
                
                	activateAlarm(modeVacAlarm)
            	}
            }
        }
        app.updateLabel("Mode <span style=\"color:red\"> Vacation</span>")
    }
    else if (evt.value == "Stealth") {
        if (modeStealth != null) {
        	if (modeStealthActivate == "Yes") {
            	if (location.mode == modeStealth) {
            		if (enableDebug) log.info("Mode change to Stealth:  location.mode ${location.mode}, lastMode ${state.lastMode}, modeNormal ${modeNormal}")
                
                	activateAlarm(modeStealthAlarm)               
            	}
            }
        }
        app.updateLabel("Mode <span style=\"color:purple\"> Stealth</span>")
    }    
    
    state.lastMode = location.mode //if this doesn't work, change to state.lastMode = evt.value
    
    if (enableDebug) log.info("Mode change to ${evt.value}: state >>> location.mode ${location.mode}, lastMode ${state.lastMode}")
}

private activateAlarm(newAlarmMode) {
  	if (newAlarmMode == "Arm Stay") {
        turnOn("stay")
    }
    else if (newAlarmMode == "Arm Away") {
        turnOn("away")
	}
    else {
        turnOff()
    }
}

private turnOn(type) { 
    if (type == "away") { //away
        parent.alarm.lock()
    }
    else { //stay
        parent.alarm.on()
    }
	
	parent.refreshAlarm()
}

private turnOff() { 
    parent.alarm.off()
	
	parent.refreshAlarm()
}

private checkAlarmStatus() {
	def result = false 
    
	def alarmState = parent.alarm.currentState("status")
    
	if (alarmState.value == "HOME") {
        result = true
    }
    else if (alarmState.value == "AWAY") {
        result = true
    }
    else if (alarmState.value == "ALARM") {
    	result = true
    }
    else if (alarmState.value == "OFF") {
    	result = false
	} 
       
    return result
}