package com.emc.cf.broker.pg.exception;

/**
 * Created by liuc11 on 4/13/16.
 */
public class TransactionException extends RuntimeException {

    public TransactionException(Throwable t) {
        super(t);
    }

}
