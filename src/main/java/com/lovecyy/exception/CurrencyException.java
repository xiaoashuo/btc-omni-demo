package com.lovecyy.exception;

/**
 * 货币异常
 * @author Yakir
 */
public class CurrencyException extends RuntimeException {
    public CurrencyException(String message) {
        super(message);
    }

    public CurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
