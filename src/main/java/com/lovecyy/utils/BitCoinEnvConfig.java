package com.lovecyy.utils;

import lombok.Data;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

/**
 * bitcoin 环境配置
 * @author Yakir
 */
@Data
public class BitCoinEnvConfig {
    private static final BitCoinEnvConfig INSTANCE=new BitCoinEnvConfig();

    private NetworkParameters networkParameters;

    public static BitCoinEnvConfig getInstance(){
        return INSTANCE;
    }

    private BitCoinEnvConfig(){

    }


}
