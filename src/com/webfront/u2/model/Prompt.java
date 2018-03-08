/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.u2.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author rlittle
 */
public class Prompt {

    private int id;

    private SimpleIntegerProperty num;
    private SimpleStringProperty message;
    private SimpleBooleanProperty required;
    private SimpleStringProperty response;

    public Prompt() {
        this.id = 0;
        this.num = new SimpleIntegerProperty();
        this.message = new SimpleStringProperty();
        this.required = new SimpleBooleanProperty();
        this.response = new SimpleStringProperty();
    }

    public Prompt(int i, int n, String m, boolean req) {
        this();
        this.id = i;
        this.num = new SimpleIntegerProperty(n);
        this.message = new SimpleStringProperty(m);
        this.required = new SimpleBooleanProperty(req);
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the num
     */
    public Integer getNum() {
        return num.get();
    }

    /**
     * @param num the num to set
     */
    public void setNum(int num) {
        this.num.set(num);
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message.get();
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message.set(message);
    }

    /**
     * @return the required
     */
    public boolean getRequired() {
        return required.get();
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required.set(required);
    }
    
    public String getResponse() {
        return response.get();
    }
    
    public void setResponse(String r) {
        this.response.set(r);
    }

}
