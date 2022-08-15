package com.bobocode.bibernate.session.impl;

import com.bobocode.bibernate.session.SessionFactory;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleSessionFactory implements SessionFactory {

  private final DataSource dataSource;

  public SimpleSession createSession() {
    return new SimpleSession(dataSource);
  }
}
