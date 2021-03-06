/**
 *  SimpliSafe integration for Hubitat
 *
 *  Copyright 2015 Felix Gorodishter
 *  Modifications by Scott Silence
 *	Modifications by Toby Harris - 11/15/2020
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

preferences {
	input(name: "username", type: "text", title: "Username", required: "true", description: "SimpliSafe Username")
	input(name: "password", type: "password", title: "Password", required: "true", description: "SimpliSafe Password")
	input(name: "ssversion", type: "enum", title: "SimpliSafe Version", required: "true", description: "Alarm system version", options: ["ss1", "ss2", "ss3"])
}

metadata {	
	definition (name: "SimpliSafe", namespace: "johnlcox", author: "Toby Harris") {
		capability "Alarm"
        capability "Acceleration Sensor"
		capability "Polling"
        capability "Contact Sensor"
		capability "Carbon Monoxide Detector"
		capability "Presence Sensor"
		capability "Smoke Detector"
        capability "Temperature Measurement"
        capability "Water Sensor"
        capability "Sensor"
        capability "Switch"
		command "off"
		command "home"
		command "away"
        command "on"
		//attribute "events", "string"
		attribute "messages", "string"
		attribute "status", "string"
	}

tiles(scale: 2) {
    multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
        tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
            attributeState "off", label:'${name}', icon: "st.security.alarm.off", backgroundColor: "#505050"
            attributeState "home", label:'${name}', icon: "st.Home.home4", backgroundColor: "#00BEAC"
            attributeState "away", label:'${name}', icon: "st.security.alarm.on", backgroundColor: "#008CC1"
			attributeState "pending off", label:'${name}', icon: "st.security.alarm.off", backgroundColor: "#ffffff"
			attributeState "pending away", label:'${name}', icon: "st.Home.home4", backgroundColor: "#ffffff"
			attributeState "pending home", label:'${name}', icon: "st.security.alarm.on", backgroundColor: "#ffffff"
			attributeState "away_count", label:'countdown', icon: "st.security.alarm.on", backgroundColor: "#ffffff"
			attributeState "failed set", label:'error', icon: "st.secondary.refresh", backgroundColor: "#d44556"
			attributeState "alert", label:'${name}', icon: "st.alarm.beep.beep", backgroundColor: "#ffa81e"
			attributeState "alarm", label:'${name}', icon: "st.security.alarm.alarm", backgroundColor: "#d44556"
			attributeState "carbonMonoxide", label:'${name}', icon: "st.alarm.carbon-monoxide.carbon-monoxide", backgroundColor: "#d44556"
			attributeState "smoke", label:'${name}', icon: "st.alarm.smoke.smoke", backgroundColor: "#d44556"
			attributeState "temperature", label:'${name}', icon: "st.alarm.temperature.freeze", backgroundColor: "#d44556"
			attributeState "water", label:'${name}', icon: "st.alarm.water.wet", backgroundColor: "#d44556"
        }
		
		tileAttribute("device.temperature", key: "SECONDARY_CONTROL", wordWrap: true) {
			attributeState("temperature", label:'${currentValue}', unit:"dF", defaultState: true)
		}
    }	
	
    standardTile("off", "device.alarm", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
        state ("off", label:"off", action:"off", icon: "st.security.alarm.off", backgroundColor: "#008CC1", nextState: "pending")
        state ("away", label:"off", action:"off", icon: "st.security.alarm.off", backgroundColor: "#505050", nextState: "pending")
        state ("home", label:"off", action:"off", icon: "st.security.alarm.off", backgroundColor: "#505050", nextState: "pending")
        state ("pending", label:"pending", icon: "st.security.alarm.off", backgroundColor: "#ffffff")
	}
    
    standardTile("off", "device.alarm", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
        state ("off", label:"off", action:"", icon: "st.security.alarm.off", backgroundColor: "#008CC1", nextState: "")
        state ("away", label:"off", action:"", icon: "st.security.alarm.off", backgroundColor: "#505050", nextState: "")
        state ("home", label:"off", action:"", icon: "st.security.alarm.off", backgroundColor: "#505050", nextState: "")
        state ("pending", label:"pending", icon: "st.security.alarm.off", backgroundColor: "#ffffff")
	}
	
    standardTile("home", "device.alarm", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
        state ("off", label:"home", action:"home", icon: "st.Home.home4", backgroundColor: "#505050", nextState: "pending")
        state ("away", label:"home", action:"home", icon: "st.Home.home4", backgroundColor: "#505050", nextState: "pending")
		state ("home", label:"home", action:"home", icon: "st.Home.home4", backgroundColor: "#008CC1", nextState: "pending")
		state ("pending", label:"pending", icon: "st.Home.home4", backgroundColor: "#ffffff")
	}
    
    standardTile("away", "device.alarm", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
        state ("off", label:"away", action:"away", icon: "st.security.alarm.on", backgroundColor: "#505050", nextState: "pending") 
		state ("away", label:"away", action:"away", icon: "st.security.alarm.on", backgroundColor: "#008CC1", nextState: "pending")
        state ("home", label:"away", action:"away", icon: "st.security.alarm.on", backgroundColor: "#505050", nextState: "pending")
		state ("pending", label:"pending", icon: "st.security.alarm.on", backgroundColor: "#ffffff")
		state ("away_count", label:"pending", icon: "st.security.alarm.on", backgroundColor: "#ffffff")
	}

		valueTile("events", "device.events", width: 6, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false, decoration: "flat", wordWrap: true) {
			state ("default", label:'${currentValue}')
		}

		main(["status"])
		details(["status","off", "home", "away", "events"])
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
	state.clear()
	log.info "Setting up schedule (every 5 minutes)..."
	runEvery5Minutes(poll)
}

// handle commands
def off() {
	log.info "Setting SimpliSafe mode to 'Off'"
	setState ('off')
    sendEvent(name: "switch", value: "off")
}

def on() {
	log.info "Setting SimpliSafe mode to 'Home'"
	setState ('home')
    sendEvent(name: "switch", value: "on")
}

def home() { 
	log.info "Setting SimpliSafe mode to 'Home'"
	setState ('home')
    sendEvent(name: "switch", value: "on")
}

def away() {
	log.info "Setting SimpliSafe mode to 'Away'"
	setState ('away')
    sendEvent(name: "switch", value: "on")
}

def setState (alState){
	//Check Auth first
	checkAuth()
    def timeout = false;
	
	if (state.auth) {
    if (alState == "off")
    {
    	try {
        	httpPost([ uri: getAPIUrl("alarmOff"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8" ]){resp ->}
        } catch (e) {
        	timeout = true;
        	log.debug "Alarm SET to OFF Error: $e"
        }
    }
    else if (alState == "home")
    {
    	try {
        	httpPost([ uri: getAPIUrl("alarmHome"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8" ]){resp ->}
        } catch (e) {
        	timeout = true;
        	log.debug "Alarm SET to HOME Error: $e"
        }
    }
    else if (alState == "away")
    {
    	try {
        	httpPost([ uri: getAPIUrl("alarmAway"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8" ]){resp ->}
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
}

def poll() {
	//Check Auth first
	checkAuth()
	
	if (state.auth) {
    //log.info "Executing polling..."

	httpGet ([uri: getAPIUrl("refresh"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8"]) { response ->
        if (response.data.subscription.location.system.alarmState.toLowerCase() != "error") {
		   //Check alarm state
		   sendEvent(name: "alarm", value: response.data.subscription.location.system.alarmState.toLowerCase())
		   sendEvent(name: "status", value: response.data.subscription.location.system.alarmState.toLowerCase())
		   // log.info "Alarm State1: $response.data.subscription.location.system.alarmState"
        }
	
		//Check temperature
		if (response.data.subscription.location.system.temperature != null) {
		sendEvent(name: "temperature", value: response.data.subscription.location.system.temperature, unit: "dF")
		// log.info "Temperature: $response.data.subscription.location.system.temperature"
		}
		
		//Set presence && virtual acceleration
		def alarm_state = response.data.subscription.location.system.alarmState.toLowerCase()
		if (alarm_state != device.currentValue("alarm")) {
		
		def alarm_presence = ['off':'present', 'home':'present', 'away':'not present']
		sendEvent(name: 'presence', value: alarm_presence.getAt(alarm_state))
	
		def alarm_acceleration = ['off':'inactive', 'home':'active', 'away':'active']
		sendEvent(name: 'acceleration', value: alarm_acceleration.getAt(alarm_state))
		}
		
		//Check messages
       	if (settings.ssversion == "ss3") {
            if (response.data.subscription.location.system.messages[0] != null)
            {
				sendEvent(name: "status", value: "alert")
                sendEvent(name: "messages", value: response.data.subscription.location.system.messages[0].text)
                // log.info "Messages: ${response.data.subscription.location.system.messages[0].text}"
				
				//Check for alerts
				if (response.data.subscription.location.system.messages[0].category == "alarm")
				{
				sendEvent(name: "contact", value: "open")
				sendEvent(name: "status", value: "alarm")
				// log.info "Message category: ${response.data.subscription.location.system.messages[0].category}"
				
					//Carbon Monoxide sensor alerts
					if (response.data.subscription.location.system.messages[0].data.sensorType == "C0 Detector")
					{
					sendEvent(name: "carbonMonoxide", value: "detected")
					sendEvent(name: "status", value: "carbonMonoxide")
					log.info "Message sensor: ${response.data.subscription.location.system.messages[0].data.sensorType}"
					}
					
					//Smoke sensor alerts
					if (response.data.subscription.location.system.messages[0].data.sensorType == "Smoke Detector")
					{
					sendEvent(name: "smoke", value: "detected")
					sendEvent(name: "status", value: "smoke")
					log.info "Message sensor: ${response.data.subscription.location.system.messages[0].data.sensorType}"
					}
					
				
					//Temperature sensor alerts
					//if (response.data.subscription.location.system.messages[0].data.sensorType == "Freeze Sensor")
					//{
					//sendEvent(name: "temperature", value: "??")
					//sendEvent(name: "status", value: "temperature")
					// log.info "Message sensor: ${response.data.subscription.location.system.messages[0].data.sensorType}"
					//}

					//Water sensor alerts
					if (response.data.subscription.location.system.messages[0].data.sensorType == "Water Sensor")
					{
					sendEvent(name: "water", value: "wet")
					sendEvent(name: "status", value: "water")
					log.info "Message sensor: ${response.data.subscription.location.system.messages[0].data.sensorType}"
					}
				}	
            }
            else
            {
				sendEvent(name: "contact", value: "closed")
				sendEvent(name: "messages", value: "none")
				sendEvent(name: "carbonMonoxide", value: "clear")
				sendEvent(name: "smoke", value: "clear")
				//sendEvent(name: "temperature", value: "??")
				sendEvent(name: "water", value: "dry")
                // log.info "Messages: ${response.data.subscription.location.system.messages}"
            }
      	}
    }
	
		//Check events
		//httpGet ([uri: getAPIUrl("events"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8"]) { response ->
		//if (response.data.events[0] != null) {
			//sendEvent(name: "events", value: response.data.events[0].messageBody)
			// log.info "Events: ${response.data.events[0].messageBody}"
    //}
//}	
	
    //log.info "Alarm State2: $response"
    //apiLogout()
 }
}

def apiLogin() {
    //log.info "Executing login..."
     
    def params = [
        uri: 'https://api.simplisafe.com',
        path: '/v1/api/token',
        contentType: "application/json",
        body: [ "grant_type": "password",
                "username": settings.username,
                "password": settings.password,
                "client_id": device.id,
                "device_id": "WebApp" ]
    ]
    asynchttpPost(apiLoginHandler, params)
}

def apiLoginHandler(response, data) {
    if (response.hasError()) {
       // log.trace "response received error: ${response.getErrorMessage()}"
       // log.trace "error response data: ${response.getErrorData()}"
        state.errorData = parseJson(response.errorData)
    if (state.errorData.error == "mfa_required") {
        // log.info state.errorData.mfa_token
        state.mfa_token = state.errorData.mfa_token
        mfaAuth()
        }
    } else {
		state.remove("errorData")
		state.remove("mfa_token")
		state.remove("mfa_challenge")
		initAuth() 
    }
}


def mfaAuth() {
    //log.info "Executing MFA login..."
	if (!state.mfa_challenge) {
    def params = [
        uri: 'https://api.simplisafe.com',
        path: '/v1/api/mfa/challenge',
        contentType: "application/json",
        body: [ "challenge_type": "oob",
                "client_id": device.id,
                "mfa_token": state.mfa_token ]
    ]
    asynchttpPost(mfaAuthHandler, params)
	    } else {
	    log.warn "MFA pending, check email"
    }
}

def mfaAuthHandler(response, data) {
    if (response.hasError()) {
        log.trace "response received error: ${response.getErrorMessage()}"
    } else {
	    log.warn "MFA sent, check email"
		state.mfa_challenge = "sent"
    }
}


def initAuth() {
	//Login to the system
	if (!state.errorData) {
    def authBody = [ "grant_type": "password",
                "username": settings.username,
                "password": settings.password,
                "client_id": device.id,
                "device_id": "WebApp" ]                 
    
    try {
        httpPost([ uri: getAPIUrl("initAuth"), contentType: "application/json; charset=utf-8", body: authBody ]) { response ->
        	state.auth = response.data
            state.auth.respAuthHeader = ["Authorization":state.auth.token_type + " " + state.auth.access_token]
            state.auth.tokenExpiry = now() + 3600000
        }
 	} catch (e) {
    log.error "initAuth something went wrong: $e"
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
 }

def getUserId() {
	//check auth and get uid    
    httpGet ([uri: getAPIUrl("authCheck"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8"]) { response ->
        state.uid = response.data.userId
    }
    // log.info "User ID: $state.uid"
}

def getSubscriptionId() {
	//get subscription id
    httpGet ([uri: getAPIUrl("subId"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8"]) { response ->
    	String tsid = response.data.subscriptions.location.sid
		state.subscriptionId = tsid.substring(1, tsid.length() - 1)
    }
    // log.info "Subscription ID: $state.subscriptionId"
}

def checkAuth()
{
	// log.info "Checking to see if token has expired...."
        
    //log.info "Expiry time: $state.auth.tokenExpiry"
    if (!state.auth || !state.auth.tokenExpiry || now() > state.auth.tokenExpiry) {    
		log.info "Token has expired, excecuting re-login..."
        apiLogin()
    } else {
    
	//Check Auth
    try {
        httpGet ([uri: getAPIUrl("authCheck"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8"]) { response ->
            return response.status        
        }
    } catch (e) {
    log.error "checkAuth something went wrong: $e"
	state.auth = null
    }
  }
 }


def apiLogout() {
    httpDelete([ uri: getAPIUrl("initAuth"), headers: state.auth.respAuthHeader, contentType: "application/json; charset=utf-8" ]) { response ->
        if (response.status == 200) {
            state.subscriptionId = null
            // log.info "Logged out from API."
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