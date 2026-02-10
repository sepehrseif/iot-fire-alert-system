
<template>
  <div :class="{ 'smoke-detected': airQualityStatus === 'dangerous', 'safe-tint': airQualityStatus === 'safe' }" class="app">
    <div class="language-switcher">
      <!-- Language switch buttons -->
      <button @click="switchLanguage('en')">{{ t('english') }}</button>
      <button @click="switchLanguage('sv')">{{ t('swedish') }}</button>
    </div>
    <!-- Smoke Detected Message -->
    <div v-if="airQualityStatus === 'dangerous'" class="fullscreen-alert">
      <div class="smoke-warning">
        <span class="warning-icon">🚨</span> {{ t('smokeDetected') }} <span class="warning-icon">🚨</span>
      </div>
    </div>
    
    <!-- Status Box -->
    <div class="status-box">
      <h1>{{ t('sensorStatus') }}</h1>

      <div v-if="status === 'connected'" class="ok">
        {{ t('sensorConnected') }}
      </div>
      <div v-else-if="status === 'disconnected'" class="alert">
        ⚠️ {{ t('sensorNotConnected') }}
      </div>
      <div v-else-if="status === 'waiting'" class="waiting">
        {{ t('DeviceNotConnected') }}      </div>

      <div v-if="rawValue !== null" class="raw">
        {{ t('sensorReading') }}: {{ rawValue }}
      </div>

      <!-- Environment Text (Safe) -->
      <div v-if="airQualityStatus !== 'dangerous'" class="environment">
        <span class="environment-text">
          {{ t('environment') }}: 
        </span>
        
        <span v-if="airQualityStatus === 'safe' && status === 'connected'" class="safe">
          {{ t('safe') }}
        </span>

        <span v-else-if="status === 'disconnected' || status === 'waiting'" class="unknown">
          {{ t('Unknown') }}
        </span>
      </div>


      <!-- Action Buttons -->
      <div class="button-row">
        <button @click="runMotorTest" class="test-button">
          {{ t('testVibrationMotor') }}
        </button>

        <button @click="viewPreviousFires" class="view-fires-button">
          {{ t('viewPreviousFires') }}
        </button>

        <button @click="sendEmergencyHelpRequest" class="emergency-help-request-button">
          {{ t('sendEmergencyHelpRequest') }}
        </button>
      </div>
    </div>

    <!-- Modal for Previous Fires -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <h2>{{ t('viewPreviousFires') }}</h2>

        <!-- Fire event list -->
        <ul v-if="fireEvents.length > 0">
          <li v-for="(event, index) in fireEvents" :key="index">{{ event }}</li>
        </ul>
        <p v-else>{{ t('noFireEvents') }}</p>

        <!-- Modal buttons -->
        <button @click="downloadPDF" class="download-pdf-button">
          {{ t('downloadPDF') }}
        </button>

        <button @click="clearHistory" class="clear-history-button">
          {{ t('clearHistory') }}
        </button>

        <button @click="closeModal" class="close-modal">{{ t('close') }}</button>
      </div>
    </div>

    <!-- Modal: emergency request confirmation -->
    <div v-if="showEmergencyScreen" class="modal-overlay" @click="closeEmergencyScreen">
      <div class="modal-content" @click.stop>
        <h2>{{ t('helpRequestSent') }}</h2>
        <p>{{ t('Emergency_services_notified') }}</p>
        <button @click="closeEmergencyScreen" class="close-modal">{{ t('close') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { jsPDF } from 'jspdf';
import { useI18n } from 'vue-i18n'
import Logo3 from './assets/Logo3.png'

const { t, locale } = useI18n()
const status = ref('waiting');
const rawValue = ref(null);
const airQualityStatus = ref('safe');
const showModal = ref(false);
const fireEvents = ref(JSON.parse(localStorage.getItem('fireEvents') || '[]'));
const showEmergencyScreen = ref(false);

// Function to switch languages
function switchLanguage(language) {
locale.value = language
}
let socket;
let reconnectInterval;
let lastActiveTime = Date.now();

function updateStatusSafe() {
  status.value = 'connected';
  lastActiveTime = Date.now();
}

// Sends help request to backend over WebSocket
function sendEmergencyHelpRequest() {
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send("Send-Help-Request");
    console.log("Help request sent.");
  } else {
    console.error("WebSocket not connected.");
  }

  showEmergencyScreen.value = true;
}

function closeEmergencyScreen() {
  showEmergencyScreen.value = false;
}

// Establish WebSocket and handle reconnection
function connectWebSocket() {
  socket = new WebSocket('ws://localhost:8080');

  socket.onopen = () => {
    console.log('✅ WebSocket connected');
    status.value = 'connected';
    if (reconnectInterval) {
      clearInterval(reconnectInterval);
      reconnectInterval = null;
    }
  };

  socket.onmessage = (event) => {
    const message = event.data;
    console.log('📩 WebSocket message:', message);

    const now = Date.now();
    const maybeNumber = parseInt(message);
    if (!isNaN(maybeNumber)) {
      rawValue.value = maybeNumber;
      updateStatusSafe();
      return;
    }

// Handle different message types from backend
    switch (message) {
      case 'SENSOR CONNECTED AND READY':
      case 'Sensor connected':
        updateStatusSafe();
        break;
      case 'SAFE GAS LEVEL':
        airQualityStatus.value = 'safe';
        updateStatusSafe();
        break;
      case 'DANGEROUS GAS LEVEL': {
        airQualityStatus.value = 'dangerous';
        updateStatusSafe();
        const timestamp = new Date().toLocaleString();
        const message = locale.value === 'sv' 
          ? `Brand upptäckt kl${timestamp}`
          : `${t('fireDetected')}${timestamp}`;
        fireEvents.value.push(message);
        localStorage.setItem('fireEvents', JSON.stringify(fireEvents.value));
        break;
      }
      case 'GAS SENSOR DISCONNECTED OR NOT CONNECTED PROPERLY':
        status.value = 'disconnected';
        break;

      case 'Device is not connected to Network':
        if (now - lastActiveTime > 5000) {
          status.value = message.includes('Network') ? 'waiting' : 'disconnected';
        } else {
          console.log('🔁 Ignoring stale disconnect/wait message');
        }
        break;
      default:
        console.warn('Unknown message:', message);
    }
  };

  socket.onclose = () => {
    console.warn('⚠️ WebSocket closed. Attempting to reconnect...');
    status.value = 'waiting';
    if (!reconnectInterval) {
      reconnectInterval = setInterval(() => {
        console.log('🔄 Attempting WebSocket reconnect...');
        connectWebSocket();
      }, 3000);
    }
  };

  socket.onerror = (error) => {
    console.error('WebSocket error:', error);
    socket.close();
  };
}
// Send a test message to the motor

function runMotorTest() {             
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send('MOTOR TEST');
    console.log('🔧 Sent MOTOR TEST');
  } else {
    console.warn('❌ WebSocket is not connected');
    alert('WebSocket is not connected! Please try again later.');
  }
}

function viewPreviousFires() {
  // Translate existing events if in Swedish
  if (locale.value === 'sv') {
    fireEvents.value = fireEvents.value.map(event => {
      if (event.startsWith('Fire detected at')) {
        const timestamp = event.replace('Fire detected at', '').trim();
        return `Brand upptäckt kl${timestamp}`;
      }
      return event;
    });
  }
  showModal.value = true;
}

function clearHistory() {
  fireEvents.value = [];
  localStorage.setItem('fireEvents', JSON.stringify(fireEvents.value));
  console.log('🧹 Fire history cleared');
}

function closeModal() {
  showModal.value = false;
}

// Generates and downloads a styled PDF report of fire events
function downloadPDF() {
  if (fireEvents.value.length === 0) {
    alert(locale.value === 'sv' ? 'Inga brandhändelser att ladda ner.' : 'No fire events to download.');
    return;
  }

  const doc = new jsPDF();
  const pageWidth = doc.internal.pageSize.getWidth();
  const pageHeight = doc.internal.pageSize.getHeight();
  
  // Color palette matching the main system
  const colors = {
    background: [240, 240, 240],  // Light gray background
    accent: [186, 14, 14],        // Red accent color (matching the system's red)
    text: [60, 60, 60],           // Dark gray text
    border: [200, 200, 200]       // Light gray border
  };

  // Translations for PDF content
  const translations = {
    en: {
      title: 'Fire Detection History',
      systemName: 'Hear Me - Fire Detection System',
      page: 'Page',
      of: 'of',
      generatedOn: 'Generated on'
    },
    sv: {
      title: 'Branddetekteringshistorik',
      systemName: 'Hear Me - Branddetekteringssystem',
      page: 'Sida',
      of: 'av',
      generatedOn: 'Genererad den'
    }
  };

  const t = translations[locale.value];

  // Function to add header and footer to each page
  function addPageElements(pageNumber, totalPages) {
    // Add background
    doc.setFillColor(...colors.background);
    doc.rect(0, 0, pageWidth, pageHeight, 'F');
    
    // Add decorative border
    doc.setDrawColor(...colors.border);
    doc.setLineWidth(2);
    doc.rect(15, 15, pageWidth - 30, pageHeight - 30);

    // Add footer
    doc.setFontSize(10);
    doc.setTextColor(...colors.text);
    doc.text(t.systemName, pageWidth/2, pageHeight - 30, { align: 'center' });
    doc.text(`${t.page} ${pageNumber} ${t.of} ${totalPages}`, pageWidth/2, pageHeight - 20, { align: 'center' });
  }

  // Function to add title and logo (only on first page)
  function addTitleAndLogo() {
    // Create circular logo with proper cropping
    const logoSize = 40;
    const logoX = pageWidth/2 - logoSize/2;
    const logoY = 30;
    
    // Add white circle background for logo
    doc.setFillColor(255, 255, 255);
    doc.circle(pageWidth/2, logoY + logoSize/2, logoSize/2, 'F');
    
    // Add logo with circular mask
    doc.addImage(Logo3, 'PNG', logoX, logoY, logoSize, logoSize, undefined, 'FAST');
    
    // Add smaller title
    doc.setFontSize(20);
    doc.setTextColor(...colors.accent);
    doc.text(t.title, pageWidth/2, logoY + logoSize + 25, { align: 'center' });
    
    // Add subtle separator
    doc.setDrawColor(...colors.accent);
    doc.setLineWidth(0.5);
    doc.line(40, logoY + logoSize + 35, pageWidth - 40, logoY + logoSize + 35);

    return logoY + logoSize + 50; // Return starting Y position for events
  }

  // Calculate total pages needed
  const eventsPerPage = 12;
  const totalPages = Math.ceil(fireEvents.value.length / eventsPerPage);

  // Add first page elements
  addPageElements(1, totalPages);
  let yPosition = addTitleAndLogo();

  // Add events
  doc.setFontSize(11);
  fireEvents.value.forEach((event, index) => {
    // Check if we need a new page
    if (index > 0 && index % eventsPerPage === 0) {
      doc.addPage();
      addPageElements(Math.floor(index / eventsPerPage) + 1, totalPages);
      yPosition = 50; // Reset Y position for new page
    }

    // Add event number without circle
    doc.setTextColor(...colors.accent);
    doc.setFont(undefined, 'bold');
    doc.text(`${index + 1}.`, 30, yPosition);
    
    // Add event text
    doc.setTextColor(...colors.text);
    doc.setFont(undefined, 'normal');
    doc.text(event, 40, yPosition);
    
    yPosition += 12;
  });

  // Add generation date on last page
  const date = new Date().toLocaleDateString(locale.value === 'sv' ? 'sv-SE' : 'en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
  doc.text(`${t.generatedOn} ${date}`, pageWidth/2, pageHeight - 10, { align: 'center' });
  
  doc.save(locale.value === 'sv' ? 'brandhandelser.pdf' : 'fire-events.pdf');
}

onMounted(() => {
  connectWebSocket();
});

onBeforeUnmount(() => {
  if (socket) socket.close();
  if (reconnectInterval) clearInterval(reconnectInterval);
});
</script>

<style scoped>

.language-switcher {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 2;
}

.language-switcher button {
  background-color: #007bff;
  color: white;
  padding: 10px;
  border: none;
  border-radius: 5px;
  margin-right: 10px;
  cursor: pointer;
}

.language-switcher button:hover {
  background-color: #0056b3;
}
.app {
  font-family: sans-serif;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  margin: 0;
  background-image: url('https://deafalert.s3.eu-north-1.amazonaws.com/Untitled+design.jpg'); /* Background image */
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  transition: background-color 1s ease-in-out;
  position: relative;
  overflow: hidden;
}

.smoke-detected::before {
  content: "";
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 1;
  background-color: #ff4c4c;
  animation: flash-background 1.5s infinite;
  pointer-events: none;
  opacity: 0.7;
}

@keyframes flash-background {
  0% { background-color: #ff4c4c; }
  50% { background-color: #ff0000; }
  100% { background-color: #ff4c4c; }
}

.smoke-warning {
  background-color: #ff4c4c;
  color: #ffffff;
  padding: 2rem 4rem;
  font-size: 3rem;
  font-weight: bold;
  border: 3px solid #ff1a1a;
  border-radius: 15px;
  text-align: center;
  animation: pulse 1.5s infinite;
  text-shadow: 2px 2px 5px rgba(0, 0, 0, 0.7);
}

@keyframes pulse {       /* Pulse animation for the warning message */
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.85; }
  100% { transform: scale(1); opacity: 1; }
}

.warning-icon {
  font-size: 3rem;
  margin: 0 0.5rem;
}

.status-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 50px;
  margin-top: 20px;
  border-radius: 15px;
  width: 100%;
  max-width: 700px;
  height: 450px;  
  text-align: center;
  font-size: 1.5rem;
  box-shadow: 0 4px 12px rgba(204, 10, 10, 0.1);
  transition: transform 0.3s ease;
  background-color: rgba(255, 255, 255, 0.8);
  position: relative;
  z-index: 2;
}

.status-box:hover {
  transform: scale(1.05);
}

h1 {
  font-size: 2.5rem;
  margin: 0 0 20px 0;
  color: #ba0e0e;
  border-bottom: 2px solid #ddd;
}

.ok {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.alert {
  background-color: #ff4c4c;
  color: #ffffff;
  border: 1px solid #ff1a1a;
}

.waiting {
  background-color: #fff3cd;
  color: #020202;
  border: 1px solid #ffeeba;
  font-family: 'Courier New', Courier, monospace;
  font-size: 1rem;
  font-weight: bold;
  font-style: italic;
}

.raw {
  margin-top: 1rem;
  font-size: 1rem;
  color: #888;
}

.environment {
  margin-top: 80px;
  font-size: 2rem;
  font-weight: bold;
}

.safe {
  color: rgb(4, 122, 4);
  font-size: 3rem;
}

.unknown {
  color: gray;
}

.fullscreen-alert {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 100%;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.button-row {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  width: 100%;
  margin-top: 160px;
}

.test-button,
.view-fires-button,
.emergency-help-request-button {
  padding: 0.75rem 1.5rem;
  font-size: 1.1rem;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.test-button {
  background-color: #007bff;
  color: white;
}

.view-fires-button {
  background-color: #28a745;
  color: white;
}

.emergency-help-request-button {
  background-color: #dc3545;
  color: white;
}

.test-button:hover {
  background-color: #0056b3;
}

.view-fires-button:hover {
  background-color: #1e7e34;
}

.emergency-help-request-button {
  background-color: #c61b2c;
}

.modal-overlay {         /* Modal background */
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 4;
}

.modal-content {
  background-color: #fff;
  padding: 2rem;
  border-radius: 15px;
  text-align: center;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.close-modal,
.download-pdf-button,
.clear-history-button {
  padding: 0.75rem 1.5rem;
  font-size: 1.1rem;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.download-pdf-button {
  background-color: #6f42c1;
  color: white;
}

.clear-history-button {
  background-color: #007bff;
  color: white;
}

.close-modal {
  background-color: #d9534f;
  color: white;
}

.download-pdf-button:hover {
  background-color: #5a32a3;
}

.clear-history-button:hover {
  background-color: #0056b3;
}

.close-modal:hover {
  background-color: #c9302c;
}

.modal-content ul {
  padding-left: 0; 
  list-style-position: inside; 
}

.modal-content li {
  margin-left: 0; 
  padding-left: 0; 
}
</style>