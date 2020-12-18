/* Smart Alarm Control - People
*
* 3.0.0   10/1/19	updated to Hubitat
* 3.0.1   10/19/19  	eliminated error state from triggering lights
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
    name: "Smart Alarm Control (People)",
    version: "3.0.1",
    namespace: "johnlcox",
    parent: "johnlcox:Smart Alarm Control",
    author: "john.l.cox@live.com",
    description: "Provides your ADT system with the ability to lights on when people arrive.",
    category: "Safety & Security",
    iconUrl: "",
    iconX2Url: "",
    importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Alarm-Control-People.groovy"
)


private def about() {
    return "Smart Alarm Control - People (Child) alerts you of people coming and going as well as turning lights on and off appropriately. All aspects of this app are customizable. \n ${updatedDate()} ${version()} ${copyright()}"
}

private def paypal() {
    return "You can contribute to the development of this app by making a PayPal donation to https://www.paypal.me/johnlcox. I appreciate your support."
}

private def updatedDate() {
	return "Updated 10/19/2019"
}

private def version() {
    return "Version 3.0.1"
}

private def copyright() {
    return "Copyright © 2017-19 John Cox"
}

preferences {
	section() {
		paragraph "Smart Alarm Control (People) ${updatedDate()} ${version()} ${copyright()}"
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
	
	section("Status lights...") {
		input "huesStatus", "capability.colorControl", title: "Which Hue bulbs (external)?", required:false, multiple:true

		paragraph "Select the color and intensity for the external disarm notification."

		input "colorOff","enum", title: "Off color?", required: true, options: ["Brick Red", "Safety Orange", "Dark Orange", "Amber", "Gold", "Yellow", "Electric Lime", "Lawn Green", "Bright Green", "Lime", "Spring Green", "Turquoise", "Aqua", "Sky Blue", "Dodger Blue",
			"Navy Blue", "Blue", "Han Purple", "Electric Indigo", "Electric Purple", "Orchid Purple", "Magenta", "Hot Pink", "Deep Pink", "Raspberry", "Crimson", "Red"], defaultValue: "Bright Green"
		input "colorOn","enum", title: "On color?", required: true, options: ["Brick Red", "Safety Orange", "Dark Orange", "Amber", "Gold", "Yellow", "Electric Lime", "Lawn Green", "Bright Green", "Lime", "Spring Green", "Turquoise", "Aqua", "Sky Blue", "Dodger Blue",
			"Navy Blue", "Blue", "Han Purple", "Electric Indigo", "Electric Purple", "Orchid Purple", "Magenta", "Hot Pink", "Deep Pink", "Raspberry", "Crimson", "Red"], defaultValue: "Red"
		input "colorAlarm","enum", title: "Alarm color?", required: true, options: ["Brick Red", "Safety Orange", "Dark Orange", "Amber", "Gold", "Yellow", "Electric Lime", "Lawn Green", "Bright Green", "Lime", "Spring Green", "Turquoise", "Aqua", "Sky Blue", "Dodger Blue",
			"Navy Blue", "Blue", "Han Purple", "Electric Indigo", "Electric Purple", "Orchid Purple", "Magenta", "Hot Pink", "Deep Pink", "Raspberry", "Crimson", "Red"], defaultValue: "Blue"
		input "colorAlert","enum", title: "Alert color?", required: true, options: ["Brick Red", "Safety Orange", "Dark Orange", "Amber", "Gold", "Yellow", "Electric Lime", "Lawn Green", "Bright Green", "Lime", "Spring Green", "Turquoise", "Aqua", "Sky Blue", "Dodger Blue",
			"Navy Blue", "Blue", "Han Purple", "Electric Indigo", "Electric Purple", "Orchid Purple", "Magenta", "Hot Pink", "Deep Pink", "Raspberry", "Crimson", "Red"], defaultValue: "Electric Purple"
		input "intensityStatus","enum", title: "External intensity (home)?", required: true, options: ["1", "3", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60", "65", "70", "75", "80", "85", "90", "95", "100"], defaultValue: "75"

		input name: "turnOnStatus", type: "enum", title: "Turn on lights for arrival?", options: ['Yes','No'], required: true, defaultValue: "Yes"
		input name: "durationStatus", type: "number", title: "Time (minutes):", required: false, multiple: false, defaultValue: 10
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
	if (parent.people != null) {
    	subscribe(parent.people, "presence", presenceChange)
    }
               
    subscribe(parent.alarm, "status", statusHandler)    
}   

def statusHandler(evt) {
	def status = evt.value
    
	sendEvent(name:"Alarm", value: "${status}", descriptionText:"Status")

    alarmStatus(status)
}

def alarmStatus(status) {
	if (status == "") {
    	status = parent.alarm.currentValue("status")
  	}

    if (enableDebug) log.info("status = ${status}")
    
	if (recentArrival()) {
		if (status.contains("OFF")) {
			sendColor(huesStatus, colorOff, intensityStatus)
		} 
		else if (status.contains("HOME")) {
			sendColor(huesStatus, colorOn, intensityStatus)
		} 
		else if (status.contains("AWAY")) {
			sendColor(huesStatus, colorOn, intensityStatus)
		} 
		else if (status.contains("ALARM")) {
			sendColor(huesStatus, colorAlarm, intensityStatus)
		} 
		else if (status.contains("ALERT")) {
			sendColor(huesStatus, colorAlert, intensityStatus)
		} 

		runIn((durationStatus * 60),"turnStatusOff")
		runIn((durationStatus * 60 + 15),"turnStatusOff2")	
	}
	else {
		if (enableDebug) log.info("no external lights, no recent arrival")
	}
}

private turnStatusOff() {
	if (huesStatus != null) {
		huesStatus.off()
  	}
}    

private turnStatusOff2() {
	turnStatusOff()
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

def recentArrival() {
    def elapsed = 0
   
	def arrival = parent.getArrival()
	
	elapsed = now() - arrival
		
	elapsed = elapsed/1000/60 //minutes
	
	if (elapsed < 5) {
		return true
	}
	else {
		return false	
	}
}

def presenceChange(evt) {
    def person = evt.getDisplayName()
	
	sendEvent(name:"Presence", value: "${evt.value}", descriptionText:"${person}")

    if (evt.value == "present") {
		parent.setArrival()
		
        parent.aggregateMsg("${person} arrived at home.")
		
		atomicState.arrived = now()
            
        if ((turnOnStatus == "Yes") && (huesStatus != null)) {
            alarmStatus("")
        }
        
        app.updateLabel("People <span style=\"color:green\"> ${person}</span>")
    }
    else {
        parent.aggregateMsg("${person} left home.")
        
        app.updateLabel("People <span style=\"color:red\"> ${person}</span>")        
    }
}