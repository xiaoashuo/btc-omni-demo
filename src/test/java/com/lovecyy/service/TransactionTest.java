package com.lovecyy.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import com.lovecyy.model.pojo.RawTransaction;
import com.lovecyy.model.pojo.SignatureData;
import com.lovecyy.model.pojo.UTXOKey;
import com.lovecyy.model.pojo.UnspentTransaction;
import com.lovecyy.model.pojo.accounts.Account;
import com.lovecyy.model.pojo.accounts.P2SHMultiSigAccount;
import com.lovecyy.utils.AddressUtils;
import com.lovecyy.utils.Converter;
import com.lovecyy.utils.TransactionUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bouncycastle.util.encoders.Hex;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.*;

import static org.bitcoinj.core.Utils.HEX;

/**
 * 测试多种地址类型之间的转账
 * 1、P2PKH+P2WPKH ----P2PKH+P2WPKH
 * 2、P2PKH+P2WPKH ----Multisign
 * 3、MultiSign   -----P2PKH+P2WPKH
 * 4、MultiSign  -----MultiSign
 */

@SpringBootTest
public class TransactionTest {


    @Autowired
    private  TransactionUtils transactionUtils;

    @Autowired
    private AddressUtils addressUtils;

    private NetworkParameters networkParameters;


    @Autowired
    private  BtcService btcService;

    /**
     * P2PKH 主地址
     * address=> miBEA6o6nZcaLZebR1dsDv4AMHRwJk1mbi
     * private key wif => 27b5bf6853c1730c20c152d67d9f4f85b20b674267f621bd1c88d177b2d56d83  not compress
     * public key => 03a5be350852bb09e24edc83f8e02070a74597a8b775af46e8efbef394ae4fa98e
     * P2PKH 主地址
     * address=> mgpmmwSgTbVoHmKGXMwdESfe5NvpR1bv41
     * mnemonics => [shiver, priority, torch, column, use, small, liberty, crowd, cable, buzz, sponsor, squeeze]
     * public key => 023f9b953f3201642159d90fe81f71c372ff58a0456df01da6dd658a0913656080
     * private key wif => cREHhtmuUU2eDwVpscKdm4PvNzLpht8SvcYnKNQKsJz1hit98WwF
     * passphrase =>123445
     * P2PKH 次地址
     * address=> mttSfCYn3aaL1nT2VUvV8AiSkYzaRjihPP
     * mnemonics => [carpet, bracket, seat, globe, daring, salad, kangaroo, more, torch, pool, home, seed]
     * public key => 03ae0ff17b01cbe504e96139e51e266eada24f689492a0f638d0a9669234faf745
     * private key wif => cPVuaMDhgy788b39DUrCpMRHFBDdGgJhr3jszQU5wYa5YqC1CR1y
     * passphrase =>123445
     * 隔离见证地址
     * address=> tb1qhgqn3zy74tgqx6gta747hmx4mpege0xdettn9l
     * passphrase=>123456
     * mnemonics =>[copper, yellow, fury, album, girl, drastic, icon, wide, fantasy, success, achieve, actress]
     * private key wif =>cUTRWmpFxbDLwZ1Zao19V5FJMDUzKqzZt7Lm6gkQXBR55NbeJJ7X
     * public key=> 0337215aa94c8c360bddf5f3cee2fbf37b31537872a4521a67622f2d2d2e59c908
     * 隔离见证地址2
     * address=> tb1qwhx7xk3pz0lj2jss9qvp82x8ecjjv6rvntv643
     * passphrase=>123456
     * mnemonics =>[entire, magic, because, pass, dust, mosquito, tuna, gesture, cage, vacuum, siege, faint]
     * private key wif =>cQz9PPWCQmAQfuiN5uCfZPFdydFxvp1F38rWGRPei8qDBYAJjXbz
     * public key=> 032a2289b70de8e28a7e9f0c21482106e56616f8d81252dc89bb8f341419c952f9
     * 多签地址1
     * address=>2N7zfvXeKE3LSUvwZrisNyuS9vhp1BoZ4oD
     * redeemScript=> 1 PUSHDATA(33)[02f1217b1b4d095b10092e2301ed3b2594e73dbe7378c0661e2f482cbe24ec05a0] PUSHDATA(33)[02f4c7f8ab68f04e75b334462b9fc5cd05be5c86abf62c86346843c655b0d61597] PUSHDATA(33)[03c6b6f2bae1a806fc7a0275a8eb1b14ca9d9cbf6739ae9c2930bba61c7d2f3c84] 3 CHECKMULTISIG
     * a1={
     *     address=>mh7s56ZuVodqSQ2sDRgmeqfdwKnNDz4P11
     *     passphrase=>123456
     *     publicKey=>03c6b6f2bae1a806fc7a0275a8eb1b14ca9d9cbf6739ae9c2930bba61c7d2f3c84
     *     mnemonic=>[bone, interest, excite, swallow, tattoo, abandon, today, quick, vendor, inflict, thing, select]
     *     private key=>cMxruYjRCsPvWHkRv1W6DtooJzKsbhNKBCJVMSjaS4nEKbwA5dvZ
     * }
     * a2=>{
     *       address=>mjwNUQtQXV5DDfb8ibYvDZxASWfjdPPJRp
     *       passphrase=>123456
     *       publicKey=>02f1217b1b4d095b10092e2301ed3b2594e73dbe7378c0661e2f482cbe24ec05a0
     *       mnemonic=>[virtual, deputy, tuna, expand, ancient, pond, waste, cry, camp, genius, tilt, street]
     *       private key=>cQ6T9Bcr5Aeb51ee2e7MyfJ9mDR875tUG4o8W3kGJ35ajMcPs68z
     * }
     * a3=>{
     *     address=>muPAkEjZP5rHWGZ7RiyumY1Ff9MFxHpMeD
     *     passphrase=>123456
     *     publicKey=>02f4c7f8ab68f04e75b334462b9fc5cd05be5c86abf62c86346843c655b0d61597
     *     mnemonic=>[surprise, scan, banner, famous, mutual, tonight, logic, act, inject, glide, blouse, puppy]
     *     private key=>cNQ3zWKgA66rNAJAAJfVhZ6wKp6Fg2CHCFN2WXw26nuLkPYZQqTP
     * }
     * 多签地址2
     *   address=>2N6yHCWzD85x5Sjz3HuCLwctD899qrDeLCN
     *   redeemScript=> 2 PUSHDATA(33)[0345f38f33cccc81a36a382b21e5dc5a712908ff0d944a96a4800cc535ba3e1728] PUSHDATA(33)[0348eb0e44ddcdaff15dbfe8b93cf47475295c7c6b9043d8f6a8c8d02fd67582c8] PUSHDATA(33)[03f9798ea3443506bbf52de99f0eaea2106c3e1ff0f220a758686c93812f276758] 3 CHECKMULTISIG
     *   a1={
     *       address=>mxMqgLpAJjpNNn88KXaqPLjYsZMrPyf2Sx
     *       passphrase=>123456
     *       publicKey=>0345f38f33cccc81a36a382b21e5dc5a712908ff0d944a96a4800cc535ba3e1728
     *       mnemonic=>[open, portion, tomorrow, seek, loyal, type, earn, barrel, palm, idle, mule, pipe]
     *       private key=>cTduivC9ac5gvPESqNjqe3twnWfY782B1rtqd8Z9gie1W2WxogeG
     *   }
     *   a2=>{
     *         address=>myHDqczKiepmvP2fGY6qovZcS6S28f5dqN
     *         passphrase=>123456
     *         publicKey=>0348eb0e44ddcdaff15dbfe8b93cf47475295c7c6b9043d8f6a8c8d02fd67582c8
     *         mnemonic=>[patient, bid, service, news, pizza, cage, topple, soon, shed, decrease, inside, scatter]
     *         private key=>cRenk8vU1rWkM3XdWpHfGqC4wdHWxT149DSF7ydiegsb3Jwa9GDG
     *   }
     *   a3=>{
     *       address=>mgxGcDyU3CNSLuMw1q9tLCfoRcErhBtcYS
     *       passphrase=>123456
     *       publicKey=>03f9798ea3443506bbf52de99f0eaea2106c3e1ff0f220a758686c93812f276758
     *       mnemonic=>[slide, viable, phrase, candy, admit, main, desk, where, fetch, online, frozen, wire]
     *       private key=>cSFLsJRhVzX1ZD3L8WVJF4mcBviCDS1jUjDH2mNeTAcVMtnVVszb
     *   }
     * 多签地址A2 两个人签名
     *   address=>2N46296qXfUPQFVGpy9pZcbap4wnYrgkqJJ
     *   redeemScript=> 2 PUSHDATA(33)[024bc8644a7e7af62fdfad00ae09154ed567d5ad8d6bb40d04a80cdfae3b90bcbc] PUSHDATA(33)[036b1a8299b53253ff6317463638e1a0961db63279dd21c62591a6f90cbaec1325] PUSHDATA(33)[03a0f8bc951ba9a26bf9ca4f1c4bf959de91847a74a321cc7f355fa8ce365c2939] 3 CHECKMULTISIG
     *   a1={
     *       address=>mttVySBtZHpMxdtMScxwrNQmXg2V1hfmf6
     *       passphrase=>123456
     *       publicKey=>024bc8644a7e7af62fdfad00ae09154ed567d5ad8d6bb40d04a80cdfae3b90bcbc
     *       mnemonic=>[human, spin, woman, memory, wealth, leopard, walnut, estate, sunset, able, foam, surface]
     *       private key=>cRzsYxircLjYotcxdYfu99sQWKEHDdBiUqZczkMhHbcLJPfnn7rv
     *   }
     *   a2=>{
     *         address=>n3V76KgHjyoqgzJeW4crh6GbL6sSQs4fFJ
     *         passphrase=>123456
     *         publicKey=>03a0f8bc951ba9a26bf9ca4f1c4bf959de91847a74a321cc7f355fa8ce365c2939
     *         mnemonic=>[inmate, myth, slam, glimpse, glass, erode, million, mirror, hen, abandon, palm, brand]
     *         private key=>cRttDzjDk62LqFo2Y9GvgRSi3yYcqwrkA6kvcysUaTQGQFqTNNja
     *   }
     *   a3=>{
     *       address=>mqW2WJDh3QMVDLDs3vVknC58LNjr9JkNKG
     *       passphrase=>123456
     *       publicKey=>036b1a8299b53253ff6317463638e1a0961db63279dd21c62591a6f90cbaec1325
     *       mnemonic=>[area, reveal, merge, rule, goddess, coil, magnet, clock, erase, universe, steak, absent]
     *       private key=>cTQGcsJyCNqZKwph87YqFi6F1qiCuNBzpdac2vHa7mCWkknEVcQP
     *   }
     */

    private Account a1Main;

    private Account segWit1Address;

    private Account segWit2Address;

    private P2SHMultiSigAccount multiSigAccountA;
    private P2SHMultiSigAccount multiSigAccountA2;


    private P2SHMultiSigAccount multiSigAccountB;
    private P2SHMultiSigAccount multiSigAccountB2;


    @BeforeEach
    public void setUp(){
        networkParameters=TestNet3Params.get();
        a1Main=new Account().setAddress("miBEA6o6nZcaLZebR1dsDv4AMHRwJk1mbi").setPublicKey("03a5be350852bb09e24edc83f8e02070a74597a8b775af46e8efbef394ae4fa98e")
                //未压缩
                .setPrivateKey("27b5bf6853c1730c20c152d67d9f4f85b20b674267f621bd1c88d177b2d56d83");

        segWit1Address=new Account().setAddress("tb1qhgqn3zy74tgqx6gta747hmx4mpege0xdettn9l")
                .setPassphrase("123456").setPrivateKey("cUTRWmpFxbDLwZ1Zao19V5FJMDUzKqzZt7Lm6gkQXBR55NbeJJ7X")
                .setPublicKey("0337215aa94c8c360bddf5f3cee2fbf37b31537872a4521a67622f2d2d2e59c908")
                .setMnemonics("copper, yellow, fury, album, girl, drastic, icon, wide, fantasy, success, achieve, actress");

        segWit2Address=new Account().setAddress("tb1qwhx7xk3pz0lj2jss9qvp82x8ecjjv6rvntv643")
                .setPassphrase("123456")
                .setPrivateKey("cQz9PPWCQmAQfuiN5uCfZPFdydFxvp1F38rWGRPei8qDBYAJjXbz")
                .setPublicKey("032a2289b70de8e28a7e9f0c21482106e56616f8d81252dc89bb8f341419c952f9")
                .setMnemonics("entire, magic, because, pass, dust, mosquito, tuna, gesture, cage, vacuum, siege, faint");
        ECKey ecKeyA = addressUtils.publicKeyToECKey("03c6b6f2bae1a806fc7a0275a8eb1b14ca9d9cbf6739ae9c2930bba61c7d2f3c84");
        ECKey ecKeyB = addressUtils.publicKeyToECKey("02f1217b1b4d095b10092e2301ed3b2594e73dbe7378c0661e2f482cbe24ec05a0");
        ECKey ecKeyC = addressUtils.publicKeyToECKey("02f4c7f8ab68f04e75b334462b9fc5cd05be5c86abf62c86346843c655b0d61597");
        multiSigAccountA = addressUtils.generateMultiSigAddress(TestNet3Params.get(),1, ListUtil.toList(ecKeyA, ecKeyB, ecKeyC));
        ECKey ecKeyBA = addressUtils.publicKeyToECKey("0345f38f33cccc81a36a382b21e5dc5a712908ff0d944a96a4800cc535ba3e1728");
        ECKey ecKeyBB = addressUtils.publicKeyToECKey("0348eb0e44ddcdaff15dbfe8b93cf47475295c7c6b9043d8f6a8c8d02fd67582c8");
        ECKey ecKeyBC = addressUtils.publicKeyToECKey("03f9798ea3443506bbf52de99f0eaea2106c3e1ff0f220a758686c93812f276758");
        multiSigAccountB = addressUtils.generateMultiSigAddress(TestNet3Params.get(),1, ListUtil.toList(ecKeyBA, ecKeyBB, ecKeyBC));
        ECKey ecKeyA2 = addressUtils.publicKeyToECKey("024bc8644a7e7af62fdfad00ae09154ed567d5ad8d6bb40d04a80cdfae3b90bcbc");
        ECKey ecKeyB2 = addressUtils.publicKeyToECKey("03a0f8bc951ba9a26bf9ca4f1c4bf959de91847a74a321cc7f355fa8ce365c2939");
        ECKey ecKeyC2 = addressUtils.publicKeyToECKey("036b1a8299b53253ff6317463638e1a0961db63279dd21c62591a6f90cbaec1325");
        multiSigAccountA2=addressUtils.generateMultiSigAddress(TestNet3Params.get(),2,ListUtil.toList(ecKeyA2,ecKeyB2,ecKeyC2));

    }


    /**
     * P2PKH 发起转账 可以发给多签 隔离
     */
    @DisplayName("P2PKHP2PKH")
    @Test
    public void P2PKH_P2PKH(){
        Double accountAmoney=0.0001;
        Double accountBmoney=0.0003;
        Double changeMoney=0d;
        Double totalOutMoney=accountAmoney+accountBmoney;
        Double totalInputMoney=0D;
        //手续费
        Double chargeMoney=Converter.satoshisToBitcoin(666L);

        ECKey ecKey = ECKey.fromPrivate(Hex.decode(a1Main.getPrivateKey()));


        //构建UTXO key列表
        List<UTXOKey> utxos=new ArrayList<>();
        List<UnspentTransaction> unspentTransactions = btcService.listUtxo(0, 9999999, new String[]{a1Main.getAddress()});
        System.out.println(unspentTransactions);

        for (UnspentTransaction unspentTransaction : unspentTransactions) {
            if (totalInputMoney>=totalOutMoney){
                break;
            }
            UTXO utxo = new UTXO(
                    Sha256Hash.wrap(unspentTransaction.getTxid()),
                    unspentTransaction.getVout(),
                    Coin.valueOf(Converter.bitcoinToSatoshis(unspentTransaction.getAmount().doubleValue())),
                    0,
                    false,
                    new Script(Hex.decode(unspentTransaction.getScriptPubKey()))
            );
            utxos.add(new UTXOKey(utxo,ecKey));
            totalInputMoney+=unspentTransaction.getAmount().doubleValue();
        }

        //找零金额
        changeMoney = totalInputMoney - totalOutMoney-chargeMoney;
       // Assert.isTrue(changeMoney>=0,"用户余额不足支付手续费");
        //接收地址 和 金额列表
        Map<String,Double> receiveAddressAndValue=new HashMap<>();
        receiveAddressAndValue.put("mgpmmwSgTbVoHmKGXMwdESfe5NvpR1bv41",accountAmoney);
        receiveAddressAndValue.put("2N7zfvXeKE3LSUvwZrisNyuS9vhp1BoZ4oD",accountBmoney);
        //只有当需要找零才构建找零交易  若刚好消费完就没必要构建找零交易了
        if (changeMoney>0){
            receiveAddressAndValue.put("miBEA6o6nZcaLZebR1dsDv4AMHRwJk1mbi",changeMoney);
        }


        System.out.println(utxos);
        Transaction transaction = transactionUtils.buildLegacyTransactionWithSigners(networkParameters, utxos, receiveAddressAndValue);

        byte[] rawTransactionHex =transaction.bitcoinSerialize();
        //交易hash
        String hash = Hex.toHexString(transaction.bitcoinSerialize());
      Object o = btcService.sendrawTransaction(hash);
        System.out.println("结果=>"+o);

    }


    /**
     * P2WPKH----P2WPKH
     */
    @DisplayName("隔离见证到隔离见证")
    @Test
    public void P2WPKH_P2WPKH(){

        Double segWit2AddressAmount=0.00001;
        Double totalInputMoney=0D;
        Double totalOutMoney=segWit2AddressAmount;
        Double changeMoney=0D;
        //手续费
        Double chargeMoney=Converter.satoshisToBitcoin(999L);
        //segWit1Address 接收0.00029999 交易id 85af4024fcf66379623e5eb1ec9c90849dbb5063d7052d1cebcdc3646cfd37f5
        ECKey ecKeyA = addressUtils.getECKeyFromPrivateKeyWif(networkParameters, segWit1Address.getPrivateKey());
        ECKey ecKeyB = addressUtils.getECKeyFromPrivateKeyWif(networkParameters, segWit2Address.getPrivateKey());
        List<UnspentTransaction> unspentTransactions = btcService.listUtxo(0, 9999999, new String[]{segWit1Address.getAddress()});
        System.out.println(unspentTransactions);
        List<UTXOKey> utxoKeys=new ArrayList<>();
        for (UnspentTransaction unspentTransaction : unspentTransactions) {
            if (totalInputMoney>=totalOutMoney){
                break;
            }
            UTXO utxo = new UTXO(
                    Sha256Hash.wrap(unspentTransaction.getTxid()),
                    unspentTransaction.getVout(),
                    Coin.valueOf(Converter.bitcoinToSatoshis(unspentTransaction.getAmount().doubleValue())),
                    0,
                    false,
                    new Script(Hex.decode(unspentTransaction.getScriptPubKey()))
            );
            utxoKeys.add(new UTXOKey(utxo,ecKeyA));
            totalInputMoney+=unspentTransaction.getAmount().doubleValue();
        }
        //找零金额
        changeMoney = totalInputMoney - totalOutMoney-chargeMoney;
        //接收地址和金额列表
        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put(segWit2Address.getAddress(),segWit2AddressAmount);
        //只有当需要找零才构建找零交易  若刚好消费完就没必要构建找零交易了
        if (changeMoney>0){
            receiveAddressAndValue.put(segWit1Address.getAddress(),changeMoney);
        }
        Transaction transaction = transactionUtils.buildLegacyTransactionWithSigners(networkParameters, utxoKeys, receiveAddressAndValue);

        byte[] transBytes = transaction.bitcoinSerialize();
        String hash = Hex.toHexString(transBytes);
//        System.out.println(hash);
//      Object o = btcService.sendrawTransaction(hash);
//        System.out.println("结果=>"+o);
    }


    /**
     * P2WPKH --- P2PKH
     */
    @DisplayName("隔离见证到普通地址")
    @Test
    public void P2WPKH_P2PKH(){
String hash="0100000001ce65cf4e28dbc3ecf05977913a5afbdf42fe8f270b58752a149b88f83d7dd41502000000fdfe0000483045022100f0e40a5948261cc32149dbde7dbb53acb1400606aa4df4de8428b38727e30e5d0220746e64cb1de0201e64a3656619904a17124e5b653ae9bd3a1bb46359f449c08901483045022100e4ee001081471ed364c4b7767ce91dbfe8c99e5bf265de6041b3b630a59c053202201c89d484ac4b05e98695a5efa881d5ee7fde87a43856cec7b4dd8e1a3b3a32d2014c69512102f1217b1b4d095b10092e2301ed3b2594e73dbe7378c0661e2f482cbe24ec05a02102f4c7f8ab68f04e75b334462b9fc5cd05be5c86abf62c86346843c655b0d615972103c6b6f2bae1a806fc7a0275a8eb1b14ca9d9cbf6739ae9c2930bba61c7d2f3c8453aeffffffff02e8030000000000001600148235ad214acba375c1e85972c432388706e2d328e8030000000000001976a91414b4266839352fed86052061482754ef784f06ba88ac00000000";

        Object o = btcService.sendrawTransaction(hash);
        System.out.println("结果=>"+o);
    }


    @DisplayName("P2PKHP2WPKHTOP2PKHP2WPKH")
    @Test
    public void P2PKH_P2WPKH_TO_P2PKH_P2WPKH(){
        RawTransaction rawTransaction = btcService.getRawTransaction("bdd4e383ad02f7a6d9814285413910ed41bf3d0f5f29d98a61fe3c2045aaa9aa");
        System.out.println(rawTransaction);

    }

    /**
     * 普通地址+隔离见证 =>多签
     * TODO 交易失败 non-mandatory-script-verify-flag (Signature must be zero for failed CHECK(MULTI)SIG operation
     */
    @DisplayName("P2PKHP2WPKHTOMultiSign")
    @Test
    public void P2PKH_P2WPKH_TO_MultiSign(){
        Double outAmountA=0.0001;
        Double outAmountB=0.0001;
        Double totalOutMoney=outAmountA+outAmountB;
        //手续费
        Double chargeMoney=Converter.satoshisToBitcoin(546L);

        Double changeAmount=0D;


        ECKey ecA1Main = ECKey.fromPrivate(Hex.decode(a1Main.getPrivateKey()));
        ECKey segWitAddressEckey = addressUtils.getECKeyFromPrivateKeyWif(networkParameters, segWit1Address.getPrivateKey());

        UnSpentListAndTotalInput unspentA = getUnspent(a1Main.getAddress(), ecA1Main, outAmountA);
        List<UTXOKey> utxoKeysA = unspentA.getUtxoKeys();
        //找零数量未排查手续费
        Double changeAmountA = unspentA.getChangeAmount();
        //总输入金额
        Double totalInputMoneyA = unspentA.getTotalInputMoney();

        UnSpentListAndTotalInput unspentB = getUnspent(segWit1Address.getAddress(), segWitAddressEckey, outAmountB);
        // 隔离见证 总输入
        List<UTXOKey> utxoKeysB = unspentB.getUtxoKeys();
        Double totalInputMoneyB = unspentB.getTotalInputMoney();
        //隔离见证输入-输出
        Double changeAmountB = unspentB.getChangeAmount();
        //总未花费的输出
        List<UTXOKey> totalUtxoKeys=new ArrayList();
        totalUtxoKeys.addAll(utxoKeysA);
        totalUtxoKeys.addAll(utxoKeysB);
        //真实找零
         changeAmount = changeAmountA + changeAmountB - chargeMoney;
        //接收地址和金额列表
        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put(segWit2Address.getAddress(), outAmountA);
        receiveAddressAndValue.put(multiSigAccountA.getAddress().toBase58(), outAmountB);
        if (changeAmount>0){
            receiveAddressAndValue.put(a1Main.getAddress(), changeAmount);
        }
        Transaction transaction = transactionUtils.buildLegacyTransactionWithSigners(networkParameters, totalUtxoKeys, receiveAddressAndValue);
        //交易hahs
        String hash = Hex.toHexString(transaction.bitcoinSerialize());
        //交易id
        String txId = transaction.getTxId().toString();
        System.out.println(hash);

        Object o = btcService.sendrawTransaction(hash);
        System.out.println("结果=>"+o);

    }

    @Data
    public static class UnSpentListAndTotalInput{
        private Double totalInputMoney;
        /**
         * 找零数量 未扣除手续费的 及 总输入-总输出
         */
        private Double changeAmount;
        /**
         * 找零数量排除手续费的  总输入-总输出-手续费
         */
        private Double changeAmountExcludeFee;
        private List<UTXOKey> utxoKeys;
    }

    private UnSpentListAndTotalInput getUnspent(String address,ECKey ecKey,Double outAmount){
        Double totalInputMoney=0D;
        List<UnspentTransaction> unspentTransactions = btcService.listUtxo(1, 999999, new String[]{address});
        Assert.notNull(unspentTransactions,"未花费的输出为空");
        List<UTXOKey> utxoKeys=new ArrayList<>();
        for (UnspentTransaction unspentTransaction : unspentTransactions) {
            if (totalInputMoney>=outAmount){
                break;
            }
            UTXO utxo = new UTXO(
                    Sha256Hash.wrap(unspentTransaction.getTxid()),
                    unspentTransaction.getVout(),
                    Coin.valueOf(Converter.bitcoinToSatoshis(unspentTransaction.getAmount().doubleValue())),
                    0,
                    false,
                    new Script(Hex.decode(unspentTransaction.getScriptPubKey()))
            );
            utxoKeys.add(new UTXOKey(utxo,ecKey));
            totalInputMoney+=unspentTransaction.getAmount().doubleValue();
        }
        double changeAmount = totalInputMoney - outAmount;
        UnSpentListAndTotalInput unSpentListAndTotalInput = new UnSpentListAndTotalInput();
        unSpentListAndTotalInput.setTotalInputMoney(totalInputMoney);
        unSpentListAndTotalInput.setUtxoKeys(utxoKeys);
        unSpentListAndTotalInput.setChangeAmount(changeAmount);
        return unSpentListAndTotalInput;
    }

    /**
     * 测试多签交易
     * 多签 => PKP2H-P2WPKH
     */
    //TODO 失败 non-mandatory-script-verify-flag (Dummy CHECKMULTISIG argument must be zero) (code 64) because of 参数不正确 因为赎回脚本 参数不正确
    @DisplayName("MultiSignTOP2PKHP2WPKH")
    @Test
    public void MultiSign_TO_P2PKH_P2WPKH(){
        System.out.println(multiSigAccountA2);
        Double accountAMoney=0.00001;
        Double accountBMoney=0.00001;
        Double totalInputMoney=0D;
        Double totalOutMoney=accountAMoney+accountBMoney;
        //手续费
        Double chargeMoney=Converter.satoshisToBitcoin(700L);
        Double changeMoney=0D;
        //地址A
        ECKey ecKeyMultiSigA = addressUtils.getECKeyFromPrivateKeyWif(networkParameters, "cMxruYjRCsPvWHkRv1W6DtooJzKsbhNKBCJVMSjaS4nEKbwA5dvZ");
        ECKey ecKeyMultiSigB = addressUtils.getECKeyFromPrivateKeyWif(networkParameters, "cQ6T9Bcr5Aeb51ee2e7MyfJ9mDR875tUG4o8W3kGJ35ajMcPs68z");
        List<UnspentTransaction> unspentTransactions = btcService.listUtxo(1, 9999999, new String[]{multiSigAccountA.getAddress().toBase58()});
        Assert.isTrue(CollectionUtil.isNotEmpty(unspentTransactions),"账户余额不足");
        System.out.println(unspentTransactions);
        List<UTXO> utxoKeys=new ArrayList<>();
        for (UnspentTransaction unspentTransaction : unspentTransactions) {
            if (totalInputMoney>=totalOutMoney){
                break;
            }
            UTXO utxo = new UTXO(
                    Sha256Hash.wrap(unspentTransaction.getTxid()),
                    unspentTransaction.getVout(),
                    Coin.valueOf(Converter.bitcoinToSatoshis(unspentTransaction.getAmount().doubleValue())),
                    0,
                    false,
                    new Script(Hex.decode(unspentTransaction.getScriptPubKey()))
            );
            utxoKeys.add(utxo);
            totalInputMoney+=unspentTransaction.getAmount().doubleValue();
        }
        changeMoney = totalInputMoney - totalOutMoney-chargeMoney;

        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put(a1Main.getAddress(),accountAMoney);
        receiveAddressAndValue.put(segWit1Address.getAddress(),accountBMoney);
        if (changeMoney>0){
            receiveAddressAndValue.put(multiSigAccountA.getAddress().toBase58(),changeMoney);
        }
        //构建交易
        Transaction transaction = transactionUtils.buildTransaction(networkParameters, utxoKeys, receiveAddressAndValue);
        //赎回脚本 首次签名会用到
        Script redeemScript = multiSigAccountA.getRedeemScript();

        //签名多签
        transactionUtils.signMultiSigTransaction(networkParameters,transaction,redeemScript,ecKeyMultiSigA,true);
           /* System.out.println("First sign:");
        int i = 0;
        for (TransactionInput input : transaction.getInputs()) {
            System.out.println("input" + i + ":" + input.getScriptSig());
            System.out.println("input" + i + ":" + Converter.byteToHex(input.getScriptSig().getProgram()));
            i++;
        }*/
        //交易16进制 第一次签名的交易hash
        String transactionHex = Converter.byteToHex(transaction.bitcoinSerialize());
        System.out.println("第一次签名=>"+transactionHex);
        //这里模拟第二个人在其他地方对这笔交易签名
        //从十六进制的文本交易还原交易
        transaction=new Transaction(networkParameters,Converter.hexToByte(transactionHex));
        //签名 若M为1 则不需要
        transactionUtils.signMultiSigTransaction(networkParameters,transaction,null,ecKeyMultiSigB,false);
           /*System.out.println("Second sign:");
        i = 0;
        for (TransactionInput input : transaction.getInputs()) {
            System.out.println("input" + i + ":" + input.getScriptSig());
            System.out.println("input" + i + ":" + Converter.byteToHex(input.getScriptSig().getProgram()));
            i++;
        }*/
        transactionHex =Converter.byteToHex(transaction.bitcoinSerialize());
        System.out.println("Second signed transaction hex:" + transactionHex);
        String txId = transaction.getTxId().toString();
        Object o = btcService.sendrawTransaction(transactionHex);
        System.out.println("结果=>"+o);
    }

    /**
     * 消费多签地址资产
     * MultiSign_TO_P2PKH_P2WPKH2_MultiSign
     */
    @DisplayName("MultiSignTOP2PKHP2WPKH2MultiSign")
    @Test
    public void MultiSign_TO_P2PKH_P2WPKH2(){

        Double accountAMoney=0.0001;
        Double accountBMoney=0.0001;
        Double totalInputMoney=0D;
        Double totalOutMoney=accountAMoney+accountBMoney;
        //手续费
        Double chargeMoney=Converter.satoshisToBitcoin(701L);
        Double changeMoney=0D;

        LegacyAddress legacyAddress = multiSigAccountA.getAddress();
        String address = legacyAddress.toBase58();
        System.out.println(address);
        List<UnspentTransaction> unspentTransactions = btcService.listUtxo(0, 9999999, new String[]{address});
        System.out.println(unspentTransactions);
        List<UTXO> utxoKeys=new ArrayList<>();
        for (UnspentTransaction unspentTransaction : unspentTransactions) {
            if (totalInputMoney>=totalOutMoney){
                break;
            }
            UTXO utxo = new UTXO(
                    Sha256Hash.wrap(unspentTransaction.getTxid()),
                    unspentTransaction.getVout(),
                    Coin.valueOf(Converter.bitcoinToSatoshis(unspentTransaction.getAmount().doubleValue())),
                    0,
                    false,
                    new Script(Hex.decode(unspentTransaction.getScriptPubKey()))
            );
            utxoKeys.add(utxo);
            totalInputMoney+=unspentTransaction.getAmount().doubleValue();
        }
        changeMoney = totalInputMoney - totalOutMoney-chargeMoney;
        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put(a1Main.getAddress(),accountAMoney);
        receiveAddressAndValue.put(multiSigAccountA2.getAddress().toBase58(),accountBMoney);
        if (changeMoney>0){
            receiveAddressAndValue.put(multiSigAccountA.getAddress().toBase58(),changeMoney);
        }
        //构建交易
        Transaction transaction = transactionUtils.buildTransaction(networkParameters, utxoKeys, receiveAddressAndValue);
        //赎回脚本
        Script redeemScript = multiSigAccountA.getRedeemScript();
        //获取待签名数据
        List<SignatureData> signatureDataList = transactionUtils.getSimplifiedTransactionHashes(transaction, redeemScript);
        System.out.println(signatureDataList);
        //模拟用户签名后上传的签名数据
//        String SignatureAInput0="3045022100f2c56653808f231ed5f4f7f92bab9d4884557b9823f311221f06d556cb0c612202204338d44a1fe284fda10a593a1e902f2b7967d46ce908b4624ce31e02e2700dfc";
//        String SignatureAInput1="3044022056c5508a8f5ba6fb2a099ab3fa58c00abb770e8e3c4f48a2874055c26eeb32ae02205f86e7db1618c1c1422c910585aa74fd132f9dc5cc66de6b3f7e1f0d17121f6b";
//        String SignatureBInput0="3045022100c3bf2f9f00a21e31bb83abcb78c9203e57fbf9523bf435ae5c0c4b0a9c8b6ba102201e2fcc1f61bc4d85c82067123fcfbba98ebbebb25dd15b9abca0ed7d8caae6a3";
//        String SignatureBInput1="304402203e87eea89d3ce11e92401049b8c845c1eabd2e7556447ae9b64c94670d2959b5022041967578a6a6fa407293b8b46b24b31dc3e03950c6c16f3ea169fe80496f0d24";
        //模拟A用户签名
        ECKey ecKeyMultiSigA = addressUtils.getECKeyFromPrivateKeyWif(networkParameters, "cMxruYjRCsPvWHkRv1W6DtooJzKsbhNKBCJVMSjaS4nEKbwA5dvZ");
        for (SignatureData signatureData : signatureDataList) {
            ECKey.ECDSASignature signature = transactionUtils.sign(ecKeyMultiSigA, signatureData.getSimplifiedTransactionHash());
            System.out.println(Hex.encode(signature.encodeToDER()));
            signatureData.getSignatures().add(signature);
        }
  //      ECKey ecKeyMultiSigB = addressUtils.getECKeyFromPrivateKeyWif(networkParameters, "cNQ3zWKgA66rNAJAAJfVhZ6wKp6Fg2CHCFN2WXw26nuLkPYZQqTP");

//        //模拟用户B签名
//        for(SignatureData signatureData:signatureDataList) {
//            ECKey.ECDSASignature signature = transactionUtils.sign(ecKeyMultiSigB, signatureData.getSimplifiedTransactionHash());
//            System.out.println(HEX.encode(signature.encodeToDER()));
//            signatureData.getSignatures().add(signature);
//        }
        //添加进行交易
        for (SignatureData signatureData : signatureDataList) {
            transactionUtils.addMultiSignatures(transaction,signatureData.getInputIndex(),signatureData.getSignatures(),redeemScript);
        }
        String transactionHex = Hex.toHexString(transaction.bitcoinSerialize());
        //预估交易hash
        String txId = transaction.getTxId().toString();
        System.out.println(transactionHex);
        System.out.println(txId);
        Object o = btcService.sendrawTransaction(transactionHex);
        System.out.println("结果=>"+o);


    }

    /**
     * M-N 2-3  至少M个签名 可以自由组合
     * 参考: https://blog.csdn.net/qq_40452317/article/details/90412806
     */
    @DisplayName("多签转账")
    @Test
    public void MultiSign2_TO_P2PKH_P2WPKH2(){

        Double accountAMoney=0.00001;
        Double accountBMoney=0.00001;
        Double totalInputMoney=0D;
        Double totalOutMoney=accountAMoney+accountBMoney;
        //手续费
        Double chargeMoney=Converter.satoshisToBitcoin(701L);
        Double changeMoney=0D;

        LegacyAddress legacyAddress = multiSigAccountA2.getAddress();
        String address = legacyAddress.toBase58();
        System.out.println(address);
        List<UnspentTransaction> unspentTransactions = btcService.listUtxo(0, 9999999, new String[]{address});
        System.out.println(unspentTransactions);
        List<UTXO> utxoKeys=new ArrayList<>();
        for (UnspentTransaction unspentTransaction : unspentTransactions) {
            if (totalInputMoney>=totalOutMoney){
                break;
            }
            UTXO utxo = new UTXO(
                    Sha256Hash.wrap(unspentTransaction.getTxid()),
                    unspentTransaction.getVout(),
                    Coin.valueOf(Converter.bitcoinToSatoshis(unspentTransaction.getAmount().doubleValue())),
                    0,
                    false,
                    new Script(Hex.decode(unspentTransaction.getScriptPubKey()))
            );
            utxoKeys.add(utxo);
            totalInputMoney+=unspentTransaction.getAmount().doubleValue();
        }
        changeMoney = totalInputMoney - totalOutMoney-chargeMoney;
        Map<String, Double> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put(a1Main.getAddress(),accountAMoney);
        receiveAddressAndValue.put(multiSigAccountA.getAddress().toBase58(),accountBMoney);
        if (changeMoney>0){
            receiveAddressAndValue.put(multiSigAccountA2.getAddress().toBase58(),changeMoney);
        }
        //构建交易
        Transaction transaction = transactionUtils.buildTransaction(networkParameters, utxoKeys, receiveAddressAndValue);
        //赎回脚本
        Script redeemScript = multiSigAccountA2.getRedeemScript();
        //获取待签名数据
        List<SignatureData> signatureDataList = transactionUtils.getSimplifiedTransactionHashes(transaction, redeemScript);
        System.out.println(signatureDataList);
        //模拟用户签名后上传的签名数据
//        String SignatureAInput0="3045022100f2c56653808f231ed5f4f7f92bab9d4884557b9823f311221f06d556cb0c612202204338d44a1fe284fda10a593a1e902f2b7967d46ce908b4624ce31e02e2700dfc";
//        String SignatureAInput1="3044022056c5508a8f5ba6fb2a099ab3fa58c00abb770e8e3c4f48a2874055c26eeb32ae02205f86e7db1618c1c1422c910585aa74fd132f9dc5cc66de6b3f7e1f0d17121f6b";
//        String SignatureBInput0="3045022100c3bf2f9f00a21e31bb83abcb78c9203e57fbf9523bf435ae5c0c4b0a9c8b6ba102201e2fcc1f61bc4d85c82067123fcfbba98ebbebb25dd15b9abca0ed7d8caae6a3";
//        String SignatureBInput1="304402203e87eea89d3ce11e92401049b8c845c1eabd2e7556447ae9b64c94670d2959b5022041967578a6a6fa407293b8b46b24b31dc3e03950c6c16f3ea169fe80496f0d24";
        //模拟A用户签名
        ECKey ecKeyMultiSigA = addressUtils.getECKeyFromPrivateKeyWif(networkParameters, "cRzsYxircLjYotcxdYfu99sQWKEHDdBiUqZczkMhHbcLJPfnn7rv");
        for (SignatureData signatureData : signatureDataList) {
            ECKey.ECDSASignature signature = transactionUtils.sign(ecKeyMultiSigA, signatureData.getSimplifiedTransactionHash());
           // System.out.println(Hex.encode(signature.encodeToDER()));
            signatureData.getSignatures().add(signature);
        }
        ECKey ecKeyMultiSigB = addressUtils.getECKeyFromPrivateKeyWif(networkParameters, "cRttDzjDk62LqFo2Y9GvgRSi3yYcqwrkA6kvcysUaTQGQFqTNNja");

        //模拟用户B签名
//        for(SignatureData signatureData:signatureDataList) {
//            ECKey.ECDSASignature signature = transactionUtils.sign(ecKeyMultiSigB, signatureData.getSimplifiedTransactionHash());
//            //System.out.println(HEX.encode(signature.encodeToDER()));
//            signatureData.getSignatures().add(signature);
//        }
        ECKey ecKeyMultiSigC = addressUtils.getECKeyFromPrivateKeyWif(networkParameters, "cTQGcsJyCNqZKwph87YqFi6F1qiCuNBzpdac2vHa7mCWkknEVcQP");
        //模拟用户C签名
        for(SignatureData signatureData:signatureDataList) {
            ECKey.ECDSASignature signature = transactionUtils.sign(ecKeyMultiSigB, signatureData.getSimplifiedTransactionHash());
            //System.out.println(HEX.encode(signature.encodeToDER()));
            signatureData.getSignatures().add(signature);
        }
        //添加进行交易
        for (SignatureData signatureData : signatureDataList) {
            transactionUtils.addMultiSignatures(transaction,signatureData.getInputIndex(),signatureData.getSignatures(),redeemScript);
        }
        String transactionHex = Hex.toHexString(transaction.bitcoinSerialize());
        //预估交易hash
        String txId = transaction.getTxId().toString();
        System.out.println(transactionHex);
        System.out.println(txId);
        Object o = btcService.sendrawTransaction(transactionHex);
        System.out.println("结果=>"+o);


    }

    /**
     * 测试签名 效验简单交易hash
     */
    @DisplayName("testSignTransactionHash")
    @Test
    public void testSignTransactionHash(){

         String simplifiedTransactionHash="bbcedd692a91879069adabc7e66fbe862af7a3970be3901a5eb63ee9d78bc480";
        ECKey ecKeyMultiSigA = addressUtils.getECKeyFromPrivateKeyWif(networkParameters, "cMxruYjRCsPvWHkRv1W6DtooJzKsbhNKBCJVMSjaS4nEKbwA5dvZ");
        ECKey.ECDSASignature ecdsaSignature = transactionUtils.sign(ecKeyMultiSigA, simplifiedTransactionHash);
        System.out.println("签名=>"+ecdsaSignature);
        System.out.println(Hex.toHexString(ecdsaSignature.encodeToDER()));
        boolean verify = ecKeyMultiSigA.verify(Sha256Hash.wrap(simplifiedTransactionHash), ecdsaSignature);
        System.out.println("是否效验通过true 通过 false 不通过=>"+verify);


    }




    @Test
    public void testOmniTransfer(){
        //这是比特币的限制最小转账金额，所以很多usdt转账会收到一笔0.00000546的btc
        BigDecimal miniBtc=BigDecimal.valueOf(546L);
        ECKey ecA1Main = ECKey.fromPrivate(Hex.decode(a1Main.getPrivateKey()));
        //手续费
        BigDecimal fee=BigDecimal.valueOf(546L);
        //usdt数量
        BigDecimal usdtAmount = BigDecimal.valueOf(1).multiply(BigDecimal.TEN.pow(8));
        //构建usdt的输出脚本 注意这里的金额是要乘10的8次方
        String usdtHex = "6a146f6d6e69" + String.format("%016x", 1) + String.format("%016x", usdtAmount.longValue());
        //1.构建交易
        Transaction tx = new Transaction(networkParameters);
        //2.添加btc最小转账金额
        tx.addOutput(Coin.valueOf(miniBtc.longValue()),Address.fromString(networkParameters,multiSigAccountA.getAddress().toBase58()));
        //3.添加usdt转账
        tx.addOutput(Coin.valueOf(0L), new Script(Utils.HEX.decode(usdtHex)));
        //4.获取未花费输出
        List<UnspentTransaction> unspentTransactions = btcService.listUtxo(1, 99999, new String[]{a1Main.getAddress()});
        BigDecimal totalInputMoney=BigDecimal.ZERO;
        //总输出聪
        BigDecimal totalOutputMoney=fee.add(miniBtc);
        BigDecimal totalOutPutMoneyBtc = totalOutputMoney.divide(BigDecimal.TEN.pow(8));
        List<UTXO> needUtxos=new ArrayList<>();
        for (UnspentTransaction unspentTransaction : unspentTransactions) {
            if (totalInputMoney.compareTo(totalOutPutMoneyBtc)>=0){
                break;
            }
            UTXO utxo = new UTXO(
                    Sha256Hash.wrap(unspentTransaction.getTxid()),
                    unspentTransaction.getVout(),
                    Coin.valueOf(Converter.bitcoinToSatoshis(unspentTransaction.getAmount().doubleValue())),
                    0,
                    false,
                    new Script(Hex.decode(unspentTransaction.getScriptPubKey()))
            );
            needUtxos.add(utxo);
            totalInputMoney=totalInputMoney.add(unspentTransaction.getAmount());
        }
        //找零 聪单位
        BigDecimal changeAmount = totalInputMoney.subtract(totalOutPutMoneyBtc);
        //4.判断
        if (changeAmount.compareTo(BigDecimal.ZERO)>0){
            BigDecimal changeAmountSatoshis = changeAmount.multiply(BigDecimal.TEN.pow(8));
            tx.addOutput(Coin.valueOf(changeAmountSatoshis.longValue()),Address.fromString(networkParameters,a1Main.getAddress()));
        }
        //5.添加未签名的输入
        for (UTXO needUtxo : needUtxos) {
            TransactionOutPoint transactionOutPoint = new TransactionOutPoint(networkParameters, needUtxo.getIndex(), needUtxo.getHash());
            tx.addSignedInput(transactionOutPoint,needUtxo.getScript(),ecA1Main,Transaction.SigHash.ALL,true);
        }
//        for (UTXO needUtxo : needUtxos) {
//            tx.addInput(needUtxo.getHash(),needUtxo.getIndex(),needUtxo.getScript());
//
//        }
//        for (int i = 0; i < needUtxos.size(); i++) {
//            UTXO utxo = needUtxos.get(i);
//
//            TransactionInput transactionInput = tx.getInput(i);
//            Script scriptPubKey = ScriptBuilder.createOutputScript(Address.fromString(networkParameters, a1Main.getAddress()));
//            Sha256Hash hash = tx.hashForSignature(i, scriptPubKey, Transaction.SigHash.ALL, false);
//            ECKey.ECDSASignature ecSig = ecA1Main.sign(hash);
//            TransactionSignature txSig = new TransactionSignature(ecSig, Transaction.SigHash.ALL, false);
//            transactionInput.setScriptSig(ScriptBuilder.createInputScript(txSig, ecA1Main));
//        }



        //交易hahs
        String hash = Hex.toHexString(tx.bitcoinSerialize());
        //交易id
        String txId = tx.getTxId().toString();
        System.out.println(hash);

        Object o = btcService.sendrawTransaction(hash);
        System.out.println("结果=>"+o);
    }

    /**
     * 发送omni交易 指定用户
     */
    @Test
    public void sendOmniTransferBySpecificUserFee(){

        //token的唯一标识
        Integer propertyId=31;
        Long amount=1L;
        // 接收usdt 地址
        String receiveUsdtAddress="";
        // 私钥支付usdt key
        String privUsdtKey=a1Main.getPrivateKey();
        // 支付 USDT地址
        String usdtAddress=a1Main.getAddress();
        // 支付矿工费地址
        String  btcAddress="";
        //支付矿工费私钥
        String btcPrivateKey="";
        //手续费
        Double fee=Converter.satoshisToBitcoin(546L);
        //btc total money
        long btcTotalMoney=0;
        long usdtTotalMoney=0;


        List<UnspentTransaction> btcUnspentList = btcService.listUtxo(1, 999999, new String[]{btcAddress});
        List<UnspentTransaction> usdtUnspentList = btcService.listUtxo(1, 99999, new String[]{usdtAddress});
        //btc未花费的输出
        List<UTXO> btcUtxos=new ArrayList<>();
        //usdt 未花费的输出
        List<UTXO> usdtUtxos=new ArrayList<>();
        for (UnspentTransaction unspentTransaction : btcUnspentList) {
            if (btcTotalMoney>=(Converter.satoshisToBitcoin(1092L)+fee)){
                break;
            }
            UTXO utxo = new UTXO(
                    Sha256Hash.wrap(unspentTransaction.getTxid()),
                    unspentTransaction.getVout(),
                    Coin.valueOf(Converter.bitcoinToSatoshis(unspentTransaction.getAmount().doubleValue())),
                    0,
                    false,
                    new Script(Hex.decode(unspentTransaction.getScriptPubKey()))
            );
            btcUtxos.add(utxo);
            btcTotalMoney+=unspentTransaction.getAmount().longValue();
        }
        //usdt
        for (UnspentTransaction unspentTransaction : usdtUnspentList) {
            if (usdtTotalMoney>=Converter.satoshisToBitcoin(546L)){
                break;
            }
            UTXO utxo = new UTXO(
                    Sha256Hash.wrap(unspentTransaction.getTxid()),
                    unspentTransaction.getVout(),
                    Coin.valueOf(Converter.bitcoinToSatoshis(unspentTransaction.getAmount().doubleValue())),
                    0,
                    false,
                    new Script(Hex.decode(unspentTransaction.getScriptPubKey()))
            );
            usdtUtxos.add(utxo);
            usdtTotalMoney+=unspentTransaction.getAmount().longValue();
        }

        //判断是否为空
        if (!btcUtxos.isEmpty()&&!usdtUtxos.isEmpty()){
            //find a btc eckey info
            DumpedPrivateKey btcPrivateKeyDump = DumpedPrivateKey.fromBase58(networkParameters, btcPrivateKey);
            ECKey btcKey = btcPrivateKeyDump.getKey();
            //a usdt eckey info
            DumpedPrivateKey usdtDumpedPrivateKey = DumpedPrivateKey.fromBase58(networkParameters, privUsdtKey);
            ECKey usdtKey = usdtDumpedPrivateKey.getKey();
            //receive address
            Address receiveAddress = Address.fromString(networkParameters, receiveUsdtAddress);
            Transaction transaction = new Transaction(networkParameters);
            //odd address
            Address oddAddress = Address.fromString(networkParameters, btcAddress);
            //如果需要找零 消费列表总金额-已经转账总金额-手续费
            //总输入-手续费--546-546=找零金额
            long leave= (long) (btcTotalMoney+usdtTotalMoney-fee-Converter.satoshisToBitcoin(1092L));
            if (leave>0){
                transaction.addOutput(Coin.valueOf(Converter.bitcoinToSatoshis(Double.valueOf(leave))),oddAddress);
            }
            // usdt
            String usdtHex = "6a146f6d6e69" + String.format("%016x", propertyId) + String.format("%016x", amount);
            //usdt transaction
            transaction.addOutput(Coin.valueOf(546),new Script(HEX.decode(usdtHex)));
            //send to address
            transaction.addOutput(Coin.valueOf(546),receiveAddress);
            //create usdt utxo data
            for (UTXO usdtUtxo : usdtUtxos) {
                TransactionOutPoint transactionOutPoint = new TransactionOutPoint(networkParameters, usdtUtxo.getIndex(), usdtUtxo.getHash());
                transaction.addSignedInput(transactionOutPoint,usdtUtxo.getScript(),usdtKey,Transaction.SigHash.ALL,true);
            }

            for (UTXO btcUtxo : btcUtxos) {
                TransactionOutPoint transactionOutPoint = new TransactionOutPoint(networkParameters, btcUtxo.getIndex(), btcUtxo.getHash());
                transaction.addSignedInput(transactionOutPoint,btcUtxo.getScript(),btcKey,Transaction.SigHash.ALL,true);
            }
            Context.getOrCreate(networkParameters);
            transaction.getConfidence().setSource(TransactionConfidence.Source.NETWORK);
            transaction.setPurpose(Transaction.Purpose.USER_PAYMENT);
            //hash
            String hash = Hex.toHexString(transaction.bitcoinSerialize());
            System.out.println(hash);


        }

    }



}
