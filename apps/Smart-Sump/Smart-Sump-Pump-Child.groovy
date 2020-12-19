/*  Smart Sump Pump (Parent)
 *
 *  Version 
 *	1.0.0 	1/1/2016
 *	1.0.1	2/26/2017	removed unnecessary code
 *	2.0.0	3/28/2017 	combined parent/child app
 *	2.0.1	3/30/2017	fixed type with phone vs phone1
 *	2.0.2	3/30/2017	added the ability to determine increasing/decreasing/stable
 *  3.0.0	11/18/19	updated to Hubitat
 *
 *  Copyright 2017 John Cox
 *
 *  Developer retains all right, title, copyright, and interest, including all copyright, patent rights, trade secret in the Background technology.
 *  May be subject to consulting fees under the Agreement between the Developer and the Customer. Developer grants a non-exclusive perpetual license
 *  to use the Background technology in the Software developed for and delivered to Customer under this Agreement. However, the Customer shall make
 *  no commercial use of the Background technology without Developer's written consent.
 *  
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied. 
 *
 *  Software Distribution is restricted and shall be done only with Developer's written approval.
 */
definition(
	name: "Smart Sump Pump Child",
    namespace: "johnlcox",
	author: "john.l.cox@live.com",
	description: "",
    singleInstance: false,
	parent: "johnlcox:Smart Sump Pump",
	category: "Convenience",
	iconUrl: "",
	iconX2Url: "",
	importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Sump-Pump-Child.groovy"
)

preferences {
    page(name: "childStartPage")
}

def childStartPage() {
	return dynamicPage(name: "childStartPage", title: "Monitor", nextPage: "", install: true, uninstall: true) { 
    
        section("") {
            paragraph "Please use each of the the sections below to setup the different preferences to your liking." 
        }    

		section("Monitor this sump pump"){
			input("multi", "capability.accelerationSensor", title: "Which?", required: true)
		}

		section("Danger Zone?") {
            paragraph("Notification will be made if the occurence exceeds a rate of 2 times in 10 minutes (prorated based upon your designated duration).")

            input "frequency", "decimal", title: "What time frame should I monitor?", description: "Minutes", required: true, defaultValue: 10
        }  
        
    	section("Send push notification"){
			input name: "notification", type: "capability.notification", title: "Notification", required: true, multiple: true
    	}
        
  		section("Message interval?") {
    		input name: "messageDelay", type: "number", title: "Minutes (default to every message)", required: false, defaultValue: 1
  		}  
        
		if (overrideLabel) {
            section("Monitor") {
                label title: "Name", defaultValue: "Sump Pump", required: false
            }
        } 
		else {
            section("Monitor") {
                paragraph app.label
            }
        }

		section {
            input "overrideLabel", "bool", title: "Edit monitor name", defaultValue: "false", required: "false", submitOnChange: true
        }
    }
}

def getDesc(param1, param2, param3, param4) {def result = param1 || param2 || param3 || param4 ? "CONFIGURED - Tap to edit/view" : "UNCONFIGURED - Tap to configure"}

def installed() {
	initializeChild()
}

def initializeChild() {  
    if (multi != null) {
		subscribe(multi, "acceleration.active", checkFrequency)
    
   		schedule("0 7/${frequency} * * * ?", checkReport) //changed 0 to 7 to hopefully avoid time out
    
    	state.count = 0    
        
        state.lastCount = 0
    }    
}

def updated() {
	updatedChild()
}

def updatedChild() {
	unsubscribe()
    unschedule()
    state.clear()
    initializeChild()
}

def checkFrequency(evt){
	state.count = state.count + 1

	if (parent.enableDebug) {log.debug("running check on ${multi.displayName.toString()} - run ${state.count} time(s)")}
}

def checkReport() {
	def max = frequency * 2 / 10

	if (state.count >= max) { 
        def msg = "Alert: ${multi.displayName.toString()} has ran ${state.count} time(s) in the last ${frequency} minutes."
        
        if (state.lastCount == null) {
        	//do nothing, this should only happen the first time
        }
        else if (state.lastCount > state.count) {
        	msg = msg + "  Flow appears to be decreasing."
			
			app.updateLabel("${multi.displayName.toString()} <span style=\"color:blue\">${state.count} - decreasing</span>")
        }
        else if (state.lastCount ==state.count) {
        	msg = msg + "  Flow appears to be stable."

			app.updateLabel("${multi.displayName.toString()} <span style=\"color:blue\">${state.count} - stable</span>")
		}	
        else if (state.lastCount < state.count) {
        	msg = msg + "  Flow appears to be increasing."

			app.updateLabel("${multi.displayName.toString()} <span style=\"color:blue\">${state.count} - increasing</span>")
		}
        
		def timeNow = now()

		msg = msg + " (${(new Date(timeNow).format('h:mm',location.timeZone))})"

		notification.deviceNotification(msg)        	
    }
	else {
		app.updateLabel("${multi.displayName.toString()} <span style=\"color:blue\">${state.count}</span>")
	}
	
    if (parent.enableDebug) {log.info("${multi.displayName.toString()} run count ${state.count} - now reset to 0")}
	
    state.lastCount = state.count
    
	state.count = 0
}    

private def about() {
    def text =
        "Smart Sump Pump (Parent) monitors and notifies you when your sump pump has ran too frequently. " +
        "All aspects of this app are customizable. \n\n" +
        "${version()}\n${copyright()}\n\n" +
        "You can contribute to the development of this app by making a PayPal donation to john.l.cox@live.com. I appreciate your support."
}

private def paypal() {
    def text =
        "You can contribute to the development of this app by making a PayPal donation to https://www.paypal.me/johnlcox. I appreciate your support."
}

private def version() {
    return "Version 3.0.0"
}

private def copyright() {
    return "Copyright © 2017-19 John Cox"
}
