package com.mycompany.hearme;

import java.util.concurrent.CountDownLatch;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttListener {

    private volatile String latestMessage;
    private volatile long lastMessageTime = System.currentTimeMillis();
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static final int DANGEROUS_THRESHOLD = 500;// Threshold for triggering danger response
    private MotorControlPublisher motorPublisher;
    private boolean lastGasLevelWasDangerous = false;

    public void start() {
        Thread mqttBackgroundThread = new Thread(() -> {
            String broker = "tcp://broker.hivemq.com:1883";
            String topic = "smoke_alert";
            String clientId = "JavaApp_" + System.currentTimeMillis();
            MqttClient sensorClient = null;

            while (shutdownLatch.getCount() > 0) {
                try {
                    sensorClient = new MqttClient(broker, clientId, new MemoryPersistence());
                    motorPublisher = new MotorControlPublisher(); // Initialize motorPublisher here

                    sensorClient.connect();
                    motorPublisher.connect(); // Make sure motor control is connected

                    System.out.println("✅ MQTT clients connected for sensor and motor control.");

                    sensorClient.subscribe(topic, (t, msg) -> {
                        String payload = msg.toString().trim();
                        System.out.println("📡 MQTT raw value received: " + payload);
                        setLatestMessage(payload); // Save value and update time

                        try {
                            int gasLevel = Integer.parseInt(payload);
                            if (gasLevel > DANGEROUS_THRESHOLD) {
                                if (!lastGasLevelWasDangerous) { // Only send 'ACTIVATE' if the last state was not
                                                                 // dangerous
                                    System.out.println("🔥 Dangerous gas level detected, sending 'ACTIVATE' to motor.");
                                    motorPublisher.publish("ACTIVATE");
                                    lastGasLevelWasDangerous = true;
                                }
                            } else {
                                if (lastGasLevelWasDangerous) { // Only send 'STOP' if the last state was dangerous
                                    System.out.println("✅ Gas level is safe, sending 'STOP' to motor.");
                                    motorPublisher.publish("STOP");
                                    lastGasLevelWasDangerous = false;
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("❌ Error handling gas level: " + e.getMessage());
                        }
                    });

                    // Keep connection alive while active
                    while (sensorClient.isConnected() && shutdownLatch.getCount() > 0) {
                        Thread.sleep(1000);
                    }

                    System.out.println("⚠️ MQTT disconnected or shutting down.");

                } catch (MqttException | InterruptedException e) {
                    System.err.println("❌ MQTT error: " + e.getMessage());
                } finally {
                    try {
                        if (sensorClient != null && sensorClient.isConnected()) {
                            sensorClient.disconnect();
                        }
                        if (motorPublisher != null) {
                            motorPublisher.disconnect();
                        }
                    } catch (MqttException e) {
                        System.err.println("⚠️ Error while disconnecting MQTT clients: " + e.getMessage());
                    }
                }

                try {
                    Thread.sleep(2000); // Reconnect delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            System.out.println("🛑 MQTT listener thread exited cleanly.");
        }, "MQTT-Background");

        mqttBackgroundThread.start();
    }

    public void stop() {
        shutdownLatch.countDown();
    }

    // Save latest MQTT message and timestamp
    public synchronized void setLatestMessage(String message) {
        this.latestMessage = message;
        this.lastMessageTime = System.currentTimeMillis();
    }

    public synchronized String getLatestMessage() {
        return latestMessage;
    }

    public synchronized long getLastMessageTime() {
        return lastMessageTime;
    }
}

