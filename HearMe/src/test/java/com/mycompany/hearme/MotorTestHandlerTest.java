package com.mycompany.hearme;

import org.junit.Before;
import org.junit.Test;


import static org.mockito.Mockito.*;

public class MotorTestHandlerTest {

    private MotorControlPublisher publisherMock;
    private MotorTestHandler handler;

    // Subclass to inject mock publisher
    private class TestableMotorTestHandler extends MotorTestHandler {
        public TestableMotorTestHandler(MotorControlPublisher mockPublisher) {
            super(); // Still calls original constructor, so we override below
            try {
                java.lang.reflect.Field field = MotorTestHandler.class.getDeclaredField("motorControlPublisher");
                field.setAccessible(true);
                field.set(this, mockPublisher);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Before
    public void setUp() {
        publisherMock = mock(MotorControlPublisher.class);
        handler = new TestableMotorTestHandler(publisherMock);
    }

    @Test
    public void testHandleMessage_sendsActivateAndStop() throws InterruptedException {
        handler.handleMessage("MOTOR TEST");

        // Verify "ACTIVATE" was sent immediately
        verify(publisherMock, timeout(500).times(1)).publish("ACTIVATE");

        // Verify "STOP" was sent after delay (allow enough time)
        verify(publisherMock, timeout(3500).times(1)).publish("STOP");
    }
}

