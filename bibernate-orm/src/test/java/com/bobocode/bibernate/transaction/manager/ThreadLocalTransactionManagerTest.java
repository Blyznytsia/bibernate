package com.bobocode.bibernate.transaction.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bobocode.bibernate.session.Session;
import com.bobocode.bibernate.session.impl.SimpleSession;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThreadLocalTransactionManagerTest {

  ThreadLocalTransactionManager txManager;
  Session session;

  @SneakyThrows
  @BeforeEach
  void setUp() {
    var mockedDataSource = mock(DataSource.class);
    var mockedConnection = mock(Connection.class);
    when(mockedDataSource.getConnection()).thenReturn(mockedConnection);

    session = new SimpleSession(mockedDataSource);
    txManager = new ThreadLocalTransactionManager(mockedDataSource);
  }

  @SneakyThrows
  @Test
  void
      get_givenConnectionRequestedMultipleTimeWithinStartedTransaction_shouldReturnSameConnection() {
    txManager.begin();

    var connection1 = txManager.get();
    var connection2 = txManager.get();

    assertThat(connection1).isSameAs(connection2);
  }

  @Test
  void begin_givenTransactionIsStartedAndNotClosed_shouldFailToBeginTransactionAgain() {
    txManager.begin();

    assertThatThrownBy(() -> txManager.begin())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Transaction is already in progress.");
  }

  @Test
  void begin_givenPreviousTransactionIsCompleted_shouldAllowToStartAnotherTransaction() {
    txManager.begin();
    txManager.commit();

    txManager.begin();
    txManager.rollback();

    txManager.begin();
    txManager.commit();
  }

  @Test
  void begin_givenMultipleThreads_shouldBeAbleToBeginMultipleTransactionsSimultaneously() {
    for (int i = 0; i < 10; i++) {
      CompletableFuture.runAsync(() -> txManager.begin());
    }
  }

  @Test
  void commit_givenTransactionIsAlreadyCommitted_shouldFailToRollback() {
    txManager.begin();
    txManager.commit();

    assertThatThrownBy(() -> txManager.rollback())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Transaction is not active.");
  }

  @Test
  void rollback_givenTransactionIsAlreadyRollBacked_shouldFailToCommit() {
    txManager.begin();
    txManager.rollback();

    assertThatThrownBy(() -> txManager.commit())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Transaction is not active.");
  }

  @Test
  void afterRollbackCallback_givenTransactionIsAlreadyRollBacked_shouldFailToCommit() {
    List<String> result = new ArrayList<>();
    txManager.setAfterRollbackCallback(() -> result.add("Executed after rollback"));

    txManager.begin();
    try {
      // to mimic some done work that created a connection to DB
      txManager.get();

      throw new RuntimeException("Transaction has failed");
    } catch (Exception ignored) {
      txManager.rollback();
    }

    assertThat(result).hasSize(1);
  }
}
