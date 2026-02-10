package com.mycompany.hearme;

import java.util.concurrent.CountDownLatch;

public class MessageBridge {
    private MqttListener mqttListener;
    private hearme_api webSocketServer;
    private SensorMonitor monitor;
    private final CountDownLatch stopLatch = new CountDownLatch(1);

    public void start() {

        if (System.getenv("CI") != null) {
            System.out.println("🚫 Skipping background services in CI environment.");
            return;
        }

        // Initialize core components
        mqttListener = new MqttListener();
        EmailSender emailSender = new EmailSender();
        MotorTestHandler motorTestHandler = new MotorTestHandler();
        CompositeHandler compositeHandler = new CompositeHandler(motorTestHandler, emailSender);
        webSocketServer = new hearme_api(8080, compositeHandler);
        monitor = new SensorMonitor();

        // Start MQTT listener
        Thread mqttThread = new Thread(() -> mqttListener.start(), "MQTT-Thread");
        mqttThread.start();

        // Start WebSocket server
        Thread webSocketThread = new Thread(() -> webSocketServer.start(), "WebSocket-Thread");
        webSocketThread.start();

        // Poll MQTT messages and send to frontend
        Thread processingThread = new Thread(() -> {
            String lastProcessedMessage = "";

            while (stopLatch.getCount() > 0) {
                String mqttMessage = mqttListener.getLatestMessage();
                long lastMessageTime = mqttListener.getLastMessageTime();
                long currentTime = System.currentTimeMillis();

                // Always track last MQTT message time
                monitor.updateLastMessageTime(lastMessageTime);

                // Timeout check
                String timeoutStatus = monitor.evaluateTimeout(currentTime);
                if (timeoutStatus != null && !timeoutStatus.equals(lastProcessedMessage)) {
                    System.out.println("⏱️ Timeout detected.");
                    webSocketServer.broadcastMessage(timeoutStatus);
                    lastProcessedMessage = timeoutStatus;
                }

                // Process new valid messages
                if (mqttMessage != null) {
                    String evaluated = monitor.evaluate(mqttMessage);
                    if (evaluated != null && !evaluated.equals(lastProcessedMessage)) {
                        System.out.println("📊 Evaluated message: " + evaluated);
                        webSocketServer.broadcastMessage(evaluated);
                        lastProcessedMessage = evaluated;
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("🛑 Processing thread interrupted.");
                    Thread.currentThread().interrupt(); // Preserve the interrupted flag
                    break;
                }
            }
        }, "Processing-Thread");
        processingThread.start();

        // Block main thread until program is stopped
        try {
            stopLatch.await(); // Keeps the program running
        } catch (InterruptedException e) {
            System.out.println("🛑 Main thread interrupted.");
            Thread.currentThread().interrupt(); // Preserve the interrupted flag
        }

        // Ensure that the threads are finished before stopping the program
        try {
            mqttThread.join();
            webSocketThread.join();
            processingThread.join();
        } catch (InterruptedException e) {
            System.out.println("🛑 Error during thread join: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        try {
            if (mqttListener != null)
                mqttListener.stop();
            if (webSocketServer != null)
                webSocketServer.stop();
            stopLatch.countDown();
        } catch (Exception e) {
            System.out.println("🛑 Error during stop: " + e.getMessage());
        }
    }
}
