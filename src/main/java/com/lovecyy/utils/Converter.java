package com.lovecyy.utils;

import java.math.BigDecimal;

/**
 * 工具转换 会丢失精度
 * @author Yakir
 */
public class Converter {
    /**
     * hex转byte数组
     * @param hex hex
     * @return byte数组
     */
    public static byte[] hexToByte(String hex){
        int m , n ;
        int byteLen = hex.length() / 2; // 每两个字符描述一个字节
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = (byte) intVal;
        }
        return ret;
    }

    /**
     * byte数组转hex
     * @param bytes byte数组
     * @return hex
     */
    public static String byteToHex(byte[] bytes){
        String strHex;
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            strHex = Integer.toHexString(aByte & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }

    /**
     * 比特币单位转换 BTC转satoshi
     * @param value BTC
     * @return satoshi
     */
    public static Long bitcoinToSatoshis(Double value){
        return  BigDecimal.valueOf(value).multiply(BigDecimal.TEN.pow(8)).longValue();
    }


    /**
     * 比特币单位转换 satoshi转BTC
     * @param value satoshi
     * @return BTC
     */
    public static Double satoshisToBitcoin(Long value){
        return  BigDecimal.valueOf(value).divide(BigDecimal.TEN.pow(8)).doubleValue();
    }
}
