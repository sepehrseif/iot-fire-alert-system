package com.mycompany.hearme;

public class CompositeHandler implements WebSocketMessageHandler {
    private final WebSocketMessageHandler[] handlers;

    public CompositeHandler(WebSocketMessageHandler... handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handleMessage(String message) {
        for (WebSocketMessageHandler handler : handlers) {
            handler.handleMessage(message);
        }
    }
}

