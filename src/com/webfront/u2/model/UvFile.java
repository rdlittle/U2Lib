/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.u2.model;

/**
 *
 * @author rlittle
 */
public class UvFile {
    private String fileName;
    private boolean read;
    private boolean write;
    private int appId;
    
    public UvFile(int aid, String name, boolean rd, boolean wr) {
        appId = aid;
        fileName = name;
        read = rd;
        write = wr;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the isRemote
     */
    public boolean isRead() {
        return read;
    }
    
    public int getRead() {
        return isRead() ? 1 : 0;
    }

    /**
     * @param isRemote the isRemote to set
     */
    public void setIsRead(boolean rd) {
        this.read = rd;
    }
    
    public int getWrite() {
        return isWrite() ? 1 : 0;
    }

    /**
     * @return the isLocal
     */
    public boolean isWrite() {
        return write;
    }

    /**
     * @param isLocal the isLocal to set
     */
    public void setIsWrite(boolean wr) {
        this.write = wr;
    }

    /**
     * @return the appId
     */
    public int getAppId() {
        return appId;
    }

    /**
     * @param appId the appId to set
     */
    public void setAppId(int appId) {
        this.appId = appId;
    }
}
