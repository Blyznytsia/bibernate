package com.bobocode.bibernate.transaction.manager;

import com.bobocode.bibernate.transaction.function.ThrowingCallable;
import com.bobocode.bibernate.transaction.function.ThrowingRunnable;

public interface TransactionManager {

  <T> T execInTransactionReturningResult(ThrowingCallable<T> throwingCallable);

  void execInTransaction(ThrowingRunnable throwingRunnable);

  void setAfterRollbackCallback(Runnable runnable);

  void begin();

  void commit();

  void rollback();
}
