#include <Arduino.h>
#include <EEPROM.h>

#include <SmartThings.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include <RadiantControl.h>

// Data wire is plugged into pin 8 on the Arduino
#define ONE_WIRE_BUS 8
#define PIN_THING_RX 10
#define PIN_THING_TX 2

// Mixing Valve pins
int mixValvePower = 7;  // the pin to turn on the mixing valve
int mixValve = 13;      // the pin for the mixing valve adjustment
int pumpPower = 10;     // pin to turn on circuit pump

// Setup a oneWire instance to communicate with any OneWire devices
OneWire oneWire(ONE_WIRE_BUS);

// Pass our oneWire reference to Dallas Temperature.
DallasTemperature sensors(&oneWire);

DeviceAddress mixTempSensor = { 0x28, 0xFF, 0x7C, 0x5A, 0x15, 0x14, 0x00, 0x6F };
DeviceAddress floorTempSensor = { 0x28, 0xFF, 0x46, 0xD2, 0x15, 0x14, 0x00, 0x93 };
byte mixSetpoint = 120;
byte floorSetpoint = 80;
int mixSTaddr = 0;
int floorSTaddr = 1;
int temperature = 0;
int floorTemp = 0;
int mixTemp = 0;
bool dirty = false;

SmartThingsCallout_t messageCallout;    // call out function forward decalaration
SmartThings smartthing(PIN_THING_RX, PIN_THING_TX, messageCallout);  // constructor

bool isDebugEnabled;    // enable or disable debug in this example
int stateNetwork;       // state of the network

//*****************************************************************************
void setNetworkStateLED()
{
  SmartThingsNetworkState_t tempState = smartthing.shieldGetLastNetworkState();
  if (tempState != stateNetwork)
  {
    switch (tempState)
    {
      case STATE_NO_NETWORK:
        if (isDebugEnabled) Serial.println("NO_NETWORK");
        smartthing.shieldSetLED(2, 0, 0); // red
        break;
      case STATE_JOINING:
        if (isDebugEnabled) Serial.println("JOINING");
        smartthing.shieldSetLED(2, 0, 0); // red
        break;
      case STATE_JOINED:
        if (isDebugEnabled) Serial.println("JOINED");
        smartthing.shieldSetLED(0, 0, 0); // off
        break;
      case STATE_JOINED_NOPARENT:
        if (isDebugEnabled) Serial.println("JOINED_NOPARENT");
        smartthing.shieldSetLED(2, 0, 2); // purple
        break;
      case STATE_LEAVING:
        if (isDebugEnabled) Serial.println("LEAVING");
        smartthing.shieldSetLED(2, 0, 0); // red
        break;
      default:
      case STATE_UNKNOWN:
        if (isDebugEnabled) Serial.println("UNKNOWN");
        smartthing.shieldSetLED(0, 2, 0); // green
        break;
    }
    stateNetwork = tempState;
  }
}

void initSetpoints()
{
  byte value = EEPROM.read(mixSTaddr);
  if (value == 0) {
    EEPROM.write(mixSTaddr, mixSetpoint);
  }
  else {
    mixSetpoint = value;
  }
  Serial.print("Mix Temp Setpoint = ");
  Serial.print(mixSetpoint);

  value = EEPROM.read(floorSTaddr);
  if (value == 0) {
    EEPROM.write(floorSTaddr, floorSetpoint);
  }
  else {
    floorSetpoint = value;
  }
  Serial.print("\n\rFloor Temp Setpoint = ");
  Serial.print(floorSetpoint);
  Serial.print("\n\r");
  dirty = true;
}

void updateTemps()
{
  sensors.requestTemperatures();
  int temp = sensors.getTempF(mixTempSensor);
  if (temp != mixTemp) {
    dirty = true;
    mixTemp = temp;
    Serial.print("Mix Temp = ");
    Serial.print(mixTemp);
    Serial.print("\n\r");
  }
  temp = sensors.getTempF(floorTempSensor);
  if (temp != floorTemp) {
    dirty = true;
    floorTemp = temp;
    Serial.print("Floor Temp = ");
    Serial.print(floorTemp);
    Serial.print("\n\r");
    temperature = floorTemp; // temporary value until another sensor is installed
  }
}

void updateSmartthings()
{
  if (stateNetwork != STATE_JOINED)
    return;

  if (dirty) {
    smartthing.send("temperature " + String(temperature));
    smartthing.send("mixTemp " + String(mixTemp));
    smartthing.send("floorTemp " + String(floorTemp));
    smartthing.send("mixSetpoint " + String(mixSetpoint));
    smartthing.send("floorSetpoint " + String(floorSetpoint));
    dirty = false;
  }
}

void setup(void)
{
  // setup default state of global variables
  isDebugEnabled = true;
  stateNetwork = STATE_JOINED;  // set to joined to keep state off if off

  // start serial port
  Serial.begin(9600);
  // this check is only needed on the Leonardo:
  while (!Serial) {
    ;   // wait for serial port to connect. Needed for Leonardo only
  }

  // Start up the library
  sensors.begin();
  // set the resolution to 10 bit (good enough?)
  sensors.setResolution(mixTempSensor, 10);
  sensors.setResolution(floorTempSensor, 10);
  initSetpoints();
  updateTemps();

  // declare mixValve to be an output:
  pinMode(mixValvePower, OUTPUT);
  pinMode(mixValve, OUTPUT);
  analogWrite(mixValve, MV_50PERCENT);
  digitalWrite(mixValvePower, LOW);
  digitalWrite(pumpPower, LOW);
}

void loop(void)
{
  delay(5000);

  // run smartthing logic
  smartthing.run();
  setNetworkStateLED();
  updateTemps();
  updateSmartthings();
}

//*****************************************************************************
void messageCallout(String message)
{
  // if debug is enabled print out the received message
  if (isDebugEnabled)
  {
    Serial.print("Received message: '");
    Serial.print(message);
    Serial.println("' ");
  }
}
