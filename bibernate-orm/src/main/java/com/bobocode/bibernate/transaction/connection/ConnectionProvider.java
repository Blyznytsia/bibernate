package com.bobocode.bibernate.transaction.connection;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionProvider {

  Connection get() throws SQLException;
}
