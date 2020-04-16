/*  Christmas Lights
 *  Version 
 *  1.0.0	12/1/19
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
    name: "Christmas Lights",
    namespace: "johnlcox",
    author: "john.l.cox@live.com",
    description: "Christmas Lights",
    category: "Safety & Security",
    singleInstance: true,
    iconUrl: "",
    iconX2Url: "",
importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Christmas-Lights.groovy"
)

private def about() {
    return "Smart Alarm Control (Parent) controls all aspects of your alarm (turning on, turning off, presence, mode change, and notifications).  All aspects of this app are customizable. \n ${updatedDate()} ${version()} ${copyright()}"
}

private def paypal() {
    return "You can contribute to the development of this app by making a PayPal donation to https://www.paypal.me/johnlcox. I appreciate your support."
}

private def updatedDate() {
	return "Updated 12/1/2019"
}

private def version() {
    return "Version 1.0.0"
}

private def copyright() {
    return "Copyright © 2019 John Cox"
}

preferences {
	section() {
		paragraph "Christmas Lights ${updatedDate()} ${version()} ${copyright()}" 
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
	
	section("Control") {
		input "controlSwitch","capability.switch", title: "Control switch?", required: true, multiple:false
	}

	section("Color") {
		section("Select the hues.") {
			input "hueGroup1", "capability.colorControl", title: "Hue bulbs?", required:true, multiple:true
            input "hueGroup2", "capability.colorControl", title: "Hue bulbs?", required:true, multiple:true
		}

		section("Select the color and intensity for group 1.") {
			input "colorGroup1","enum", title: "Group 1 color?", required: true, options: ["White", "Daylight", "Soft White", "Warm White", "Brick Red", "Safety Orange", "Dark Orange", "Amber", "Gold", "Yellow", "Electric Lime", "Lawn Green", "Bright Green", "Lime", "Spring Green", "Turquoise", "Aqua", "Sky Blue", "Dodger Blue", "Navy Blue", "Blue", "Han Purple", "Electric Indigo", "Electric Purple", "Orchid Purple", "Magenta", "Hot Pink", "Deep Pink", "Raspberry", "Crimson", "Red"], defaultValue: "Red"
			input "intensityGroup1","enum", title: "Group 1 intensity?", required: true, options: ["1", "3", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60", "65", "70", "75", "80", "85", "90", "95", "100"], defaultValue: "100"
		}

		section("Select the color and intensity for group 2.") {
			input "colorGroup2","enum", title: "Group 2 color?", required: true, options: ["White", "Daylight", "Soft White", "Warm White", "Brick Red", "Safety Orange", "Dark Orange", "Amber", "Gold", "Yellow", "Electric Lime", "Lawn Green", "Bright Green", "Lime", "Spring Green", "Turquoise", "Aqua", "Sky Blue", "Dodger Blue", "Navy Blue", "Blue", "Han Purple", "Electric Indigo", "Electric Purple", "Orchid Purple", "Magenta", "Hot Pink", "Deep Pink", "Raspberry", "Crimson", "Red"], defaultValue: "Bright Green"
			input "intensityGroup2","enum", title: "Group 2 intensity?", required: true, options: ["1", "3", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60", "65", "70", "75", "80", "85", "90", "95", "100"], defaultValue: "100"
		}
		
		section("Control") {
			input "controlTime","number", title: "Control time (m)?", required: true, range: "1..10", defaultValue: 2
		}
		
		section("Select the hues.") {
			input "hue1", "capability.colorControl", title: "Hue bulbs 1?", required:true, multiple:true
            input "hue2", "capability.colorControl", title: "Hue bulbs 2?", required:true, multiple:true
            input "hue3", "capability.colorControl", title: "Hue bulbs 3?", required:true, multiple:true
            input "hue4", "capability.colorControl", title: "Hue bulbs 4?", required:true, multiple:true
            input "hue5", "capability.colorControl", title: "Hue bulbs 5?", required:true, multiple:true
            input "hue6", "capability.colorControl", title: "Hue bulbs 6?", required:true, multiple:true
            input "hue7", "capability.colorControl", title: "Hue bulbs 7?", required:true, multiple:true
            input "hue8", "capability.colorControl", title: "Hue bulbs 8?", required:true, multiple:true
            input "hue9", "capability.colorControl", title: "Hue bulbs 9?", required:true, multiple:true
            input "hue10", "capability.colorControl", title: "Hue bulbs 10?", required:true, multiple:true
            input "hue11", "capability.colorControl", title: "Hue bulbs 11?", required:true, multiple:true
            input "hue12", "capability.colorControl", title: "Hue bulbs 12?", required:true, multiple:true
            input "hue13", "capability.colorControl", title: "Hue bulbs 13?", required:true, multiple:true
		}
		
		section("Select the color and intensity for chaser 1.") {
			input "colorChaser1","enum", title: "Chaser 1 color?", required: true, options: ["White", "Daylight", "Soft White", "Warm White", "Brick Red", "Safety Orange", "Dark Orange", "Amber", "Gold", "Yellow", "Electric Lime", "Lawn Green", "Bright Green", "Lime", "Spring Green", "Turquoise", "Aqua", "Sky Blue", "Dodger Blue", "Navy Blue", "Blue", "Han Purple", "Electric Indigo", "Electric Purple", "Orchid Purple", "Magenta", "Hot Pink", "Deep Pink", "Raspberry", "Crimson", "Red"], defaultValue: "Blue"
			input "intensityChaser1","enum", title: "Chaser 1 intensity?", required: true, options: ["1", "3", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60", "65", "70", "75", "80", "85", "90", "95", "100"], defaultValue: "100"
		}	
		
		section("Select the color and intensity for chaser 2.") {
			input "colorChaser2","enum", title: "Chaser 2 color?", required: true, options: ["White", "Daylight", "Soft White", "Warm White", "Brick Red", "Safety Orange", "Dark Orange", "Amber", "Gold", "Yellow", "Electric Lime", "Lawn Green", "Bright Green", "Lime", "Spring Green", "Turquoise", "Aqua", "Sky Blue", "Dodger Blue", "Navy Blue", "Blue", "Han Purple", "Electric Indigo", "Electric Purple", "Orchid Purple", "Magenta", "Hot Pink", "Deep Pink", "Raspberry", "Crimson", "Red"], defaultValue: "Daylight"
			input "intensityChaser2","enum", title: "Chaser 2 intensity?", required: true, options: ["1", "3", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60", "65", "70", "75", "80", "85", "90", "95", "100"], defaultValue: "100"
		}		
		
		section("Chaser") {
			input "chaserTime","number", title: "Chaser time (s)?", required: true, range: "1..10", defaultValue: 2
		}
	}        
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
	unschedule()
    initialize()
}

def initialize() {
	subscribe(controlSwitch, "switch", activate)   
}

def activate(evt) {
	if (evt.value == "on") {
		if (enableDebug) log.info("turning Christmas lights on")
		
		state.sequence = 0
		
		process()
		
		schedule("0 */${controlTime} * * * ?", process) //changed 0 to 8 to hopefully avoid time out  
		
		//runEvery5Minutes(process)
	}
	else {
		if (enableDebug) log.info("turning Christmas lights off")
		
		state.sequence = 0
		
		hueGroup1.off()
		hueGroup2.off()
		
		unschedule()
	}
}

def process() {
	if (enableDebug) log.info("processing sequence ${state.sequence}")
							  
	switch(state.sequence) {
		case 0:	
			state.sequence = 1
			chaser() 
			break;
		case 1:
			state.sequence = 2
			sendColor(hueGroup1, colorGroup1, intensityGroup1)  
			sendColor(hueGroup2, colorGroup2, intensityGroup2)  
			break;
		case 2:
			state.sequence = 3
			chaser() 
			break;
		case 3:
			state.sequence = 4
			sendColor(hueGroup2, colorGroup1, intensityGroup1)  
			sendColor(hueGroup1, colorGroup2, intensityGroup2)  
			break;		
		case 4:
			state.sequence = 1
			chaser() 
			break;
	}		
}

def chaser() {
	if (enableDebug) log.info("chaser")
	chaserGroup1()
}

def chaserGroup1() {
	if (enableDebug) log.info("chaser 1")
	sendColor(hue1,colorChaser1,intensityChaser1)
	runIn(chaserTime, chaserGroup2)
}

def chaserGroup2() {
	if (enableDebug) log.info("chaser 2")
	sendColor(hue2,colorChaser1,intensityChaser1)
	sendColor(hue1,colorChaser2,intensityChaser2)
	runIn(chaserTime, chaserGroup3)
}
def chaserGroup3() {
	if (enableDebug) log.info("chaser 3")
	sendColor(hue3,colorChaser1,intensityChaser1)
	sendColor(hue2,colorChaser2,intensityChaser2)
	runIn(chaserTime, chaserGroup4)
}
def chaserGroup4() {
	if (enableDebug) log.info("chaser 4")
	sendColor(hue4,colorChaser1,intensityChaser1)
	sendColor(hue3,colorChaser2,intensityChaser2)
	runIn(chaserTime, chaserGroup5)
}
def chaserGroup5() {
	if (enableDebug) log.info("chaser 5")
	sendColor(hue5,colorChaser1,intensityChaser1)
	sendColor(hue4,colorChaser2,intensityChaser2)
	runIn(chaserTime, chaserGroup6)
}
def chaserGroup6() {
	if (enableDebug) log.info("chaser 6")
	sendColor(hue6,colorChaser1,intensityChaser1)
	sendColor(hue5,colorChaser2,intensityChaser2)
	runIn(chaserTime, chaserGroup7)
}
def chaserGroup7() {
	if (enableDebug) log.info("chaser 7")
	sendColor(hue7,colorChaser1,intensityChaser1)
	sendColor(hue6,colorChaser2,intensityChaser2)
	runIn(chaserTime, chaserGroup8)
}
def chaserGroup8() {
	if (enableDebug) log.info("chaser 8")
	sendColor(hue8,colorChaser1,intensityChaser1)
	sendColor(hue7,colorChaser2,intensityChaser2)
	runIn(chaserTime, chaserGroup9)
}
def chaserGroup9() {
	if (enableDebug) log.info("chaser 9")
	sendColor(hue9,colorChaser1,intensityChaser1)
	sendColor(hue8,colorChaser2,intensityChaser2)
	runIn(chaserTime, chaserGroup10)
}
def chaserGroup10() {
	if (enableDebug) log.info("chaser 10")
	sendColor(hue10,colorChaser1,intensityChaser1)
	sendColor(hue9,colorChaser2,intensityChaser2)
	runIn(chaserTime, chaserGroup11)
}
def chaserGroup11() {
	if (enableDebug) log.info("chaser 11")
	sendColor(hue11,colorChaser1,intensityChaser1)
	sendColor(hue10,colorChaser2,intensityChaser2)
	runIn(chaserTime, chaserGroup12)
}
def chaserGroup12() {
	if (enableDebug) log.info("chaser 12")
	sendColor(hue12,colorChaser1,intensityChaser1)
	sendColor(hue11,colorChaser2,intensityChaser2)
	runIn(chaserTime, chaserGroup13)
}
def chaserGroup13() {
	if (enableDebug) log.info("chaser 13")
	sendColor(hue13,colorChaser1,intensityChaser1)
	sendColor(hue12,colorChaser2,intensityChaser2)
	runIn(chaserTime, process)
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
                hueColor = 100
                saturation = 0
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

        if (enableDebug) log.info("----------->new value = ${newValue}")

        bulbs*.setColor(newValue)
  	}
}