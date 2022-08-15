package com.bobocode.bibernate.session.impl;

import com.bobocode.bibernate.context.PersistenceContext;
import com.bobocode.bibernate.session.Session;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleSession implements Session {

  private final PersistenceContext context;
  private final JDBCRepository jdbcRepository;

  public SimpleSession(DataSource dataSource) {
    this.context = new PersistenceContext();
    this.jdbcRepository = new JDBCRepository(dataSource, context);
  }

  @Override
  public <T> T find(Class<T> entityType, Object id) {
    log.info("Searching for {} with id = {}", entityType.getSimpleName(), id);
    return jdbcRepository.findOneById(entityType, id);
  }

  @Override
  public void remove(Object entity) {}

  @Override
  public void persist(Object entity) {}

  @Override
  public void flush() {}

  @Override
  public void close() {}
}
