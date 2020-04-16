/*  Smart Garage Heater
 *
 *  Version 
 *	1.0.0 	11/7/17	initial
 *	1.1.0	1/18/18	improved door opened and closed logic
 *	1.1.1	1/31/18	fixed bug regarding initialization (forces heater off)
 *  2.0.0	2/16/18	split doors to allow more granular control, especially with the service door
 *	2.0.1	2/27/18	added touch switch
 *	2.1.0	4/26/18	added seasons, delay due to temperature
 *  3.0.0   9/28/19   updated to Hubitat
 *  3.0.1   12/11/19 removed setAway and resumeProgramming
 *
 *  Copyright 2017-19 John Cox
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
    	name: "Smart Garage Heater",
    	namespace: "johnlcox",
    	author: "john.l.cox@live.com",
    	description: "Controls your garage thermostat based upon temp and doors.",
    	category: "Convenience",
    	iconUrl: "",
    	iconX2Url: "",
	importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Garage-Heater.groovy"
)


private def about() {
    return "Smart Garage Thermostat - controls your garage thermostat based upon temp and doors.  All aspects of this app are customizable.\n${updatedDate()} ${version()} ${copyright()}"
}

private def paypal() {
    return "You can contribute to the development of this app by making a PayPal donation to https://www.paypal.me/johnlcox. I appreciate your support."
}

private def updatedDate() {
	return "Updated 12/11/2019"
}

private def version() {
    return "Version 3.0.1"
}

private def copyright() {
    return "Copyright © 2017-19 John Cox"
}

preferences {
	section() {
		paragraph "Smart Garage Heater ${updatedDate()} ${version()} ${copyright()}"
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

	section() {
		input "garageThermostat", "capability.thermostat", title: "Thermostat?", multiple: false, required: true
	}
	section("Delay Main") {
		input "garageDelayOff", "number", title: "Turning equipment off (min)?", multiple: false, required: true, defaultValue: 3
		input "garageDelayOn", "number", title: "Turning equipment on (min)?", multiple: false, required: true, defaultValue: 5
	}
	section("Delay Side") {
		input "garageDelayOff2", "number", title: "Turning equipment off (min)?", multiple: false, required: true, defaultValue: 3
		input "garageDelayOn2", "number", title: "Turning equipment on (min)?", multiple: false, required: true, defaultValue: 5
	}        
	section() {
		paragraph "If the outside temperature is above or equal to the threshold, the unit will be turned off." 
	}

	section("Outside temperature reference") {
		input "pws", "capability.temperatureMeasurement", title: "PWS?", multiple: false, required: true
		input "outsideTemp", "number", title: "Temperature (F)?", multiple: false, required: true, defaultValue: 65
		input "tempDelay", "number", title: "Delay due to temp (min)?", required:true, defaultValue:60
	}

	section("Seasons") {
		input "seasonHeat", "enum", title: "Heat?", options:["1","2","3","4","5","6","7","8","9","10","11","12"], multiple: true, required: true
		input "seasonNoHeat", "enum", title: "No Heat?", options:["1","2","3","4","5","6","7","8","9","10","11","12"], multiple: true, required: true
		input "seasonOverride", "number", title: "Override temperature (F)?", required: true, defaultValue: 40
	}

	section() {
		paragraph "If the any doors are opened, the unit will be turned off." 
	}

	section("Overhead Garage Doors") {
		input "garageDoor1", "capability.contactSensor", title: "Garage door (big)?", multiple: false, required: true
		input "garageDoor2", "capability.contactSensor", title: "Garage door (small)?", multiple: false, required: true
	}
	section("Service Door") {
		input "garageDoor3", "capability.contactSensor", title: "Garage door (service)?", multiple: false, required: true
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
	state.mode = null
    state.attempts = 0 
    
    if (pws != null) {
    	subscribe(pws, "temperature", pwsHandler)
    }    
    
    subscribe(garageDoor1, "contact", doorHandler1)
    subscribe(garageDoor2, "contact", doorHandler2)    
    subscribe(garageDoor3, "contact", doorHandler3)
    
    subscribe(location, "mode", modeChange)

   	scheduleChecks()

	state.doorOpened = now()
    state.doorClosed = now()
    state.doorLast = "closed"
    state.doorOpened2 = now()
    state.doorClosed2 = now()
    state.doorLast2 = "closed"
    state.doorOpened3 = now()
    state.doorClosed3 = now()
    state.doorLast3 = "closed"
    state.tempOff = ""   
}

def pwsHandler(evt) {
	def currentTemp = evt.value

    if (enableDebug) log.info("pws change = ${currentTemp}, calling checkProcess")
    
    checkProcess()
}

def getSchedule(time) { 
    def parts1 = time.tokenize("T")

   	def parts2 = parts1[1].tokenize(':')
  	def hour = parts2[0].toInteger()
	def minute = parts2[1].toInteger()
    
    def scheduleTime = ""
    
	scheduleTime = "30 ${minute} ${hour} ? * *" 
    
    return scheduleTime
}

def checkTemp() {
    def outside = pws.currentValue("temperature")     
    
    def processTemp = false
    
    if (state.tempOff == "") {
 		processTemp = true
    }
    else {
    	processTemp = checkTempOff()
    }
    
    def tempOK = false
 
 	if (processTemp) {
        if (outside < outsideTemp) {
        	tempOK = true
    	}
    	else {
        	tempOK = false
            
            state.tempOff = now()
        }
    }
    
    return tempOK
}

def getMonth() {
	def currentDate = new Date()
	def currentMonth = (currentDate.format('MM', location.timeZone) as BigInteger)
	def currentday = (currentDate.format('dd', location.timeZone) as BigInteger)
    
    return currentMonth
}

def checkSeason() {
	def seasonOK = false
    
	def month = getMonth().toString()
    
    if (month in seasonHeat) {
    	seasonOK = true
    }
    else {
    	seasonOK = false
        
        def outside = pws.currentValue("temperature") 
        
        if (outside < seasonOverride) {
        	seasonOK = true
        }
    }
      
    return seasonOK
}

def checkDoors() {
    def doorOK = false

    if (garageDoor1.currentValue("contact").contains("open")) {
        doorOK = false
    }
    else {
        doorOK = true
    }

    if (doorOK) {
        if (garageDoor2.currentValue("contact").contains("open")) {
            doorOK = false
        }

        if (doorOK) {
            if (garageDoor3.currentValue("contact").contains("open")) {
                doorOK = false
            }
        }
    }
    
    return doorOK
}

def checkTimeOpened() {
    def	timeOpenedOK1 = false
    def	timeOpenedOK2 = false
    def	timeOpenedOK3 = false
    
    timeOpenedOK1 = checkSpecificDoorOpened(state.doorOpened, state.doorClosed, garageDelayOff, garageDelayOn) 
    timeOpenedOK2 = checkSpecificDoorOpened(state.doorOpened2, state.doorClosed2, garageDelayOff, garageDelayOn) 
    timeOpenedOK3 = checkSpecificDoorOpened(state.doorOpened3, state.doorClosed3, garageDelayOff2, garageDelayOn2) 
      
    def timeOpened = timeOpenedOK1 && timeOpenedOK2 && timeOpenedOK3
        
    return (timeOpened)
}

def checkSpecificDoorOpened(doorOpened, doorClosed, delayOn, delayOff) {
	def timeOpenedOK = false
    def elapsed = 0
    
    if ((doorOpened != null) && (doorClosed != null)) {
        if (doorOpened > doorClosed) { //door opened more recently
            elapsed = now() - doorOpened

            elapsed = elapsed/1000/60 //minutes

            if (elapsed >= delayOff) { 
                timeOpenedOK = false
            }
            else {
                timeOpenedOK = true
            }
        }
        else {      
            elapsed = now() - doorClosed

            elapsed = elapsed/1000/60 //minutes

            if (elapsed < delayOn) { 
                timeOpenedOK = false
            }
            else {
                timeOpenedOK = true
            }
        }
    }
    
    return timeOpenedOK
}

def checkTimeClosed() {
	def	timeClosedOK1 = false
    def	timeClosedOK2 = false
    def	timeClosedOK3 = false
    
	timeClosedOK1 = checkSpecificDoorClosed(state.doorOpened, state.doorClosed, garageDelayOff, garageDelayOn) 
    timeClosedOK2 = checkSpecificDoorClosed(state.doorOpened2, state.doorClosed2, garageDelayOff, garageDelayOn) 
	timeClosedOK3 = checkSpecificDoorClosed(state.doorOpened3, state.doorClosed3, garageDelayOff2, garageDelayOn2)     
    
    def timeClosed = timeClosedOK1 && timeClosedOK2 && timeClosedOK3    
    
    return (timeClosed)
}

def checkSpecificDoorClosed(doorOpened, doorClosed, delayOff, delayOn) {
	def	timeClosedOK = false
	def elapsed = 0
    
    if ((doorOpened != null) && (doorClosed != null)) {
        if (doorClosed > doorOpened) { //door closed more recently
        	//checking to see if it was opened and closed quickly
            def diff = (doorClosed - doorOpened)/1000/60 //minutes
            
            if (diff < delayOff) {
            	//then we should not turn the garage off
                //set elasped to a high number
                elapsed = 99
            }
            else {      
            	elapsed = now() - doorClosed

            	elapsed = elapsed/1000/60 //minutes
        	}

            if (elapsed < delayOn) { 
                timeClosedOK = false
            }
            else {
                timeClosedOK = true
            }
        }
        else {
            if (doorOpened == doorClosed) {
                //default values from install
                elapsed = 0
            }
            else {
                elapsed = now() - doorOpened

                elapsed = elapsed/1000/60 //minutes
            }

            if (elapsed >= delayOff) { 
              	timeClosedOK = false
            }
            else {
                timeClosedOK = true
            }
        }
    }
    
    return timeClosedOK
}

def checkTempOff() {
	def	tempOK = false
	def elapsed = 0
    
    if (state.tempOff != null) {
    	if (state.tempOff == "") {
        	state.tempOff = 0
        }
    
        elapsed = now() - state.tempOff
		
    	elapsed = elapsed/1000/60 //minutes
    }
    else {
    	elapsed = 999
    }
    
    if (elapsed < tempDelay) { 
        tempOK = false
    }
    else {
        tempOK = true
    }
    
    return tempOK
}

def checkProcess() {
    def tempOK = checkTemp()
    def doorOK = checkDoors()
    def timeOpenedOK = checkTimeOpened()
    def timeClosedOK = checkTimeClosed()
    def seasonOK = checkSeason()
    
    if (enableDebug) log.info("tempOK = ${tempOK}, doorOK = ${doorOK}, seasonOK = ${seasonOK}, timeOpenedOK = ${timeOpenedOK}, timeClosedOK = ${timeClosedOK}")                

    def timeNow = now()

    if (tempOK && timeOpenedOK && timeClosedOK && seasonOK && checkTempOff()) {
        if (garageThermostat.currentValue("thermostatMode").contains("heat")) { 
            if (enableDebug) log.info("-----> ${garageThermostat} thermostat already set to heat")
        }
        else {
            sendEvent(name:"Thermostat", value: "HEAT", descriptionText:"${garageThermostat}")
            
            
            garageThermostat.heat()

            app.updateLabel("Smart Garage Heater <span style=\"color:green\"> On</span>")

            if (enableDebug) log.info("-----> setting ${garageThermostat} thermostat to heat")   

            def msg = "Setting ${garageThermostat} thermostat to heat (${(new Date(timeNow).format('h:mm', location.timeZone))})."

            sendMessage(msg)
        }
    }
    else if (tempOK && timeClosedOK && seasonOK && checkTempOff()) {       
        //
    }
    else {
        if (garageThermostat.currentValue("thermostatMode").contains("off")) {
            if (enableDebug) log.info("${garageThermostat} thermostat already set to off") 

            app.updateLabel("Smart Garage Heater <span style=\"color:red\"> Off</span>")
        }  
        else {
            sendEvent(name:"Thermostat", value: "OFF", descriptionText:"${garageThermostat}")
            
            garageThermostat.off()

            app.updateLabel("Smart Garage Heater <span style=\"color:red\"> Off/span>")              

            def msg = ""

            if (seasonOK) {
                if (tempOK) {
                    if (enableDebug) log.info("setting ${garageThermostat} thermostat to off")  

                    msg = "Setting ${garageThermostat} thermostat to off (${(new Date(timeNow).format('h:mm', location.timeZone))})."
                }
                else {
                    if (enableDebug) log.info("setting ${garageThermostat} thermostat to off due to outside temperature")  

                    msg = "Setting ${garageThermostat} thermostat to off due to outside temperature (${(new Date(timeNow).format('h:mm', location.timeZone))})."
                }
            }
            else {
                if (enableDebug) log.info("setting ${garageThermostat} thermostat to off due to the season")  

                msg = "Setting ${garageThermostat} thermostat to off due to the season (${(new Date(timeNow).format('h:mm', location.timeZone))})."
            }

            sendMessage(msg)
        }
    }
}

def doorHandler1(evt) {
	if (evt.value == "open") {
    	atomicState.doorOpened = now()
        atomicState.doorLast = "open"
    }
    else {
    	atomicState.doorClosed = now()
        atomicState.doorLast = "closed"
   	}
}

def doorHandler2(evt) {
	if (evt.value == "open") {
    	atomicState.doorOpened2 = now()
        atomicState.doorLast2 = "open"
    }
    else {
    	atomicState.doorClosed2 = now()
        atomicState.doorLast2 = "closed"
   	}
}

def doorHandler3(evt) {
	if (evt.value == "open") {
    	atomicState.doorOpened3 = now()
        atomicState.doorLast3 = "open"
    }
    else {
    	atomicState.doorClosed3 = now()
        atomicState.doorLast3 = "closed"
   	}
}

def modeChange(evt) {
	if (enableDebug) log.debug "processing mode change to ${evt.value}"

	state.mode = null
    state.attempts = 0 
    
    if (evt.value == "Day") {
		homeMode("day")
	} 
    else if (evt.value == "Night") {
		nightMode("night")
    }
    else if (evt.value == "Away") {
		awayMode("away")
    }
    else if (evt.value == "Vacation") {
		vacMode("vacation")
    }
}

def homeMode(msg) {
    if (garageThermostat != null) {
    	if (enableDebug) log.info("running day mode")
              
        //garageThermostat.resumeProgram()

        if (enableDebug) log.info("Setting ${garageThermostat} to resume programming.")
    }
}

def nightMode(msg) {
    if (garageThermostat != null) {
    	if (enableDebug) log.info("running night mode")
               
        //garageThermostat.resumeProgram()

        if (enableDebug) log.info("Setting ${garageThermostat} to resume programming.") 
    }
}        

def awayMode(msg) {
	if (enableDebug) log.info("running away mode")
    
    //garageThermostat.setAway()
}        

def vacMode(msg) {
	if (enableDebug) log.info("running vacation mode")
    
    //garageThermostat.setAway()
}    

def scheduleChecks() {      
	if (resumeFrequency == "60") {
        schedule("0 */5 * * * ?", checkProcess)
    }
    else {
        schedule("0 */5 * * * ?", checkProcess)
    }
}

def sendMessage(msg) {
    sendPushMessage.deviceNotification(msg)
}