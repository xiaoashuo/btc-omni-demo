package com.lovecyy.model.pojo.accounts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.script.Script;

/**
 * P2SH多签账户
 * @author Yakir
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class P2SHMultiSigAccount {
    /**
     * 多签赎回脚本
     */
    private Script redeemScript;
    /**
     * 和脚本关联的地址对象
     */
    private LegacyAddress address;
}
