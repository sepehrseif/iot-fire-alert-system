package com.mycompany.hearme;

import org.eclipse.paho.client.mqttv3.*;
import org.junit.Before;
import org.junit.Test;


import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

public class MotorControlPublisherTest {

    private MotorControlPublisher publisher;
    private MqttClient mockClient;

    @Before
    public void setUp() throws Exception {
        publisher = new MotorControlPublisher();

        // Inject a mock client into the publisher
        mockClient = mock(MqttClient.class);

        Field clientField = MotorControlPublisher.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(publisher, mockClient);
    }

    @Test
    public void testConnect_WhenNotConnected() throws Exception {
        when(mockClient.isConnected()).thenReturn(false);

        publisher.connect();

        verify(mockClient, times(1)).connect();
    }

    @Test
    public void testConnect_WhenAlreadyConnected() throws Exception {
        when(mockClient.isConnected()).thenReturn(true);

        publisher.connect();

        verify(mockClient, never()).connect();
    }

    @Test
    public void testPublish_WhenConnected() throws Exception {
        when(mockClient.isConnected()).thenReturn(true);

        publisher.publish("ACTIVATE");

        verify(mockClient, times(1)).publish(eq("motor_control"), any(MqttMessage.class));
    }

    @Test
    public void testPublish_WhenNotConnected() throws Exception {
        when(mockClient.isConnected()).thenReturn(false);

        publisher.publish("ACTIVATE");

        verify(mockClient, never()).publish(anyString(), any(MqttMessage.class));
    }

    @Test
    public void testDisconnect_WhenConnected() throws Exception {
        when(mockClient.isConnected()).thenReturn(true);

        publisher.disconnect();

        verify(mockClient, times(1)).disconnect();
    }

    @Test
    public void testDisconnect_WhenNotConnected() throws Exception {
        when(mockClient.isConnected()).thenReturn(false);

        publisher.disconnect();

        verify(mockClient, never()).disconnect();
    }
}

