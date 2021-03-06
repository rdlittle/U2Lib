/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.u2.util;

import com.webfront.u2.model.Account;
import javafx.util.StringConverter;

/**
 *
 * @author rlittle
 */
public class AccountConverter  extends StringConverter<Account> {
        @Override
        public String toString(Account object) {
            if (object==null) {
                return "";
            }
            return object.getName();
        }

        @Override
        public Account fromString(String string) {
            for(Account a : Config.getInstance().getAccounts()) {
                if(string.equalsIgnoreCase(a.getName())) {
                    return a;
                }
            }
            Account a = new Account();
            a.setName(string);
            return a;
        }
    
}
