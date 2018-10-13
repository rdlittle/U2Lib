/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.u2.util;

import static asjava.uniclientlibs.UniTokens.UVE_RNF;
import asjava.uniobjects.UniFile;
import static asjava.uniobjects.UniObjectsTokens.LOCK_NO_LOCK;
import com.webfront.exception.NotFoundException;
import com.webfront.exception.RecordLockException;
import com.webfront.u2.model.UvData;
import java.util.HashMap;

/**
 *
 * @author rlittle
 */
public class MaOrder {
    private final HashMap<String, UniFile> fileMap;

    public MaOrder(HashMap<String, UniFile> fileMap) {
        this.fileMap = fileMap;
    }
    
    public UvData getOrder(String aoId) throws NotFoundException {
        UvData uvData = new UvData();
        uvData.setId(aoId);
        boolean isReleased = false;
        boolean isArchive = false;
        String orderFileName = "ORDERS.RELEASE";
        int result = FileUtils.getRecord(fileMap.get(orderFileName), uvData);
        if (result == UVE_RNF) {
            isReleased = true;
            orderFileName = "ORDERS";
            result = FileUtils.getRecord(fileMap.get(orderFileName), uvData);
            if (result == UVE_RNF) {
                orderFileName = "ORDERS.HISTORY";
                result = FileUtils.getRecord(fileMap.get(orderFileName), uvData);
                isArchive = true;
                if (result == UVE_RNF) {
                    throw new NotFoundException(aoId + " not found");
                }
            }
        }
        uvData.setFileName(orderFileName);
        return uvData;
    }

    public UvData lockOrder(String aoId) throws NotFoundException, RecordLockException {
        UvData uvData = new UvData();
        uvData.setId(aoId);
        boolean isReleased = false;
        boolean isArchive = false;
        String orderFileName = "ORDERS.RELEASE";
        int result = FileUtils.lockRecord(fileMap.get(orderFileName), uvData);
        if (result == LOCK_NO_LOCK) {
            throw new RecordLockException("ORDERS.RELEASE lock error " + aoId);
        }
        if (result == UVE_RNF) {
            result = FileUtils.unlockRecord(fileMap.get(orderFileName), uvData);
            isReleased = true;
            orderFileName = "ORDERS";
            result = FileUtils.lockRecord(fileMap.get(orderFileName), uvData);
            if (result == UVE_RNF) {
                result = FileUtils.unlockRecord(fileMap.get(orderFileName), uvData);
                orderFileName = "ORDERS.HISTORY";
                result = FileUtils.getRecord(fileMap.get(orderFileName), uvData);
                isArchive = true;
                if (result == UVE_RNF) {
                    result = FileUtils.unlockRecord(fileMap.get(orderFileName), uvData);
                    orderFileName = null;
                    throw new NotFoundException(aoId + " not found");
                }
            }
        }
        uvData.setFileName(orderFileName);
        return uvData;
    }

    public void unlockAo(UvData rec) {
        String orderFileName = rec.getFileName();
        if (orderFileName == null) {
            return;
        }
        if (rec == null) {
            return;
        }
        FileUtils.unlockRecord(fileMap.get(orderFileName), rec);
    }
    
    public void writeAo(UvData aoRec) {
        int result = FileUtils.writeRecord(fileMap.get(aoRec.getFileName()), aoRec);
    }
}
