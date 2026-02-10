package com.mycompany.hearme;

public class MotorTestHandler implements WebSocketMessageHandler {
    private final MotorControlPublisher motorControlPublisher;

    public MotorTestHandler() {
        this.motorControlPublisher = new MotorControlPublisher();
        this.motorControlPublisher.connect();
    }

    @Override
    public void handleMessage(String message) {
        if ("MOTOR TEST".equalsIgnoreCase(message.trim())) {
            System.out.println("🛠️ Running motor test for 3 seconds...");
            motorControlPublisher.publish("ACTIVATE");// Start motor

            // Schedule STOP command after 3 seconds in a separate thread
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                motorControlPublisher.publish("STOP");// Stop motor
                System.out.println("✅ Motor test completed.");
            }).start();
        }
    }
}