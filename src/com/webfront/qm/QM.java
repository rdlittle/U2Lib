/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.qm;
import java.io.IOException;
import qmclient.*;

/**
 *
 * @author rlittle
 */
public class QM {
    private static qmclient qm;
    
    public static void main(String[] argv) throws IOException {
        qm = new qmclient();
        if(!qm.ConnectLocal("QMUSERS")) {
            System.out.println("Can't connect to openQM: QMUSERS");
            System.exit(0);
        }
        int usersFile = qm.Open("USERS");
        if(usersFile == 0) {
            System.out.println("Can't open USERS file");
            System.exit(0);
        }
        String userRec = qm.Read(usersFile, "1");
        System.out.println(userRec);
    }
    
}
