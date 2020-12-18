/* Smart Alarm Control - Presence
*
* 4.0.0		11/10/19 	updated to Hubitat
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
    name: "Smart Alarm Control (Presence)",
    version: "4.0.0",
    namespace: "johnlcox",
    parent: "johnlcox:Smart Alarm Control",
    author: "john.l.cox@live.com",
    description: "Provides your ADT system with the ability to turn itself off when someone arrives and then re-arm.",
    category: "Safety & Security",
    iconUrl: "",
    iconX2Url: "",
    importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Alarm-Control-Presence.groovy"
)


private def about() {
    return "Smart Alarm Control - Presence (Child) turns your alarm on and off based upon the presence of people in the home.  The alarm will also re-arm if within a specified window.\n ${updatedDate()} ${version()} ${copyright()}"
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
		paragraph "Smart Alarm Control (Presence)\n" + 
			"${updatedDate()}\n${version()}\n${copyright()}"
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
		paragraph "When someone arrives, disarm the alarm.  Presence can be ST fobs, mobile phone, or Life360." 
	}

	section("Re-arm...") {
		paragraph "The purpose to re-arm is to turn the alarm back on once you return home after which the alarm has already been activated automatically."
		input name: "rearmAlarm", type: "enum", title: "Re-arm the alarm?", options: ['Yes','No'], required: true

		paragraph "It is necessary to provide a re-arming window, otherwise the alarm will also be re-armed everytime you or someone in your house arrives home and the alarm was initially on."
		input name: "rearmBefore", type: "time", title:"Select time before which to re-arm (typically early morning):", required: true, multiple: false
		input name: "rearmAfter", type: "time", title:"Select time after which to re-arm (typically late evening/night):", required: true, multiple: false

		paragraph "Be sure to allow enough time between disarming (influenced by geofencing) and your ability to be safely inside."
		input name: "rearmTime", type: "number", title: "Re-arm (minutes):", required: true, multiple: false, defaultValue: 10
	}

	section() {
		paragraph "When presence changes, change the mode accordingly.  The mode will be changed between Home, Night, and Away.  If the mode is originally vacation, it will only be changed to Home or Night based upon time.  No condition will set the mode to Vacation - this must be done manually."
	}

	section("Home and Night...") {
		paragraph "If someone is home and the mode is not currently Home or Night, then process the change to Home or Night based upon the time."

		input name: "modeHome", type: "mode", title: "Home mode:", required: true, multiple: false 
		input name: "processHome", type: "enum", title: "Process mode home?", options: ['Yes','No'], required: true, multiple: false, defaultValue: 'Yes'

		input name: "modeNight", type: "mode", title: "Night mode:", required: true, multiple: false 
		input name: "processNight", type: "enum", title: "Process mode night?", options: ['Yes','No'], required: true, multiple: false, defaultValue: 'Yes'
	}

	section("Away...") {
		paragraph "If no one is home and the mode is not currently Away, then process the change to Away."

		input name: "modeAway", type: "mode", title: "Away mode:", required: true, multiple: false 
		input name: "processAway", type: "enum", title: "Process mode away?", options: ['Yes','No'], required: true, multiple: false, defaultValue: 'Yes'
	}

	section("Vacation...") {
		paragraph "If someone arrives home and the mode is not currently Home or Night, then process the change to Home or Night based upon the time."

		input name: "modeVacation", type: "mode", title: "Vacation mode:", required: true, multiple: false 
		input name: "processVacation", type: "enum", title: "Process mode vacation?", options: ['Yes','No'], required: true, multiple: false, defaultValue: 'Yes'
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
	if (parent.people != null) {
    	subscribe(parent.people, "presence", presenceChange)
    }
    
    subscribe(location, "mode", modeHandler)
    
    subscribe(parent.alarm, "status", alarmSystem)       
}   

def alarmSystem(evt) {
	def status = evt.value
    
	sendEvent(name:"Alarm", value: "${status}", descriptionText:"Status")

    if (enableDebug) log.info("status = ${status}")
    
	//set new status
	if (status.contains("OFF")) {
		//do nothing
	} 
	else if (status.contains("HOME")) {
		if (enableDebug) log.info("proactivley unscheduling processRearm since alarm has been armed (stay)")

		unschedule() //unschedule all jobs to save time
	} 
	else if (status.contains("AWAY")) {
		if (enableDebug) log.info("proactivley unscheduling processRearm since alarm has been armed (away)")

		unschedule() //unschedule all jobs to save time
	} 
	else if (status.contains("ALARM")) {
		if (enableDebug) log.info("proactivley unscheduling processRearm since alarm is active (alarming)")

		unschedule() //unschedule all jobs to save time
	}
}

def presenceChange(evt) {
    def person = evt.getDisplayName()
    
	sendEvent(name:"Presence", value: "${evt.value}", descriptionText:"${person}")
	
    if (evt.value == "present") {
		parent.setArrival()
		
		if (checkAlarmStatus()) {
            if (rearmAlarm == "Yes") {   
                turnOff()
                
                def timeNow = now()

                state.schedule = timeNow //set time now

                if (enableDebug) log.info("timeNow: ${timeNow}, schedule: ${state.schedule}")

                def scheduleBefore = timeToday(rearmBefore, location.timeZone)
                def scheduleAfter = timeToday(rearmAfter, location.timeZone)

                if (enableDebug) log.info("timeNow = ${timeNow}, scheduleBefore = ${scheduleBefore}, scheduleAfter = ${scheduleAfter}")

                if ((timeNow <= scheduleBefore.time) || (timeNow >= scheduleAfter.time)) { //setup to arm in rearmTime minutes	
                    if (enableDebug) log.info("scheduling scheduleRearm")

                    scheduleRearm() //separated to minimize the timeout error 
                }
            }
        }
        else { //alarm is off, but someone arrived
            if (!recentNotPresent) { //not (so must be a real arrival)
                parent.aggregateMsg("Alarm is already off.")
            }
		}
  	}

	//commenting out the next block causes changing from vaction to home/night to not execute
	if ((modeHome != null) && (modeNight != null)) {
        if (((processHome == "Yes") || (processNight == "Yes") || (processVacation == "Yes")) && anyoneIsHome()) {
            processModeChangeHomeNightVac()
        }
	        
		if (modeAway != null) {
	        if ((processAway == "Yes") && everyoneIsAway()) {
    	        processModeChangeAway()
        	}   	
      	}
 	}
}

def scheduleRearm() { //this routine is used to try and order the messages and then schedule the rearming process
    parent.aggregateMsg("Arming alarm in ${rearmTime} minutes.")

	if (enableDebug) log.info("scheduling processRearm")

    runIn(rearmTime * 60 - 10, processRearm)
	
	app.updateLabel("Presence <span style=\"color:purple\"> Re-arming</span>")
}

def modeHandler(evt) {
	sendEvent(name:"Mode", value: "${evt.value}", descriptionText:"Mode")
	
	if ((modeHome != null) && (modeNight != null)) {
    	if ((location.mode == modeHome) || (location.mode == modeNight)) {
        	//do nothing, unnecessary to change anything based on mode for home or night
			app.updateLabel("Presence <span style=\"color:green\"> ${evt.value}</span>")
       	}
        else {
        	//most changed to something that may pose a problem if a rearm was enabled
            if (enableDebug) log.info("proactivley unscheduling processRearm since mode is no longer home or night to prevent timing issues")
            
            unschedule() //unschedule all jobs to save time
        }
 	}
}

def processRearm() { //separated to allow for better chance to run???
    if (enableDebug) log.info("executing turnOn")

    turnOn()
    
    app.updateLabel("Presence <span style=\"color:blue\"> Re-armed</span>")
}

def processModeChangeHomeNightVac() {
    if ((location.mode == modeHome) || (location.mode == modeNight)) {
        //do nothing, already in Home or Night mode
    }
    else {
        //switch to Home or Night, from Away or Vacation
        def timeNow = now()
        
        def sunriseTime = timeToday(location.currentValue("sunriseTime"), location.timeZone)
	    def sunsetTime = timeToday(location.currentValue("sunsetTime"), location.timeZone)

		try {
            if (timeNow >= sunsetTime)  {	
                location.helloHome.execute(modeNight)

                parent.aggregateMsg("SAC - Changed mode to night.")
				
				app.updateLabel("Presence <span style=\"color:green\"> Night</span>")
            }
            else if (timeNow >= sunriseTime)  {	
                location.helloHome.execute(modeHome)

                parent.aggregateMsg("SAC - Changed mode to day.")
				
				app.updateLabel("Presence <span style=\"color:green\"> Day</span>")
            }
            else {
                location.helloHome.execute(modeNight)

                parent.aggregateMsg("SAC - Changed mode to night.")
				
				app.updateLabel("Presence <span style=\"color:green\"> Night</span>")
            }
        }
   		catch (e) { 
            if (enableDebug) log.warn("error generated accessing date/time timeNow = ${timeNow}, sunriseTime = ${sunriseTime}, sunsetTime = ${sunsetTime}")
		}
    } 
}

def processModeChangeAway(){
    if ((location.mode == modeHome) || (location.mode == modeNight)) {
        if (enableDebug) log.info("Everyone is away - preparing to change mode to Away")

        def delay = (falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold * 60 : 10 * 60 

        runIn(delay, "setAwayMode")
    }
}    

def setAwayMode() {
	if(everyoneIsAway()) { //see if everyone is still away (will also catch some false alarms
        parent.aggregateMsg("SAC - Changed mode to away.")
          
        location.helloHome.execute(modeAway)	
		
		app.updateLabel("Presence <span style=\"color:red\"> Away</span>")		
  	}         
}

private everyoneIsHome() {
	def result = true
    
	for (person in parent.people) {
		if (person.currentPresence == "not present") {
			result = false
            
			break
		}
	}
    
	return result
}
 
private everyoneIsAway() {
	def result = true
    
    if(parent.people.findAll { it?.currentPresence == "present" }) {
    	result = false
 	}
       
    return result
}

private anyoneIsHome() {
	def result = false
    
    if(parent.people.findAll { it?.currentPresence == "present" }) {
    	result = true
   	}
    
    return result
}

def turnOn() {
    parent.alarm.on()
	
	parent.refreshAlarm()
}

def turnOff() {
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