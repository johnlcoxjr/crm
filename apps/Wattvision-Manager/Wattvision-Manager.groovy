import hubitat.helper.InterfaceUtils

definition(
	name: "Wattvision Manager",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Monitor your whole-house energy use by connecting to your Wattvision account",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/wattvision.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/wattvision%402x.png",
importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Wattvision-Manager.groovy"
)

preferences {
	section {
		href(url: "https://www.wattvision.com", title: "Learn More About Wattvision", style: "external", description: null)
	}

	section("-= <b>Debug Menu</b> =-") {
		input "enableDebug", "bool", title: "Enable debug output?", required: false, defaultValue: false
	}
	
	section("Wattvision") {
		input "apiSensorID", "text", title: "API Sensor?", multiple: false, required: true, defaultValue: "95169802"
		input "apiID", "text", title: "API ID?", multiple: false, required: true, defaultValue: "mxpcrajwstympmoyhrz7xm183eq7436v"
		input "apiKey", "text", title: "API Key?", multiple: false, required: true, defaultValue: "m58eyh37i96ibwul4kwxxrgqj27aqtyz"
		input "apiName", "text", title: "Device name?", multiple: false, required: true, defaultValue: "ComEd"
		input "apiRefresh","enum", title: "Update frequency?", options: ['5','10','15','20','25','30','35','40','45','55'], required: true, defaultValue: '15'
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
	createDevice()
	getDataFromWattvision()
	scheduleDataCollection()
}

String.metaClass.encodeURL = {
   java.net.URLEncoder.encode(delegate, "UTF-8")
}

def getDataFromWattvision() {
	def children = getChildDevices()
	if (!children) {
		if (enableDebug) log.warn "No children. Not collecting data from Wattvision"
		return
	}

	def endDate = new Date()
	def startDate

	if (!state.lastUpdated) {
		startDate = new Date(hours: endDate.hours - 3)
	} 
	else {
		startDate = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", state.lastUpdated)
	}

	state.lastUpdated = endDate.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

	children.each { child ->
		getDataForChild(child, startDate, endDate)
	}
}

def getDataForChild(child, startDate, endDate) {
	if (!child) {
		return
	}

	def wattvisionURL = wattvisionURL()

	if (wattvisionURL) {
		try {
			httpGet(uri: wattvisionURL) { response ->
				def json = new org.json.JSONObject(response.data.toString())
				child.addWattvisionData(json)
				return "success"
			}
		} 
		catch (groovyx.net.http.HttpResponseException httpE) {
			if (enableDebug) log.debug "wattvisionURL = ${wattvisionURL}"
			return "fail"
		} 
		catch (e) {
			if (enableDebug) log.debug "wattvisionURL = ${wattvisionURL}"
			return "fail"
		}
	}
}

def wattvisionURL() {
	date = new Date()

	def params = [
		"sensor_id" : apiSensorID,
		"api_id"    : apiID,
		"api_key"   : apiKey,
		"type"      : "latest_rate",
		"start_time": date.format("yyyy-MM-dd"),
		"end_time"  : date.format("yyyy-MM-dd"),
	]
		
	def parameterString = params.collect { key, value -> "${key}=${value}" }.join("&")
	def url = "https://www.wattvision.com/api/v0.2/elec?${parameterString}"

	if (enableDebug) log.debug "wattvisionURL: ${url}"
	return url
}

def getData() {
	state.lastUpdated = new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
}

def childMarshaller(child) {
	return [
		name     : child.name,
		label    : child.label,
		sensor_id: child.deviceNetworkId,
		location : child.location.name
	]
}

def createDevice() {
	if (enableDebug) log.trace "Creating Wattvision device"
	if (!getChildDevice(apiSensorID)) {
		def child = addChildDevice("smartthings", "Wattvision", apiSensorID, null, [name: "Wattvision", label: apiName])

		getDataForChild(child, null, null)

		return childMarshaller(child)
	}
	else {
		if (enableDebug) log.warn "Device already exists"
	}
}

def deleteDevice() {
	if (enableDebug) log.trace "Deleting Wattvision device"

	deleteChildDevice(params.sensorId)
}

def scheduleDataCollection() {
	schedule("/${apiRefresh} * * * * ?", "getDataFromWattvision") // every 1 minute
}

def createChild(sensorId, sensorName) {
	if (enableDebug) log.trace "creating Wattvision Child"

	def child = getChildDevice(sensorId)

	if (child) {
		if (enableDebug) log.warn "Device already exists"
	} 
	else {
		child = addChildDevice("smartthings", "Wattvision", sensorId, null, [name: "Wattvision", label: sensorName])
	}

	child.setGraphUrl(getGraphUrl(sensorId));

	getDataForChild(child, null, null)

	scheduleDataCollection()

	return childMarshaller(child)
}