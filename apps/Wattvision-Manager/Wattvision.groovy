/*  Wattvision
*
* 2.0.0		10/5/19		updated to Hubitat  
*
* Copyright 2017-20 John Cox
*
* Developer retains all right, title, copyright, and interest, including all copyright, patent rights, trade secret in the Background technology.
* May be subject to consulting fees under the Agreement between the Developer and the Customer. Developer grants a non-exclusive perpetual license
* to use the Background technology in the Software developed for and delivered to Customer under this Agreement. However, the Customer shall make
* no commercial use of the Background technology without Deimport hubitat.helper.InterfaceUtils

metadata {
	preferences {
    	section("Preferences") {
        	input "showLogs", "bool", required: false, title: "Show Debug Logs?", defaultValue: false
    	}
	}	
	
	definition(name: "Wattvision", version: "2.0.0", namespace: "johnlcox", author: "John Cox",importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/Wattvision.groovy") {
		capability "Power Meter"
		capability "Refresh"
		capability "Sensor"
		attribute "powerContent", "string"
	}
}

def refresh() {
	parent.getDataFromWattvision()
}

public addWattvisionData(json) {
	if (showLogs) log.trace("Adding data from Wattvision")

	def data = parseJson(json.data.toString())
	def units = json.units ?: "watts"

	if (data.size() > 0) {
		def latestData = data[-1]
		data.each {
			if (state.power != it.v.toInteger()) {
				sendPowerEvent(it.t, it.v.toInteger(), units, (latestData == it))
				state.power = it.v.toInteger()
			}
			else {
				if (showLogs) log.info("duplicate value")
			}
		}
	}
}

private sendPowerEvent(time, value, units, isLatest = false) {
	def wattvisionDateFormat = "yyyy-MM-dd'T'HH:mm:ss"

	def eventData = [
		date           : new Date().parse(wattvisionDateFormat, time),
		value          : value,
		name           : "power",
		displayed      : isLatest,
		isStateChange  : isLatest,
		description    : "${value.toInteger()} ${units}",
		descriptionText: "${value.toInteger()} ${units}"
	]

	if (showLogs) log.debug("sending event: ${eventData}")
	if (showLogs) log.info("power = ${value} ${units}")
	sendEvent(eventData)
}

def parseJson(String s) {
	new groovy.json.JsonSlurper().parseText(s)
}veloper's written consent.
* 
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
* WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied. 
*
* Software Distribution is restricted and shall be done only with Developer's written approval.
*/

import hubitat.helper.InterfaceUtils

metadata {
    preferences {
    	section("Preferences") {
            input "showLogs", "bool", required: false, title: "Enable debug logging", defaultValue: false
	    input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: false
    	}
    }	
	
    definition(
	name: "Wattvision", 
	version: "2.0.0", 
	namespace: "smartthings", 
	author: "Steve Vlaminck",
	importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/Wattvision.groovy"
    ) {
	capability "Power Meter"
	capability "Refresh"
	capability "Sensor"

	attribute "powerContent", "string"
    }
}

def refresh() {
	parent.getDataFromWattvision()
}

public addWattvisionData(json) {
	if (showLogs) log.trace("Adding data from Wattvision")

	def data = parseJson(json.data.toString())
	def units = json.units ?: "watts"

	if (data.size() > 0) {
		def latestData = data[-1]
		data.each {
			if (state.power != it.v.toInteger()) {
				sendPowerEvent(it.t, it.v.toInteger(), units, (latestData == it))
				state.power = it.v.toInteger()
			}
			else {
				if (showLogs) log.info("duplicate value")
			}
		}
	}
}

private sendPowerEvent(time, value, units, isLatest = false) {
	def wattvisionDateFormat = "yyyy-MM-dd'T'HH:mm:ss"

	def eventData = [
		date           : new Date().parse(wattvisionDateFormat, time),
		value          : value,
		name           : "power",
		displayed      : isLatest,
		isStateChange  : isLatest,
		description    : "${value.toInteger()} ${units}",
		descriptionText: "${value.toInteger()} ${units}"
	]

	if (showLogs) log.debug("sending event: ${eventData}")
	if (showLogs) log.info("power = ${value} ${units}")
	sendEvent(eventData)
}

def parseJson(String s) {
	new groovy.json.JsonSlurper().parseText(s)
}