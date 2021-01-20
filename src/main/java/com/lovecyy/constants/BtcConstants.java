package com.lovecyy.constants;

/**
 * btc rpc 常量
 * @author Yakir
 */
public class BtcConstants {
    /**
     * 列出未花费的交易
     */
    public static final String LIST_UNSPENT="listunspent";
    /**
     * 交易信息 只能查看本钱包内的
     * 这个命令只能查到本地节点的钱包地址交易，
     * 查询节点外的交易会报Invalid or non-wallet transaction id,
     * 而且如果钱包地址的私钥不在节点的话查询出来的数据中details字段是空的，
     * 也就是无法查询到该交易的输入输出数据。
     */
    public static final String  GET_TRANSACTION ="gettransaction";
    /**
     * 交易信息 可用查询所有的 并且txIndex=1 开启
     */
    public static final String  GET_RAWTRANSACTION ="getrawtransaction";
    /**
     * 获得所有余额
     */
    public static final String  GET_ALL_BALANCES_FOR_ADDRESS ="getallbalancesforaddress";
    /**
     * 得到块信息
     * @params txHash
     * @params format false 序列化表示 true json表示
     */
    public static final String  GET_BLOCK ="getblock";
    /**
     * 得到区块hash
     */
    public static final String  GET_BLOCKHASH ="getblockhash";
    /**
     * 得到块数目
     */
    public static final String  GET_BLOCK_COUNT ="getblockcount";
}
