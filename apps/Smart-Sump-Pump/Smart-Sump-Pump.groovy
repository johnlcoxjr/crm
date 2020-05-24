/*  Smart Sump Pump (Parent)
 *
 *  Version 
 *	1.0.0 	1/1/2016
 *	1.0.1	2/26/2017	removed unnecessary code
 *	2.0.0	3/28/2017 	combined parent/child app
 *	2.0.1	3/30/2017	fixed type with phone vs phone1
 *	2.0.2	3/30/2017	added the ability to determine increasing/decreasing/stable
 *  3.0.0   9/28/2019   update to Hubitat
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
	name: "Smart Sump Pump",
    namespace: "johnlcox",
	author: "john.l.cox@live.com",
	description: "Sends a message with presence detection to indicate that laundry is done.",
    singleInstance: false,
	category: "Convenience",
	iconUrl: "",
	iconX2Url: ""
	importUrl: "https://raw.githubusercontent.com/johnlcoxjr/Hubitat/master/Apps/Smart-Sump-Pump"
)

preferences {
    page(name: "parentPage")
}

def parentPage() {
	return dynamicPage(name: "parentPage", title: "", nextPage: "", install: true, uninstall: true) {  
        section("Create a sump pump monitor.") {
            app(name: "childApps", appName: "Smart Sump Pump Child", namespace: "johnlcox", title: "New sump pump monitor", multiple: true)
        }
        
        section("Statistics") {
        	paragraph "There are ${childApps.size()} monitors(s) configured."
        }
        
        section() {
            paragraph "Smart Sump Pump\n" + 
                "${version()}\n${copyright()}"
        }
        
        section("About", hideable:true, hidden:true) {
            def hrefAbout = [
                url:        "http://www.coxscience.org/",
                style:      "embedded",
                title:      "Tap for more information...",
                description:"http://www.coxscience.org/",
                required:   false
            ]

            paragraph about()
            href hrefAbout
        }  
        
        section("Paypal", hideable:true, hidden:true) {
            def hrefPaypal = [
                url:        "https://www.paypal.me/johnlcox",
                style:      "embedded",
                title:      "Tap for Paypal...",
                description:"https://www.paypal.me/johnlcox",
                required:   false
            ]
            
            paragraph paypal()
            href hrefPaypal           
        }  
		
		section("-= <b>Debug Menu</b> =-") {
			input "enableDebug", "bool", title: "Enable debug output?", required: false, defaultValue: false
		}
    }
}

def getDesc(param1, param2, param3, param4) {def result = param1 || param2 || param3 || param4 ? "CONFIGURED - Tap to edit/view" : "UNCONFIGURED - Tap to configure"}

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
