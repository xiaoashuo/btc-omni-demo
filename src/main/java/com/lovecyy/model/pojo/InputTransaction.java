package com.lovecyy.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 原生裸交易输入
 * @author Yakir
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InputTransaction {
    /**
     * 交易id
     */
    private String txid;
    /**
     * 交易指向 引用
     */
    private Integer vout;
}
