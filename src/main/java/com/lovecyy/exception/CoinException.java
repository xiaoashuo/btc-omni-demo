package com.lovecyy.exception;

public class CoinException  extends CurrencyException{
    public CoinException(String message) {
        super(message);
    }
    public CoinException(String message,Throwable throwable) {
        super(message,throwable);
    }
}
