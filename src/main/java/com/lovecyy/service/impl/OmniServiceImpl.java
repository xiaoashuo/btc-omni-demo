package com.lovecyy.service.impl;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.lovecyy.config.CoinRpcClient;
import com.lovecyy.constants.OmniConstants;
import com.lovecyy.exception.CoinException;
import com.lovecyy.model.pojo.omni.OmniBalance;
import com.lovecyy.model.pojo.omni.OmniTokenBalance;
import com.lovecyy.model.pojo.omni.OmniTransaction;
import com.lovecyy.service.OmniService;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.ECKey;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OmniServiceImpl implements OmniService {

    private final CoinRpcClient coinRpcClient;
    @Override
    public OmniBalance omniGetBalance(Integer propertyId, String address) throws CoinException {
        try {
          return coinRpcClient.getClient().invoke("omni_getbalance", new Object[]{address, propertyId}, OmniBalance.class);
        } catch (Throwable e) {
            throw new CoinException(e.getMessage() + String.format("[params]: account=%s , propertyId=%s", address,propertyId));
        }
    }

    @Override
    public List<OmniTokenBalance> getAllBalancesForAddress(String address) {
        try {
            return   coinRpcClient.getClient().invokeList(OmniConstants.OMNI_GETALLBALANCESFORADDRESS, new Object[]{address}, OmniTokenBalance.class);
        } catch (Throwable e) {
            throw new CoinException(StrUtil.format("[params]: address={} ",address),e);
        }
    }

    @Override
    public List<String> listBlockTransactions(Integer blockIndex) throws CoinException {
        try {
            return coinRpcClient.getClient().invokeList(OmniConstants.OMNI_LISTBLOCKTRANSACTIONS, new Object[]{blockIndex}, String.class);

        } catch (Throwable e) {
            throw new CoinException(StrUtil.format("[params]: blockIndex={} ",blockIndex),e);
        }
    }

    @Override
    public OmniTransaction getTransaction(String txHash) throws CoinException {
        try {
           return coinRpcClient.getClient().invoke(OmniConstants.OMNI_GETTRANSACTION, new Object[]{txHash}, OmniTransaction.class);
        } catch (Throwable e) {
            throw new CoinException(StrUtil.format("[params]: txHash={} ",txHash),e);
        }
    }

    /**
     *  isValid 必须为true
     *  confirm 必须大于6
     */
    @Override
    public void omniScanBlock() {


    }

    public static void main(String[] args) {
        String WIFPrivateKey="cNxfiLn7KHSzeCCuEgRmvRHSH3gQuSPR3zENnwvZgnRNXTF9Wx5g";
        String substring = WIFPrivateKey.substring(2, WIFPrivateKey.length() - 10);
        ECKey ecKey = ECKey.fromPrivate(HexUtil.decodeHex("0x"+substring));
        System.out.println(ecKey);
    }
    //
//    /***
//     *
//     * 创建并发送资助的简单发送交易。 来自发件人的所有比特币都被消费，如果缺少比特币，则从指定的费用来源获取。更改将发送到费用来源！
//     * @param fromaddress 发送地址, toaddress 接收地址, propertyid 令牌id, amount =金额, feeaddress
//     * 支付手续费的地址 ]
//     * @return java.lang.String
//     * https://github.com/OmniLayer/omnicore/blob/master/src/omnicore/doc/rpc-api.md#omni_funded_send
//     **/
//    public String sendOmniToken(String fromaddress, String toaddress, long propertyid, BigDecimal amount,
//                                String feeaddress) {
//        AssertUp.isTrue(validateAddress(feeaddress).isIsvalid(), E.ADDRESS_ERROR);
//        AssertUp.isTrue(validateAddress(toaddress).isIsvalid(), E.ADDRESS_ERROR);
//        AssertUp.isTrue(validateAddress(fromaddress).isIsvalid(), E.ADDRESS_ERROR);
//
//        return http.engine("omni_funded_send", fromaddress, toaddress, propertyid, amount.toString(), feeaddress);
//    }
}
