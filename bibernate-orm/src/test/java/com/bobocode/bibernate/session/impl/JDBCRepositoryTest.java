package com.bobocode.bibernate.session.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.bobocode.bibernate.context.PersistenceContext;
import com.bobocode.bibernate.entity.eager.EagerUser;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class JDBCRepositoryTest {

  @Test
  void findOneById_givenMultipleEntitiesFoundForTheSameId_shouldThrowException() {
    var persistenceContext = mock(PersistenceContext.class);
    var dataSource = mock(DataSource.class);
    var repositorySpy = spy(new JDBCRepository(dataSource, persistenceContext));

    List<Object> users = List.of(new EagerUser().setId(100L), new EagerUser().setId(100L));
    doReturn(users).when(repositorySpy).findAllByField(any(), any(), any());
    doReturn(false).when(persistenceContext).contains(any());

    var id = 22L;
    var entityType = EagerUser.class.getSimpleName();
    assertThatThrownBy(() -> repositorySpy.findOneById(EagerUser.class, id))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Found more than 1 record for entity %s with id=%s", entityType, id);
  }
}
