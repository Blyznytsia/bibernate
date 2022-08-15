package com.bobocode;

import com.bobocode.bibernate.session.impl.SimpleSessionFactory;
import com.bobocode.entities.User;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

public class BibernateDemo {

  public static void main(String[] args) {
    var sessionFactory = new SimpleSessionFactory(inmemoryH2Datasource());
    var session = sessionFactory.createSession();

    User user = session.find(User.class, 2L);
    User sameUser = session.find(User.class, 2L);

    assert user == sameUser;

    user.getTweets().forEach(System.out::println);
  }

  static DataSource inmemoryH2Datasource() {
    var ds = new JdbcDataSource();
    ds.setUrl("jdbc:h2:mem:bibernate;INIT=runscript from 'classpath:data.sql'");
    ds.setUser("sa");
    return ds;
  }
}
