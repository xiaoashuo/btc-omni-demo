package com.lovecyy.service;

import com.lovecyy.enums.AddressType;
import com.lovecyy.exception.CoinException;
import com.lovecyy.model.pojo.accounts.Account;
import com.lovecyy.model.pojo.BlockInfo;
import com.lovecyy.model.pojo.RawTransaction;
import com.lovecyy.model.pojo.UnspentTransaction;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

import java.math.BigDecimal;
import java.util.List;

public interface BtcService {
    /**
     * @throws Throwable
     * @throws Exception
     * @Title: validateaddress 验证钱包地址是否有效
     * @param @param address
     * @param @return    参数
     * @return boolean    返回类型
     * @throws
     */
    boolean validateAddress(String address) throws CoinException;


    /**
     * @throws CoinException
     * @Title: getBalance
     * @param @param account
     * @param @return    参数
     * @return Object    返回类型
     * @throws
     */
    double getBalance(String  account) throws CoinException;


    /**
     * @throws CoinException
     * @Title: signSendToAddress
     * @param @param address
     * @param @param amount
     * @param @return    参数
     * @return Object    返回类型
     * @throws
     * <amount>是一个实数，并四舍五入到小数点后8位。如果成功，则返回事务ID <txid>
     */
    Object signSendToAddress(String address,double amount) throws CoinException;


    /**
     * @throws CoinException
     * @Title: multiSendToAddress
     * @param @param fromaccount
     * @param @param target
     * @param @return    参数
     * @return Object    返回类型
     * @throws
     */
    Object multiSendToAddress(String fromaccount,Object target) throws CoinException;


    /**
     * @throws CoinException
     * @Title: getTransaction
     * @param @param txId
     * @param @return    参数
     * @return Object    返回类型
     * @throws
     */
    Object getTrawtransaction(String txId,int verbose) throws CoinException;


    /**
     * @Title: createrawTransaction
     * @param @param transferInfo
     * @param @return
     * @param @throws CoinException    参数
     * @return Object    返回类型
     * @throws
     */
    Object createrawTransaction(Object transferInfo,Object sendInfo) throws CoinException;


    /**
     * @Title: signrawTransaction
     * @param @param hexstring
     * @param @param transferInfo
     * @param @return
     * @param @throws CoinException    参数
     * @return Object    返回类型
     * @throws
     */
    Object signrawTransaction(String hexstring, Object transferInfo) throws CoinException;



    /**
     * @Title: sendrawTransaction
     * @param @param hexHash
     * @param @return
     * @param @throws CoinException    参数
     * @return Object    返回类型
     * @throws
     */

    Object sendrawTransaction(String hexHash) throws CoinException;



    /**
     * @Title: decoderawtransaction
     * @param @param hex
     * @param @return
     * @param @throws CoinException    参数
     * @return Object    返回类型
     * @throws
     */
    Object decoderawtransaction(String hex) throws CoinException;
    /**
     * @Title: getTransaction
     * @param @param txId
     * @param @return
     * @param @throws CoinException    参数
     * @return Object    返回类型
     * @throws
     */

    Object getTransaction(String txId) throws CoinException;

    /**
     * 得到raw裸交易
     * @param txId
     * @param format true  json格式化  false 16进制
     * @param clazz  元素类型
     * @return
     * @throws CoinException
     */
    <T>T getRawTransaction(String txId, Boolean format,Class<T> clazz) throws CoinException;

    /**
     * 得裸交易
     * @param txId
     * @return
     * @throws CoinException
     */
    RawTransaction getRawTransaction(String txId)throws CoinException;


    /**
     * @throws CoinException
     * @Title: setAccount
     * @param @param address
     * @param @param account
     * @param @return    参数
     * @return Object    返回类型
     * @throws
     */
    Object setAccount(String address,String account) throws CoinException;


    /**
     * @Title: listReceivedByAddress
     * @param @param minconf
     * @param @return
     * @param @throws CoinException    参数
     * @return Object    返回类型
     * @throws
     */
    Object listReceivedByAddress(int minconf) throws CoinException;

    /**
     * 列出未花费的输出
     * @param minCount
     * @param maxCount
     * @param addresses
     * @return
     */
    List<UnspentTransaction> listUtxo(Integer minCount,Integer maxCount,String [] addresses);

    /**
     * @Title: settxfee
     * @param @param account
     * @param @return
     * @param @throws CoinException    参数
     * @return Object    返回类型
     * @throws
     */

    Object settxfee(double account) throws CoinException;


    /**
     * @Title: encryptwallet
     * @param @param passphrase
     * @param @return
     * @param @throws CoinException    参数
     * @return Object    返回类型
     * @throws
     */

    Object encryptwallet(String passphrase) throws CoinException;


    /**
     * @Title: walletpassphrase
     * @param @param passphrase
     * @param @param timeout
     * @param @return
     * @param @throws CoinException    参数
     * @return Object    返回类型
     * @throws
     */

    Object walletpassphrase(String passphrase,int timeout) throws CoinException;



    /**
     * @Title: blockCount
     * @param @return
     * @param @throws CoinException    参数
     * @return Object    返回类型
     * @throws
     */
    Integer getBlockCount() throws CoinException;


    /**
     * @Title: getBlockHash
     * @param @param index
     * @param @return
     * @param @throws CoinException    参数
     * @return String    返回类型
     * @throws
     */
    String getBlockHash(int index) throws CoinException;

    /**
     * @Title: getblock
     * @param @param blockHash
     * @param @return
     * @param @throws CoinException    参数
     * @return Object    返回类型
     * @throws
     */
    BlockInfo getblock(String blockHash) throws CoinException;

    /**
     * 获得费率
     * @return num/聪
     */
    Long  getFeeRate();

    /**
     * 得到手续费
     * size = 148 x inputNum + 34 x outputNum + 10
     * 算出字节数后，再乘以rate（Satoshi/byte）,rate通过费用估算API获取
     * 提示：所以为了转账少花手续费，最好把utxo列表根据余额从大到小做个排序
     * @param amount
     * @param utxos
     * @return
     */
    BigDecimal getFee(BigDecimal amount, List<UnspentTransaction> utxos);

    String getAddressByPublicKey(NetworkParameters networkParameters, ECKey ecKey, AddressType type);


    public BigDecimal calculationFee(Integer inputCount);


    /**
     * omni 扫块
     */
    void scanBlock();
}
