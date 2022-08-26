package com.bobocode.bibernate.transaction.function;

@FunctionalInterface
public interface ThrowingRunnable {

  void run() throws Exception;
}
