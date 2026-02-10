package com.mycompany.hearme;

public class HearMe {
    public static void main(String[] args) {
        System.out.println("🔊 Starting HearMe App..."); 

        MessageBridge bridge = new MessageBridge(); // Initialize the main communication bridge between components
        Runtime.getRuntime().addShutdownHook(new Thread(bridge::stop));// Ensure the bridge stops gracefully on application shutdown
        bridge.start();
    }
}