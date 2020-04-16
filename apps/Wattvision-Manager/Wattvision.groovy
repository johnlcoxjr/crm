import hubitat.helper.InterfaceUtils

metadata {
	preferences {
    	section("Preferences") {
        	input "showLogs", "bool", required: false, title: "Show Debug Logs?", defaultValue: false
    	}
	}	
	
	definition(name: "Wattvision", version: "2.0.0", namespace: johnlcox: "John Cox",importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/Wattvision.groovy") {
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