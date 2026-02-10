import { createApp } from 'vue'
import App from './App.vue'
import { createI18n } from 'vue-i18n'

// Define translations into different languages
const messages = {
    en: {
        smokeDetected: 'Smoke Detected!',
        sensorStatus: 'Smoke Detector Status',
        sensorConnected: '✅ Sensor Connected',
        environment: 'Environment',
        safe: 'Safe',
        noFireEvents: 'No fire events found',
        testVibrationMotor: 'Test Vibration Motor',
        viewPreviousFires: 'View Previous Fires',
        emergency : 'Emergency Contacts',
        Contacts: 'Emergency Contacts',
        emergencyContacts: 'Emergency Contact ',
        emergencyNumber: 'Emergency Number - 112',
        close: 'Close',
        downloadPDF: '📄 Download PDF',
        clearHistory: '🗑️ Clear History',
        fireDetectedAt: 'Fire detected at',
        sensorNotConnected: '⌛ GAS SENSOR DISCONNECTED OR NOT CONNECTED PROPERLY.',
        sendEmergencyHelpRequest: 'Emergency Help Request',
        helpRequestSent:'🚨 Help Request Sent!',
        Emergency_services_notified: 'Emergency services have been notified.',
        fireDetected: 'Fire detected at',
        DeviceNotConnected:'Device Is Not Connected, Waiting...',
        Unknown: "Unknown"
    },
  sv: {
    smokeDetected: 'Rök Detekterad!',
    sensorStatus: 'Status för rökdetektor',
    sensorConnected: '✅ Sensor Ansluten',
    environment: 'Miljö',
    safe: 'Säker',
    noFireEvents: 'Inga brandhändelser hittades',
    testVibrationMotor: 'Testa Vibrationsmotor',
    viewPreviousFires: 'Visa Tidigare Bränder',
    emergencyContacts : 'Nödkontakter',
    emergencyContactOptions: 'Nödkontaktalternativ',
    emergencyNumber: 'Nödnnummer - 112',
    close: 'Stäng',
    downloadPDF: '📄 Ladda ner PDF',
    clearHistory: '🗑️ Rensa Historik',
    fireDetectedAt: 'Brand upptäcktes vid',
    sensorNotConnected: '⌛ Enheten är inte ansluten till nätverket, Väntar på sensorstatus...',
    sendEmergencyHelpRequest: 'Begäran om nödhjälp',
    helpRequestSent:'🚨 Begäran om hjälp skickad!',
    Emergency_services_notified: 'Nödtjänster har meddelats.',
    fireDetected: 'Brand upptäckt kl',
    DeviceNotConnected:'Enheten är inte ansluten, väntar...',
    Unknown: "Okänd"
  }
}

// vue-i18n configuration
const i18n = createI18n({
  legacy: false,  
  locale: 'en',   // Default language
  messages,       
})

// Building a Vue application and using i18n
const app = createApp(App)
app.use(i18n)
app.mount('#app')