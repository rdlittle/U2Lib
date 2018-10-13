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
public class AffiliateOrder {

    private final HashMap<String, UniFile> fileMap;

    public AffiliateOrder(HashMap<String, UniFile> fileMap) {
        this.fileMap = fileMap;
    }

    public UvData getAo(String aoId) throws NotFoundException {
        UvData uvData = new UvData();
        uvData.setId(aoId);
        boolean isReleased = false;
        boolean isArchive = false;
        String aoFileName = "AFFILIATE.ORDERS";
        int result = FileUtils.getRecord(fileMap.get(aoFileName), uvData);
        if (result == UVE_RNF) {
            isReleased = true;
            aoFileName = "AOP.HIST";
            result = FileUtils.getRecord(fileMap.get(aoFileName), uvData);
            if (result == UVE_RNF) {
                aoFileName = "AOP.HIST.ARCHIVE";
                result = FileUtils.getRecord(fileMap.get(aoFileName), uvData);
                isArchive = true;
                if (result == UVE_RNF) {
                    throw new NotFoundException(aoId + " not found");
                }
            }
        }
        uvData.setFileName(aoFileName);
        return uvData;
    }

    public UvData lockAo(String aoId) throws NotFoundException, RecordLockException {
        UvData uvData = new UvData();
        uvData.setId(aoId);
        boolean isReleased = false;
        boolean isArchive = false;
        String aoFileName = "AFFILIATE.ORDERS";
        int result = FileUtils.lockRecord(fileMap.get(aoFileName), uvData);
        if (result == LOCK_NO_LOCK) {
            throw new RecordLockException("AFFILIATE.ORDERS lock error " + aoId);
        }
        if (result == UVE_RNF) {
            result = FileUtils.unlockRecord(fileMap.get(aoFileName), uvData);
            isReleased = true;
            aoFileName = "AOP.HIST";
            result = FileUtils.lockRecord(fileMap.get(aoFileName), uvData);
            if (result == UVE_RNF) {
                result = FileUtils.unlockRecord(fileMap.get(aoFileName), uvData);
                aoFileName = "AOP.HIST.ARCHIVE";
                result = FileUtils.getRecord(fileMap.get(aoFileName), uvData);
                isArchive = true;
                if (result == UVE_RNF) {
                    result = FileUtils.unlockRecord(fileMap.get(aoFileName), uvData);
                    aoFileName = null;
                    throw new NotFoundException(aoId + " not found");
                }
            }
        }
        uvData.setFileName(aoFileName);
        return uvData;
    }

    public void unlockAo(UvData rec) {
        String aoFileName = rec.getFileName();
        if (aoFileName == null) {
            return;
        }
        if (rec == null) {
            return;
        }
        FileUtils.unlockRecord(fileMap.get(aoFileName), rec);
    }
    
    public void writeAo(UvData aoRec) {
        int result = FileUtils.writeRecord(fileMap.get(aoRec.getFileName()), aoRec);
    }
}
