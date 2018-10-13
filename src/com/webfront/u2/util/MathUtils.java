/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.u2.util;

import asjava.uniclientlibs.UniDynArray;

/**
 *
 * @author rlittle
 */
public class MathUtils {
    
    public static String sum(UniDynArray uda, int fieldNum) {
        int sum = 0;
        int vals = uda.dcount(fieldNum);
        boolean hasDecimal = false;
        for (int val = 1; val<=vals; val++) {
            String amt = uda.extract(fieldNum, val).toString();
            if (amt.isEmpty()) {
                continue;
            }
            if(amt.indexOf(".") > 0) {
                hasDecimal = true;
            }
            amt = amt.replaceAll("\\.", "");
            sum += Integer.parseInt(amt);
        }
        String sumString = Integer.toString(sum);
        if (hasDecimal) {
            StringBuilder sb = new StringBuilder(sumString);
            int sbLen = sumString.length();
            sb.insert(sbLen -2, ".");
            return sb.toString();
        }
        return Integer.toString(sum);
    }
}
