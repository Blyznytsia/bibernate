package com.bobocode.bibernate.session;

public interface Session {

  <T> T find(Class<T> entityClass, Object primaryKey);

  void remove(Object entity);

  void persist(Object entity);

  void flush();

  void close();
}
