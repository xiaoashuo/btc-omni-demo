package com.lovecyy.model.pojo.omni;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


/**
 * omni Token balance
 * @author Yakir
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Data
public class OmniTokenBalance extends OmniBalance {
    /**
     * 资产id
     */
    private Integer propertyid;
    /**
     * 资产名称
     */
    private String name;
}
