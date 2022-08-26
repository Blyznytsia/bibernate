package com.bobocode.bibernate.transaction.function;

@FunctionalInterface
public interface ThrowingCallable<T> {

  T call() throws Exception;
}
