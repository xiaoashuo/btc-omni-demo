package com.lovecyy.service;

import com.google.common.collect.ImmutableList;
import com.lovecyy.model.pojo.accounts.Account;
import com.lovecyy.model.pojo.accounts.P2SHMultiSigAccount;
import com.lovecyy.utils.AddressUtils;
import com.lovecyy.utils.BitCoinEnvConfig;
import com.lovecyy.utils.Converter;
import com.lovecyy.utils.NumericUtil;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.util.Lists;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;


import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class AddressTest {

    @Autowired
    private AddressUtils adAddress;
    private NetworkParameters networkParameters;

    @BeforeEach
    public void setNetWork(){
         networkParameters=TestNet3Params.get();
    }

    @DisplayName("创建新地址")
    @Test
    public void createNewAddress(){
        BitCoinEnvConfig.getInstance().setNetworkParameters(TestNet3Params.get());
        NetworkParameters networkParameters = BitCoinEnvConfig.getInstance().getNetworkParameters();
        AddressUtils addressUtils = new AddressUtils();
        List<String> mnemonicCode = Arrays.asList("weasel", "street", "dutch", "vintage", "legal", "network",
                "squirrel", "sort", "stereo", "drum", "trumpet", "farm");
        String passphrase = "your passphrase";
        byte[] seed = addressUtils.generateSeed(mnemonicCode, passphrase);
        ECKey ecKey = adAddress.getECKey(seed, 0, 0);
        String legacyAddress = adAddress.getLegacyAddress(networkParameters,ecKey);
        System.out.println(legacyAddress);
         ecKey = adAddress.getECKey(seed, 1, 0);
         legacyAddress = adAddress.getLegacyAddress(networkParameters,ecKey);
        System.out.println(legacyAddress);
         ecKey = adAddress.getECKey(seed, 1, 1);
         legacyAddress = adAddress.getLegacyAddress(networkParameters,ecKey);
        System.out.println(legacyAddress);
         ecKey = adAddress.getECKey(seed, 1, 10);
         legacyAddress = adAddress.getLegacyAddress(networkParameters,ecKey);
        System.out.println(legacyAddress);
        Account account = adAddress.generateAddress(networkParameters,"123445");
        System.out.println(account);
    }

    @DisplayName("测试获取WIF")
    @Test
    public void testWif(){
        NetworkParameters networkParameters=TestNet3Params.get();
        List<String> mnemonicCode = Arrays.asList("weasel", "street", "dutch", "vintage", "legal", "network",
                "squirrel", "sort", "stereo", "drum", "trumpet", "farm");
        String passphrase = "your passphrase";
        byte[] seed = adAddress.generateSeed(mnemonicCode, passphrase);
        ECKey ecKey = adAddress.getECKey(seed, 1, 10);
        String address = adAddress.getLegacyAddress(networkParameters, ecKey);
        String privateKeyAsHex = adAddress.getPrivateKeyAsHex(ecKey);
        String publicKeyAsHex=adAddress.getPublicKeyAsHex(ecKey);
        String wif = adAddress.getWIF(networkParameters, ecKey);
        System.out.println("地址=>"+address);
        System.out.println("私钥16进制=>"+privateKeyAsHex);
        System.out.println("公钥16进制=>"+publicKeyAsHex);
        System.out.println("私钥WIF=>"+wif);
    }


    /**
     * 测试创建多签地址
     */
    @DisplayName("测试生成多签地址")
    @Test
    public void testGenerateMultiSigAddress(){
        Account accountA = adAddress.generateAddress(networkParameters, "123456");
        Account accountB = adAddress.generateAddress(networkParameters, "123456");
        Account accountC = adAddress.generateAddress(networkParameters, "123456");
        ECKey publicKeyA = adAddress.publicKeyToECKey(accountA.getPublicKey());
        ECKey publicKeyB = adAddress.publicKeyToECKey(accountB.getPublicKey());
        ECKey publicKeyC = adAddress.publicKeyToECKey(accountC.getPublicKey());
        List<ECKey> keys= ImmutableList.of(publicKeyA,publicKeyB,publicKeyC);
        P2SHMultiSigAccount p2SHMultiSigAccount = adAddress.generateMultiSigAddress(networkParameters, 2, keys);
        Script multiSigOutputScript = ScriptBuilder.createMultiSigOutputScript(2, keys);
        System.out.println("账户A=>"+accountA);
        System.out.println("账户B=>"+accountB);
        System.out.println("账户C=>"+accountC);
        System.out.println("多签账户=>"+p2SHMultiSigAccount);
    }

    /**
     * 测试消息签名,验签
     */
    @DisplayName("测试消息签名,验签")
    @Test
    public void testSignVerifyMessage(){
        String WIF="cRrGxgDd9JYP9gUeZbiXJeHd7MQ9fiHNuAJyyiRp9nH6LNe1CLv1";
        String message="hello world";
        String signatureBase64 = adAddress.signMessage(networkParameters, WIF, message);
        System.out.println(signatureBase64);
        ECKey ecKey = adAddress.getECKeyFromPrivateKeyWif(networkParameters, WIF);
        boolean isTrue = adAddress.verifyMessage(ecKey.getPublicKeyAsHex(), message, signatureBase64);
        System.out.println(isTrue);
    }

    /**
     * 测试生成隔离见证地址
     */
    @DisplayName("测试隔离见证地址")
    @Test
    public void testSegWitAddress(){
       // networkParameters= MainNetParams.get();
        Account account = adAddress.generateSegWitAddress(networkParameters, "123456");
        System.out.println(account);
    }

    /**
     * 测试地址类型
     */
    @DisplayName("测试地址类型")
    @Test
    public void testAddressType(){
        //普通地址
       String address="mtfEHDUMEJeetpJvoEv8ne9H49bf7FhGiF";
       //多签地址
       String multiSigAddress= "2N8R1vsCeNmTmEcv9Rd4y5jgCqWHcH7SXtY";
       //隔离见证地址
       String segWitAddress="tb1q5dw93gp96mtq3953f9kfuq9wqlwdwd7sahyze9";

        boolean isLegacyAddress = adAddress.isLegacyAddress(networkParameters, address);
        boolean isLegacyAddress1 = adAddress.isLegacyAddress(networkParameters, multiSigAddress);
        boolean isLegacyAddress2 = adAddress.isLegacyAddress(networkParameters, segWitAddress);
        System.out.println("是否为遗留地址=>"+isLegacyAddress+"--"+isLegacyAddress1+"--"+isLegacyAddress2);
        boolean segWitAddress1 = adAddress.isSegWitAddress(networkParameters,address);
        boolean segWitAddress2 = adAddress.isSegWitAddress(networkParameters,multiSigAddress);
        boolean segWitAddress3 = adAddress.isSegWitAddress(networkParameters,segWitAddress);
        System.out.println("是否为隔离见证地址=>"+segWitAddress1+"--"+segWitAddress2+"--"+segWitAddress3);
    }

    /**
     * 测试脚本转地址
     */
    @DisplayName("测试脚本转地址")
    @Test
    public void testScriptToAddress(){
        //HASH160 PUSHDATA(20)[24f100c8b4de8938c24a11041004bcffd53f1894] EQUAL
        //2N6CwD92GzvAgAbGUj9UGSvVPoXfRkEE1Kh
        Script script = new Script(Converter.hexToByte("a9148e2acc223101503adec422af45bcac35ff38b8ce87"));
        String address = adAddress.scriptToAddress(networkParameters, script);
        System.out.println(address);

    }

    @Test
    public void testExtendedPublicKeyToPublicKey(){
        DeterministicKey xpub=DeterministicKey.deserializeB58("xpub6EwHXXLWjhe22uYULsvj19f5fvjKXZPucprenCNjMFADEAUFUhSXw31YRtGq1APaQGHmQA4LGunTnBqmREQoFtNh4d26qXuKcVUvU16daf7",MainNetParams.get());
        System.out.println(xpub.getPublicKeyAsHex());
        System.out.println(LegacyAddress.fromPubKeyHash(MainNetParams.get(),xpub.getPubKeyHash()).toBase58());
        System.out.println(xpub.getPathAsString());


        DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(xpub);
        List<ChildNumber> parentPath = new ArrayList<>();
//        parentPath.add(new ChildNumber(32));
//        parentPath.add(new ChildNumber(0));
        parentPath.add(new ChildNumber(0));
//        parentPath.add(new ChildNumber(0));
//
        DeterministicKey deterministicKey =deterministicHierarchy.deriveChild(parentPath, false, true, new ChildNumber(0));
        System.out.println(deterministicKey.getPublicKeyAsHex());
        System.out.println(LegacyAddress.fromPubKeyHash(MainNetParams.get(),deterministicKey.getPubKeyHash()).toBase58());
        try {
            deterministicKey.verifyMessage("74dc031cf3b23fbfe8115b1c262f8d142c64231b07afdacbab82bcc2fb30a461","IBPTh2TFbFgGLhbvovnaruQngceuXHz4fitu7PUPwNA2SC3d6She0AryneRRq+x8nNlybVXFmNDqbNoNZ5Q4oTo=");
        } catch (SignatureException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试生成助记词，种子
     *
     *
     */
    @DisplayName("测试生成助记词,种子")
    @Test
    public void testGenerateMnemonicCode(){
        List<String> strings = adAddress.generateMnemonicCode("123456");
        System.out.println(strings);
        byte[] seed = adAddress.generateSeed(strings, "123456");
        System.out.println(seed);
    }


}
