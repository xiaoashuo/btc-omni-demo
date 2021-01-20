package com.lovecyy.service;

import cn.hutool.json.JSONUtil;
import com.lovecyy.OmniWalletApplication;
import com.lovecyy.config.CoinRpcClient;
import com.lovecyy.config.JsonRpcHttpClientEnhanche;
import com.lovecyy.model.pojo.InputTransaction;
import com.lovecyy.model.pojo.SignResult;
import com.lovecyy.model.pojo.SimpleUnspentTransaction;
import com.lovecyy.model.pojo.UnspentTransaction;
import com.lovecyy.model.pojo.omni.OmniBalance;
import com.lovecyy.model.pojo.omni.OmniTokenBalance;
import com.lovecyy.model.pojo.omni.OmniTransaction;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = OmniWalletApplication.class)
class OmniServiceTest {

    @Autowired
    private CoinRpcClient coinRpcClient;

    @Autowired
    private OmniService omniService;

    @Autowired
    private BtcService btcService;


    @Test
    public void btcServiceTest() throws Throwable {
        String address="2Mw3TeWtsJwJ6C7WE8cpjMhS2X4cWk87NLC";
        OmniBalance omniBalance = omniService.omniGetBalance(1, address);
        System.out.println(omniBalance);
        String txId="63d7e22de0cf4c0b7fd60b4b2c9f4b4b781f7fdb8be4bcaed870a8b407b90cf1";
      Object objs=  coinRpcClient.getClient().invoke("omni_gettransaction",new Object[]{txId},Object.class);
        System.out.println(objs);
    }

    @Test
    void allBalanceByAddress(){
        String address="2Mw3TeWtsJwJ6C7WE8cpjMhS2X4cWk87NLC";

        List<OmniTokenBalance> allBalancesForAddress = omniService.getAllBalancesForAddress(address);
        System.out.println(allBalancesForAddress);
    }

    @Test
    void listBlockTx() throws Throwable {
        List<String> txs = omniService.listBlockTransactions(279007);
     //   Object omni_listblocktransactions = coinRpcClient.getClient().invoke("omni_listblocktransactions", new Object[]{279007}, Object.class);

//        List<String> txs = JSONUtil.toList(JSONUtil.parseArray(omni_listblocktransactions), String.class);
        for (String tx : txs) {
            OmniTransaction transaction = omniService.getTransaction(tx);
            //OmniTransaction omni_gettransaction = coinRpcClient.getClient().invoke("omni_gettransaction", new Object[]{tx}, OmniTransaction.class);
            System.out.println(transaction);
        }

        System.out.println(txs);
    }



    @Test
    void validateAddress() throws Throwable {


//        Object importaddress = coinRpcClient.getClient().invoke("importaddress", new Object[]{"miBEA6o6nZcaLZebR1dsDv4AMHRwJk1mbi","",true}, Object.class);
//        System.out.println(importaddress);

        List<UnspentTransaction> unspentTransactions = btcService.listUtxo(0, 9999999, new String[]{"miBEA6o6nZcaLZebR1dsDv4AMHRwJk1mbi"});
        for (UnspentTransaction unspentTransaction : unspentTransactions) {
            System.out.println(unspentTransaction);
        }
    }

    /**
     *
     */
    @Test
    public void signRawTransaction(){
        //发起交易地址
        String sendAddress="2Mw3TeWtsJwJ6C7WE8cpjMhS2X4cWk87NLC";
        String receiveAddress="miBEA6o6nZcaLZebR1dsDv4AMHRwJk1mbi";

        //未花费的输出列表
        List<UnspentTransaction> unspentTransactions = btcService.listUtxo(1, 9999999, new String[]{sendAddress});
        //0.00000546 btc  ==546聪 手续费
        BigDecimal fee = BigDecimal.valueOf(
                0.00000546);
        int propertyId=2;
        BigDecimal transferAmount=BigDecimal.valueOf(0.01).multiply(BigDecimal.TEN.pow(8));
        //测试链
        TestNet3Params testNet3Params = TestNet3Params.get();
        Transaction transaction = new Transaction(testNet3Params);
        if (unspentTransactions==null||unspentTransactions.size()==0){
            throw new RuntimeException("utxo为空");
        }
        //这是比特币的限制最小转账金额，所以很多usdt转账会收到一笔0.00000546的btc
        long miniBtc=546L;
        transaction.addOutput(Coin.valueOf(miniBtc), Address.fromString(testNet3Params,receiveAddress));
        //构建usdt输出脚本 注意 这里的金额是乘10的8次方
        String usdtHex="6a146f6d6e69"+ String.format("%016x", propertyId)+String.format("%016x",transferAmount.longValue());
        Script script = new Script(Utils.HEX.decode(usdtHex));
        transaction.addOutput(Coin.valueOf(0L),script);
        //找零费用
        long changeAmount=0L;
        //miniBtc+
        for (UnspentTransaction unspentTransaction : unspentTransactions) {

        }

    }

    /**
     * omni币转账是附加在btc上的所以只需要在满足btc限制的最小转账金额 即可 eg546聪
     * omni创建原生代币签名交易 步骤
     *  步骤1: 创建usdt交易 createPayloadSimpleSend
     *  步骤2：创建交易 createRawTransaction
     *  步骤3: usdt交易附加到btc上 createRawtx_opreturn
     *  步骤4: 设置归总地址 createRawtx_reference
     *  步骤5：填写手续费及找零地址 createRawtx_change
     *  步骤6: singRawtransaction 获取原生交易hex
     *  步骤7: 广播交易 sendRawTransaction
     */
    @Test
    public void createRawTransactionTest() throws Throwable {
        //发起交易地址
        String address="2Mw3TeWtsJwJ6C7WE8cpjMhS2X4cWk87NLC";
        //退回 找零地址
        String changeAddress="2Mw3TeWtsJwJ6C7WE8cpjMhS2X4cWk87NLC";
        //接收地址
        String receiveAddress="miBEA6o6nZcaLZebR1dsDv4AMHRwJk1mbi";
//        String address="miBEA6o6nZcaLZebR1dsDv4AMHRwJk1mbi";
//        String chargeAddress="miBEA6o6nZcaLZebR1dsDv4AMHRwJk1mbi";
//        String receiveAddress="2Mw3TeWtsJwJ6C7WE8cpjMhS2X4cWk87NLC";
        BigDecimal omniOutValue=BigDecimal.valueOf(0.01);
        BigDecimal fee = BigDecimal.valueOf(
                0.00000546);
        List<UnspentTransaction> unspentTransactions = btcService.listUtxo(1, 9999999, new String[]{address});
        UnspentTransaction unspentTransaction=new UnspentTransaction();
        for (UnspentTransaction utxo : unspentTransactions) {
            BigDecimal amount = utxo.getAmount();
            if (amount.compareTo(fee)>0){
                unspentTransaction.setTxid(utxo.getTxid());
                unspentTransaction.setVout(utxo.getVout());
                unspentTransaction.setScriptPubKey(utxo.getScriptPubKey());
                unspentTransaction.setAmount(utxo.getAmount());
            }
        }
        //判断手续费是否符合
        ////计算字节大小和费用(因为是归集USDT 所以我用最小的输入来降低手续费，如果你是BTC和USDT一起归总那就要根据归集的输入来计算了)
        //BigDecimal fee = btcService.calculationFee(1);

        //将聪换算成BTC
        BigDecimal transferFee = fee.divide(new BigDecimal("100000000"), 8, RoundingMode.HALF_UP);
        JsonRpcHttpClientEnhanche client = coinRpcClient.getClient();
         /*//解锁钱包
            client.invoke("walletpassphrase", new Object[]{"xxxx", 100}, Object.class);*/
        //1.创建usdt交易
        String simpleSendResult = client.invoke("omni_createpayload_simplesend", new Object[]{2, omniOutValue.toString()}, String.class);
        //2.创建BTC裸交易
        InputTransaction inputTransaction = new InputTransaction(unspentTransaction.getTxid(),unspentTransaction.getVout());
        List<InputTransaction> inputs=new ArrayList<>();
        inputs.add(inputTransaction);
        //输出 K->address v->金额
        Map<String,BigDecimal> outputs=new HashMap<>();
        String createrawtransaction = client.invoke("createrawtransaction", new Object[]{inputs,outputs}, String.class);
        //3.usdt 附加到btc交易上  也就是给交易添加payload
        //在交易数据中加上omni代币数据,从第1步获取有效负载，从第2步获取基本事务。 **/
        String createrawtxOpreturn = client.invoke("omni_createrawtx_opreturn", new Object[]{createrawtransaction,simpleSendResult}, String.class);
        //4.设置归总地址 在扩展的交易数据上面添加接收地址
        String createrawtxReference = client.invoke("omni_createrawtx_reference", new Object[]{createrawtxOpreturn, receiveAddress}, String.class);
        //5.设置手续费和找零地址
        List<SimpleUnspentTransaction> lists=new ArrayList<>();
        SimpleUnspentTransaction simpleUnspentTransaction = new SimpleUnspentTransaction(unspentTransaction.getTxid(),unspentTransaction.getVout(),unspentTransaction.getScriptPubKey(),unspentTransaction.getAmount());
        lists.add(simpleUnspentTransaction);
        String createrawtxChange = client.invoke("omni_createrawtx_change", new Object[]{createrawtxReference, lists, changeAddress, fee.toString()}, String.class);
       //6.签名交易
        SignResult signrawtransaction = client.invoke("signrawtransaction", new Object[]{createrawtxChange}, SignResult.class);
        System.out.println(signrawtransaction);

        //7.广播交易
//        String txId = client.invoke("sendrawtransaction", new Object[]{signrawtransaction.getHex()}, String.class);
//        System.out.println(txId);
    }


}