package com.lovecyy.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 未花费的输出
 * @author Yakir
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SimpleUnspentTransaction {


    private String txid;
    private Integer vout;
    private String scriptPubKey;
    private BigDecimal value;



}
