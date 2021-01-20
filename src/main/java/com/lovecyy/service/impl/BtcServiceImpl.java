package com.lovecyy.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.ImmutableList;
import com.lovecyy.config.CoinRpcClient;
import com.lovecyy.constants.BtcConstants;
import com.lovecyy.enums.AddressType;
import com.lovecyy.exception.AddressException;
import com.lovecyy.exception.CoinException;
import com.lovecyy.model.pojo.accounts.Account;
import com.lovecyy.model.pojo.BlockInfo;
import com.lovecyy.model.pojo.RawTransaction;
import com.lovecyy.model.pojo.UnspentTransaction;
import com.lovecyy.model.pojo.accounts.P2SHMultiSigAccount;
import com.lovecyy.service.BtcService;
import io.github.novacrypto.bip39.SeedCalculator;
import io.github.novacrypto.bip39.wordlists.English;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BtcServiceImpl implements BtcService {

    @Autowired
    private CoinRpcClient client;

    @Override
    public boolean validateAddress(String address) throws CoinException {
        try {
            String res = (String) client.getClient().invoke("validateaddress", new Object[]{address}, Object.class);
            if (!StringUtils.isEmpty(res)) {
                JSONObject obj = JSONUtil.parseObj(res);
                if (obj.getBool("isvalid") == true) {
                    return true;
                }
            }
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.BtcServiceImpl.validateAddress(String):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: address=%s", address));
        }
        return false;
    }

    @Override
    public double getBalance(String account) throws CoinException {
        double balance;
        try {
            if (!StringUtils.isEmpty(account)) {
                balance = (double) client.getClient().invoke("getbalance", new Object[]{account}, Object.class);
            } else {
                balance = (double) client.getClient().invoke("getbalance", new Object[]{}, Object.class);
            }
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.BtcServiceImpl.getBalance(String...):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: account=%s", account));
        }
        return balance;
    }

    @Override
    public Object signSendToAddress(String address, double amount) throws CoinException {
        try {
            String txId = client.getClient().invoke("sendtoaddress", new Object[]{address, amount}, Object.class).toString();
            return txId;
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.BtcServiceImpl.signSendToAddress(String, double):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: address=%s,amount=%s", address, amount));
        }
    }

    @Override
    public Object multiSendToAddress(String fromaccount, Object target) throws CoinException {
        try {
            String txId = client.getClient().invoke("sendmany", new Object[]{fromaccount, target}, Object.class).toString();
            return txId;
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.BtcServiceImpl.signSendToAddress(String, double):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: fromaccount=%s,target=%s", fromaccount, target));
        }
    }

    @Override
    public Object getTrawtransaction(String txId, int verbose) throws CoinException {
        try {
            return client.getClient().invoke("getrawtransaction", new Object[]{txId, verbose}, Object.class);
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.BtcServiceImpl.getTrawtransaction(String):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: txId=%s", txId));
        }
    }

    @Override
    public Object getTransaction(String txId) throws CoinException {
        try {
            return client.getClient().invoke(BtcConstants.GET_TRANSACTION, new Object[]{txId}, Object.class);
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.BtcServiceImpl.getTransaction(String):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: txId=%s", txId));
        }
    }

    @Override
    public <T> T getRawTransaction(String txId, Boolean format, Class<T> clazz) throws CoinException {
        try {
            return client.getClient().invoke(BtcConstants.GET_RAWTRANSACTION, new Object[]{txId, format}, clazz);
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.BtcServiceImpl.getTransaction(String):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: txId=%s", txId));
        }
    }

    @Override
    public RawTransaction getRawTransaction(String txId) throws CoinException {
        return getRawTransaction(txId, true, RawTransaction.class);
    }

    @Override
    public Object setAccount(String address, String account) throws CoinException {
        try {
            return client.getClient().invoke("setaccount", new Object[]{address, account}, Object.class);
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.BtcServiceImpl.setAccount(String, String):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: address=%s,account=%s", address, account));
        }
    }

    @Override
    public Object listReceivedByAddress(int minconf) throws CoinException {
        try {
            if (1 != minconf) {
                return client.getClient().invoke("listreceivedbyaccount", new Object[]{minconf}, Object.class);
            } else {
                return client.getClient().invoke("listreceivedbyaccount", new Object[]{}, Object.class);
            }
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.BtcServiceImpl.listReceivedByAddress(int minconf):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: minconf=%s", minconf));
        }
    }

    @Override
    public List<UnspentTransaction> listUtxo(Integer minCount, Integer maxCount, String[] addresses) {
        try {
            return client.getClient().invokeList(BtcConstants.LIST_UNSPENT, new Object[]{minCount, maxCount, addresses}, UnspentTransaction.class);
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.BtcServiceImpl.listUtxo(Integer minCount, Integer maxCount, String[] addresses){} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: minCount=%s,maxCount=%s ,address=%s", minCount, maxCount, addresses));
        }
    }

    @Override
    public Object settxfee(double account) throws CoinException {
        try {
            return client.getClient().invoke("settxfee", new Object[]{account}, Object.class);
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.impl.BtcServiceImpl.settxfee(double):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: account=%s", account));
        }
    }

    @Override
    public Object encryptwallet(String passphrase) throws CoinException {
        try {
            return client.getClient().invoke("encryptwallet", new Object[]{passphrase}, Object.class);
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.impl.BtcServiceImpl.encryptwallet(String) ===", e.getMessage(), e);
            throw new CoinException(e.getMessage());
        }
    }

    @Override
    public Object walletpassphrase(String passphrase, int timeout) throws CoinException {
        try {
            return client.getClient().invoke("walletpassphrase", new Object[]{passphrase, timeout}, Object.class);
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.impl.BtcServiceImpl.walletpassphrase(String, int):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: passphrase=%s,timeout=%s", passphrase, timeout));
        }
    }

    @Override
    public Object createrawTransaction(Object transferInfo, Object sendInfo) throws CoinException {
        try {
            return client.getClient().invoke("createrawtransaction", new Object[]{transferInfo, sendInfo}, Object.class);
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.impl.BtcServiceImpl.createrawTransaction(Object):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: transferInfo=%s,sendInfo=%s", transferInfo, sendInfo));
        }
    }

    @Override
    public Object signrawTransaction(String hexstring, Object transferInfo) throws CoinException {
        try {
            return client.getClient().invoke("signrawtransaction", new Object[]{hexstring, transferInfo}, Object.class);
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.impl.BtcServiceImpl.signrawTransaction(String, Object):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: hexstring=%s,transferInfo=%s", hexstring, transferInfo));
        }
    }

    @Override
    public Object sendrawTransaction(String hexHash) throws CoinException {
        try {
            return client.getClient().invoke("sendrawtransaction", new Object[]{hexHash}, Object.class);
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.impl.BtcServiceImpl.sendrawTransaction(String):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: hexHash=%s", hexHash));
        }
    }


    @Override
    public Object decoderawtransaction(String hex) throws CoinException {
        try {
            return client.getClient().invoke("decoderawtransaction", new Object[]{hex}, Object.class);
        } catch (Throwable e) {
            log.info("=== com.wallet.bit.service.btc.impl.BtcServiceImpl.decoderawtransaction(String):{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage() + String.format("[params]: hex=%s", hex));
        }
    }


    @Override
    public Integer getBlockCount() throws CoinException {
        try {
            return client.getClient().invoke(BtcConstants.GET_BLOCK_COUNT, new Object[]{}, Integer.class);
        } catch (Throwable e) {
            log.info("=== com.wallet.bit.service.btc.impl.BtcServiceImpl.getBlockCount():{} ===", e.getMessage(), e);
            throw new CoinException(e.getMessage());
        }
    }

    @Override
    public String getBlockHash(int index) throws CoinException {
        try {
            return client.getClient().invoke("getblockhash", new Object[]{index}, String.class);
        } catch (Throwable e) {
            log.info("=== com.wallet.bit.service.btc.impl.BtcServiceImpl.getBlockHash(int):{} ===", e.getMessage(), e);
            throw new CoinException(StrUtil.format("[params]: index={} ", index), e);
        }
    }


    @Override
    public BlockInfo getblock(String blockHash) throws CoinException {
        try {
            return client.getClient().invoke(BtcConstants.GET_BLOCK, new Object[]{blockHash}, BlockInfo.class);
        } catch (Throwable e) {
            log.info("=== com.bscoin.bit.service.btc.impl.BtcServiceImpl.getblock(String):{} ===", e.getMessage(), e);
            throw new CoinException(StrUtil.format("[params]: blockHash={} ", blockHash), e);
        }
    }

    @Override
    public Long getFeeRate() {
        String feeResponse = HttpUtil.get("https://bitcoinfees.earn.com/api/v1/fees/recommended");
        Map map = JSONUtil.toBean(feeResponse, Map.class);
        String fastestFee = map.get("fastestFee").toString();
        return Long.valueOf(fastestFee);
    }

    public BigDecimal getMaxSendValue(List<UnspentTransaction> unSpentBTCList, BigDecimal totalMoney) {
        while (true) {
            BigDecimal fee = getFee(totalMoney, unSpentBTCList);
            if (fee.equals(-1)) {
                totalMoney = totalMoney.subtract(BigDecimal.valueOf(100));
            } else {
                break;
            }
        }
       /* long fee = getFee(unSpentBTCList, totalMoney, rate);
        if (fee == -1) {
            totalMoney -= 100;
            getMaxSendValue(unSpentBTCList, totalMoney, rate);
        }*/
        return totalMoney;
    }

    @Override
    public BigDecimal getFee(BigDecimal amount, List<UnspentTransaction> utxos) {
        Long feeRate = getFeeRate();
        BigDecimal utxoAmount = BigDecimal.ZERO;
        BigDecimal fee = BigDecimal.ZERO;
        Long utxoSize = 0L;
        for (UnspentTransaction utxo : utxos) {
            utxoSize++;
            BigDecimal totalSpend = amount.add(fee);
            if (utxoAmount.compareTo(totalSpend) >= 0) {
                //符合
                return fee;
            } else {
                utxoAmount = utxoAmount.add(utxo.getAmount());
                fee = BigDecimal.valueOf((utxoSize * 148 * 34 * 3 + 10) * feeRate);
            }
        }
        return BigDecimal.valueOf(-1);
    }
    /**
     * 计算 btc 交易的手续费
     * @param inNumber 输入数量
     * @param outNumber 输出数量
     * @param fee 每字节手续费单价
     * @return org.bitcoinj.core.Coin
     * @author lingting 2021-01-07 14:02
     */
    public static Coin getSumFee(long inNumber, long outNumber, Coin fee) {
        return fee.multiply(inNumber * 148 + outNumber * 34 + 10);
    }
    /**
     * 根据公钥得到地址
     *
     * @param networkParameters
     * @param ecKey
     * @param type
     * @return
     */
    @Override
    public String getAddressByPublicKey(NetworkParameters networkParameters, ECKey ecKey, AddressType type) {
//    LegacyAddress address = LegacyAddress.fromPubKeyHash(TestNet3Params.get(), ecKey.getPubKeyHash());
        switch (type) {
            case SINGLE:
                //普通地址
                return LegacyAddress.fromPubKeyHash(networkParameters, ecKey.getPubKeyHash()).toBase58();
            case MULTIPLE:
                return LegacyAddress.fromScriptHash(networkParameters, ecKey.getPubKeyHash()).toBase58();
            //多签地址
            case ISOLATION:
                //隔离见证
                return SegwitAddress.fromHash(networkParameters, ecKey.getPubKeyHash()).toBech32();
            default:
                throw new AddressException("Invalid address convert type ");
        }
    }



    public static void main(String[] args) {

        SecureRandom secureRandom = new SecureRandom();
        DeterministicSeed ds = new DeterministicSeed(secureRandom, 128, "");
        System.out.println(ds.getSeedBytes());
        List<String> mnemonicCode = ds.getMnemonicCode();
        byte[] bytes = new SeedCalculator().withWordsFromWordList(English.INSTANCE).calculateSeed(mnemonicCode, "");
        System.out.println(bytes);


    }
    /**
     * 计算手续费
     *费率*(inNumber * 148 + outNumber * 34 + 10)
     * @param inputCount
     * @return
     */
    @Override
    public BigDecimal calculationFee(Integer inputCount) {
        Long feeRate = getFeeRate();
        //=====resut===>转对象Model省略了，其实http请求都有公用的方法所以我随便写了。。
        BigDecimal keyCount = BigDecimal.valueOf((inputCount * 148 + 44) * feeRate);
        //
        return keyCount;

    }













    @Override
    public void scanBlock() {
        Integer blockCount = getBlockCount();
        String blockHash = getBlockHash(blockCount);
        BlockInfo blockInfo = getblock(blockHash);
        List<String> txs = blockInfo.getTx();
        for (String tx : txs) {
            parseRawTransaction(tx);
        }


    }

    private void parseRawTransaction(String tx) {
        RawTransaction rawTransaction = getRawTransaction(tx);
        //确认数
        Integer confirmations = rawTransaction.getConfirmations();
        //区块出块时间
        Long time = rawTransaction.getTime();
        Long blocktime = rawTransaction.getBlocktime();
        //输入
        List<RawTransaction.VinBean> vins = rawTransaction.getVin();
        //输出
        List<RawTransaction.VoutBean> vouts = rawTransaction.getVout();

        BigDecimal sumvin = BigDecimal.ZERO;
        BigDecimal sumvout = BigDecimal.ZERO;
        //从地址
        List<String> fromAddresses = new ArrayList<>();
        for (RawTransaction.VinBean vin : vins) {
            String txid = vin.getTxid();
            if (StrUtil.isEmpty(txid)) {
                continue;
            }
            Integer vinN = vin.getVout();
            RawTransaction parentTransaction = getRawTransaction(txid);
            List<RawTransaction.VoutBean> parentVouts = parentTransaction.getVout();
            for (RawTransaction.VoutBean parentVout : parentVouts) {
                int n = parentVout.getN();
                if (vinN.equals(n)) {
                    //收款金额
                    BigDecimal value = parentVout.getValue();
                    sumvin = sumvin.add(value);
                    RawTransaction.VoutBean.ScriptPubKeyBean scriptPubKey = parentVout.getScriptPubKey();
                    List<String> addresses = scriptPubKey.getAddresses();
                    fromAddresses.addAll(addresses);
                }
            }

        }

        //vout
        for (RawTransaction.VoutBean vout : vouts) {
            Integer n = vout.getN();
            RawTransaction.VoutBean.ScriptPubKeyBean scriptPubKey = vout.getScriptPubKey();
            String hex = scriptPubKey.getHex();
            String type = scriptPubKey.getType();//usdt  nulldata
            //btc交易
            List<String> addresses = scriptPubKey.getAddresses();
            //金额
            BigDecimal value = vout.getValue();
            //汇总输出金额
            sumvout = sumvout.add(value);


        }

        log.info("交易hash {} 总输入{} ,总输出{} 手续费{}", tx, sumvin, sumvout, sumvin.subtract(sumvout));

    }
}
