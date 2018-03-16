/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.exception;

/**
 *
 * @author rlittle
 */
public class RecordLockException extends Exception {
    public RecordLockException(String msg) {
        super(msg);
    }
    
}
