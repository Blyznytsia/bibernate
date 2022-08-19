package com.bobocode.bibernate.session;

import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class BaseTest {

  @Container
  static PostgreSQLContainer<?> postgresContainer =
      new PostgreSQLContainer<>("postgres:14-alpine").withInitScript("init.sql");

  public static DataSource inmemoryH2Datasource() {
    var ds = new JdbcDataSource();
    ds.setUrl("jdbc:h2:mem:bibernate;INIT=runscript from 'classpath:init.sql'");
    ds.setUser("sa");

    return ds;
  }

  public static DataSource postgresDataSource() {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setURL(postgresContainer.getJdbcUrl());
    dataSource.setUser(postgresContainer.getUsername());
    dataSource.setPassword(postgresContainer.getPassword());

    return dataSource;
  }
}
