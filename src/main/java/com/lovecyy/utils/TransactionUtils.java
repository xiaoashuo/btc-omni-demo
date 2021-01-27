package com.lovecyy.utils;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.lovecyy.model.pojo.SignatureData;
import com.lovecyy.model.pojo.UTXOKey;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;

/**
 *
 * @author Yakir
 */
@RequiredArgsConstructor
@Component
public class TransactionUtils {

    private final AddressUtils addressUtils;

    /**
     * 构建并签名Legacy(遗留、常规)转账交易，包含P2PK,P2PKH,P2SH,P2WPKH,P2WSH
     * @param networkParameters
     * @param utxoKeys
     * @param receiveAddressAndValue
     * @return
     */
    public Transaction buildLegacyTransactionWithSigners(NetworkParameters networkParameters, List<UTXOKey> utxoKeys, Map<String, Double> receiveAddressAndValue){
        //新建交易
        Transaction transaction = new Transaction(networkParameters);
        //添加输出列表
        addOutputs(networkParameters,transaction,receiveAddressAndValue);
        //遍历未花费的输出并签名
        for (UTXOKey utxoKey : utxoKeys) {
            UTXO utxo = utxoKey.getUtxo();
            //获取utxo脚本类型
            Script script = utxo.getScript();
            Script.ScriptType scriptType = script.getScriptType();
            //区分Legacy和Segwit的utxo
            if (scriptType== Script.ScriptType.P2PK||scriptType== Script.ScriptType.P2PKH||scriptType== Script.ScriptType.P2SH){
                signLegacyTransaction(networkParameters,transaction, utxo, utxoKey.getEcKey());
            }else if (scriptType== Script.ScriptType.P2WPKH||scriptType== Script.ScriptType.P2WSH){
                signSegWitTransaction(networkParameters,transaction, utxo, utxoKey.getEcKey());
            }

        }
        return  transaction;
    }

    /**
     * 签名Legacy(遗留、常规)转账交易，包含P2WPKH,P2WSH
     * @param networkParameters
     * @param transaction 待签名交易
     * @param utxo
     * @param ecKey
     */
    private void signSegWitTransaction(NetworkParameters networkParameters, Transaction transaction, UTXO utxo, ECKey ecKey) {
        //构建新交易的input
        TransactionInput transactionInput = transaction.addInput(utxo.getHash(), utxo.getIndex(), utxo.getScript());
        //计算见证签名
        Script scriptCode = ScriptBuilder.createP2PKHOutputScript(ecKey);
        // 使用如下包装 后报错non-mandatory-script-verify-flag (Signature must be zero for failed CHECK(MULTI)SIG operation)
        //  Script scriptCode = new ScriptBuilder().data(ScriptBuilder.createP2PKHOutputScript(ecKey).getProgram()).build();
        TransactionSignature txSig1 = transaction.calculateWitnessSignature(transactionInput.getIndex(), ecKey,
                scriptCode, utxo.getValue(),
                Transaction.SigHash.ALL, false);

        //设置input的交易见证
        transactionInput.setWitness(TransactionWitness.redeemP2WPKH(txSig1, ecKey));
        //隔离见证的input不需要scriptSig
        transactionInput.clearScriptBytes();
    }


    /**
     * 签名Legacy(遗留、常规)转账交易，包含P2PK,P2PKH,P2SH
     *
     * @param transaction 待签名交易
     * @param utxo        UTXO
     * @param ecKey       ECKey
     */
    private void signLegacyTransaction(NetworkParameters networkParameters,Transaction transaction, UTXO utxo, ECKey ecKey) {
        TransactionOutPoint outPoint = new TransactionOutPoint(networkParameters, utxo.getIndex(), utxo.getHash());
        transaction.addSignedInput(outPoint, utxo.getScript(), ecKey, Transaction.SigHash.ALL, true);
    }


    private void addOutputs( NetworkParameters networkParameters,Transaction transaction, Map<String, Double> receiveAddressAndValue) {
        for (Map.Entry<String, Double> entry : receiveAddressAndValue.entrySet()) {
            String receiveAddress = entry.getKey();
            Address address;
            //判断是否为遗留地址
            if (addressUtils.isLegacyAddress(networkParameters,receiveAddress)){
                address= LegacyAddress.fromBase58(networkParameters, receiveAddress);
            }else if (addressUtils.isSegWitAddress(networkParameters,receiveAddress)){
                address = SegwitAddress.fromBech32(networkParameters, receiveAddress);
            }else{
                throw new AddressFormatException.InvalidPrefix("No network found for "+receiveAddress);
            }
            Coin value=Coin.valueOf(Converter.bitcoinToSatoshis(entry.getValue()));
            transaction.addOutput(value,address);
        }
    }

    /**
     * 构建待签名交易
     *
     * @param utxos                  UTXO
     * @param receiveAddressAndValue 接收地址和金额列表
     * @return 交易
     */
    public Transaction buildTransaction(NetworkParameters networkParameters,List<UTXO> utxos, Map<String, Double> receiveAddressAndValue) {
        //新建交易
        Transaction transaction = new Transaction(networkParameters);
        //构建输出列表
        addOutputs(networkParameters,transaction, receiveAddressAndValue);
        //构建Inputs
        for (UTXO utxo : utxos) {
            ScriptBuilder scriptBuilder = new ScriptBuilder();
            // Script of this output
            scriptBuilder.data(utxo.getScript().getProgram());
//            Script script = utxo.getScript();
            transaction.addInput(utxo.getHash(), utxo.getIndex(), scriptBuilder.build());
        }

        return transaction;
    }

    /**
     * 多签转账交易签名
     * @param networkParameters
     * @param transaction 多签交易
     * @param knownRedeemScript 已知的赎回脚本(创建多签地址的时候生成的)
     * @param key 签名所需的ECKey
     * @param first  首次签名(也就是第一个签名)
     */
    public void signMultiSigTransaction(NetworkParameters networkParameters, Transaction transaction, Script knownRedeemScript, ECKey key, boolean first) {
        List<TransactionInput> inputs = transaction.getInputs();
        for (int i = 0; i < inputs.size(); i++) {
            TransactionInput transactionInput = inputs.get(i);

            //若为第一个签名
            if (first){
                //计算待签名hash，也就是交易的简化形式的hash值，包含当前要签名的input和所有output,对这个hash值进行签名
                Sha256Hash sigHash = transaction.hashForSignature(i, knownRedeemScript, Transaction.SigHash.ALL, false);
                //签名之后得到ECDSASignature
                ECKey.ECDSASignature ecdsaSignature = key.sign(sigHash);
                //EcdsaSignature转换为交易签名TransactionSignature
                TransactionSignature transactionSignature = new TransactionSignature(ecdsaSignature, Transaction.SigHash.ALL, false);
                //创建P2SH多重签名的输入脚本
                Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(Collections.singletonList(transactionSignature), knownRedeemScript);
                //将脚本添加进input
                transactionInput.setScriptSig(inputScript);
            }
            //若不为第一个签名
            else{
                //获取输入的脚本元素列表(可以理解为操作码列表)
                Script inputScript = transactionInput.getScriptSig();
                List<ScriptChunk> scriptChunks = inputScript.getChunks();
                //创建一个空的签名列表，这个List最后会保存所有的签名
                List<TransactionSignature> signatureList=new ArrayList<>();
                //由于脚本元素列表中的最后一个签名是redeemScript,所以用过迭代的方法获取redeemScript
                //第一个元素一般都为 0
                //迭代过程中也将别人之前的签名添加到了上面签名列表里
                Iterator<ScriptChunk> iterator = scriptChunks.iterator();
                Script redeemScript=null;
                while (iterator.hasNext()){

                    ScriptChunk chunk = iterator.next();
                    if (iterator.hasNext()&&chunk.opcode==0){
                        //若有下一项并且操作码为0 则跳过
                        continue;
                    }else if (iterator.hasNext()&&chunk.opcode!=0){
                        //若有下一项 操作码不能0 则为别人的签名
                        TransactionSignature transactionSignature=null;
                        try {
                              //若能解码则为别人之前的签名
                            transactionSignature = TransactionSignature.decodeFromBitcoin(Objects.requireNonNull(chunk.data), false, false);
                        } catch (SignatureDecodeException e) {
                            e.printStackTrace();
                        }
                        signatureList.add(transactionSignature);
                    }else  if (!iterator.hasNext()&&chunk.opcode!=0){
                        //若无下一项并且操作码不为0 则为赎回脚本
                        redeemScript=new Script(Objects.requireNonNull(chunk.data));
                    }

                }

                //计算待签名hash 也就是交易的简化形式的hash值，对这个hash值进行签名
                Sha256Hash sigHash = transaction.hashForSignature(i, Objects.requireNonNull(redeemScript), Transaction.SigHash.ALL, false);
                //签名之后得到ECDSASignature
                ECKey.ECDSASignature ecdsaSignature = key.sign(sigHash);
                //ECSDASignature转换为TransactionSignature
                TransactionSignature transactionSignature = new TransactionSignature(ecdsaSignature, Transaction.SigHash.ALL, false);
                //添加本次签名的数据
                signatureList.add(transactionSignature);
                //重新构建P2SH多重签名的输入脚本
                inputScript= ScriptBuilder.createP2SHMultiSigInputScript(signatureList, redeemScript);
                //更新新的脚本
                transactionInput.setScriptSig(inputScript);
            }
        }

    }
    /**
     * 获取所有待签名hash列表，hash数量等于input的数量,也就是交易的简化形式的hash值，
     * 包含当前要签名的input和所有output,对这个hash值进行签名
     *
     * @param transaction  交易
     * @param redeemScript 赎回脚本
     * @return 待签名hash列表
     */
    public List<SignatureData> getSimplifiedTransactionHashes(Transaction transaction, Script redeemScript) {
        List<SignatureData> signatureDataList = new ArrayList<>();
        for (TransactionInput transactionInput: transaction.getInputs()) {
            Sha256Hash sigHash = transaction.hashForSignature(transactionInput.getIndex(), redeemScript, Transaction.SigHash.ALL, false);
            signatureDataList.add(new SignatureData(transactionInput.getIndex(),sigHash.toString()));
        }
        return signatureDataList;
    }

    /**
     * 对交易的简化形式（包含当前要签名的input和所有output）的hash值进行签名
     *
     * @param ecKey                     ECKey
     * @param simplifiedTransactionHash 交易的简化形式
     * @return ECDSASignature
     */
    public ECKey.ECDSASignature sign(ECKey ecKey, String simplifiedTransactionHash) {
        return ecKey.sign(Sha256Hash.wrap(simplifiedTransactionHash));
    }
    /**
     * 将签名添加进交易
     *
     * @param transaction 交易
     * @param inputIndex  input下标（表示签名的是哪个input）
     * @param signatures  签名列表（多签的签名不止一个）
     * @param redeemScript 赎回脚本
     */
    public void addMultiSignatures(Transaction transaction, int inputIndex, List<ECKey.ECDSASignature> signatures, Script redeemScript) {
        List<TransactionSignature> signatureList = new ArrayList<>();
        for (ECKey.ECDSASignature signature : signatures) {
            TransactionSignature transactionSignature = new TransactionSignature(signature, Transaction.SigHash.ALL, false);
            signatureList.add(transactionSignature);
        }
        // 重新构建p2sh多重签名的输入脚本
        Script inputScript = ScriptBuilder.createP2SHMultiSigInputScript(signatureList, redeemScript);

        //更新新的脚本
        transaction.getInputs().get(inputIndex).setScriptSig(inputScript);
    }


    /**
     * 计算 btc 交易的手续费
     * @param inNumber 输入数量
     * @param outNumber 输出数量
     * @param fee 每字节手续费单价
     * @return org.bitcoinj.core.Coin
     * @author lingting 2021-01-07 14:02
     */
    public  Coin getFee(long inNumber, long outNumber, Coin fee) {
        return fee.multiply(inNumber * 148 + outNumber * 34 + 10);
    }

    public Long getFeeRate() {
        String feeResponse = HttpUtil.get("https://bitcoinfees.earn.com/api/v1/fees/recommended");
        Map map = JSONUtil.toBean(feeResponse, Map.class);
        String fastestFee = map.get("fastestFee").toString();
        return Long.valueOf(fastestFee);
    }
}
