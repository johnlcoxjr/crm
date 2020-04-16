/*  Smart Fun
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
    name: "Smart Fun",
    version: "2.0.0",
    namespace: "johnlcox",
    author: "john.l.cox@live.com",
    description: "Controls your fun.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Fun.groovy"
)

private def about() {
    return "Smart Movie configures fun mode.  All aspects of this app are customizable. \n ${updatedDate()} ${version()} ${copyright()}"
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
			paragraph "Smart Fun ${updatedDate()} ${version()} ${copyright()}" 
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
    
    section("Fun") {       
        paragraph "Select the switch that initiates fun mode." 

        input "fun", "capability.switch", title: "Fun switch?", required: false, multiple: true          
    }

    section("Thermostats") {
        paragraph "Select the thermostat to override for temperature control during fun mode." 

        input "thermostats", "capability.thermostat", title: "Thermostat?", multiple: true, required: false
    }
    section("Settings") {
        paragraph "Set the desired temperature for fun mode." 

        input "cool", "number", title: "Cool (F)?", multiple: false, required: false, defaultValue: 72
        input "heat", "number", title: "Heat (F)?", multiple: false, required: false, defaultValue: 72
    }
    
    section("Lights (always on/off)") {  
        paragraph "Select the lights to control during fun mode." 

        input "turnOn", "capability.switch", title: "Lights on?", required: false, multiple: true          
        input "turnOff", "capability.switch", title: "Lights off?", required: false, multiple: true    
    }

    section("Lights & Doors (conditionally on/off)") {  
        paragraph "Select the lights and doors that will turn off only if the selected doors are open." 

        input "doors", "capability.contactSensor", title: "Doors?", required: false, multiple: true 
        input "turnOffConditional", "capability.switch", title: "Lights off?", required: false, multiple: true           
    }

    section("Doors (opened)") {  
        paragraph "Select the doors that will be reset if opened." 

        input "doorsOpened", "capability.contactSensor", title: "Doors?", required: false, multiple: true 
    }

    section("Virtual Lights (always off)") {  
        paragraph "Select the lights to ensure are off once mode is exited." 

        input "turnOffVirtual", "capability.switch", title: "Virtual lights off?", required: false, multiple: true    
    }
    
    section("Fans (always on)") {
        paragraph "Select the fans to control during fun mode."

        input "fanOn", "capability.switch", title: "Fan on?", required: false, multiple: true 
    }

    section("Fans & Temperature (conditionally on/off)") {       
        paragraph "Select the fans to conditionally control during fun mode."

        input "fanOff", "capability.switch", title: "Fan off?", required: false, multiple: true
    }

    section("Color") {
        paragraph "Select the color to control during fun mode."

        input "hues", "capability.colorControl", title: "Which color bulbs (accent)?", required:true, multiple:true
        input "hues2", "capability.colorControl", title: "Which color bulbs (lamps)?", required:true, multiple:true

        input "color","enum", title: "Color?", required: true, options: ["Brick Red", "Safety Orange", "Dark Orange", "Amber", "Gold", 
                                                                         "Yellow", "Electric Lime", "Lawn Green", "Bright Green", "Lime", "Spring Green", "Turquoise", "Aqua", "Sky Blue", "Dodger Blue",
                                                                         "Navy Blue", "Blue", "Han Purple", "Electric Indigo", "Electric Purple", "Orchid Purple", "Magenta", "Hot Pink", "Deep Pink",
                                                                         "Raspberry", "Crimson", "Red"], defaultValue: "Orchid Purple"

        input "intensity","enum", title: "Intensity (accent)?", required: true, options: ["1", "3", "5", "10", "15", "20", "25", "30", "35",
                                                                                          "40", "45", "50", "55", "60", "65", "70", "75", "80", "85", "90", "95", "100"], defaultValue: "5"
        input "intensity2","enum", title: "Intensity (lamps)?", required: true, options: ["1", "3", "5", "10", "15", "20", "25", "30", "35",
                                                                                          "40", "45", "50", "55", "60", "65", "70", "75", "80", "85", "90", "95", "100"], defaultValue: "10"
    }

    section("Color Temperature") {
        paragraph "Select the color temperature to return the color lights to once the mode is exited."

        input "colorTemp","number", title: "Color temperature (accent)?", required: false, defaultValue: 4000               
        input "colorTemp2","number", title: "Color temperature (lamps)?", required: false, defaultValue: 4000
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
    if (fun != null) {
        subscribe(fun, "switch", modeHandler)
    }   
}

def modeHandler(evt) {
    sendEvent(name:"Fun", value: "${evt.value}", descriptionText:"Status")

    if (evt.value == "on") {
        app.updateLabel("Smart Fun <span style=\"color:green\"> On</span>")
    }
    else {
        app.updateLabel("Smart Fun <span style=\"color:red\"> Off</span>")    
    }
    
	processCheck(evt.value)
}

def doorHandler(evt) {
    if (allOn(fun)) {
		runIn(15, processCheckAuto)
    }
}

def hueHandler(evt) {
    if (allOn(fun)) {
		runIn(15, processCheckAuto)
    }
}

def hueHandler2(evt) {
    if (allOn(fun)) {
		runIn(15, processCheckAuto)
    }
}

def processCheckAuto() {
	processCheck("on")
}

def processCheck(mode) {
    if (mode == "on") {
        if (thermostats != null) {
        	controlzone(thermostats, cool, heat)
            
            if (enableDebug) log.info("overriding themostats (${thermostats})")
        }
        
        if (turnOn != null) {
        	turnOn.on()
            
            if (enableDebug) log.info("turning lights (${turnOn}) on")
        }
        
        if (turnOff != null) {
        	turnOff.off()
            
            if (enableDebug) log.info("turning lights (${turnOff}) off")
        }

        if (turnOffConditional != null) {
        	if (!selectedDoorClosed()) { //none of the selected doors are closed
        		turnOffConditional.off()
                
                if (enableDebug) log.info("turning conditional lights (${turnOffConditional}) off")
           	}
            else {
                if (enableDebug) log.info("leaving conditional lights (${turnOffConditional}) alone")            
            }
        }

        if (fanOn != null) {
        	fanOn.on()
            
            if (enableDebug) log.info("turning fans (${fanOn}) on")
        }
        
        if (hues != null) {
        	sendColor(hues, intensity)
            
            if (enableDebug) log.info("turning hues (${hues}) on")
        }
        
        if (hues2 != null) {
        	sendColor(hues2, intensity2)
            
            if (enableDebug) log.info("turning hues (${hues2}) on")
        }   
            
    	runIn(15, bulbFollowup)
        
        if (doorsOpened != null) {
            subscribe(doorsOpened, "contact.open", doorHandler)
            
            if (enableDebug) log.info("enabling door watch (${doorsOpened})")
        }     
        
        if (hues != null) {
            subscribe(hues, "switch.off", hueHandler)
            
            if (enableDebug) log.info("enabling hue watch (${hues})")
        }     
        
        if (hues2 != null) {
            subscribe(hues2, "switch.off", hueHandler2)
            
            if (enableDebug) log.info("enabling hue 2 watch (${hues2})")
        }           
    } 
    else if (mode == "off") {
        if (thermostats != null)  {
        	thermostats.resumeProgram()
            
            if (enableDebug) log.info("resuming thermostats (${thermostats})")
        }

        if (turnOn != null) {
        	turnOn.off()
            
            if (enableDebug) log.info("turning lights (${turnOn}) off")
        }
        
        if (turnOff != null) {
        	turnOff.off()
            
            if (enableDebug) log.info("turning lights (${turnOff}) off")
        }
        
        if (fanOn != null) {
        	fanOn.off()
            
            if (enableDebug) log.info("turning fans (${fanOn}) off")
        }

        if (fanOff != null) {
        	fanOff.on()
            
            if (enableDebug) log.info("turning fans (${fanOff}) on")
        }       
        
        if (hues != null) {
        	setTemperature(hues, colorTemp)
        
        	hues.off()
            
            if (enableDebug) log.info("turning hues (${hues}) off")
        } 
        
        if (hues2 != null) {
        	setTemperature(hues2, colorTemp2)
            
        	hues2.off()
            
            if (enableDebug) log.info("turning hues (${hues2}) off")
        }   
        
        if (turnOffVirtual != null) {
        	turnOffVirtual.off()
            
            if (enableDebug) log.info("turning virtual lights (${turnOffVirtual}) off")
       	}
        
        if (doorsOpened != null) {
            unsubscribe(doorsOpened)
            
            if (enableDebug) log.info("disabling door watch (${doorsOpened})")
        }    
        
        if (hues != null) {
            unsubscribe(hues)
            
            if (enableDebug) log.info("disabling hue watch (${hues})")
        } 
        
        if (hues2 != null) {
            unsubscribe(hues2)
            
            if (enableDebug) log.info("disabling hue watch (${hues2})")
        }         
    }
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

def controlzone(stat, cool, heat) {
    if (stat.thermostatMode == "heat") {
        stat.setHeatingSetpoint(heat)
    }
    else if (stat.thermostatMode == "cool") {
    	stat.setCoolingSetpoint(cool)
    }
    
    if (enableDebug) log.info "Setting ${stat.label} >>>>> Cool = ${cool} Heat = ${heat}, mode = ${stat.thermostatMode}"
}

def sendColor(bulbs, brightness) {
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
			break;
		case "Safety Orange":
			hueColor = 7
			break;
        case "Dark Orange":
			hueColor = 10
			break;             
        case "Amber":
			hueColor = 13
			break;             
        case "Gold":
			hueColor = 15
			break;             
		case "Yellow":
			hueColor = 17
			break;
        case "Electric Lime":
			hueColor = 21
			break;             
        case "Lawn Green":
			hueColor = 25
			break;             
        case "Bright Green":
			hueColor = 28
			break;             
        case "Lime":
			hueColor = 34
			break;             
        case "Spring Green":
			hueColor = 42
			break;             
        case "Turquoise":
			hueColor = 47
			break;             
        case "Aqua":
			hueColor = 50
			break;             
        case "Sky Blue":
			hueColor = 54
			break;             
        case "Dodger Blue":
			hueColor = 59
			break;             
        case "Navy Blue":
			hueColor = 61
			break;             
        case "Blue":
			hueColor = 63
			break;             
        case "Han Purple":
			hueColor = 71
			break;             
		case "Electric Indigo":
			hueColor = 74
			break;
        case "Electric Purple":
			hueColor = 78
			break;             
        case "Orchid Purple":
			hueColor = 82
			break; 
        case "Magenta":
			hueColor = 86
			break;             
        case "Hot Pink":
			hueColor = 89
			break;             
        case "Deep Pink":
			hueColor = 92
			break;             
        case "Raspberry":
			hueColor = 94
			break;             
        case "Crimson":
			hueColor = 96
			break;     	
		case "Red":
			hueColor = 100
			break;
	}
    
    bulbs.on()

  	def lightLevel = 50
    
    if (brightness != null) {
    	lightLevel = brightness.toInteger()
    }
     
	def newValue = [hue: hueColor, saturation: saturation, level: lightLevel]  
    
    state.colorValue = newValue
	
    if (enableDebug) log.info("new value = ${newValue}")

	bulbs.setColor(newValue)
    
    bulbs.setLevel(lightLevel.toInteger())
}

def bulbFollowup() {
	if (allOn(fun)) {
        if (hues != null) {
            hues.setLevel(intensity.toInteger())
        }

        if (hues2 != null) {
            hues2.setLevel(intensity2.toInteger())
        } 
   	}
}

def setTemperature(bulb, value) {
    bulb.setColorTemperature(value)
}

private selectedDoorClosed() {
	def result = false
    
    if (doors.contact == "closed") {
    	result = true
 	}
    
    return result
}