package com.bobocode.bibernate.session.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bobocode.bibernate.entity.User;
import com.bobocode.bibernate.session.DataSourceProvider;
import com.bobocode.bibernate.session.Session;
import com.bobocode.bibernate.session.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionTest implements DataSourceProvider {

  Session session;
  SessionFactory sessionFactory;

  @BeforeEach
  void setUp() {
    sessionFactory = new SimpleSessionFactory(postgresDataSource());
    session = sessionFactory.createSession();
  }

  @Test
  void find_givenEntityIsMultipleTimes_shouldReturnCachedEntityFromTheContext() {
    User user = session.find(User.class, 2L);
    User sameUser = session.find(User.class, 2L);

    assertThat(user).isSameAs(sameUser);
  }

  @Test
  void find_givenNoEntityFoundById_shouldThrowException() {
    var id = 22L;
    var entityType = User.class.getSimpleName();
    assertThatThrownBy(() -> session.find(User.class, id))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No records found for '%s' entity with id=%s", entityType, id);
  }
}
