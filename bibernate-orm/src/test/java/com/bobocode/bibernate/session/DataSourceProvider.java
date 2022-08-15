package com.bobocode.bibernate.session;

import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.postgresql.ds.PGSimpleDataSource;

public interface DataSourceProvider {

  default DataSource inmemoryH2Datasource() {
    var ds = new JdbcDataSource();
    ds.setUrl("jdbc:h2:mem:bibernate;INIT=runscript from 'classpath:data.sql'");
    ds.setUser("sa");

    return ds;
  }

  default DataSource postgresDataSource() {
    PGSimpleDataSource dataSource = new PGSimpleDataSource();
    dataSource.setURL("jdbc:postgresql://localhost:5432/postgres");
    dataSource.setUser("baeldung");
    dataSource.setPassword("baeldung");

    return dataSource;
  }
}
