package com.bobocode.bibernate.exception;

public class TransactionManagerException extends RuntimeException {

  public TransactionManagerException(String message) {
    super(message);
  }

  public TransactionManagerException(String message, Throwable cause) {
    super(message, cause);
  }

  public TransactionManagerException(Throwable cause) {
    super(cause);
  }
}
