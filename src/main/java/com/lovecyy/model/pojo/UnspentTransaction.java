package com.lovecyy.model.pojo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 未花费的输出
 * @author Yakir
 */
@Data
public class UnspentTransaction {

    /**
     * txid 交易id: d54994ece1d11b19785c7248868696250ab195605b469632b7bd68130e880c9a
     * vout 输出序号: 1
     * address 地址: mgnucj8nYqdrPFh2JfZSB1NmUThUGnmsqe
     * label 关联账号: test label
     * redeemScript 赎回脚本 -> 0014e3fdf7a758fa4f9ecad09f4a3d3e4e8021e9a1d7
     * scriptPubKey 公钥脚本: 76a9140dfc8bafc8419853b34d5e072ad37d1a5159f58488ac
     * amount 金额: 1.0E-4
     * confirmations 确认数: 6210
     * spendable 是否可消费，当钱包中包含对应的私钥时，该值为true: true
     * solvable 是否可用,钱包中包含私钥时该值被忽略: true
     * desc -> sh(wpkh([415cd314/0'/0'/2']03fb005387502bd1e07f6202ef804ffde302f3c6f062e15ad634d1d06c95d08f96))#wkmss9wq
     * safe -> {Boolean@9264} true
     */

    private String txid;
    private Integer vout;
    private String address;
    private String label;
    private String scriptPubKey;
    private String redeemScript;
    private BigDecimal amount;
    private Integer confirmations;
    private boolean spendable;
    private boolean solvable;
    private String desc;
    private boolean safe;


}
