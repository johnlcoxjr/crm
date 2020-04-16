/*  Smart Lighting Director
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
    name: "Smart Lighting Director",
    version: "2.0.0",
    namespace: "johnlcox",
    author: "John Cox",
    category: "Safety & Security",
    description: "Randomly turn on/off lights to simulate the appearance of a occupied home while you are away.",
    iconUrl: "",
    iconX2Url: "",
    importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Lighting-Director.groovy"
)

preferences {
    page name:"pageSetup"
    page name:"Setup"
    page name:"Settings"
    page name: "timeIntervalInput"

}

def pageSetup() {

    def pageProperties = [
        name:       "pageSetup",
        title:      "Status",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

	return dynamicPage(pageProperties) {
    	section(""){
        	paragraph "This app can be used to make your home seem occupied anytime you are away from your home.  Please use each of the the sections below to setup the different preferences to your liking. " 
        }
		section("-= <b>Debug Menu</b> =-") {
			input "enableDebug", "bool", title: "Enable debug output?", required: false, defaultValue: false
		}
        section("Setup Menu") {
            href "Setup", title: "Setup", description: "", state:greyedOut()
            href "Settings", title: "Settings", description: "", state: greyedOutSettings()
            }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}

def Setup() {
    def pageName = "Setup"   
    def pageProperties = [name: "Setup", title: "Setup", nextPage: "pageSetup"]

    return dynamicPage(pageProperties) {
		section(""){            
			paragraph "In this section you need to setup the deatils of how you want your lighting to be affected while you are away.  All of these settings are required in order for the simulator to run correctly."
        }
        section("Simulator Triggers") {
			input name: "newMode", type: "mode", title: "Modes", multiple: true, required: true
			href "timeIntervalInput", title: "Times", description: timeIntervalLabel(), refreshAfterSelection:true
        }
        section("Light switches to turn on/off") {
			input name: "switches", type: "capability.switch", title: "Switches", multiple: true, required: true          
        }
        section("How often to cycle the lights") {
			input name: "frequency_minutes", type: "number", title: "Minutes?", required: true          
        }
        section("Number of active lights at any given time") {
			input name: "number_of_active_lights", type: "number", title: "Number of active lights", required:	true           
        }    
    }    
}

def Settings() {
    def pageName = "Settings"   
    def pageProperties = [name: "Settings", title: "Settings", nextPage: "pageSetup"]
    
    return dynamicPage(pageProperties) {
		section(""){              
			paragraph "In this section you can restrict how your simulator runs.  For instance you can restrict on which days it will run as well as a delay for the simulator to start after it is in the correct mode.  Delaying the simulator helps with false starts based on a incorrect mode change."
        }
        section("Delay to start simulator") {
			input name: "falseAlarmThreshold", type: "decimal", title: "Default is 2 minutes", required: false
        }
        section("People") {
			paragraph "Not using this setting may cause some lights to remain on when you arrive home"
			input name: "people", type: "capability.presenceSensor", title: "If these people are home do not change light status", required: false, multiple: true            
        }
        section("More options") {
			input name: "days", type: "enum", title: "Only on certain days of the week", multiple: true, required: false, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        } 
    }   
}

def timeIntervalInput() {
	dynamicPage(name: "timeIntervalInput") {
		section {
			input "startTimeType", "enum", title: "Starting at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], defaultValue: "time", submitOnChange: true
			if (startTimeType in ["sunrise","sunset"]) {
				input "startTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
			}
			else {
				input "starting", "time", title: "Start time", required: false
			}
		}
		section {
			input "endTimeType", "enum", title: "Ending at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], defaultValue: "time", submitOnChange: true
			if (endTimeType in ["sunrise","sunset"]) {
				input "endTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
			}
			else {
				input "ending", "time", title: "End time", required: false
			}
		}
	}
}


def installed() {
initialize()
}

def updated() {
  unsubscribe();
  unschedule();
  initialize()
}

def initialize(){
	if (newMode != null) {
		subscribe(location, modeChangeHandler)
    }
    if (starting != null) {
    	schedule(starting, modeChangeHandler)
    }
}

def modeChangeHandler(evt) {
	def delay = (falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold * 60 : 2 * 60  
	runIn(delay.toInteger(), scheduleCheck)
}


//Main logic to pick a random set of lights from the large set of lights to turn on and then turn the rest off
def scheduleCheck(evt) {
    if(allOk){
        if (enableDebug) log.debug("Running")
        // turn off all the switches
        switches.off()
		sendEvent(name:"Light", value: "OFF", descriptionText:"${switches}")
        
        // grab a random switch
        def random = new Random()
        def inactive_switches = switches
        for (int i = 0 ; i < number_of_active_lights ; i++) {
            // if there are no inactive switches to turn on then let's break
            if (inactive_switches.size() == 0){
                break
            }
            
            // grab a random switch and turn it on
            def random_int = random.nextInt(inactive_switches.size())
            inactive_switches[random_int].on()
			sendEvent(name:"Light", value: "ON", descriptionText:"${inactive_switches[random_int]}")
            
            // then remove that switch from the pool off switches that can be turned on
            inactive_switches.remove(random_int)
        }
        
        // re-run again when the frequency demands it
        schedule("0 0/${frequency_minutes} * 1/1 * ? *", scheduleCheck)
    }
    //Check to see if mode is ok but not time/day.  If mode is still ok, check again after frequency period.
    else if (modeOk) {
        if (enableDebug) log.debug("mode OK.  Running again")
        switches.off()
    }
    //if none is ok turn off frequency check and turn off lights.
    else {
    	if(people){
        	//don't turn off lights if anyone is home
        	if(someoneIsHome){
        	    if (enableDebug) log.debug("Stopping Check for Light")
        	    unschedule()
        	}
        	else{
        	    if (enableDebug) log.debug("Stopping Check for Light and turning off all lights")
        	    switches.off()
        	    unschedule()
        	}
    	}
        else if (!modeOk) {
        	unschedule()
        }
    }
}      


//below is used to check restrictions
private getAllOk() {
	modeOk && daysOk && timeOk && homeIsEmpty
}


private getModeOk() {
	def result = !newMode || newMode.contains(location.mode)
	if (enableDebug) log.trace "modeOk = $result"
	result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	if (enableDebug) log.trace "daysOk = $result"
	result
}

private getHomeIsEmpty() {
  def result = true

  if(people?.findAll { it?.currentPresence == "present" }) {
    result = false
  }

  if (enableDebug) log.debug("homeIsEmpty: ${result}")

  return result
}

private getSomeoneIsHome() {
  def result = false

  if(people?.findAll { it?.currentPresence == "present" }) {
    result = true
  }

  if (enableDebug) log.debug("anyoneIsHome: ${result}")

  return result
}

private getTimeOk() {
	def result = true
	def start = timeWindowStart()
	def stop = timeWindowStop()
	if (start && stop && location.timeZone) {
		result = timeOfDayIsBetween(start, stop, new Date(), location.timeZone)
	}
	if (enableDebug) log.trace "timeOk = $result"
	result
}

private timeWindowStart() {
	def result = null
	if (startTimeType == "sunrise") {
		result = location.currentState("sunriseTime")?.dateValue
		if (result && startTimeOffset) {
			result = new Date(result.time + Math.round(startTimeOffset * 60000))
		}
	}
	else if (startTimeType == "sunset") {
		result = location.currentState("sunsetTime")?.dateValue
		if (result && startTimeOffset) {
			result = new Date(result.time + Math.round(startTimeOffset * 60000))
		}
	}
	else if (starting && location.timeZone) {
		result = timeToday(starting, location.timeZone)
	}
	if (enableDebug) log.trace "timeWindowStart = ${result}"
	result
}

private timeWindowStop() {
	def result = null
	if (endTimeType == "sunrise") {
		result = location.currentState("sunriseTime")?.dateValue
		if (result && endTimeOffset) {
			result = new Date(result.time + Math.round(endTimeOffset * 60000))
		}
	}
	else if (endTimeType == "sunset") {
		result = location.currentState("sunsetTime")?.dateValue
		if (result && endTimeOffset) {
			result = new Date(result.time + Math.round(endTimeOffset * 60000))
		}
	}
	else if (ending && location.timeZone) {
		result = timeToday(ending, location.timeZone)
	}
	if (enableDebug) log.trace "timeWindowStop = ${result}"
	result
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private timeIntervalLabel() {
	def start = ""
	switch (startTimeType) {
		case "time":
			if (ending) {
            	start += hhmm(starting)
            }
			break
		case "sunrise":
		case "sunset":
        	start += startTimeType[0].toUpperCase() + startTimeType[1..-1]
			if (startTimeOffset) {
				start += startTimeOffset > 0 ? "+${startTimeOffset} min" : "${startTimeOffset} min"
			}
			break
	}

    def finish = ""
	switch (endTimeType) {
		case "time":
			if (ending) {
            	finish += hhmm(ending)
            }
			break
		case "sunrise":
		case "sunset":
        	finish += endTimeType[0].toUpperCase() + endTimeType[1..-1]
			if (endTimeOffset) {
				finish += endTimeOffset > 0 ? "+${endTimeOffset} min" : "${endTimeOffset} min"
			}
			break
	}
	start && finish ? "${start} to ${finish}" : ""
}

//sets complete/not complete for the setup section on the main dynamic page
def greyedOut(){
	def result = ""
    if (switches) {
    	result = "complete"	
    }
    result
}

//sets complete/not complete for the settings section on the main dynamic page
def greyedOutSettings(){
	def result = ""
    if (people || days || falseAlarmThreshold ) {
    	result = "complete"	
    }
    result
}