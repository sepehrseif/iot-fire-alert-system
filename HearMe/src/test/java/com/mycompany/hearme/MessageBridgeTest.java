package com.mycompany.hearme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class MessageBridgeTest {

    private MessageBridge bridge;
    private MqttListener mqttMock;
    private hearme_api wsMock;
    private SensorMonitor monitorMock;

    @BeforeEach
    public void setup() throws Exception {
        bridge = new MessageBridge();

        mqttMock = mock(MqttListener.class);
        wsMock = mock(hearme_api.class);
        monitorMock = mock(SensorMonitor.class);

        // Inject mocks using reflection since the fields are private and not set via constructor
        Field mqttField = MessageBridge.class.getDeclaredField("mqttListener");
        mqttField.setAccessible(true);
        mqttField.set(bridge, mqttMock);

        Field wsField = MessageBridge.class.getDeclaredField("webSocketServer");
        wsField.setAccessible(true);
        wsField.set(bridge, wsMock);

        Field monitorField = MessageBridge.class.getDeclaredField("monitor");
        monitorField.setAccessible(true);
        monitorField.set(bridge, monitorMock);
    }

    @Test
    public void testStopShutsDownServices() throws Exception {
        bridge.stop();

        verify(mqttMock, times(1)).stop();
        verify(wsMock, times(1)).stop();

     
    }
}

