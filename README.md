# 🚨 Hear me System

### 🎯 Goal of the Project

The purpose of the Hear Me System is to enhance safety for individuals with hearing impairments by providing non-auditory alerts for dangerous situations such as smoke or gas leaks. 
Traditional alarm systems rely on sound, making them ineffective for people who are deaf or hard of hearing. This project solves that problem by using visual (LED) and tactile (vibration) alerts, alongside real-time notifications via a web interface.

--- 

### 🌟 Benefits

- 👂 Improved emergency awareness for the hearing-impaired community

- 🌐 Real-time monitoring and remote alerting via WebSocket

- 🏠 Increased household safety using reliable sensors

- 🔌 Simple integration with IoT devices like the Wio Terminal

---

### 🧪 Sensors Used

**Supported sensors include:**

<br><br>

## 🔌 Hardware Components

<br><br>

### 📟 Wio Terminal

* **Name:** Wio Terminal – ATSAMD51 Core with Realtek RTL8720DN
* **Type:** All-in-one IoT Development Board
* **Application:** Embedded systems, IoT projects, edge computing
* **Core Processor:** ARM Cortex-M4F (ATSAMD51P19)
* **Wireless Connectivity:** Wi-Fi 2.4GHz/5GHz & Bluetooth 5.0 (Realtek RTL8720DN)
* **Display:** 2.4" 320x240 IPS LCD
* **Built-in Sensors & Features:**

  * 🌞 Light sensor

  * 🎤 Microphone

  * 🔔 Buzzer

  * 📊 3-axis Accelerometer (LIS3DHTR)

  * 📡 Infrared emitter

  * 🔌 Grove connectors (for sensor expansion)

  * 💾 microSD card slot
* **Compatibility:** Arduino, MicroPython, CircuitPython
* **Ports:** USB-C, 40-pin GPIO, Grove x2
* **Power Supply:** 5V via USB-C
* **Additional Features:** Programmable buttons, onboard RTC, RTC backup battery holder
* **Link:** [Get Started with Wio Terminal | Seeed Studio Wiki](https://wiki.seeedstudio.com/Wio-Terminal-Getting-Started/)

---

### 🔥 Smoke Sensor

* **Name:** Ximimark MQ-135 Air Quality Sensor
* **Type:** Hazardous Gas Detection Module
* **Application:** Detects smoke, CO₂, and other gases
* **Compatibility:** Arduino and similar microcontrollers
* **Link:** [MQ135 Air Quality Sensor Datasheet : Working & Its Applications](https://components101.com/sensors/mq135-gas-sensor)

---

### 💥 Vibration Sensor

* **Model:** Grove - Vibration Motor (GD)
* **Specifications:**

  * ⚡ **Operating Voltage:** 3.0V to 5.5V
  * 🎛️ **Control Mode:** Digital logic level (ON when HIGH, OFF when LOW)
  * 🔄 **Rated Speed:** \~9000 RPM
  * 📏 **Dimensions:** 24mm × 20mm × 9.8mm
  * 📦 **Weight:** 9g
  * 🔌 **Interface:** Grove connector (4-pin: VCC, GND, SIG, NC)
  * 🔋 **Power Consumption:** Low; suitable for battery-powered applications
  * 🛠️ **Built-in Driver:** Includes transistor for direct microcontroller control
* **Application:** Provides tactile vibration alerts
* **Link:** [Grove - Vibration Motor | Seeed Studio](https://wiki.seeedstudio.com/Grove-Vibration_Motor/)

---

### 💡 LED (Visual Indicator)

* **Type:** Grove - Red LED Button (SKU: 111020044)
* **Specifications:**

  * ⚡ **Operating Voltage:** 3.3V / 5V
  * 🔌**Interface:** Grove 4-pin connector (VCC, GND, SIG1, SIG2)
  * 🕹️ **LED Control:** N-Channel MOSFET for efficient switching
  * ⏳ **Button Lifespan:** >100,000 presses
  * 📏 **Dimensions:** 40mm × 20mm × 16mm
  * 📦 **Weight:** \~10.3g
  * 🔧 **Press Resistance:** <100mΩ
* **Application:** Visual alerts and manual input
* **Color:** 🔴 Red
* **Link:** [Grove - LED Button | Seeed Studio Wiki](https://wiki.seeedstudio.com/Grove-LED_Button/)

---

## 🔧 Program Requirements 
<br>
This section explains the tools, software, and hardware needed to fully set up the HearMe system. 
You’ll learn how to connect sensors to the Wio Terminal, prepare the backend server, and run the web interface to monitor sensor data in real time.

<br><br>

### 1. 🖥️ Wio Terminal Setup (C++ / Arduino)

To enable the Wio Terminal to read gas sensor data and communicate via MQTT:

#### ✅ Prerequisites:
- **Arduino IDE**: Download version **2.3.6** from the [official Arduino site](https://www.arduino.cc/en/software).
- **USB Cable**: Use a **data-capable USB** cable to connect the Wio Terminal to your PC.

#### ⚙️ Arduino Configuration:
- Open the Arduino IDE.
- **Select the correct board**:
  - Tools → Board → **Seeed SAMD Boards** → *Seeeduino Wio Terminal*
- **Select the correct port**:
  - Tools → Port → *e.g., COM3* (matching your Wio Terminal)

#### 📦 Install Required Libraries:
Install the following libraries via the Arduino Library Manager:

- 📶 **rpcWiFi** (by Seeed Studio) – for `rpcWiFi.h`
- 📨 **PubSubClient** (by Nick O'Leary) – for MQTT communication (`PubSubClient.h`)

#### 📡 Functionality:
The Arduino code reads analog values from an **MQ-2 gas sensor** and sends alerts via MQTT when:
- 🚨 Smoke is detected
- ⚠️ The sensor is disconnected or malfunctioning

---

### 2. 🔗 WebSocket Server (Java Backend)

The WebSocket server is implemented in Java and facilitates communication between the backend and the frontend via the WebSocket protocol.

#### ✅ Requirements:

* ☕ **Java 17**: Ensure you are using Java 17 for the backend.
* 🌐 **WebSocket Library**: The backend uses Java’s built-in `WebSocket` library or another WebSocket library for real-time communication.
* 📦 **Maven**: You can manage dependencies with Maven for simplicity.

#### 🧩 WebSocket Dependencies:

If you are using Maven to manage dependencies, include the WebSocket API dependency in your `pom.xml`:

```xml
<!-- If using Maven -->
<dependency>
  <groupId>org.java-websocket</groupId>
  <artifactId>Java-WebSocket</artifactId>
  <version>1.5.2</version>
</dependency>
```

Alternatively, you can manually include the `Java-WebSocket` library, available for download from [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket).

#### 📡 Functionality:

* The backend communicates over WebSocket with the frontend, sending updates whenever sensor data changes.
* Sensor data, status, and alerts are broadcasted to connected WebSocket clients.
* If the Wio Terminal is disconnected, a "Device is not connected to Network" message is broadcasted.
* If the gas sensor disconnects or is malfunctioning, a "GAS SENSOR DISCONNECTED OR NOT CONNECTED PROPERLY" message is broadcasted.

---

### 3. 🖥️ Frontend (Vue.js)

The frontend (built with Vue.js) listens for messages from the backend via WebSocket and displays real-time updates to the user interface.

#### ✅ Requirements:

* 🟢 **Node.js v22.14.0**
* ⚙️ **Vue CLI v5.0.8**

#### 🛠️ Setup:

1. **Install dependencies**:

```bash
npm install
```
2. **Install additional library for PDF export**

```bash
npm install jspdf
```
3. **Install  library for Multil-language**
```bash
npm install vue-i18n@9
```
4. **Run the development server**:

```bash
npm run serve
npm run dev 
```
> Note: The development server command defined in the package.json scripts is serve, not dev.
- serve is the default Vue CLI command to start the development server locally on your machine.
- dev is sometimes used as an alias for the development server, but this project does not.

<br>

> Note: The Vue CLI project can become large (up to 100MB) due to dependencies. If you're using Git, you may want to exclude `node_modules/` from version control by adding it to `.gitignore` and running `npm install` during deployment.

<br>

#### 🌐 Functionality:

* The frontend subscribes to WebSocket messages from the Java backend.
* It updates the user interface dynamically based on the data received, showing alerts and status messages, such as:

  * ✅ "SENSOR CONNECTED AND READY"
  * ⚠️ "GAS SENSOR DISCONNECTED OR NOT CONNECTED PROPERLY"
  * ❌ "Device is not connected to Network"

---

### 4. ⚡ WebSocket Communication Protocol

#### Messages Sent to Frontend:

The backend (Java) sends messages to the frontend over WebSocket whenever there is a change in sensor data, status, or when specific events (e.g., disconnections) occur. The WebSocket API supports the following types of messages:

* 🔵 **Sensor Status**: `"SENSOR CONNECTED AND READY"`, `"GAS SENSOR DISCONNECTED OR NOT CONNECTED PROPERLY"`
* ⏳ **Network Timeout**: `"Device is not connected to Network"`

---

### 5. 🔧 Testing and Debugging

To ensure smooth communication between the backend and frontend, verify the following:

* ✅ **Backend**: The WebSocket server should continuously broadcast status messages.
* ✅ **Frontend**: Ensure the WebSocket client is connected and receiving updates in real-time.

You can monitor the WebSocket traffic via the browser’s developer tools or use tools like Postman or a WebSocket client to manually test the connection and message flow.

---

**🛠️ Final Setup and Running Instructions**

_- Connect hardware carefully:_

  - Attach the MQ-2 gas sensor and vibration/light modules to the Wio Terminal following the official wiring diagram.

  - Double-check all connections to ensure sensor inputs are correctly wired.

_- Prepare the Arduino sketch:_

  - Open the sketch_apr16b file in Arduino IDE.

  - Connect the Wio Terminal to your Wi-Fi network through the sketch.

_- Verify connections:_

  - Open the Serial Monitor in Arduino IDE.

  - Confirm the Wio Terminal successfully connects to both the Wi-Fi network and the MQTT broker.

_- Run the frontend:_

  - Open a terminal, navigate to the hear-me-app frontend directory.

  - Run npm run serve.

  - Open the displayed localhost URL in your browser to access the HearMe web interface.

_- Test the system:_

  - Check sensor status messages on the web interface.

  - Test the gas and vibration sensors as described in the User Manual.

---

### 📖 User Manual

This section guides you on how to use the HearMe system. You can view sensor status and alerts on the web page and quickly request emergency help when needed. 
The system is designed especially to assist deaf users in staying aware of environmental dangers easily.



Welcome to HearMe — a system designed to help deaf individuals detect gas, smoke, and vibrations, and request emergency help quickly.

### 🧪 What You Can Do


- 👀 See sensor status:
  The interface will show messages like:

  - ✅ SENSOR CONNECTED AND READY

  - ⚠️ GAS SENSOR DISCONNECTED OR NOT CONNECTED PROPERLY

  - ❌ Device is not connected to Network

- 🔧 Test sensors:

  - Bring gas near the MQ-2 gas sensor to check smoke detection.

  - Tap the Test Vibration Sensor to test vibration alerts and LED.

- 🚨 Request help fast:

  - Press the Emergency Help Request button in the app.

  - Your location (based on your IP) will be sent automatically to emergency services.

---

### 👥 Team Contributions

Member 1: **Abulfazl**

**Role:** Developer

**Contributions:**

•	🗂️ Initialized and maintained the Git repository to kick-start the project.   
•	🤖 Configured and maintained GitLab CI pipelines for automated builds and Maven-based test verification.    
•	🧩 Added gas sensor connectivity and disconnection handling.   
•	🔄 Ensured reliable background processing for uninterrupted operation.   
•	⏱️ Enabled 24/7 system availability with stable uptime.   
•	📬 Integrated email notifications for system events.   
•	📍 Revised location-sharing format for full system compatibility.   
•	⚙️ Built backend logic in Java (MQTT, WebSocket API).   
• 🔄 Refactored system to move processing from device to backend for better reliability.   
• 🧪 Created unit tests to ensure backend functionality.   

---

Member 2: **Sara**

**Role:** Developer

**Contributions:**
- Implemented backend-to-frontend message communication through MessageBridge class
- Developed bold, flashing smoke warning screen
- Synchronized backend-triggered vibration and LED blinking with frontend warning display
- Enabled email-based help requests with IP-based location data
- Designed WebSocket message handling system and implemented MotorTestHandler

---

Member 3: **Valentina**

**Role:** Developer

**Contributions:**
- Implemented and tested UI state feedback for smoke detection
- Ensured visual/functional consistency across states
- Developed and validated vibration device testing feature
- Enabled log download functionality for fire event

---

Member 4: **Juliana**

**Role:** Developer

**Contributions:**      
-🎨 Developed the initial Vue.js app interface.   
-💬 Created a modal to show fire event logs and a button to clear event history.   
-🕒 Added timestamps to each recorded event.    
-📍 Implemented user geolocation for emergency messages.   
-📝 Enhanced PDF layout for better readability.   


---

Member 5: **Sepehr**

**Role:** Developer
**Contributions:**

-	🖼️ Integrated the main background and styling into the GUI, hosted via Amazon AWS for enhanced accessibility and performance.
-	🎨 Polished the GUI by enhancing button font colors and improving overall text readability for better user experience.
-	🧱 Designed and implemented the main frame structure of the web-based GUI for a clear and responsive user interface.
-	🌐 contributing to developing and adding a WebSocket server in Java to enable real-time backend-to-frontend communication.	
-	📳 Implemented real-time message reception from the backend to control the vibration motor through the Java application.
-	🌍 Added multilingual support, including English and Swedish translations, to improve user experience and inclusivity.

---

### ✨🎥✨ Demo Video

Watch our project in action here:  [(https://drive.google.com/file/d/1kQC34i6iIblojTv3UBgFvI440Nwxtmxo/view?usp=sharing)]


