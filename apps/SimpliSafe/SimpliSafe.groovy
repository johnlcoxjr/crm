/**
 *  SimpliSafe integration for SmartThings
 *
 *  Copyright 2015 Felix Gorodishter
 *  Modifications by Scott Silence
 *	Modifications by Toby Harris - 2/10/2018
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

import hubitat.helper.InterfaceUtils

metadata {
	preferences {
		section("Account") {
			input(name: "username", type: "text", title: "Username", required: "true", description: "SimpliSafe Username")
			input(name: "password", type: "password", title: "Password", required: "true", description: "SimpliSafe Password")
			input(name: "ssversion", type: "enum", title: "SimpliSafe Version", required: "true", description: "Alarm system version", options: ["ss1", "ss2", "ss3"])
		}
	
    	section("Preferences") {
        	input "showLogs", "bool", required: false, title: "Show Debug Logs?", defaultValue: false
    	}
	}
	
	definition (name: "SimpliSafe",version: "1.0.0", namespace: "johnlcox", author: "John Cox",importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/SimpliSafe.groovy") {
		capability "Alarm"
		capability "Polling"
       // capability "Contact Sensor"
		capability "Carbon Monoxide Detector"
		capability "Presence Sensor"
		capability "Smoke Detector"
        capability "Temperature Measurement"
        capability "Water Sensor"
        capability "Switch"  // <on> will arm alarm in "stay" mode; same as armStay
		command "off"
		command "home"
		command "away"
		command "update_state"
		//attribute "events", "string"
		attribute "messages", "string"
		attribute "status", "string"
	}
}

def installed() {
  init()
}

def updated() {
  unschedule()
  init()
}
  
def init() {
	runEvery5Minutes(poll)
}

// handle commands
def off() {
	if (showLogs) log.info("Setting SimpliSafe mode to 'Off'")
	setState ('off')
}

def on() { 
	if (showLogs) log.info("Setting SimpliSafe mode to 'Home'")
	setState ('home')
}

def home() { 
	if (showLogs) log.info("Setting SimpliSafe mode to 'Home'")
	setState ('home')
}

def away() {
	if (showLogs) log.info("Setting SimpliSafe mode to 'Away'")
	setState ('away')
}

def update_state() {
	if (showLogs) log.info("Refreshing SimpliSafe state...")
	poll()
}

def setState (alState){
	//Check Auth first
	checkAuth()
    def timeout = false;
    
    if (alState == "off")
    {
    	try {
        	httpPost([ uri: getAPIUrl("alarmOff"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8" ]){response ->}
        } catch (e) {
        	timeout = true;
        	log.debug "Alarm SET to OFF Error: $e"
        }
    }
    else if (alState == "home")
    {
    	try {
        	httpPost([ uri: getAPIUrl("alarmHome"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8" ]){response ->}
        } catch (e) {
        	timeout = true;
        	log.debug "Alarm SET to HOME Error: $e"
        }
    }
    else if (alState == "away")
    {
    	try {
        	httpPost([ uri: getAPIUrl("alarmAway"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8" ]){response ->}
        } catch (e) {
        	timeout = true;
        	log.debug "Alarm SET to AWAY Error: $e"
        }
    }
    else
    {
        log.info "Invalid state requested."
    }
    
    //If not a timeout, we can poll immediately, otherwise wait 10 seconds
    if (!timeout) {
    	poll()
    } else {
    	//There was a timeout, so we can't poll right away. Wait 10 seconds and try polling.
    	runIn(10, poll)
    }
}

def poll() {
	//Check Auth first
	checkAuth()

	httpGet ([uri: getAPIUrl("refresh"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8"]) { response ->

		if(response.data.subscription.location.system.alarmState != "error") {
			//Check alarm state
			sendEvent(name: "alarm", value: response.data.subscription.location.system.alarmState)
			sendEvent(name: "status", value: response.data.subscription.location.system.alarmState)
			if (showLogs) log.info("Alarm State1: $response.data.subscription.location.system.alarmState")
		
			//Check temperature
			if (response.data.subscription.location.system.temperature != null) {
				sendEvent(name: "temperature", value: response.data.subscription.location.system.temperature, unit: "dF")
				if (showLogs) log.info("Temperature: $response.data.subscription.location.system.temperature")
			}
		
			//Check messages
    	   	if (settings.ssversion == "ss3") {
        		if (response.data.subscription.location.system.messages[0] != null) {
					sendEvent(name: "status", value: "alert")
    	            sendEvent(name: "messages", value: response.data.subscription.location.system.messages[0].text)
        	        if (showLogs) log.info("Messages: ${response.data.subscription.location.system.messages[0].text}")
				
					//Check for alerts
					if (response.data.subscription.location.system.messages[0].category == "alarm") {
						sendEvent(name: "status", value: "alarm")
						if (showLogs) log.info("Message category: ${response.data.subscription.location.system.messages[0].category}")
				
						//Carbon Monoxide sensor alerts
						if (response.data.subscription.location.system.messages[0].data.sensorType == "C0 Detector") {
							sendEvent(name: "carbonMonoxide", value: "detected")
							sendEvent(name: "status", value: "carbonMonoxide")
							if (showLogs) log.info("Message sensor: ${response.data.subscription.location.system.messages[0].data.sensorType}")
						}
					
						//Smoke sensor alerts
						if (response.data.subscription.location.system.messages[0].data.sensorType == "Smoke Detector") {
							sendEvent(name: "smoke", value: "detected")
							sendEvent(name: "status", value: "smoke")
							if (showLogs) log.info("Message sensor: ${response.data.subscription.location.system.messages[0].data.sensorType}")
						}				
				
						//Temperature sensor alerts
						//if (response.data.subscription.location.system.messages[0].data.sensorType == "Freeze Sensor") {
						//sendEvent(name: "temperature", value: "??")
						//sendEvent(name: "status", value: "temperature")
						//if (showLogs) log.info("Message sensor: ${response.data.subscription.location.system.messages[0].data.sensorType}")
						//}

						//Water sensor alerts
						if (response.data.subscription.location.system.messages[0].data.sensorType == "Water Sensor") {
							sendEvent(name: "water", value: "wet")
							sendEvent(name: "status", value: "water")
							if (showLogs) log.info("Message sensor: ${response.data.subscription.location.system.messages[0].data.sensorType}")
						}
					}	
            	}
            	else {
					sendEvent(name: "messages", value: "none")
					sendEvent(name: "carbonMonoxide", value: "clear")
					sendEvent(name: "smoke", value: "clear")
					//sendEvent(name: "temperature", value: "??")
					sendEvent(name: "water", value: "dry")
	                if (showLogs) log.info("Messages: ${response.data.subscription.location.system.messages}")
    	        }
      		}
		}
		else {
			log.info("SimpliSafe ERROR")	
		}
    }
	
	//Check events
	/*httpGet ([uri: getAPIUrl("events"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8"]) { response ->
		if (response.data.events[0] != null) {
			sendEvent(name: "events", value: response.data.events[0].messageBody)
			if (showLogs) log.info("Events: ${response.data.events[0].messageBody}")
    	}
	}	*/
	
	//Set presence
	def alarm_state = device.currentValue("alarm")
	def alarm_presence = ['OFF':'present', 'HOME':'present', 'AWAY':'not present']
	sendEvent(name: 'presence', value: alarm_presence.getAt(alarm_state))
}

def apiLogin() {
	//Login to the system
  
   	//Define the login Auth Body and Header Information
    def authBody = [ "grant_type":"password",
    				"device_id":"WebApp",
                    "username":settings.username,
                    "password": settings.password ]                    
    def authHeader = [ "Authorization":"Basic NGRmNTU2MjctNDZiMi00ZTJjLTg2NmItMTUyMWIzOTVkZWQyLjEtMC0wLldlYkFwcC5zaW1wbGlzYWZlLmNvbTo="	]
    
    try {
        httpPost([ uri: getAPIUrl("initAuth"), headers: authHeader, contentType: "application/json; charset=utf-8", body: authBody ]) { response ->
        	state.auth = response.data
            state.auth.respAuthHeader = ["Authorization":state.auth.token_type + " " + state.auth.access_token]
            state.auth.tokenExpiry = now() + 3600000
        }
 	} catch (e) {
    	//state.token = 
    }
    
    //Check for valid UID, and if not get it
    if (!state.uid)
   	{
    	getUserId()
   	}
    
    //Check for valid Subscription ID, and if not get it
    //Might be able to expand this to multiple systems
    if (!state.subscriptionId)
    {
    	getSubscriptionId()
    }
}

def getUserId() {
	//check auth and get uid    
    httpGet ([uri: getAPIUrl("authCheck"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8"]) { response ->
        state.uid = response.data.userId
    }
    if (showLogs) log.info("User ID: $state.uid")
}

def getSubscriptionId() {
	//get subscription id
    httpGet ([uri: getAPIUrl("subId"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8"]) { response ->
    	String tsid = response.data.subscriptions.location.sid
		state.subscriptionId = tsid.substring(1, tsid.length() - 1)
    }
    if (showLogs) log.info("Subscription ID: $state.subscriptionId")
}

def checkAuth()
{
    //If no State Auth, or now Token Expiry, or time has expired, need to relogin
    if (showLogs) log.info("Expiry time: $state.auth.tokenExpiry")
    if (!state.auth || !state.auth.tokenExpiry || now() > state.auth.tokenExpiry) {    
    	log.info"Token Time has expired, excecuting re-login..."
        apiLogin()
    }
    
	//Check Auth
    try {
        httpGet ([uri: getAPIUrl("authCheck"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8"]) { response ->
            return response.status        
        }
    } catch (e) {
        state.clear()
        apiLogin()
        httpGet ([uri: getAPIUrl("authCheck"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8"]) { response ->
            return response.status        
    }
}
}

def apiLogout() {
    httpDelete([ uri: getAPIUrl("initAuth"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8" ]) { response ->
        if (response.status == 200) {
            state.subscriptionId = null
            if (showLogs) log.info("Logged out from API.")
        }
    }
}

def getTime()
{
	def tDate = new Date()
    return tDate.getTime()
}

def getAPIUrl(urlType) {
	if (urlType == "initAuth")
    {
    	return "https://api.simplisafe.com/v1/api/token"
    }
    else if (urlType == "authCheck")
    {
    	return "https://api.simplisafe.com/v1/api/authCheck"
    }
    else if (urlType == "subId" )
    {
    	return "https://api.simplisafe.com/v1/users/$state.uid/subscriptions?activeOnly=false"
    }
    else if (urlType == "alarmOff" )
    {
    	if (settings.ssversion == "ss3") 
        {
    		return "https://api.simplisafe.com/v1/$settings.ssversion/subscriptions/$state.subscriptionId/state/off"
        }
        else
        if (settings.ssversion == "ss2")
        {
        	return "https://api.simplisafe.com/v1/subscriptions/$state.subscriptionId/state?state=off"
        }       
    }
    else if (urlType == "alarmHome" )
    {
   		if (settings.ssversion == "ss3") 
        {
    		return "https://api.simplisafe.com/v1/$settings.ssversion/subscriptions/$state.subscriptionId/state/home"
        }
        else
        if (settings.ssversion == "ss2")
        {
        	return "https://api.simplisafe.com/v1/subscriptions/$state.subscriptionId/state?state=home"
        }
    }
    else if (urlType == "alarmAway" )
    {
   		if (settings.ssversion == "ss3") 
        {
    		return "https://api.simplisafe.com/v1/$settings.ssversion/subscriptions/$state.subscriptionId/state/away"
        }
        else
        {
        	return "https://api.simplisafe.com/v1/subscriptions/$state.subscriptionId/state?state=away"
        }
    }
    else if (urlType == "refresh")
    {
    	return "https://api.simplisafe.com/v1/subscriptions/$state.subscriptionId/"
    }
    else if (urlType == "events")
    {
    	return "https://api.simplisafe.com/v1/subscriptions/$state.subscriptionId/events?numEvents=1"
    }
    else
    {
    	log.info "Invalid URL type"
    }
}