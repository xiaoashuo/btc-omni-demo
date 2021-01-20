package com.lovecyy.model.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RawTransaction {

    /**
     * vsize : 109
     * locktime : 0
     * txid : 097e793f07028cab84677b6b8c730e9aa3ab4409818f18b5040865cb1f643c2c
     * weight : 436
     * confirmations : 1623796
     * version : 1
     * vout : [{"n":0,"scriptPubKey":{"addresses":["n3wc3rZU1KUexpdm9UY6VKtvFUmnjJmpsP"],"type":"pubkey","asm":"028b31a5d03834c1c6a07db26189d25f504655774c4e4f3cb9df835a9585de1e69 OP_CHECKSIG","hex":"21028b31a5d03834c1c6a07db26189d25f504655774c4e4f3cb9df835a9585de1e69ac","reqSigs":1},"value":25.0018516}]
     * blockhash : 000000002101ea0da161b6a63f4d1a0e37a2bd58c5aee49a3b8fd80640b09662
     * size : 109
     * blocktime : 1409941113
     * vin : [{"sequence":4294967295,"coinbase":"03df4104020203062f503253482f"}]
     * hex : 01000000010000000000000000000000000000000000000000000000000000000000000000ffffffff0e03df4104020203062f503253482fffffffff0148cc0595000000002321028b31a5d03834c1c6a07db26189d25f504655774c4e4f3cb9df835a9585de1e69ac00000000
     * time : 1409941113
     * hash : 097e793f07028cab84677b6b8c730e9aa3ab4409818f18b5040865cb1f643c2c
     */
    /**
     * 替代交易大小 virtual size
     */
    private Integer vsize;
    /**
     * Locktime, 也被称为nLockTime, 它定义了个最早时间，
     * 只有过了这个最早时间，这个transaction可以被发送到比特币网络
     * 。通常被设置为0，表示transaction一创建好就马上发送到比特币网络
     * locktime不满足条件不会被广播，不会被打包。
     * 因为没被打包和广播（没进内存池），locktime设置在未来时间的转账，可以被双花而变得无效。
     * locktime的时间是绝对时间。有两种表达方式，一种是时间戳，另一种是块高度。
     */
    private Integer locktime;
    /**
     * 交易id
     */
    private String txid;
    /**
     * BIP141定义的区块权重
     */
    private Integer weight;
    /**
     * 所在区块确认数
     */
    private Integer confirmations;
    /**
     * 版本
     */
    private Integer version;
    /**
     * 区块hash
     */
    private String blockhash;
    /**
     * 区块字节数
     */
    private Integer size;
    /**
     * 所在区块的出块时间
     */
    private Long blocktime;
    /**
     * 序列号字符串
     */
    private String hex;
    /**
     * 所在区块的出块时间
     */
    private Long time;
    /**
     * 交易hash
     */
    private String hash;
    /**
     * 输出
     */
    private List<VoutBean> vout;
    /**
     * 输入
     */
    private List<VinBean> vin;
    @Data
    public static class VinBean  {
        /**
         * sequence : 4294967295
         * coinbase : 03df4104020203062f503253482f
         */

        private long sequence;
        /**
         * 创币交易  utxo需要100个确认才能使用
         * Coinbase交易是区块中的第一笔交易。这是矿工可以创建的一种独特的比特币交易。
         * 矿工使用它来收集其工作的集体奖励，矿工收取的任何其他交易费用也将在此交易中发送。
         *该块中的第一个事务称为coinbase事务。
         */
        private String coinbase;
        private String txid;
        // #引用交易中的UTXO索引（第一个为0，此处代表上述txid交易中的第一个UTXO）
        private Integer vout;
        //#解锁脚本，用于解锁UTXO的脚本（这是可以花费这笔UTXO的关键信息）
        private ScriptSigBean scriptSig;
        private List<String> txinwitness;
        @Data
        public static class ScriptSigBean {
            /**
             * asm : 028b31a5d03834c1c6a07db26189d25f504655774c4e4f3cb9df835a9585de1e69 OP_CHECKSIG
             * hex : 21028b31a5d03834c1c6a07db26189d25f504655774c4e4f3cb9df835a9585de1e69ac
             */
            private String asm;
            private String hex;
        }
    }

    @Data
    public static class VoutBean  {
        /**
         * n : 0
         * scriptPubKey : {"addresses":["n3wc3rZU1KUexpdm9UY6VKtvFUmnjJmpsP"],"type":"pubkey","asm":"028b31a5d03834c1c6a07db26189d25f504655774c4e4f3cb9df835a9585de1e69 OP_CHECKSIG","hex":"21028b31a5d03834c1c6a07db26189d25f504655774c4e4f3cb9df835a9585de1e69ac","reqSigs":1}
         * value : 25.0018516
         */
        /**
         * 索引
         */
        private Integer n;
        /**
         * 金额 btc单位
         */
        private BigDecimal value;
        // #锁定脚本，后续的交易如要使用该输出，必须解锁锁定脚本
        private ScriptPubKeyBean scriptPubKey;



        @Data
        public static class ScriptPubKeyBean {
            /**
             * addresses : ["n3wc3rZU1KUexpdm9UY6VKtvFUmnjJmpsP"]
             * type : pubkey
             * asm : 028b31a5d03834c1c6a07db26189d25f504655774c4e4f3cb9df835a9585de1e69 OP_CHECKSIG
             * hex : 21028b31a5d03834c1c6a07db26189d25f504655774c4e4f3cb9df835a9585de1e69ac
             * reqSigs : 1
             */

            private String type;
            private String asm;
            private String hex;
            private int reqSigs;
            private List<String> addresses;
        }
    }


}
