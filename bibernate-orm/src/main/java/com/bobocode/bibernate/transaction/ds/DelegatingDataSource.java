package com.bobocode.bibernate.transaction.ds;

import com.bobocode.bibernate.transaction.connection.ConnectionProvider;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class DelegatingDataSource implements DataSource {

  @Delegate(excludes = Exclude.class)
  private final DataSource delegate;

  private final ConnectionProvider connectionProvider;

  @Override
  public Connection getConnection() throws SQLException {
    return connectionProvider.get();
  }

  private interface Exclude {
    void getConnection();
  }
}
