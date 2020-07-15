/* ESP8266 Pinger
*
* 1.0.0    	7/5/20    	initial
*
* Copyright 2020 John Cox
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

metadata {
    definition (
	name: "ESP32 Pinger", 
	version: "1.0.0", 
	namespace: "johnlcox", 
	author: "john.l.cox@live.com",
	importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/ESP8266-Pinger.groovy")
    {
        capability "Sensor"
        capability "Refresh"
		capability "Actuator"
        capability "Switch"

        command "getVersion"
        command "checkPlex"
        command "checkInternet"
        command "checkPower"
        command "checkBattery"
        command "statusPlex"
        command "statusInternet"
        command "statusPower"
        command "statusBattery"
		command "reboot"
        command "initialize"
        command "refresh"
        attribute "plex", "string"
        attribute "internet", "string"
        attribute "power", "string"
        attribute "battery", "string"
        attribute "msg", "string"
    }
    
    preferences {
	    section("Arduino") {
    	    input "ip", "text", title: "Arduino IP Address", description: "ip", required: true, displayDuringSetup: true
	        input "port", "text", title: "Arduino Port", description: "port", required: true, displayDuringSetup: true
            input "mac", "text", title: "Arduino MAC Addr", description: "mac", required: true, displayDuringSetup: true
            input "freq", "text", title: "Ping Frequency", description: "freq", required: true, displayDuringSetup: true, defaultValue: "15"
	    }
	    section("Preferences") {
            input "showLogs", "bool", required: false, title: "Show Debug Logs?", defaultValue: false
    	}
    }
    
    main(["pinger"])
    details(["pinger", "msg", "version"])
}

def ping() {
    if (showLogs) log.info("ping")
    
    sendEthernet("ping")
}

def initialize() {
    state.clear()
    
    schedule("0/${settings.freq} * * * * ? *", ping)
}

def parse(String description) {          
    def msg = parseLanMessage(description).payload
          
    if (showLogs) log.info("description = ${description}")
    
    if (showLogs) log.info("payload = ${msg}")
	    
    msg = hexToASCII(msg);
    if (showLogs) log.info("message = ${msg}")
	
    if (msg != null) {
        if (msg.contains("ping") || msg.equals("")) {
            sendEvent(name: "msg", value: "Ready")
        }
        
        else if (msg.contains("Version")) {
            sendEvent(name: "msg", value: "${msg}")
            
            if (showLogs) log.info("version = ${msg}")
        }       
        
        else if (msg.contains(":")) {
            sendEvent(name: "msg", value: "Ready")

		    def parts = msg.tokenize(":")

	       	def part1 = parts[0]
	  	    def part2 = parts[1].toInteger()
            
            if (part1 == "battery") {
                if (state.power == "okay") {
                    if (part2 == 1) {
                        sendEvent(name: "${part1}", value: "okay, on AC") 
                    }
                    else {
                        sendEvent(name: "${part1}", value: "fail, state unknown")
                    }                  
                }
                else {
                    if (part2 == 1) {
                       sendEvent(name: "${part1}", value: "okay, on UPS") 
                    }
                    else {
                        sendEvent(name: "${part1}", value: "fail, state unknown")
                    }                           
                }    
            }
            else {          
                if (part2 == 1) {
                    sendEvent(name: "${part1}", value: "okay")
                    if (part1 == "power") {state.power = "okay"}
                }
                else {
                    sendEvent(name: "${part1}", value: "fail")
                    if (part1 == "power") {state.power = "fail"}
                }  
            
                if (part1 == "power") {
                    if (part2 == 1) {
                        sendEvent(name: "switch", value: "on")
                    }
                    else {
                        sendEvent(name: "switch", value: "off")
                    }
                }
            }
                       
            sendEvent(name: "msg", value: "${msg}", displayed: true, isStateChange: true, isPhysical: true)  
      	}        
                    
		else {
			sendEvent(name: "msg", value: "${msg}", displayed: true, isStateChange: true, isPhysical: true)   
		} 
 	}
}

private static String hexToASCII(String hexValue) {
    StringBuilder output = new StringBuilder("");
    for (int i = 0; i < hexValue.length(); i += 2) {
        String str = hexValue.substring(i, i + 2);
        output.append((char) Integer.parseInt(str, 16));
    }
    return output.toString();
}

def refresh() {
    checkPlex()
    checkInternet()
    checkPower()
}

def checkPlex() {
	if (showLogs) log.debug("Sent to device:  checkPlex")
      
	sendEthernet("checkPlex")
}

def checkInternet() {
	if (showLogs) log.debug("Sent to device:  checkInternet")
      
	sendEthernet("checkInternet")
}

def checkPower() {
	if (showLogs) log.debug("Sent to device:  checkPower")
      
	sendEthernet("checkPower")
}

def checkBattery() {
	if (showLogs) log.debug("Sent to device:  checkBattery")
      
	sendEthernet("checkBattery")
}

def statusPlex() {
	if (showLogs) log.debug("Sent to device:  statusPlex")
      
	sendEthernet("statusPlex")
}

def statusInternet() {
	if (showLogs) log.debug("Sent to device:  statusInternet")
      
	sendEthernet("statusInternet")
}

def statusPower() {
	if (showLogs) log.debug("Sent to device:  statusPower")
      
	sendEthernet("statusPower")
}

def statusBattery() {
	if (showLogs) log.debug("Sent to device:  statusBattery")
      
	sendEthernet("statusBattery")
}

def on() {
    sendEvent(name: "switch", value: "on")
}

def off() {
    sendEvent(name: "switch", value: "off")
}

private getHostAddress() {
    def ip = settings.ip
    def port = settings.port

	if (showLogs) log.info("Using ip: ${ip} and port: ${port} for device: ${device.id}")
    return ip + ":" + port
}

def sendEthernet(message) {
	if (showLogs) log.info("Executing 'sendEthernet' ${message}")
     
    def myHubAction = new hubitat.device.HubAction(message, 
								hubitat.device.Protocol.LAN, 
                                settings.mac,
								[type: hubitat.device.HubAction.Type.LAN_TYPE_UDPCLIENT, 
		                        destinationAddress: getHostAddress(),
                                callback: parse])
    sendHubCommand(myHubAction)
}

def getVersion() {
	if (showLogs) log.info("Sent to device:  version")
    
    sendEthernet("version")
}

def reboot() {
	if (showLogs) log.info("Sent to device:  reboot")
    
    sendEthernet("reboot")
}