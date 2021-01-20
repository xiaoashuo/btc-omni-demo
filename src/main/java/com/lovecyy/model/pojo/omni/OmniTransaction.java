package com.lovecyy.model.pojo.omni;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * omni 交易
 * @author Yakir
 */
@Data
public class OmniTransaction {

    /**
     * txid : 63d7e22de0cf4c0b7fd60b4b2c9f4b4b781f7fdb8be4bcaed870a8b407b90cf1
     * amount : 0.00000000
     * ecosystem : test
     * propertyname : Bazz
     * data : BazzyFoo
     * divisible : true
     * fee : 0.00010000
     * propertytype : divisible
     * ismine : false
     * type : Create Property - Manual
     * confirmations : 1623760
     * version : 0
     * url : www.bazzcoin.info
     * sendingaddress : 2N1WnASsjgwrzbucBHhMd6gHLqpipq7kUZM
     * valid : true
     * blockhash : 000000002101ea0da161b6a63f4d1a0e37a2bd58c5aee49a3b8fd80640b09662
     * blocktime : 1409941113
     * positioninblock : 19
     * block : 279007
     * category : FooMANAGED
     * subcategory : Bar
     * propertyid : 2147483664
     * type_int : 54
     */
    /**
     * 16进制编码的交易hash
     */
    private String txid;
    /**
     * 发送方比特币地址
     */
    private String sendingaddress;
    /**
     * 作为参照的比特币地址 接收
     */
    private String referenceaddress;
    /**
     * 数量
     */
    private String amount;
    /**
     * 确认数
     */
    private int confirmations;
    /**
     * 系统环境 ecosystem -> test
     */
    private String ecosystem;
    /**
     * 交易手续费
     */
    private String fee;


    /**
     * 交易是否与钱包内某个地址相关
     */
    private boolean ismine;


    /**
     * 交易版本
     */
    private int version;


    /**
     * 交易是否有效 true 有效 false 无效
     */
    private boolean valid;
    /**
     * 区块hash
     */
    private String blockhash;
    /**
     * 包含交易区块的时间戳
     */
    private int blocktime;
    /**
     * 交易在区块内的序号
     */
    private int positioninblock;
    /**
     * 交易区块高度
     */
    private int block;
    /**
     * 交易类型代码
     */
    private int type_int;
    /**
     * 交易类型
     */
    private String type;

    /**
     * 合约属性id
     */
    private long propertyid;
    /**
     * 合约属性名称
     */
    private String propertyname;
    /**
     * 属性类型 propertytype -> divisible
     */
    private String propertytype;
    /**
     * 合约属性类别
     */
    private String category;
    /**
     * 合约属性子类别
     */
    private String subcategory;
    /**
     * 合约属性data
     */
    private String data;
    /**
     * 合约发布url
     */
    private String url;
    /**
     * 是否可分割
     */
    private boolean divisible;

}
