package com.bobocode.bibernate.transaction.connection;

import java.sql.Connection;
import java.sql.SQLException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class DelegatingConnection implements Connection {

  @Delegate(excludes = Exclude.class)
  @Getter
  private final Connection delegate;

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    if (autoCommit) {
      throw new UnsupportedOperationException("Autocommit cannot be enabled");
    }

    delegate.setAutoCommit(false);
  }

  @Override
  public void commit() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void rollback() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    // NOOP
  }

  private interface Exclude {
    void setAutoCommit(boolean autoCommit);

    void commit();

    void rollback();

    void close();
  }
}
