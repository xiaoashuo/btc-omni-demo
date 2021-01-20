package com.lovecyy.utils;

import com.lovecyy.model.pojo.accounts.Account;
import com.lovecyy.model.pojo.accounts.P2SHMultiSigAccount;
import io.github.novacrypto.bip39.SeedCalculator;
import io.github.novacrypto.bip39.wordlists.English;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;
import sun.nio.ch.Net;

import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class AddressUtils {

    /**
     * 通过种子和路径获取ECKey
     *
     * @param seed         种子
     * @param accountIndex 账户索引
     * @param addressIndex 地址索引
     * @return ECKey
     */
    public  ECKey getECKey(byte[] seed, int accountIndex, int addressIndex) {
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(masterPrivateKey);
        List<ChildNumber> parentPath = new ArrayList<>();
        parentPath.add(new ChildNumber(accountIndex));
        DeterministicKey deterministicKey = deterministicHierarchy.deriveChild(parentPath, false, true, new ChildNumber(addressIndex));
        return ECKey.fromPrivate(deterministicKey.getPrivKey());
    }
    /**
     * p2sh 测试网地址 196 https://en.bitcoin.it/wiki/BIP_0013
     * 判断是否是普通地址
     * @param addressBase58 地址文本格式
     * @return 结果
     */
    public  boolean isLegacyAddress(NetworkParameters networkParameters,String addressBase58) {
        try {
            byte[] versionAndDataBytes = Base58.decodeChecked(addressBase58);
            int version = versionAndDataBytes[0] & 0xFF;
            return version == networkParameters.getAddressHeader() ||
                    version == networkParameters.getP2SHHeader();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是否是隔离见证地址
     * @param addressBase58 地址文本格式
     * @return 结果
     */
    public    boolean isSegWitAddress(NetworkParameters networkParameters,String addressBase58) {
        try {
            Bech32.Bech32Data bechData = Bech32.decode(addressBase58);
            return bechData.hrp.equals(networkParameters.getSegwitAddressHrp());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 消息签名
     *
     * @param WIF     钱包可导入模式的私钥
     * @param message 待签名消息
     * @return 签名后的base64文本
     */
    public  String signMessage(NetworkParameters networkParameters,String WIF, String message) {
        ECKey ecKey = getECKeyFromPrivateKeyWif(networkParameters,WIF);
        return ecKey.signMessage(message);
    }
    /**
     * 消息验签
     *
     * @param publicKeyHex    十六进制公钥
     * @param message         消息
     * @param signatureBase64 签名base64格式文本
     * @return 结果
     */
    public  boolean verifyMessage(String publicKeyHex, String message, String signatureBase64) {
        ECKey ecKey = publicKeyToECKey(publicKeyHex);
        try {
            ecKey.verifyMessage(message, signatureBase64);
        } catch (SignatureException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 通过十六进制公钥文本构建ECKey，通常用于验签
     *
     * @param publicKeyHex 十六进制公钥文本
     * @return ECKey
     */
    public  ECKey publicKeyToECKey(String publicKeyHex) {
        return ECKey.fromPublicOnly(Hex.decode(publicKeyHex));
    }

    /**
     * 得到Eckey 根据压缩后的私钥
     *
     * @param privateKeyWif
     * @return
     */
    public  ECKey getECKeyFromPrivateKeyWif(NetworkParameters networkParameters,String privateKeyWif) {
        return DumpedPrivateKey.fromBase58(networkParameters , privateKeyWif).getKey();
    }

    /**
     * 通过ECKey获取钱包可导入格式的私钥
     *
     * @param ecKey ECKey
     * @return 钱包可导入格式的私钥
     */
    public  String getWIF(NetworkParameters networkParameters,ECKey ecKey) {
        return ecKey.getPrivateKeyAsWiF(networkParameters);
    }
    /**
     * 通过ECKey获取私钥的十六进制格式文本
     *
     * @param ecKey ECKey
     * @return 私钥的十六进制格式文本
     */
    public  String getPrivateKeyAsHex(ECKey ecKey) {
        return ecKey.getPrivateKeyAsHex();
    }

    /**
     * 根据脚本计算地址文本
     * @param script 脚本
     * @return 地址
     */
    public  String scriptToAddress(NetworkParameters networkParameters, Script script) {
        Script.ScriptType scriptType = script.getScriptType();
        if (scriptType == Script.ScriptType.P2PK || scriptType == Script.ScriptType.P2SH || scriptType == Script.ScriptType.P2PKH) {
            return ((LegacyAddress) script.getToAddress(networkParameters)).toBase58();
        } else if (scriptType == Script.ScriptType.P2WPKH || scriptType == Script.ScriptType.P2WSH) {
            return ((SegwitAddress) script.getToAddress(networkParameters)).toBech32();
        } else {
            return null;
        }
    }

    /**
     * 生成助记词
     * @return 助记词
     */
    public   List<String> generateMnemonicCode(String passphrase) {
        SecureRandom secureRandom = new SecureRandom();
        DeterministicSeed ds = new DeterministicSeed(secureRandom, 128, passphrase);
        return ds.getMnemonicCode();
    }



    /**
     * 通过助记词和密码生成种子
     * @param mnemonicCode 助记词
     * @param passphrase 种子
     * @return 种子
     */
    public  byte[] generateSeed(List<String> mnemonicCode, String passphrase) {
        return new SeedCalculator().withWordsFromWordList(English.INSTANCE).calculateSeed(mnemonicCode, passphrase);
    }


    /**
     * 助记词->种子->私钥->公钥->地址
     * 参考4.2.3  4.2.3.1 https://book.8btc.com/books/6/masterbitcoin2cn/_book/ch04.html
     * 私钥 wif一般以k开头 表明被编码私钥有一个后缀01
     * 公钥压缩 一般02/03开头 非压缩 04
     * @param passphrase
     * @return
     */
    public  Account generateAddress( NetworkParameters networkParameters,String passphrase){
        Account account = new Account();
        //bit  最小128 max512  整除32 觉得助记词长短
        DeterministicSeed seed = new DeterministicSeed(new SecureRandom(), 128, passphrase);
        Wallet wallet = Wallet.fromSeed(networkParameters, seed, Script.ScriptType.P2PKH);
        DeterministicKey deterministicKey = wallet.currentReceiveKey();
        String privateKey = deterministicKey.getPrivateKeyAsWiF(networkParameters);
        String mnemonics = wallet.getKeyChainSeed().getMnemonicCode().toString();
        String publicKey = Hex.toHexString(ECKey.publicKeyFromPrivate(deterministicKey.getPrivKey(), true));
        String address = wallet.currentReceiveAddress().toString();
        return account.setAddress(address).setPublicKey(publicKey).setPrivateKey(privateKey).setMnemonics(mnemonics).setPassphrase(passphrase);
    }



    /**
     * 生成隔离见证地址
     * @param networkParameters
     * @param passphrase
     * @return
     */
    public Account generateSegWitAddress(NetworkParameters networkParameters,String passphrase){
        //bit  最小128 max512  整除32 觉得助记词长短
        DeterministicSeed seed = new DeterministicSeed(new SecureRandom(), 128, passphrase);
        Wallet wallet = Wallet.fromSeed(networkParameters, seed, Script.ScriptType.P2WPKH);
        DeterministicKey deterministicKey = wallet.currentReceiveKey();
        String privateKey = deterministicKey.getPrivateKeyAsWiF(networkParameters);
        String mnemonics = wallet.getKeyChainSeed().getMnemonicCode().toString();
        String publicKey = Hex.toHexString(ECKey.publicKeyFromPrivate(deterministicKey.getPrivKey(), true));
        String address = wallet.currentReceiveAddress().toString();
        Account account = new Account();
        return account.setAddress(address).setPublicKey(publicKey).setPrivateKey(privateKey).setMnemonics(mnemonics).setPassphrase(passphrase);
    }


    /**
     * 生成多签地址
     * @param threshold 最低签名数量
     * @param keys 公钥创建的EcKey列表
     * @return
     */
    public  P2SHMultiSigAccount generateMultiSigAddress(NetworkParameters networkParameters, int threshold, List<ECKey> keys){
        //创建多签赎回脚本,下面是2/3示例
        //2 PUSHDATA(33)[0218e262023a9e32eb44cdc18a2158dc5a81c747e6e9c78e0c6a7edb8100a0147e] PUSHDATA(33)[03124af1502666ba7bf2e833ddab36a7f68e21340c97cc77ad0678291bde4c5282] PUSHDATA(33)[03c19f6736ba4d7851bae2f7b95e8aa7f919dca8ab0fc4c7483b265c0ebc970e47] 3 CHECKMULTISIG
        Script redeemScript = ScriptBuilder.createRedeemScript(threshold, keys);
        //为给定的赎回脚本 创建scriptPubkey
        Script script = ScriptBuilder.createP2SHOutputScript(redeemScript);
        //返回一个地址，该地址表示从给定的scriptPubKey中提取的脚本HASH
        byte[] scriptHash = ScriptPattern.extractHashFromP2SH(script);
        LegacyAddress legacyAddress = LegacyAddress.fromScriptHash(networkParameters, scriptHash);

        return new P2SHMultiSigAccount(redeemScript,legacyAddress);
    }

    /**
     * 通过ECKey获取P2PKH地址
     *
     * @param ecKey ECKey
     * @return Base58编码的地址文本
     */
    public  String getLegacyAddress(NetworkParameters networkParameters,ECKey ecKey) {
        LegacyAddress legacyAddress = LegacyAddress.fromKey(networkParameters, ecKey);
        return legacyAddress.toBase58();
    }

    /**
     * 获取隔离见证地址
     * @param ecKey ECKey
     * @return bech32编码后的地址文本
     */
    public  String getSegWitAddress(NetworkParameters networkParameters,ECKey ecKey) {
        SegwitAddress segwitAddress = SegwitAddress.fromKey(networkParameters, ecKey);
        return segwitAddress.toBech32();
    }


    public String getPublicKeyAsHex(ECKey ecKey) {
        return ecKey.getPublicKeyAsHex();
    }
}
