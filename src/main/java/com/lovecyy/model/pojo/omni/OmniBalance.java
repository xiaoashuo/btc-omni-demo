package com.lovecyy.model.pojo.omni;

import lombok.Data;

import java.math.BigDecimal;

/**
 * omni 余额
 * @author Yakir
 */
@Data
public class OmniBalance {
    /**
     * 资产余额
     */
    // (string) the available balance of the address
    private BigDecimal balance;
    /**
     * 保留数量
     */
    // (string) the amount reserved by sell offers and accepts
    private BigDecimal reserved;
    /**
     * 被发行人冻结的数量
     */
    // (string) the amount frozen by the issuer (applies to managed properties on
    private BigDecimal frozen;
}
