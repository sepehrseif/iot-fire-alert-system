#include <rpcWiFi.h>
#include <PubSubClient.h>

// Wi-Fi credentials, Should be updated based on new wifi
const char* ssid = "Tele2_23D296";
const char* password = "5662pgs2";

// MQTT broker settings
const char* mqtt_server = "broker.hivemq.com"; 
const int mqtt_port = 1883;

WiFiClient wifiClient;
PubSubClient client(wifiClient);

// Pin definitions
const int gasSensorPin = A0;
const int motorPin = D3;
const int dangerLEDPin = D5;

// Blinking control variables
bool isActivated = false;
bool blinkState = false;
unsigned long lastBlinkTime = 0;
const unsigned long blinkInterval = 1000; // ms

void setup() {
  Serial.begin(115200);
  
  pinMode(gasSensorPin, INPUT);
  pinMode(motorPin, OUTPUT);
  pinMode(dangerLEDPin, OUTPUT);
  
  digitalWrite(motorPin, LOW);
  digitalWrite(dangerLEDPin, LOW);

  connectToWiFi();
  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(mqttCallback);
  connectToMQTT();
}

void loop() {
  if (!client.connected()) {
    connectToMQTT();
  }
  client.loop();

  // Read and publish gas sensor value
  int gasValue = analogRead(gasSensorPin);
  Serial.print("Gas sensor value: ");
  Serial.println(gasValue);
  
  char payload[10];
  snprintf(payload, sizeof(payload), "%d", gasValue);
  client.publish("smoke_alert", payload);

  // Handle blinking if activated
  if (isActivated) {
    unsigned long currentTime = millis();
    if (currentTime - lastBlinkTime >= blinkInterval) {
      blinkState = !blinkState;
      digitalWrite(dangerLEDPin, blinkState ? HIGH : LOW);
      digitalWrite(motorPin, blinkState ? HIGH : LOW);
      lastBlinkTime = currentTime;
    }
  }

  delay(100); // Slight delay for stability
}

void connectToWiFi() {
  Serial.print("Connecting to WiFi...");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nConnected to WiFi.");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
}

void connectToMQTT() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    String clientId = "WioTerminal-";
    clientId += String(random(0, 65535), HEX);
    if (client.connect(clientId.c_str())) {
      Serial.println("connected to MQTT!");
      client.subscribe("motor_control");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(". Trying again in 5 seconds.");
      delay(5000);
    }
  }
}

void mqttCallback(char* topic, byte* payload, unsigned int length) {
  String message = "";
  for (unsigned int i = 0; i < length; i++) {
    message += (char)payload[i];
  }

  Serial.print("Received message: ");
  Serial.println(message);

  if (message == "ACTIVATE") {
    isActivated = true;
    Serial.println("ACTIVATED - Blinking and vibration started");
  } else if (message == "STOP") {
    isActivated = false;
    digitalWrite(dangerLEDPin, LOW);
    digitalWrite(motorPin, LOW);
    Serial.println("STOPPED - Blinking and vibration stopped");
  } else {
    Serial.println("Unexpected message received.");
  }
}