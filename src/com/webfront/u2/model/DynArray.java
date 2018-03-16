/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.u2.model;

import asjava.uniclientlibs.UniDynArray;
import java.util.HashMap;

/**
 *
 * @author rlittle
 */
public class DynArray extends UniDynArray {

    public static UniDynArray toDynArray(HashMap<Integer, Object> map) {
        UniDynArray uda = new UniDynArray();
        for (Integer key : map.keySet()) {
            int attr = (int) key;
            Object val = map.get(key);
            uda.insert(attr, val);
        }
        return uda;
    }

    public static HashMap<Integer, Object> toHashMap(UniDynArray uda) {
        HashMap<Integer, Object> map = new HashMap<>();
        int attrs = uda.dcount();
        for (int attr = 1; attr <= attrs; attr++) {
            Integer fieldNum = attr;
            UniDynArray fieldValue = uda.extract(attr);
            map.put(fieldNum, fieldValue);
        }
        return map;
    }

    public static int locate(UniDynArray uda, String target, int attribute) {
        if (!target.isEmpty() && attribute > 0) {
            int values = uda.dcount(attribute);
            for (int val = 1; val <= values; val++) {
                if (target.equals(uda.extract(attribute, val).toString())) {
                    return val;
                }
            }
        }
        return -1;
    }

    public static Result locate(UniDynArray uda, String target, int attribute, String dir) {
        Result result = new Result();
        result.location = locate(uda,target,attribute);
        result.isSuccess = result.location > 0;
        if (result.isSuccess) {
            return result;
        }
        
        if (dir.isEmpty()) {
            dir = "ar";
        }

        if (!target.isEmpty() && attribute > 0) {
            int vals = uda.dcount(attribute);
            if (dir.equalsIgnoreCase("ar")) {
                for (int val = 1; val <= vals; val++) {
                    String subject = uda.extract(attribute, val).toString();
                    int test = target.compareTo(subject);
                    if (test > 0  && !subject.isEmpty()) {
                        result.location = val+1;
                        break;
                    }
                }
                if(result.location==-1) {
                    result.location = vals;
                }
            } else {
                for (int val = vals; val >= 1; val--) {
                    int test = target.compareTo(uda.extract(attribute, val).toString());
                    if (test < 0) {
                        result.location = val;
                        result.isSuccess = true;
                        break;
                    } else if (test > 0) {
                        continue;
                    } else {
                        result.location = val;
                        result.isSuccess = true;
                    }
                }
            }
        }

        return result;
    }

    public static int sum(UniDynArray uda, int attribute) {
        int total = 0;
        if (attribute > 0) {
            int vals = uda.dcount(attribute);
            for (int val = 1; val <= vals; val++) {
                String value = uda.extract(attribute, val).toString();
                if(value.isEmpty()) {
                    value="0";
                }
                total += Integer.parseInt(value);
            }
        }
        return total;
    }
}
