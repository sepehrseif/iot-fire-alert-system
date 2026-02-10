package com.mycompany.hearme;

import org.junit.Test;

import static org.junit.Assert.*;

public class SensorMonitorTest {

    @Test
    public void testDisconnectedSensor() {
        SensorMonitor monitor = new SensorMonitor();
        String result = monitor.evaluate("100");
        assertEquals("GAS SENSOR DISCONNECTED OR NOT CONNECTED PROPERLY", result);
    }

    @Test
    public void testConnectedSensor() {
        SensorMonitor monitor = new SensorMonitor();
        monitor.evaluate("100"); // simulate disconnected first
        String result = monitor.evaluate("200"); // now connected
        assertEquals("SENSOR CONNECTED AND READY", result);
    }

    @Test
    public void testSafeLevel() {
        SensorMonitor monitor = new SensorMonitor();
        monitor.evaluate("200"); // ensure connected
        String result = monitor.evaluate("400");
        assertEquals("SAFE GAS LEVEL", result);
    }

    @Test
    public void testDangerousLevel() {
        SensorMonitor monitor = new SensorMonitor();
        monitor.evaluate("200"); // ensure connected
        String result = monitor.evaluate("600");
        assertEquals("DANGEROUS GAS LEVEL", result);
    }

    @Test
    public void testTimeout() {
        SensorMonitor monitor = new SensorMonitor();
        monitor.updateLastMessageTime(System.currentTimeMillis() - 5000);
        String result = monitor.evaluateTimeout(System.currentTimeMillis());
        assertEquals("Device is not connected to Network", result);
    }
}

