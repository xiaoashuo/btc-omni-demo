package com.lovecyy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * btc 地址类型
 * @author Yakir
 */

@Getter
@RequiredArgsConstructor
public enum AddressType {
    /**
     * 普通地址
     */
    SINGLE(1),
    /**
     * 多签地址
     */
    MULTIPLE(2),
    /**
     * 隔离见证地址
     */
    ISOLATION(3);
    /**
     * 地址类型
     */
    private final int type;

}
