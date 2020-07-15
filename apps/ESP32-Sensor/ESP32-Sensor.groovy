/*
*   2.0.0   10/2/19     ported to Hubitat
*	2.0.1	11/10/19	added debug
*   3.0.0   7/8/20      upgraded to common framework
*/

import hubitat.helper.InterfaceUtils

metadata {
	definition (
    name: "ESP32 Sensor",
    version: "3.0.0",
    namespace: "johnlcox", 
    author: "john.l.cox@live.com",
    importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Drivers/ESP32-Sensor.groovy")
    {
		capability "Sensor"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Illuminance Measurement"       
        capability "Color Control"
        capability "Color Temperature"
        
        attribute "temperature","string"
        attribute "humidity","string"
        attribute "pressure","string"
        attribute "altitude","string"
        attribute "lux","string"
        attribute "colorTemp","string"
        attribute "color","string"
        attribute "mfd","string"
        attribute "rssi","string"
        attribute "voltage","string"

        command "getVersion"
        command "data"
        command "reboot"
        command "initialize"

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
 
	main (["temperature"])
		details (["temperature","humidity","pressure","mfd","altitude","lux","colorTemp","color","msg","version"])
}

def ping() {
    if (showLogs) log.info("ping")
    
    sendEthernet("ping")
}

def initialize() {
    state.clear()
    
    schedule("0/${settings.freq} * * * * ? *", ping)
}

// parse events into attributes
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

        else if ((msg.contains("Version")) && (msg.contains("|"))) {
            def parts = msg.tokenize('|')
            def temp = parts[0].toString()
            def hum = parts[1].toString()
            def press = parts[2].toString()
            def alt = parts[3].toString()
            def lux = parts[4].toString()
            def colorTemp = parts[5].toString()
            def color = parts[6].toString()
            def mfd = parts[7].toString()

            def subparts = ""
            def parm = ""
            float value = 0
            def valueS = ""

            if (msg.contains("t")) {
                subparts = temp.tokenize(' ')
                value = subparts[1].toFloat()
                sendEvent(name: 'temperature', value: value)

                if (msg.contains("h")) {
                    subparts = hum.tokenize(' ')
                    value = subparts[1].toFloat()
                    sendEvent(name: 'humidity', value: value)
                }
                if (msg.contains("p")) {
                    subparts = press.tokenize(' ')
                    value = subparts[1].toFloat()
                    sendEvent(name: 'pressure', value: value)
                }
                if (msg.contains("a")) {
                    subparts = alt.tokenize(' ')
                    value = subparts[1].toFloat()
                    sendEvent(name: 'altitude', value: value)
                }
                if (msg.contains("l")) {
                    subparts = lux.tokenize(' ')
                    value = subparts[1].toInteger()
                    sendEvent(name: 'lux', value: value)
                }    
                if (msg.contains("k")) {
                    subparts = colorTemp.tokenize(' ')
                    value = subparts[1].toInteger()
                    sendEvent(name: 'colorTemp', value: value)
                }  
                if (msg.contains("c ")) {
                    subparts = color.tokenize(' ')
                    valueS = subparts[1]
                    sendEvent(name: 'color', value: valueS)
                }              
                if (msg.contains("m ")) {
                    subparts = mfd.tokenize(' ')
                    value = subparts[1].toInteger()
                    sendEvent(name: 'mfd', value: value)
                }              
                if (msg.contains("mode")) {
                    subparts = mode.tokenize(' ')
                    value = subparts[1].toInteger()
                    if (value == 1) {
                        sendEvent(name: 'mode', value: "sleep")
                    }
                    else {
                        sendEvent(name: 'mode', value: "awake")
                    }
                }

                msg = msg.replace("|L", "\n")

                sendEvent(name: 'msg', value: msg)
            }

            if (msg.contains("rssi")) {
                subparts = msg.tokenize(' ')
                value = subparts[1].toInteger()
                sendEvent(name: 'rssi', value: value)

                sendEvent(name: 'msg', value: 'Ready')
            } 
        }
        else if (msg.contains("Version")) {
            sendEvent(name: "msg", value: "${msg}")

            if (showLogs) log.info("version = ${msg}")
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

def data() {
	if (showLogs) log.info("Sent to device:  data")
    
    sendEthernet("data")
}

def getVersion() {
	if (showLogs) log.info("Sent to device:  version")
    
    sendEthernet("version")
}

def reboot() {
	if (showLogs) log.info("Sent to device:  reboot")
    
    sendEthernet("reboot")
}