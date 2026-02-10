package com.mycompany.hearme;

public class SensorMonitor {
    private boolean previouslyDisconnected = false;
    private boolean sensorReadyMessageSent = false;
    private String lastAirQualityStatus = ""; // To track last air quality state sent

    private volatile long lastMessageTime = System.currentTimeMillis(); // any MQTT message (valid or not)

    private static final int CONNECTION_THRESHOLD = 150;
    private static final int DANGEROUS_THRESHOLD = 500;

    public void updateLastMessageTime(long time) {
        lastMessageTime = time;
    }

    public String evaluate(String message) {
        try {
            int value = Integer.parseInt(message.trim());

            // Check sensor connection status
            if (value <= CONNECTION_THRESHOLD) {
                if (!previouslyDisconnected) {
                    previouslyDisconnected = true;
                    sensorReadyMessageSent = false;
                    return "GAS SENSOR DISCONNECTED OR NOT CONNECTED PROPERLY";
                }
            } else {
                if (!sensorReadyMessageSent) {
                    sensorReadyMessageSent = true;
                    previouslyDisconnected = false;
                    return "SENSOR CONNECTED AND READY";
                }
            }

            // Check gas danger level
            String newAirStatus = null;
            if (value > DANGEROUS_THRESHOLD) {
                newAirStatus = "DANGEROUS GAS LEVEL";
            } else if (value > CONNECTION_THRESHOLD && value <= DANGEROUS_THRESHOLD) {
                newAirStatus = "SAFE GAS LEVEL";
            }

            // Only broadcast if status changed
            if (newAirStatus != null && !newAirStatus.equals(lastAirQualityStatus)) {
                lastAirQualityStatus = newAirStatus;
                return newAirStatus;
            }

        } catch (NumberFormatException e) {
            // Ignore invalid messages
        }

        return null;
    }

    public String evaluateTimeout(long currentTime) {
        if (currentTime - lastMessageTime > 4000) {
            return "Device is not connected to Network";
        }
        return null;
    }
}


