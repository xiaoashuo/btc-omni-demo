package com.lovecyy.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.HexUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.lovecyy.OmniWalletApplication;
import com.lovecyy.config.CoinRpcClient;
import com.lovecyy.enums.AddressType;
import com.lovecyy.model.pojo.BlockInfo;
import com.lovecyy.model.pojo.RawTransaction;
import com.lovecyy.model.pojo.UnspentTransaction;
import lombok.Data;
import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(classes = OmniWalletApplication.class)
class BtcServiceTest {

    @Autowired
    private CoinRpcClient coinRpcClient;

    @Autowired
    private BtcService btcService;


    @Test
    public void btcServiceTest() throws Throwable {
        Object dumpprivkey = coinRpcClient.getClient().invoke("dumpprivkey", new Object[]{Customize.initAddress().getSendAddress()}, Object.class);
        System.out.println(dumpprivkey);
        String WIFPrivateKey="Kwp2iYJSk8qW4zfiUmJKQX4Rd9fivvLGeuRvAhkJreBdQwEdh3ij";

        ECKey ecKey = DumpedPrivateKey.fromBase58(MainNetParams.get(), WIFPrivateKey).getKey();
        String addressByPublicKey = btcService.getAddressByPublicKey(MainNetParams.get(), ecKey, AddressType.SINGLE);

//        String publicKeyAsHex1 = ECKey.fromPrivate(privateKey.getPrivKey(), true).getPublicKeyAsHex();
        String publicKeyAsHex = ecKey.getPublicKeyAsHex();
        LegacyAddress address = LegacyAddress.fromPubKeyHash(TestNet3Params.get(), ecKey.getPubKeyHash());

        System.out.println(address);
//        btcService.scanBlock();
        RawTransaction rawTransaction = btcService.getRawTransaction("63d7e22de0cf4c0b7fd60b4b2c9f4b4b781f7fdb8be4bcaed870a8b407b90cf1");
        System.out.println(rawTransaction);
//        Integer blockCount = btcService.getBlockCount();
//          System.out.println(blockCount);
//        String blockHash = btcService.getBlockHash(279007);
//        BlockInfo getblock = btcService.getblock( blockHash);
//        List<String> txs = getblock.getTx();
//        for (String tx : txs) {
//            RawTransaction rawTransaction = btcService.getRawTransaction(tx);
//            System.out.println(rawTransaction);
//        }
//
//        System.out.println(getblock);

    }



    @Test
    void validateAddress() throws Throwable {
        String address="2Mw3TeWtsJwJ6C7WE8cpjMhS2X4cWk87NLC";
        Integer propertyid=2;
        Object omni_getbalance = coinRpcClient.getClient().invoke("omni_getbalance", new Object[]{address, propertyid}, Object.class);
        System.out.println(omni_getbalance);
        Object getblockcount = coinRpcClient.getClient().invoke("getblockcount", new Object[]{}, Object.class);
        System.out.println(getblockcount);
        String[] addresses={address};
        //得到未花费的输出
        List<UnspentTransaction> unspentTransactions = btcService.listUtxo(1, 9999999, addresses);

        System.out.println(unspentTransactions);


    }



    @Test
    public void createRawTransactionTest() throws Throwable {
//        Object dumpprivkey = coinRpcClient.getClient().invoke("dumpprivkey", new Object[]{"2Mw3TeWtsJwJ6C7WE8cpjMhS2X4cWk87NLC"}, Object.class);
//        System.out.println(dumpprivkey);
        Long feeRate = btcService.getFeeRate();
        System.out.println(feeRate);
    }

    @Test
    public void generateBtcWallet(){
        MainNetParams mainNetParams = MainNetParams.get();
    }

    @Data
    public static class Customize{
        //发送地址
        private String sendAddress;
        /**
         * 发送人私钥
         */
        private String sendPrivateKey;
        //接收地址
        private String receiveAddress;
        //手续费地址
        private String feeAddress;
        //找零地址
        private String changeAddress;

        public static Customize initAddress(){
            Customize customize = new Customize();
            customize.setSendAddress("2Mw3TeWtsJwJ6C7WE8cpjMhS2X4cWk87NLC");
            customize.setReceiveAddress("mjYkD95Zn76o13j7Mz4P3QSiENu1GEDcEa");
            customize.setChangeAddress("2Mw3TeWtsJwJ6C7WE8cpjMhS2X4cWk87NLC");
            return customize;
        }

    }



    @Test
    public void signTransaction(){
        Customize customize = Customize.initAddress();
        TestNet3Params testNet3Params = TestNet3Params.get();

        BigDecimal fee=BigDecimal.valueOf(0);
        BigDecimal transferAmount=BigDecimal.valueOf(0.00000038);
        BigDecimal totalOutAmount=BigDecimal.valueOf(0);
        BigDecimal changeAmount=BigDecimal.valueOf(0);
        //总输入
        BigDecimal totalInput = fee.add(transferAmount);
        //计算 需要的交易 以及总输出
        List<UnspentTransaction> unspentTransactions = btcService.listUtxo(1, 9999999, new String[]{customize.getSendAddress()});
        List<UnspentTransaction> needUnspentTransactions=new ArrayList<>();
        for (UnspentTransaction unspentTransaction : unspentTransactions) {
            if (totalOutAmount.compareTo(totalInput)>=0){
                break;
            }
            BigDecimal amount = unspentTransaction.getAmount();
            needUnspentTransactions.add(unspentTransaction);
            totalOutAmount=totalOutAmount.add(amount);
        }
        //总输出小于总输入 说明余额不足
        if (totalOutAmount.compareTo(totalInput)<0){
            throw new RuntimeException("当前用户余额不足");
        }
        //计算找零数量
        changeAmount=totalOutAmount.subtract(totalInput);
        //构造输出交易
        Transaction transaction = new Transaction(testNet3Params);
        transaction.addOutput(Coin.valueOf(transferAmount.longValue()), Address.fromString(testNet3Params, customize.receiveAddress));
        long satoshis = changeAmount.longValue();
        if (satoshis>0){
            transaction.addOutput(Coin.valueOf(satoshis), Address.fromString(testNet3Params, customize.getChangeAddress()));
        }
        //构造输入
        DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(testNet3Params, customize.getSendPrivateKey());
        ECKey ecKey = dumpedPrivateKey.getKey();
        for (UnspentTransaction utxo : needUnspentTransactions) {
            String scriptPubKey = utxo.getScriptPubKey();
            Script script = new Script(Utils.HEX.decode(scriptPubKey));

            TransactionOutPoint outPoint = new TransactionOutPoint(testNet3Params, utxo.getVout().longValue(), Sha256Hash.wrap(utxo.getTxid()));
            transaction.addSignedInput(outPoint, script, ecKey, Transaction.SigHash.ALL, true);
        }
        byte[] bytes = transaction.bitcoinSerialize();
        //这是签名之后的原始交易，直接去广播就行了
        String signHex = Hex.toHexString(bytes);
        //这是交易的hash
        String txHash = Hex.toHexString(Utils.reverseBytes(Sha256Hash.hash(Sha256Hash.hash(bytes))));

        //广播hash

    }

    /**
     * 参考链接: https://segmentfault.com/a/1190000019291453
     * previous utxo lock_script === next input unlock_script
     * tips:创建btc交易
     * 1.在比特币钱包中查看所有的UTXO
     * 2.查看一个特定的UTXO的细节
     * 3.创建一个原始(裸)交易
     * 4.对该原始交易进行签名
     * 5.将这个交易提交到网络
     * 6.通过txId查询所创建的交易
     * @throws Throwable
     */
    @Test
    public void createSignTransaction() throws Throwable{
        //发送地址
        String sendAddress="2Mw3TeWtsJwJ6C7WE8cpjMhS2X4cWk87NLC";
        //接收地址
        String receiveAddress="";
        //私钥
        String privateKey="";
        //转账金额
        BigDecimal sendAmount=BigDecimal.valueOf(0.00001);
        //手续费
        BigDecimal chargeFee =BigDecimal.valueOf(0.00001);
        //总输出
        BigDecimal outAmount=sendAmount.add(chargeFee);
        //总输入
        BigDecimal inputAmount=BigDecimal.ZERO;
        //找零金额=总输入-总输出=总输入-转出金额-手续费
        BigDecimal giveChange=BigDecimal.ZERO;
        //1.查看所有的未花费的输出
        List<UnspentTransaction> unspentTransactions = btcService.listUtxo(1, 99999999, new String[]{sendAddress});
        //总需要手续费
        BigDecimal fee = btcService.getFee(outAmount, unspentTransactions);
        //总输入列表
        List<UnspentTransaction> totalInput=new ArrayList<>();
        for (UnspentTransaction unspentTransaction : unspentTransactions) {
            if (inputAmount.compareTo(outAmount)>=0){
                break;
            }
            BigDecimal amount = unspentTransaction.getAmount();
            inputAmount=inputAmount.add(amount);
            totalInput.add(unspentTransaction);
        }

        MainNetParams mainNetParams = MainNetParams.get();
        DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(mainNetParams, privateKey);

    }

}