/*  Smart Heater
*
* 2.0.0   	9/28/19    	updated to Hubitat
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
    name: "Smart Heater",
    version: "2.0.0",
    namespace: "johnlcox",
    author: "john.l.cox@live.com",
    description: "Controls your space heater.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Heater.groovy"
)

private def about() {
    return "Smart Heater - controls your space heater based upon temperature and thermostat settings. All aspects of this app are customizable. \n${updatedDate()} ${version()} ${copyright()}"
}

private def paypal() {
    return "You can contribute to the development of this app by making a PayPal donation to https://www.paypal.me/johnlcox. I appreciate your support."
}

private def updatedDate() {
	return "Updated 10/11/2019"
}

private def version() {
    return "Version 2.0.0"
}

private def copyright() {
    return "Copyright © 2017-19 John Cox"
}

preferences {
	section() {
		paragraph "Smart Heater ${updatedDate()} ${version()} ${copyright()}" 
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

	section("Space Heater") {
		input "heaterOutlet1", "capability.switch", title: "Heater outlet?", multiple: false, required: false
		input "heaterIR1", "capability.switch", title: "Heater IR control?", multiple: false, required: false
	}
	section("Open area for air flow") {
		input "door1","capability.contactSensor", title: "Door?", multiple: false, required: false
	}
	section("Track thermostat/temperature sensors") {
		input "thermostat1", "capability.thermostat", title: "Thermostat?", multiple: false, required: false
		input "sensor1", "capability.temperatureMeasurement", title: "Temperature sensor?", multiple: false, required: false
	}
	section("Thresholds") {
		paragraph "If the outside temperature is below the outside temperature threshold, then the temperature difference is used as it.  " +
			"If the outside temperature is above the outside temeprature threshold, then the temperature difference will be doubled."
		input "tempDiff1", "decimal", title: "Temperature difference (F)?", multiple: false, required: false, defaultValue: 1.5
	}
	section("Outside") {
		input "pws", "capability.temperatureMeasurement", title: "Outisde temperature?", multiple: false, required: false
		input "threshold", "number", title: "Temperature threshold (F)?", multiple: false, required: false, defaultValue: 15
	}
	section("Only when these switches are NOT on...") {
		input "avoidSwitch", "capability.switch", title: "Avoid switch?", multiple: true, required: false
	}
	section("Only in these mode(s)...") {
		input "modes", "mode",title:"Mode(s)?", required: true, multiple: true
	}              

	section( "Notifications" ) {
		input "sendPushMessage", "capability.notification", title: "Notifications?"
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
    scheduleChecks()
    
    if (sensor1 != null) {
    	subscribe(sensor1, "temperature", temperatureHandler)
    }

    if (pws != null) {
    	subscribe(pws, "temperature", pwsHandler)
    }
}

def scheduleChecks() {   
	def sched = "15 5/5 * * * ?"
    
  	schedule(sched, process) //changed 0 to 15 to hopefully avoid time out
}

def process() {
	if (modes != null) {
    	if (location.mode in modes) {
            if ((heaterOutlet1 != null) && (heaterIR1 != null) && (thermostat1 != null) && (sensor1 != null)) {
                def currentThermostatSetpoint = thermostat1.currentValue("heatingSetpoint")
                def currentThermostatMode = thermostat1.currentValue("thermostatMode")
                def currentSensorTemp = sensor1.currentTemperature
                def currentPWSTemp = getPWSTemp()
                
                //check door status
                def currentDoorStatus = getDoorStatus(door1)
                //calc temp diff
                def currentTempDiff = getTempDiff()
                //check avoid switches
                def currentSwitchStatus = getSwitchStatus(avoidSwitch)
                
                def effectiveThreshold = currentThermostatSetpoint - currentTempDiff

                if (enableDebug) log.info("location mode = ${location.mode}, thermostat mode = ${currentThermostatMode}, thermostat setpoint = ${currentThermostatSetpoint}, effective temp = ${effectiveThreshold}, temp diff = ${currentTempDiff}, temp = ${currentSensorTemp}, outside = ${currentPWSTemp}, door = ${currentDoorStatus}")

				if (currentSwitchStatus) {
                    if ((currentThermostatMode == "heat") || (currentThermostatMode == "auto")) {
                        if (currentSensorTemp <= effectiveThreshold) {//below the thermostat setpoint
                            //the area is colder than the thermostat setpoint, so lets turn on heater
                            if (currentDoorStatus) { //door is either open or not configured
                                activateHeater1("process (ON) current temp (${currentSensorTemp}) less than thermostat (${currentThermostatSetpoint}) minus threshold (${currentTempDiff}) --> (${effectiveThreshold})")
                            }
                            else {
                                sendMessage("${door1} is closed so ${heaterOutlet1} cannot be activated.")
                            }                
                        }
                        else if (currentSensorTemp >= currentThermostatSetpoint) {//greater the thermostat setpoint
                            //the area is warmer than the thermostat setpoint, so lets turn off heater
                            if (currentDoorStatus) { //door is either open or not configured
                                deactivateHeater1("process (OFF) current temp (${currentSensorTemp}) greater than thermostat (${currentThermostatSetpoint}) minus threshold (${currentTempDiff}) --> (${effectiveThreshold})")
                            }
                            else {
                                deactivateHeater1("${door1} is closed so ${heaterOutlet1} cannot be deactivated appropriately.")
                                
                                sendMessage("${door1} is closed so ${heaterOutlet1} cannot be deactivated appropriately.")
                            }
                        }
                        else {
                        	if (enableDebug) log.info("process (NO CHANGE) current temp (${currentSensorTemp}) within the thermostat (${currentThermostatSetpoint}) minus threshold (${currentTempDiff}) --> (${effectiveThreshold})")
                        }
                    }
                    else {
                        deactivateHeater1("thermostat mode = ${currentThermostatMode}, not valid for space heater = ${heaterOutlet1}")
                    }
             	}
                else {
               		deactivateHeater1("switches on = ${avoidSwitch}, not valid for space heater = ${heaterOutlet1}")                
                }
            }
      	}
        else {
            deactivateHeater1("location mode = ${location.mode}, not valid for space heater = ${heaterOutlet1}")
        }
  	}
}

def activateHeater1(msg) {
    if (enableDebug) log.info(msg)

	startHeaterOutlet1()

    runIn(15, startHeaterIR1)
    
    app.updateLabel("Smart Heater <span style=\"color:green\"> On</span>")    
}

def deactivateHeater1(msg) {
	if (enableDebug) log.info(msg)
    
    stopHeaterIR1()
    
	runIn(15, stopHeaterOutlet1)  
    
    app.updateLabel("Smart Heater <span style=\"color:red\"> Off</span>")    
}

def startHeaterOutlet1() {
	if (!allOn(heaterOutlet1)) {
        sendEvent(name:"Heater", value: "ON", descriptionText:"Active")

        heaterOutlet1.on()
        
        if (enableDebug) log.info("turning ${heaterOutlet1} on")
    }
    else {
        if (enableDebug) log.info("${heaterOutlet1} already on")
	}    
}

def stopHeaterOutlet1() {
	if (allOn(heaterOutlet1)) {
        sendEvent(name:"Heater", value: "OFF", descriptionText:"Inactive")

        heaterOutlet1.off()
        
        if (enableDebug) log.info("turning ${heaterOutlet1} off")
    }
    else {
        if (enableDebug) log.info(" ${heaterOutlet1} already off")    
    }
}

def startHeaterIR1() {
	if (!allOn(heaterIR1)) {
		heaterIR1.on()

        if (enableDebug) log.info("turning ${heaterIR1} on")
    }
   	else {
        if (enableDebug) log.info("${heaterIR1} already on")
	}    
}

def stopHeaterIR1() {
	if (allOn(heaterIR1)) {
		heaterIR1.off()
        
        if (enableDebug) log.info("turning ${heaterIR1} off")
    }
   	else {
        if (enableDebug) log.info("${heaterIR1} already off")
	}
}

def getSwitchStatus(switches) {
	def currentSwitchStatus = false
    
    if (switches != null) {
    	currentSwitchStatus = allOn(switches)
   	}
    
    return !currentSwitchStatus
}

def allOn(switches){
    def allOn = true
	switches.each {eachSwitch->
        if (eachSwitch.currentValue("switch") == "off") {
            allOn = false
        }
	}
	return allOn
}

def allOff(switches){
    def allOff = true
	switches.each {eachSwitch->
        if (eachSwitch.currentValue("switch") == "on") {
            allOff = false
        }
	}
	return allOff
}

def getDoorStatus(door) {
	def currentDoorStatus = true
    
    if (door != null) {
        currentDoorStatus = door.currentValue("contact")

        if (currentDoorStatus == "closed") {
            currentDoorStatus = false
        }
    }
    
    return currentDoorStatus
}

def getTempDiff() {
    def currentTempDiff = tempDiff1

	if (pws != null) {
    	def currentOutsideTemp = pws.currentTemperature
    
    	if (currentOutsideTemp > threshold) {
    		currentTempDiff = tempDiff1 * 2
    	}
    }
    
	return currentTempDiff
}

def getPWSTemp() {
    def currentPWSTemp = "n/a"

	if (pws != null) {
    	currentPWSTemp = pws.currentTemperature
    }
    
	return currentPWSTemp
}

def temperatureHandler(evt) {
	def currentTemp = evt.value

    process()
}

def pwsHandler(evt) {
	def currentTemp = evt.value

    if (enableDebug) log.info("pws change = ${currentTemp}, calling process")
    
    process()
}

def sendMessage(msg) {
   	sendPushMessage.deviceNotification(msg)
}