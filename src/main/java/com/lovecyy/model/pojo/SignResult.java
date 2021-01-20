package com.lovecyy.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SignResult {
    /**
     * hex : 020000000186ed3e288892b4f5b5a140689cca932fe3a83de19763d17f9ad5a1fd7f6e1fc10100000000ffffffff020000000000000000166a146f6d6e69000000000000000200000000000f42401c0200000000000017a91429a6e9e848e36f986c661d819f7d92003e8893368700000000
     * complete : false
     * errors : [{"scriptSig":"","txid":"c11f6e7ffda1d59a7fd16397e13da8e32f93ca9c6840a1b5f5b49288283eed86","error":"Unable to sign input, invalid stack size (possibly missing key)","vout":1,"witness":[],"sequence":4294967295}]
     */
    /**
     * 签名后的交易序列字符串，16进制表示
     */
    private String hex;
    /**
     * 交易是否具备完整签名，false表示还需要更多的签名
     */
    private boolean complete;
    private List<ErrorsBean> errors;

    @NoArgsConstructor
    @Data
    public static class ErrorsBean  {
        /**
         * scriptSig :
         * txid : c11f6e7ffda1d59a7fd16397e13da8e32f93ca9c6840a1b5f5b49288283eed86
         * error : Unable to sign input, invalid stack size (possibly missing key)
         * vout : 1
         * witness : []
         * sequence : 4294967295
         */

        private String scriptSig;
        private String txid;
        private String error;
        private int vout;
        private long sequence;
        private List<?> witness;
    }


}
