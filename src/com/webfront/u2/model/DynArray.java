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
    
    public static UniDynArray toDynArray(HashMap<Integer,Object> map) {
        UniDynArray uda = new UniDynArray();
        for(Integer key : map.keySet()) {
            int attr = (int) key;
            Object val = map.get(key);
            uda.insert(attr, val);
        }
        return uda;
    }
    
    public static HashMap<Integer,Object> toHashMap(UniDynArray uda) {
        HashMap<Integer,Object> map = new HashMap<>();
        int attrs = uda.dcount();
        for (int attr = 1; attr <= attrs; attr++) {
            Integer fieldNum = attr;
            UniDynArray fieldValue = uda.extract(attr);
            map.put(fieldNum,fieldValue);
        }
        return map;
    }
}
