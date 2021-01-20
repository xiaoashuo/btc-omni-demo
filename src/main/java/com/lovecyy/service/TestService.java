package com.lovecyy.service;


import com.lovecyy.enums.AddressType;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.jcajce.provider.digest.RIPEMD160;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TestService {
public static String t(Double doa){
    doa=doa+3;
    return "";
}

    public static void main(String[] args) {



//        TestNet3Params testNet3Params = TestNet3Params.get();
//        ECKey ecKey = new ECKey();
//       //p2pkh 个人
//        System.out.println("私钥 =>"+ ecKey.getPrivateKeyAsHex());
//        System.out.println("公钥 =>"+ ecKey.getPublicKeyAsHex());
//        String addressFromPub = getAddressFromPub(ecKey.getPublicKeyAsHex());
//
//        System.out.println("地址 =>"+ addressFromPub);
//        System.out.println("--------------------------------");
//        Map map = genAddress();
//      // LegacyAddress.fromPubKeyHash().toString()
//        MainNetParams networkParameters = MainNetParams.get();
//        LegacyAddress address1 = LegacyAddress.fromKey(networkParameters, ecKey);
//        LegacyAddress address2 = LegacyAddress.fromPubKeyHash(networkParameters, ecKey.getPubKeyHash());
//        System.out.println("1开头的地址：" + address1.toBase58());
//        System.out.println("1开头的地址：" + address2.toBase58());
//
//        LegacyAddress address3 = LegacyAddress.fromScriptHash(networkParameters, ecKey.getPubKeyHash());
//        System.out.println("3开头的地址：" + address3.toBase58());
//
//        SegwitAddress segwitAddress = SegwitAddress.fromKey(networkParameters, ecKey);
//        SegwitAddress segwitAddress1 = SegwitAddress.fromHash(networkParameters, ecKey.getPubKeyHash());
//        System.out.println("bc1开头的地址：" + segwitAddress.toBech32());
//        System.out.println("bc1开头的地址：" + segwitAddress1.toBech32());
    }

    public static String getAddressFromPub(String pubkey){
        byte[] sha256Bytes = Sha256Hash.hash(Hex.decode(pubkey));
        //2.将sha256bytes 进行 ripemd160 哈希

        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(sha256Bytes,0,sha256Bytes.length);
        byte[] ripemd160Bytes = new byte[digest.getDigestSize()];
        digest.doFinal(ripemd160Bytes,0);
        //3.将btc地址版本号00加在第二部结果签名
        String result= "00"+Hex.toHexString(ripemd160Bytes);
        //4.将上一部的记过进行双hash256
        byte[] firstHash = Sha256Hash.hash(Hex.decode(result));
        byte[] doubleHash = Sha256Hash.hash(firstHash);
        //5取第4结果的前4字节并加在第3步结果后面
        String checkSum = Hex.toHexString(doubleHash).substring(0, 8);
        String checkStr = result + checkSum;
        String address = Base58.encode(Hex.decode(checkStr));
        return address;


    }

    /**
     * BTC助记词生成种子，种子生成私钥，私钥生成公钥，公钥生成地址。
     * @return
     */
    public static Map genAddress(){
        TestNet3Params testNet3Params = TestNet3Params.get();
        DeterministicSeed seed = new DeterministicSeed(new SecureRandom(), 128, "");

        Wallet wallet = Wallet.fromSeed(testNet3Params, seed, Script.ScriptType.P2PKH);
         String privateKey = wallet.currentReceiveKey().getPrivateKeyAsWiF(testNet3Params);
         String mnemonics = wallet.getKeyChainSeed().getMnemonicCode().toString();
         String publicKey = Hex.toHexString(ECKey.publicKeyFromPrivate(wallet.currentReceiveKey().getPrivKey(), true));
        String address = wallet.currentReceiveAddress().toString();
        System.out.println("私钥 =>"+ privateKey);
        System.out.println("公钥 =>"+ publicKey);
        System.out.println("助记词 =>"+ mnemonics);
        System.out.println("地址 =>"+ address);
        return new HashMap();
    }



}
