/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.u2;

import asjava.uniobjects.UniSessionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rlittle
 */
public class UvConnectionTest {
    
    private Uv uv;
    private String userName;
    private String password;
    private String hostName;
    private String accountName;
    
    public UvConnectionTest() {
        
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        hostName = "corvette";
        userName = "uvuser";
        password = "uvuser999";
        accountName = "UVUSERS";
        uv = Uv.newInstance(hostName, userName, password, accountName);
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void connect() {
        boolean connected = false;
        try {
            connected = uv.connect();
            uv.disconnect();
        } catch (UniSessionException ex) {
            Logger.getLogger(UvConnectionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertTrue(connected);
    }
}
