package com.mycompany.hearme;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MotorControlPublisher {

    private MqttClient client;
    private static final String BROKER = "tcp://broker.hivemq.com";
    private static final String MOTOR_CONTROL_TOPIC = "motor_control";

    public MotorControlPublisher() {
        // Generate a unique client ID to avoid MQTT collisions
        try {
            String clientId = "MotorControlPublisher_" + System.currentTimeMillis();
            client = new MqttClient(BROKER, clientId, new MemoryPersistence());
        } catch (MqttException e) {
            System.err.println("❌ Error initializing MotorControlPublisher: " + e.getMessage());
        }
    }

    // Method to connect the publisher to the MQTT broker
    public void connect() {
        try {
            if (!client.isConnected()) {
                client.connect();
                System.out.println("✅ Motor control publisher connected.");
            }
        } catch (MqttException e) {
            System.err.println("❌ Error connecting MotorControlPublisher: " + e.getMessage());
        }
    }

    // Method to publish messages (e.g., 'ACTIVATE', 'STOP')
    public void publish(String message) {
        try {
            if (client.isConnected()) {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                client.publish(MOTOR_CONTROL_TOPIC, mqttMessage);
                System.out.println("📡 Published message: " + message);
            } else {
                System.err.println("❌ Motor control publisher not connected.");
            }
        } catch (MqttException e) {
            System.err.println("❌ Error publishing message: " + e.getMessage());
        }
    }

    // Method to disconnect the publisher from the MQTT broker
    public void disconnect() {
        try {
            if (client.isConnected()) {
                client.disconnect();
                System.out.println("✅ Motor control publisher disconnected.");
            }
        } catch (MqttException e) {
            System.err.println("❌ Error disconnecting MotorControlPublisher: " + e.getMessage());
        }
    }
}

