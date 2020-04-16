/*
*    1.0.0    11/10/19    added debug logs
*/ 

import hubitat.helper.InterfaceUtils
  
metadata {
	definition (name: "GE 12730 Fan Controller", version: "1.0.0", namespace: "johnlcox", author: "John Cox",importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/GE%2012730%20Fan%20Controller.groovy") {
		capability "Switch Level"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		command "lowSpeed"
		command "medSpeed"
		command "highSpeed"
        command "off"

		attribute "currentState", "string"

		fingerprint deviceId: "0x1101", inClusters: "0x26, 0x27, 0x70, 0x86, 0x72"
	}

	preferences {
		section("Fan Thresholds") {
			input "lowThreshold", "number", title: "Low Threshold", range: "1..99"
			input "medThreshold", "number", title: "Medium Threshold", range: "1..99"
			input "highThreshold", "number", title: "High Threshold", range: "1..99"
		}
        section("Preferences") {
            input "showLogs", "bool", required: false, title: "Show Debug Logs?", defaultValue: false
        }
	}
}

def parse(String description) {
	def item1 = [
		canBeCurrentState: false,
		linkText: getLinkText(device),
		isStateChange: false,
		displayed: false,
		descriptionText: description,
		value:  description
	]
	def result
	def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
	if (cmd) {
		result = createEvent(cmd, item1)
	}
	else {
		item1.displayed = displayed(description, item1.isStateChange)
		result = [item1]
	}
	if (showLogs) log.debug("Parse returned ${result?.descriptionText}")
    
	result
}

def createEvent(hubitat.zwave.commands.basicv1.BasicReport cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
  	result[i].type = "physical"
	}
	if (showLogs) log.trace("BasicReport")
  result
}

def createEvent(hubitat.zwave.commands.basicv1.BasicSet cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	if (showLogs) log.trace("BasicSet")
	result
}

def createEvent(hubitat.zwave.commands.switchmultilevelv1.SwitchMultilevelStartLevelChange cmd, Map item1) {
	[]
	if (showLogs) log.trace("StartLevel")
}

def createEvent(hubitat.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd, Map item1) {
	[response(zwave.basicV1.basicGet())]
}

def createEvent(hubitat.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	if (showLogs) log.trace("SwitchMultiLevelSet")
	result
}

def createEvent(hubitat.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	result[0].descriptionText = "${item1.linkText} is ${item1.value}"
	result[0].handlerName = cmd.value ? "statusOn" : "statusOff"
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "digital"
	}
	if (showLogs) log.trace("SwitchMultilevelReport")
	result
}

def doCreateEvent(hubitat.zwave.Command cmd, Map item1) {
	def result = [item1]
	def lowThresholdvalue = (settings.lowThreshold != null && settings.lowThreshold != "") ? settings.lowThreshold.toInteger() : 33
	def medThresholdvalue = (settings.medThreshold != null && settings.medThreshold != "") ? settings.medThreshold.toInteger() : 67
	def highThresholdvalue = (settings.highThreshold != null && settings.highThreshold != "") ? settings.highThreshold.toInteger() : 99

	item1.name = "switch"
	item1.value = cmd.value ? "on" : "off"
	if (item1.value == "off") {
		sendEvent(name: "currentState", value: "OFF" as String)
	}
	item1.handlerName = item1.value
	item1.descriptionText = "${item1.linkText} was turned ${item1.value}"
	item1.canBeCurrentState = true
	item1.isStateChange = isStateChange(device, item1.name, item1.value)
	item1.displayed = false

	if (cmd.value) {
		def item2 = new LinkedHashMap(item1)
		item2.name = "level"
		item2.value = cmd.value as String
		item2.unit = "%"
		item2.descriptionText = "${item1.linkText} dimmed ${item2.value} %"
		item2.canBeCurrentState = true
		item2.isStateChange = isStateChange(device, item2.name, item2.value)
		item2.displayed = false

		if (item2.value.toInteger() <= lowThresholdvalue) { sendEvent(name: "currentState", value: "LOW" as String) }
		if (item2.value.toInteger() >= lowThresholdvalue+1 && item2.value.toInteger() <= medThresholdvalue) { sendEvent(name: "currentState", value: "MED" as String) }
		if (item2.value.toInteger() >= medThresholdvalue+1) { sendEvent(name: "currentState", value: "HIGH" as String) }

		result << item2
	}
	if (showLogs) log.trace("doCreateEvent")
	result
}

def zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd) {
	def value = "when off"
	if (showLogs) log.trace("ConfigurationReport")
	if (cmd.configurationValue[0] == 1) {value = "when on"}
	if (cmd.configurationValue[0] == 2) {value = "never"}
	[name: "indicatorStatus", value: value, display: false]
}

def createEvent(hubitat.zwave.Command cmd,  Map map) {
	// Handles any Z-Wave commands we aren't interested in
	if (showLogs) log.debug("UNHANDLED COMMAND $cmd")
}

def on() {
	if (showLogs) log.info("on")
	delayBetween([zwave.basicV1.basicSet(value: 0xFF).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 1000)
}

def off() {
	if (showLogs) log.info("off")
	delayBetween ([zwave.basicV1.basicSet(value: 0x00).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 1000)
}

def setLevel(value) {
	def lowThresholdvalue = (settings.lowThreshold != null && settings.lowThreshold != "") ? settings.lowThreshold.toInteger() : 33
	def medThresholdvalue = (settings.medThreshold != null && settings.medThreshold != "") ? settings.medThreshold.toInteger() : 67
	def highThresholdvalue = (settings.highThreshold != null && settings.highThreshold != "") ? settings.highThreshold.toInteger() : 99
	
	if (value == "LOW") { value = lowThresholdvalue }
	if (value == "MED") { value = medThresholdvalue }
	if (value == "HIGH") { value = highThresholdvalue }

	def level = Math.min(value as Integer, 99)
	
	if (showLogs) log.trace("setLevel(value): ${level}")
    
	if (level <= lowThresholdvalue) { sendEvent(name: "currentState", value: "ADJUSTING.LOW" as String, displayed: false) }
	if (level >= lowThresholdvalue+1 && level <= medThresholdvalue) { sendEvent(name: "currentState", value: "ADJUSTING.MED" as String, displayed: false) }
	if (level >= medThresholdvalue+1) { sendEvent(name: "currentState", value: "ADJUSTING.HIGH" as String, displayed: false) }
	
	delayBetween ([zwave.basicV1.basicSet(value: level as Integer).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 1000)
}

def setLevel(value, duration) {
	def lowThresholdvalue = (settings.lowThreshold != null && settings.lowThreshold != "") ? settings.lowThreshold.toInteger() : 33
	def medThresholdvalue = (settings.medThreshold != null && settings.medThreshold != "") ? settings.medThreshold.toInteger() : 67
	def highThresholdvalue = (settings.highThreshold != null && settings.highThreshold != "") ? settings.highThreshold.toInteger() : 99

	if (value == "LOW") { value = lowThresholdvalue }
	if (value == "MED") { value = medThresholdvalue }
	if (value == "HIGH") { value = highThresholdvalue }

	def level = Math.min(value as Integer, 99)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	
	if (showLogs) log.trace("setLevel(value): ${level}")
	
	if (level <= lowThresholdvalue) { sendEvent(name: "currentState", value: "ADJUSTING.LOW" as String, displayed: false) }
	if (level >= lowThresholdvalue+1 && level <= medThresholdvalue) { sendEvent(name: "currentState", value: "ADJUSTING.MED" as String, displayed: false) }
	if (level >= medThresholdvalue+1) { sendEvent(name: "currentState", value: "ADJUSTING.HIGH" as String, displayed: false) }
    
	zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format()
}

def lowSpeed() {
	setLevel("LOW")
}

def medSpeed() {
	setLevel("MED")
}

def highSpeed() {
	setLevel("HIGH")
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def indicatorWhenOn() {
	sendEvent(name: "indicatorStatus", value: "when on", display: false)
	zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
}

def indicatorWhenOff() {
	sendEvent(name: "indicatorStatus", value: "when off", display: false)
	zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format()
}

def indicatorNever() {
	sendEvent(name: "indicatorStatus", value: "never", display: false)
	zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 3, size: 1).format()
}