/**
 *  Copyright 2015 SmartThings
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
 *	Author: SmartThings
 *	Date: 2013-06-13
 */
metadata {
	definition (name: "Radiant Control", namespace: "asarhan", author: "Adel Sarhan") {
		capability "Temperature Measurement"
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
	}

	tiles(scale:2) {
		valueTile("temperature", "device.temperature", width: 6, height: 4) {
			state("temperature", label:'${currentValue}°', unit:"F",
					backgroundColors:[
							// Celsius
							[value: 0, color: "#153591"],
							[value: 7, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 23, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 35, color: "#d04e00"],
							[value: 37, color: "#bc2323"],
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
		multiAttributeTile(name:"floorTemp", type:"generic", width:6, height:4) {
			tileAttribute("device.floorTemp", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue}°', unit:"F",
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
			tileAttribute("device.floorSetpoint", key: "VALUE_CONTROL") {
		 		attributeState "VALUE_UP", action: "raiseFloorSetpoint"
		 		attributeState "VALUE_DOWN", action: "lowerFloorSetpoint"
			}
		}
		multiAttributeTile(name:"mixTemp", type:"generic", width:6, height:4) {
			tileAttribute("device.mixTemp", key: "PRIMARY_CONTROL") {
				attributeState("default", label:'${currentValue}°', unit:"F",
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
			tileAttribute("device.mixSetpoint", key: "VALUE_CONTROL") {
				attributeState "VALUE_UP", action: "raiseMixSetpoint"
				attributeState "VALUE_DOWN", action: "lowerMixSetpoint"
			}
		}
		main "temperature"
		details(["temperature", "floorTemp", "mixTemp"])
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
		log.debug "floorSetpoint: ${degreesF}, this"
		sendEvent(name: "floorSetpoint", value: degreesF)
}

def setMixSetpoint(degreesF) {
		log.debug "mixSetpoint: ${degreesF}, this"
		sendEvent(name: "mixSetpoint", value: degreesF)
}

def raiseFloorSetpoint(Map results) {
	def floorSetpoint = device.latestValue("floorSetpoint") as Integer ?: 0
	if (floorSetpoint < 85) {
		floorSetpoint = floorSetpoint + 1
	}
	setFloorSetpoint(floorSetpoint)
}

def lowerFloorSetpoint(Map results) {
	def floorSetpoint = device.latestValue("floorSetpoint") as Integer ?: 0
	if (floorSetpoint > 60) {
		floorSetpoint = floorSetpoint - 1
	}
	setFloorSetpoint(floorSetpoint)
}

def raiseMixSetpoint(Map results) {
	def mixSetpoint = device.latestValue("mixSetpoint") as Integer ?: 0
	if (mixSetpoint < 145) {
		mixSetpoint = mixSetpoint + 1
	}
	setMixSetpoint(mixSetpoint)
}

def lowerMixSetpoint(Map results) {
	def mixSetpoint = device.latestValue("mixSetpoint") as Integer ?: 0
	if (mixSetpoint > 80) {
		mixSetpoint = mixSetpoint - 1
	}
	setMixSetpoint(mixSetpoint)
}
