package com.lovecyy.service;

import com.lovecyy.model.pojo.omni.OmniBalance;
import com.lovecyy.model.pojo.omni.OmniTokenBalance;
import com.lovecyy.model.pojo.omni.OmniTransaction;

import java.util.List;

/**
 * omni协议层service
 * @author Yakir
 */
public interface OmniService {
    /**
     * omni 获取余额
     * @param propertyId
     * @param address
     * @return OmniBalance
     */
    OmniBalance omniGetBalance(Integer propertyId, String address);

    /**
     * 得到所有余额
     * @param address
     * @return
     */
    List<OmniTokenBalance> getAllBalancesForAddress(String address);

    /**
     * 获取块交易列表
     * @param blockIndex 块索引
     * @return
     */
    List<String> listBlockTransactions(Integer blockIndex);

    /**
     * 获取交易信息
     * @param txHash 交易hash
     * @return
     */
    OmniTransaction getTransaction(String txHash);
    /**
     * omni 扫块
     */
    void omniScanBlock();
}
