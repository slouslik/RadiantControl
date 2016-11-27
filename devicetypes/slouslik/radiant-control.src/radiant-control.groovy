/**
 *  Copyright 2015 Adel Sarhan
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	Ecobee Thermostat
 *
 *	Author: Adel Sarhan
 *	Date: 2016-11-24
 */
metadata {
	definition (name: "Radiant Control", namespace: "slouslik", author: "Adel Sarhan") {
        capability "Refresh"

        command "raiseFloorSetpoint"
		command "lowerFloorSetpoint"
		command "raiseMixSetpoint"
		command "lowerMixSetpoint"

		attribute "temperature", "number"
		attribute "floorSetpoint", "number"
		attribute "floorTemp", "number"
		attribute "mixSetpoint", "number"
		attribute "mixTemp", "number"
        attribute "mixValvePosition", "number"
	}

	tiles(scale:2) {
		multiAttributeTile(name:"temperature", type:"generic", width: 6, height: 4, decoration: "flat") {
        	tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("temperature", label:'${currentValue}°', unit:"F", 
					backgroundColors:[
							// Fahrenheit
							[value: 40, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					]
				)
        	}
            tileAttribute("device.temperature", key: "SECONDARY_CONTROL") {
				attributeState "default", label:'Bathroom Temperature', backgroundColor:"#79b821", defaultState: true
			}
		}
		multiAttributeTile(name:"floorTemp", type:"generic", width:6, height:4, decoration: "flat") {
			tileAttribute("device.floorTemp", key: "PRIMARY_CONTROL") {
				attributeState("floorTemp", label:'${currentValue}°', unit:"F",
					backgroundColors:[
							// Fahrenheit
							[value: 40, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					]
				)
			}
            tileAttribute("device.floorSetpoint", key: "SECONDARY_CONTROL") {
				attributeState "floorSetpoint", label:'Floor setpoint: ${currentValue}°' , backgroundColor:"#79b821", defaultState: true
			}
			tileAttribute("device.floorSetpoint", key: "VALUE_CONTROL") {
		 		attributeState "VALUE_UP", action: "raiseFloorSetpoint"
		 		attributeState "VALUE_DOWN", action: "lowerFloorSetpoint"
			}
		}
		multiAttributeTile(name:"mixTemp", type:"generic", width:6, height:4, decoration: "flat") {
			tileAttribute("device.mixTemp", key: "PRIMARY_CONTROL") {
				attributeState("mixTemp", label:'${currentValue}°', unit:"F",
					backgroundColors:[
							// Fahrenheit
							[value: 40, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					]
				)
			}
            tileAttribute("device.mixSetpoint", key: "SECONDARY_CONTROL") {
				attributeState "mixSetpoint", label:'Mixing valve setpoint: ${currentValue}°' , backgroundColor:"#79b821", defaultState: true
			}
			tileAttribute("device.mixSetpoint", key: "VALUE_CONTROL") {
				attributeState "VALUE_UP", action: "raiseMixSetpoint"
				attributeState "VALUE_DOWN", action: "lowerMixSetpoint"
			}
		}
     	valueTile("mixValvePosition", "device.mixValvePosition", width: 4, height: 2) {
        	state "mixValvePosition", label:'Mix Valve Position\n${currentValue}%', defaultState: true
    	}
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
		main "temperature"
		details(["temperature", "floorTemp", "mixTemp", "mixValvePosition", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    def msg = zigbee.parse(description)?.text
    log.debug "Parse got '${msg}'"

    def parts = msg.split(" ")
    def name  = parts.length>0?parts[0].trim():null
    def value = parts.length>1?parts[1].trim():null

    name = value != "ping" ? name : null

    def result = createEvent(name: name, value: value)
    log.debug result
    return result
}

def setFloorSetpoint(degreesF) {
	log.debug "floorSetpoint: ${degreesF}"
	sendEvent(name:"floorSetpoint", value: degreesF)
}

def setMixSetpoint(degreesF) {
	log.debug "mixSetpoint: ${degreesF}"
	sendEvent(name:"mixSetpoint", value: degreesF)
}

def raiseFloorSetpoint(Map results) {
	def floorSetpoint = device.latestValue("floorSetpoint") as Integer ?: 0
	if (floorSetpoint < 85) {
		def setpoint = floorSetpoint + 1
        setFloorSetpoint(setpoint)
    	zigbee.smartShield(text: "floorSetpoint ${setpoint}").format()
	}
}

def lowerFloorSetpoint(Map results) {
	def floorSetpoint = device.latestValue("floorSetpoint") as Integer ?: 0
	if (floorSetpoint > 60) {
		def setpoint = floorSetpoint - 1
		setFloorSetpoint(setpoint)
        zigbee.smartShield(text: "floorSetpoint ${setpoint}").format()
	}
}

def raiseMixSetpoint(Map results) {
	def mixSetpoint = device.latestValue("mixSetpoint") as Integer ?: 0
	if (mixSetpoint < 145) {
		def setpoint = mixSetpoint + 1
		setMixSetpoint(setpoint)
        zigbee.smartShield(text: "mixSetpoint ${setpoint}").format()
	}
}

def lowerMixSetpoint(Map results) {
	def mixSetpoint = device.latestValue("mixSetpoint") as Integer ?: 0
	if (mixSetpoint > 80) {
		def setpoint = mixSetpoint - 1
		setMixSetpoint(setpoint)
        zigbee.smartShield(text: "mixSetpoint ${setpoint}").format()
	}
}

def refresh() {
	zigbee.smartShield(text: "refresh").format()
}
