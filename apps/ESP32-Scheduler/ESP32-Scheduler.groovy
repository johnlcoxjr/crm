/* ESP32 Scheduler
*
* 2.0.0    	10/2/19    	updated to Hubitat
* 2.0.1		11/10/19	added debug
* 2.0.2		11/10/19	added catch all message via UDP
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

metadata {
    definition (
	name: "ESP32 Scheduler", 
	version: "2.0.1", 
	namespace: "johnlcox", 
	author: "john.l.cox@live.com",
	importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/ESP32-Scheduler.groovy"
    ) {
        capability "Sensor"
		//capability "Momentary"
		//capability "Switch"

        command "getVersion"
        command "getTime"
		command "reboot"
        command "initialize"

        attribute "schedule1","string"
        attribute "schedule2","string"
        attribute "schedule3","string"
        attribute "schedule4","string"
        attribute "schedule5","string"
        attribute "schedule10","string"  
        attribute "schedule15","string"
        attribute "schedule20","string"
        attribute "schedule30","string"
        attribute "schedule45","string"
        attribute "schedule60","string"
        attribute "udpMessage","string"
        attribute "system", "string"
        attribute "msg", "string"
        attribute "comChecked", "string"
	attribute "zone","string"
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
    
    main(["scheduler"])
    details(["scheduler", "msg", "version","screen"])
}

def ping() {
    if (showLogs) log.info("ping")
    
    sendEthernet("ping")
}

def initialize() {
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
        
        else if (msg.contains("time")) {
            sendEvent(name: "msg", value: "Ready")

		    def parts = msg.tokenize(" ")

	       	def part1 = parts[0]
	  	    def part2 = parts[1]
            
            if (part2 != null) {
		    	def subparts = part2.tokenize(":")

                int subpart1 = subparts[0].toInteger()
                int subpart2 = subparts[1].toInteger()
                int subpart3 = subparts[2].toInteger()
				def subpart4 = subparts[3].toString()
                
                def setTime = "${subpart1}:${subparts[1]}"
            
                if (subpart2 != null) {
                	sendEvent(name: "msg", value: "Ready")
                    
                    sendEvent(name: "scheduler", value: "${setTime}")
                    sendEvent(name: "schedule1", value: "1=${setTime}.")            

                   if (subpart2 % 2 == 0) {
                        sendEvent(name: "schedule2", value: "2=${setTime}.")
                   }
                   if (subpart2 % 3 == 0) {
                        sendEvent(name: "schedule3", value: "3=${setTime}.")
                   }
                   if (subpart2 % 4 == 0) {
                        sendEvent(name: "schedule4", value: "4=${setTime}.")
                   }
                   if (subpart2 % 5 == 0) {
                        sendEvent(name: "schedule5", value: "5=${setTime}.")
                   }
                   if (subpart2 % 10 == 0) {
                        sendEvent(name: "schedule10", value: "10=${setTime}.")
                   }
                   if (subpart2 % 15 == 0) {
                        sendEvent(name: "schedule15", value: "15=${setTime}.")
                   }
                   if (subpart2 % 20 == 0) {
                        sendEvent(name: "schedule20", value: "20=${setTime}.")
                   }
                   if (subpart2 % 30 == 0) {
                        sendEvent(name: "schedule30", value: "30=${setTime}.")
                   }
                   if (subpart2 % 45 == 0) {
                        sendEvent(name: "schedule45", value: "45=${setTime}.")
                   }
                   if (subpart2 == 0) {
                        sendEvent(name: "schedule60", value: "60=${setTime}.")
                   }
            	} 
				if (subpart4 != null) {
					sendEvent(name: "zone", value: subpart4)	
				}
            }    
       	}        
             
        else if (msg.contains("<udp>")) {
		    def parts = msg.tokenize("=")

	       	def part1 = parts[0]
	  	    def part2 = parts[1]
            
            if (part2 != null) {
	            sendEvent(name: "udpMessage", value: "UDP=${part2}")

				if (showLogs) log.info("UDP --> ${part2}")
			}        
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

def getTime() {
	if (showLogs) log.debug("Sent to device:  time")
      
	sendEthernet("gettime")
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