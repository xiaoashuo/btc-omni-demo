package com.lovecyy.model.pojo.accounts;

import lombok.Data;
import lombok.experimental.Accessors;


/**
 * btc账号 普通账户
 * @author Yakir
 */
@Accessors(chain = true)
@Data
public class Account {
    /**
     * 私钥wif格式
     */
    private String privateKey;
    /**
     * 助记词
     */
    private String mnemonics;
    /**
     * 公钥
     */
    private String publicKey;
    /**
     * 地址
     */
    private String address;
    /**
     * 密码短语
     */
    private String passphrase;

}
