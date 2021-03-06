/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.u2.client;

import com.webfront.u2.Uv;
import asjava.uniclientlibs.UniDynArray;
import asjava.uniobjects.UniCommand;
import asjava.uniobjects.UniCommandException;
import asjava.uniobjects.UniFile;
import asjava.uniobjects.UniFileException;
import asjava.uniobjects.UniObjectsTokens;
import asjava.uniobjects.UniSelectList;
import asjava.uniobjects.UniSelectListException;
import asjava.uniobjects.UniSessionException;
import com.webfront.u2.model.Profile;
import com.webfront.u2.model.UvData;
import com.webfront.u2.util.Progress;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 *
 * @author rlittle
 */
public class UvClient {

    private final Progress progress;

    private Profile sourceProfile;
    private Profile destProfile;
    private UniDynArray itemList;
    private Uv.SelectType selectType;
    private Uv.Existing existingPolicy;
    private Uv.Missing missingPolicy;
    private Uv.SelectFrom selectFrom;
    private UvData sourceData;
    private UvData destData;

    private Uv sourceSession;
    private Uv destSession;

    public Double totalRecords;

    public UvClient(Progress p) {
        this.progress = p;
    }

    /**
     * @return the sourceProfile
     */
    public Profile getSourceProfile() {
        return sourceProfile;
    }

    public Uv getSourceSession() {
        if (sourceSession == null) {
            return null;
        }
        return sourceSession;
    }

    /**
     * @param sourceProfile the sourceProfile to set
     */
    public void setSourceProfile(Profile sourceProfile) {
        this.sourceProfile = sourceProfile;
    }

    /**
     * @return the destProfile
     */
    public Profile getDestProfile() {
        return destProfile;
    }

    public Uv getDestSession() {
        if (destSession == null) {
            return null;
        }
        return destSession;
    }

    /**
     * @param destProfile the destProfile to set
     */
    public void setDestProfile(Profile destProfile) {
        this.destProfile = destProfile;
    }

    /**
     * @return the selectType
     */
    public Uv.SelectType getSelectType() {
        return selectType;
    }

    /**
     * @param selectType the selectType to set
     */
    public void setSelectType(Uv.SelectType selectType) {
        this.selectType = selectType;
    }

    /**
     * @return the existingPolicy
     */
    public Uv.Existing getExistingPolicy() {
        return existingPolicy;
    }

    /**
     * @param existingPolicy the existingPolicy to set
     */
    public void setExistingPolicy(Uv.Existing existingPolicy) {
        this.existingPolicy = existingPolicy;
    }

    /**
     * @return the missingPolicy
     */
    public Uv.Missing getMissingPolicy() {
        return missingPolicy;
    }

    /**
     * @param missingPolicy the missingPolicy to set
     */
    public void setMissingPolicy(Uv.Missing missingPolicy) {
        this.missingPolicy = missingPolicy;
    }

    /**
     * @return the sourceData
     */
    public UvData getSourceData() {
        return sourceData;
    }

    /**
     * @param sourceData the sourceData to set
     */
    public void setSourceData(UvData sourceData) {
        this.sourceData = sourceData;
    }

    /**
     * @return the destData
     */
    public UvData getDestData() {
        return destData;
    }

    /**
     * @param destData the destData to set
     */
    public void setDestData(UvData destData) {
        this.destData = destData;
    }

    public boolean doCopy() {
        UniFile sourceFile;
        UniFile destFile;
        UniDynArray sourceRecord = null;
        UniDynArray destRecord = null;
        UniSelectList list;
        Double recordsDone;

        progress.updateProgressBar(0D);
        if (doConnect()) {
            try {
                if (selectType == Uv.SelectType.LIST) {
                    if (selectFrom == Uv.SelectFrom.SOURCE) {
                        list = getList(sourceSession, sourceData);
                    } else {
                        list = getList(destSession, destData);
                    }
                } else if (selectType == Uv.SelectType.QUERY) {
                    if (selectFrom == Uv.SelectFrom.SOURCE) {
                        list = doQuery(sourceSession, sourceData);
                    } else {
                        list = doQuery(destSession, destData);
                    }
                } else {
                    UniDynArray uda = new UniDynArray();
                    list = sourceSession.getSession().selectList(0);
                    list.formList(getItemList());
                    totalRecords = new Double(getItemList().dcount());
                }
                if (list == null) {
                    progress.display("Unable to create select list");
                    doDisconnect();
                    return false;
                }
                if (totalRecords == 0) {
                    progress.display("No items found");
                    doDisconnect();
                    return false;
                }
                sourceFile = sourceSession.getSession().openFile(sourceData.getFileName());
                destFile = destSession.getSession().openFile(destData.getFileName());
                String srcHost = sourceProfile.getServerName();
                String destHost = destProfile.getServerName();
                recordsDone = new Double(0);
                while (!list.isLastRecordRead()) {
                    String recordId = list.next().toString();
                    recordsDone += 1;
                    Double pct = recordsDone / totalRecords;
                    progress.updateProgressBar(pct);
                    if (recordId.isEmpty()) {
                        continue;
                    }
                    sourceData.setId(recordId);
                    destData.setId(recordId);
                    // Read the source record
                    try {
                        progress.state("Read " + srcHost + " " + sourceData.getId() + " ");
                        int readStatus = getRecord(sourceFile, sourceData);
                        if (readStatus == UniObjectsTokens.UVE_RNF) {
                            progress.display("Not found");
                            continue;
                        }
                        if (readStatus == -1) {
                            progress.display("Error");
                            continue;
                        }
                        progress.display("OK");
                        progress.state("Read " + destHost + " " + sourceData.getId() + " ");
                        // Read the destination record
                        readStatus = getRecord(destFile, destData);
                        switch (readStatus) {
                            case -1:
                                progress.display("Error");
                                continue;
                            case UniObjectsTokens.UVE_RNF:
                                if (missingPolicy == Uv.Missing.IGNORE) {
                                    progress.display("Not found.  Skipping");
                                    continue;
                                }
                                break;
                            case UniObjectsTokens.UVE_NOERROR:
                                if (existingPolicy == Uv.Existing.PRESERVE) {
                                    progress.display("Skipping existing record");
                                    continue;
                                }
                                break;
                        }
                        progress.display("OK");
                        doRecordSetup(sourceData, destData);
                        int lockStatus = lockRecord(destFile, destData);
                        switch (lockStatus) {
                            case UniObjectsTokens.LOCK_NO_LOCK:
                                progress.display("Lock failed: " + destData.getId());
                                continue;
                            case UniObjectsTokens.UVE_RNF:
                                if (getMissingPolicy() == Uv.Missing.IGNORE) {
                                    destFile.unlockRecord(recordId);
                                    continue;
                                }
                                progress.state("Write " + destHost + " " + destData.getId() + " ");
                                writeRecord(destFile, destData);
                                destFile.unlockRecord(recordId);
                                progress.display("OK");
                                break;
                            default:
                                if (existingPolicy == Uv.Existing.PRESERVE) {
                                    destFile.unlockRecord(recordId);
                                    continue;
                                }
                                progress.state("Write " + destHost + " " + destData.getId() + " ");
                                writeRecord(destFile, destData);
                                destFile.unlockRecord(recordId);
                                progress.display("OK");
                                break;
                        }
                    } catch (UniFileException ex) {
                        Logger.getLogger(UvClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (UniSessionException ex) {
                Logger.getLogger(UvClient.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UniSelectListException usle) {

            }
            doDisconnect();
        }
        return true;
    }

    public void doQuery() {

    }

    public void doRecordSetup(UvData source, UvData dest) {
        if (source.getValue() > 0) {
            if (dest.getValue() > 0) {
                String data = source.getData().extract(1, 1, source.getValue()).toString();
                dest.getData().replace(1, 1, dest.getValue(), data);
            }
        } else {
            if (source.getField() > 0) {
                dest.getData().replace(1, source.getData());
            } else {
                dest.setData(source.getData());
            }
        }
    }

    public UniSelectList doQuery(Uv uv, UvData data) {
        UniSelectList list = null;
        try {
            UniCommand cmd;
            cmd = uv.getSession().command();
            String[] commandStack = data.getSelectCriteria().split("\n");
            for (String cmdSegment : commandStack) {
                if (!cmdSegment.isEmpty()) {
                    cmd.setCommand(cmdSegment);
                    cmd.exec();
                }
            }
            list = uv.getSession().selectList(0);
            totalRecords = new Double(cmd.getAtSelected());
        } catch (UniSessionException ex) {
            Logger.getLogger(UvClient.class.getName()).log(Level.SEVERE, null, ex);
            progress.display(ex.toString());
        } catch (UniCommandException ex) {
            Logger.getLogger(UvClient.class.getName()).log(Level.SEVERE, null, ex);
            progress.display(ex.toString());
        }
        return list;
    }

    public UniSelectList getList(Uv session, UvData data) {
        UniSelectList list = null;
        try {
            list = session.getSession().selectList(0);
            try {
                list.getList(data.getSelectCriteria());
                UniDynArray recList = list.readList();
                list.getList(data.getSelectCriteria());
                totalRecords = new Double(recList.dcount());
            } catch (UniSelectListException ex) {
                Logger.getLogger(UvClient.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } catch (UniSessionException ex) {
            Logger.getLogger(UvClient.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return list;
    }

    public boolean doConnect() {
        sourceSession = Uv.newInstance(sourceProfile);
        destSession = Uv.newInstance(destProfile);
        try {
            sourceSession.connect();
            progress.updateLed("source", true);
            progress.display("Connected to " + sourceProfile.getServerName());
        } catch (UniSessionException ex) {
            Logger.getLogger(UvClient.class.getName()).log(Level.SEVERE, null, ex);
            progress.display(ex.toString());
            return false;
        }
        try {
            destSession.connect();
            progress.updateLed("dest", true);
            progress.display("Connected to " + destProfile.getServerName());
        } catch (UniSessionException ex) {
            Logger.getLogger(UvClient.class.getName()).log(Level.SEVERE, null, ex);
            if (sourceSession.getSession().isActive()) {
                try {
                    sourceSession.disconnect();
                    progress.updateLed("source", false);
                } catch (UniSessionException ex1) {
                    Logger.getLogger(UvClient.class.getName()).log(Level.SEVERE, null, ex1);
                    Platform.runLater(() -> progress.display(ex1.toString()));
                }
            }
            return false;
        }

        return true;
    }

    public void doSingleConnect(String name) throws UniSessionException {
        if (name == "source") {
            sourceSession = Uv.newInstance(getSourceProfile());
            sourceSession.connect();
            progress.updateLed(name, true);
        } else {
            destSession = Uv.newInstance(getDestProfile());
            destSession.connect();
            progress.updateLed(name, true);
        }
    }

    public boolean doDisconnect() {
        try {
            sourceSession.disconnect();
            progress.updateLed("source", false);
            progress.display("Disconnected from " + sourceProfile.getServerName());
            destSession.disconnect();
            progress.updateLed("dest", false);
            progress.display("Disconnected from " + destProfile.getServerName());
        } catch (UniSessionException ex) {
            progress.display(ex.toString());
            return false;
        }
        return true;
    }

    public void doSingleDisconnect(String name) throws UniSessionException {
        if (name.equals("source")) {
            sourceSession.disconnect();
            progress.updateLed(name, false);
        } else {
            destSession.disconnect();
            progress.updateLed(name, false);
        }
    }

    public int getRecord(UniFile file, UvData data) {
        UniDynArray record;
        try {
            if (data.getValue() > 0) {
                record = new UniDynArray(file.readField(data.getId(), data.getField()));
            } else {
                if (data.getField() > 0) {
                    record = new UniDynArray(file.readField(data.getId(), data.getField()));
                } else {
                    record = new UniDynArray(file.read(data.getId()));
                }
            }
        } catch (UniFileException ex) {
            if (ex.getErrorCode() == UniObjectsTokens.UVE_RNF) {
                return UniObjectsTokens.UVE_RNF;
            }
            return -1;
        }
        data.setData(record);
        return UniObjectsTokens.UVE_NOERROR;
    }

    public int lockRecord(UniFile file, UvData data) {
        try {
            file.lockRecord(data.getId(), UniObjectsTokens.UVT_EXCLUSIVE_READ);
            if (!file.isRecordLocked()) {
                return UniObjectsTokens.LOCK_NO_LOCK;
            }
        } catch (UniFileException ufe) {
            if (ufe.getErrorCode() == UniObjectsTokens.UVE_RNF) {
                return UniObjectsTokens.UVE_RNF;
            }
            Logger.getLogger(UvClient.class.getName()).log(Level.SEVERE, null, ufe);
            return UniObjectsTokens.LOCK_NO_LOCK;
        }
        return UniObjectsTokens.LOCK_MY_FILELOCK;
    }

    public int writeRecord(UniFile file, UvData data) {
        try {
            if (data.getValue() > 0) {
                file.writeField(data.getId(), data.getData(), data.getField(), data.getValue());
            } else {
                if (data.getField() > 0) {
                    file.writeField(data.getId(), data.getData(), data.getField());
                } else {
                    file.write(data.getId(), data.getData());
                }
            }
        } catch (UniFileException ex) {
            try {
                if (file.isRecordLocked()) {
                    file.unlockRecord(data.getId());
                }
            } catch (UniFileException ex1) {
                Logger.getLogger(UvClient.class.getName()).log(Level.SEVERE, null, ex1);
                return -1;
            }
            Logger.getLogger(UvClient.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return 0;
    }

    /**
     * @return the selectFrom
     */
    public Uv.SelectFrom getSelectFrom() {
        return selectFrom;
    }

    /**
     * @param selectFrom the selectFrom to set
     */
    public void setSelectFrom(Uv.SelectFrom selectFrom) {
        this.selectFrom = selectFrom;
    }

    /**
     * @return the itemList
     */
    public UniDynArray getItemList() {
        return itemList;
    }

    /**
     * @param itemList the itemList to set
     */
    public void setItemList(ArrayList<String> items) {
        this.itemList = new UniDynArray();
        for (String s : items) {
            this.itemList.insert(-1, s);
        }
    }

}
