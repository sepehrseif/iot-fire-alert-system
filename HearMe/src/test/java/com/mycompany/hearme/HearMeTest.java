package com.mycompany.hearme;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HearMeTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        // runs once before all tests
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // runs once after all tests
    }

    @Before
    public void setUp() throws Exception {
        // runs before each test
    }

    @After
    public void tearDown() throws Exception {
        // runs after each test
    }

    /**
     * Test of main method, of class HearMe.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        HearMe.main(args);
    }
}
