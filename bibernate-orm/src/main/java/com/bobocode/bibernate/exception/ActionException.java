package com.bobocode.bibernate.exception;

import java.sql.SQLException;

public class ActionException extends RuntimeException{

    public ActionException(String message, Throwable e) {
        super(message, e);
    }
}
