package com.bobocode.bibernate.session.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bobocode.bibernate.entity.eager.EagerTweet;
import com.bobocode.bibernate.entity.eager.EagerUser;
import com.bobocode.bibernate.entity.lazy.LazyUser;
import com.bobocode.bibernate.session.BaseTest;
import com.bobocode.bibernate.session.Session;
import com.bobocode.bibernate.session.SessionFactory;
import java.lang.reflect.Proxy;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SessionTest extends BaseTest {

  Session session;
  SessionFactory sessionFactory;

  @BeforeEach
  void setUp() {
    sessionFactory = new SimpleSessionFactory(postgresDataSource());
    session = sessionFactory.createSession();
  }

  @Test
  void find_givenEntityIsMultipleTimes_shouldReturnCachedEntityFromTheContext() {
    var txManager = session.getTransactionManager();

    txManager.execInTransaction(
        () -> {
          EagerUser user = session.find(EagerUser.class, 2L);
          EagerUser sameUser = session.find(EagerUser.class, 2L);

          assertThat(user).isSameAs(sameUser);
        });
  }

  /*
   TODO: rewrite as unit test to check that no calls to db are made,
    checking if a collection is proxied isn't enough.
  */
  @Test
  void find_givenEntityHasOneToManyRelationWithDefaultFetchType_shouldContainLazyCollection() {
    var txManager = session.getTransactionManager();
    txManager.execInTransaction(
        () -> {
          LazyUser user = session.find(LazyUser.class, 2L);

          var collectionType = user.getTweets().getClass();
          user.getTweets().forEach(System.out::println);
          assertThat(Proxy.isProxyClass(collectionType)).isTrue();
        });
  }

  @Test
  void find_givenEntityHasOneToManyRelationWithEagerFetchType_shouldContainPopulatedCollection() {
    var txManager = session.getTransactionManager();

    EagerUser user =
        txManager.execInTransactionReturningResult(() -> session.find(EagerUser.class, 2L));

    var tweets = user.getTweets();
    var collectionType = tweets.getClass();

    var tweet1 = new EagerTweet();
    tweet1.setId(3L);
    tweet1.setTweetText("Spring Break is coming to Alumni Athletics");
    tweet1.setUser(user);

    var tweet2 = new EagerTweet();
    tweet2.setId(4L);
    tweet2.setTweetText("Yikes");
    tweet2.setUser(user);

    assertThat(Proxy.isProxyClass(collectionType)).isFalse();
    assertThat(tweets)
        // ignore nested 'tweets' in the user
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("user.tweets")
        .containsExactlyInAnyOrder(tweet1, tweet2);
  }

  @Test
  void find_givenNoEntityFoundById_shouldThrowException() {
    var id = ThreadLocalRandom.current().nextInt(100, 200);
    var txManager = session.getTransactionManager();

    txManager.begin();

    var entityType = EagerUser.class.getSimpleName();
    assertThatThrownBy(() -> session.find(EagerUser.class, id))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("No records found for '%s' entity with id=%s", entityType, id);
  }
}
