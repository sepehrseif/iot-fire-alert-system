package com.mycompany.hearme;

import java.net.InetSocketAddress;
import java.util.concurrent.CopyOnWriteArraySet;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

public class hearme_api extends WebSocketServer {
    private final CopyOnWriteArraySet<WebSocket> connections = new CopyOnWriteArraySet<>();
    private final WebSocketMessageHandler messageHandler; // New field

    // ✅ New constructor with handler
    public hearme_api(int port, WebSocketMessageHandler messageHandler) {
        super(new InetSocketAddress(port));
        this.messageHandler = messageHandler;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        System.out.println("🌐 New WebSocket connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        System.out.println("🔌 WebSocket connection closed: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("📩 Received from client: " + message);
        messageHandler.handleMessage(message); //  Delegate to the handler
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("❗ WebSocket error from connection " +
                (conn != null ? conn.getRemoteSocketAddress() : "unknown") + ": " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("🚀 WebSocket server started on port " + getPort());
    }

    public void broadcastMessage(String message) {
        for (WebSocket conn : connections) {
            if (conn.isOpen()) {
                conn.send(message);
            }
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();
            System.out.println("🛑 WebSocket server stopped.");
        } catch (InterruptedException e) {
            System.err.println("❌ Failed to stop WebSocket server: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}