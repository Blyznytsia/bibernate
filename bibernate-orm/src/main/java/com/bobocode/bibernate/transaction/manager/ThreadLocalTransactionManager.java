package com.bobocode.bibernate.transaction.manager;

import com.bobocode.bibernate.exception.TransactionManagerException;
import com.bobocode.bibernate.transaction.connection.ConnectionProvider;
import com.bobocode.bibernate.transaction.connection.DelegatingConnection;
import com.bobocode.bibernate.transaction.function.ThrowingCallable;
import com.bobocode.bibernate.transaction.function.ThrowingRunnable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ThreadLocalTransactionManager implements TransactionManager, ConnectionProvider {

  private static final DelegatingConnection TRANSACTION_MARKER = new DelegatingConnection(null);
  private final ThreadLocal<DelegatingConnection> connections = new ThreadLocal<>();
  private final DataSource rawDataSource;

  @Setter private Runnable afterRollbackCallback;

  @Override
  public DelegatingConnection get() throws SQLException {
    DelegatingConnection delegatingConnection = connections.get();
    checkConnectionActive(delegatingConnection);

    if (delegatingConnection == TRANSACTION_MARKER) {
      Connection physicalConnection = rawDataSource.getConnection();
      physicalConnection.setAutoCommit(false);
      delegatingConnection = new DelegatingConnection(physicalConnection);

      connections.set(delegatingConnection);
      log.debug("Obtained a new DB connection");
    }

    return delegatingConnection;
  }

  @Override
  public <T> T execInTransactionReturningResult(ThrowingCallable<T> throwingCallable) {
    begin();
    try {
      T result = throwingCallable.call();
      commit();
      return result;
    } catch (Exception e) {
      rollback();
      throw new TransactionManagerException(e);
    }
  }

  public void execInTransaction(ThrowingRunnable throwingRunnable) {
    execInTransactionReturningResult(
        () -> {
          throwingRunnable.run();
          return null;
        });
  }

  @Override
  public void begin() {
    verifyNoOngoingTransaction();

    // setting a special marker to indicate that transaction has been started
    connections.set(TRANSACTION_MARKER);
    log.debug("Transaction has started");
  }

  @Override
  public void commit() {
    getRawConnection()
        .ifPresent(
            rawConnection -> {
              try {
                rawConnection.commit();
                rawConnection.close();
                log.debug("Transaction has been successfully committed");
              } catch (SQLException ex) {
                throw new TransactionManagerException("Exception during transaction commit", ex);
              }
            });
  }

  @Override
  public void rollback() {
    getRawConnection()
        .ifPresent(
            rawConnection -> {
              try {
                rawConnection.rollback();
                rawConnection.close();
                log.debug("Transaction has been successfully rolled back");
              } catch (SQLException ex) {
                throw new TransactionManagerException("Exception during transaction rollback", ex);
              } finally {
                if (afterRollbackCallback != null) {
                  afterRollbackCallback.run();
                }
              }
            });
  }

  private Optional<Connection> getRawConnection() {
    DelegatingConnection delegatingConnection = connections.get();
    checkConnectionActive(delegatingConnection);

    connections.remove();

    // if transaction is still marked as started but hasn't obtained a physical connection to DB,
    // thus nothing to commit/rollback yet
    if (delegatingConnection == TRANSACTION_MARKER) {
      return Optional.empty();
    }

    //  otherwise return the obtained physical DB connection
    return Optional.of(delegatingConnection.getDelegate());
  }

  private void checkConnectionActive(DelegatingConnection connection) {
    if (connection == null) {
      throw new IllegalStateException("Transaction is not active.");
    }
  }

  private void verifyNoOngoingTransaction() {
    if (connections.get() != null) {
      throw new IllegalStateException("Transaction is already in progress.");
    }
  }
}
