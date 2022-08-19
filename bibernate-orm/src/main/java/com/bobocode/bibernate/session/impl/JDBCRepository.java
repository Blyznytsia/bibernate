package com.bobocode.bibernate.session.impl;

import static com.bobocode.bibernate.enums.FetchType.LAZY;
import static com.bobocode.bibernate.util.EntityUtils.extractCollectionType;
import static com.bobocode.bibernate.util.EntityUtils.extractFieldName;
import static com.bobocode.bibernate.util.EntityUtils.extractId;
import static com.bobocode.bibernate.util.EntityUtils.extractIdField;
import static com.bobocode.bibernate.util.EntityUtils.extractTableAlias;
import static com.bobocode.bibernate.util.EntityUtils.findJoinColumn;
import static com.bobocode.bibernate.util.EntityUtils.setManyToOneField;
import static com.bobocode.bibernate.util.EntityUtils.setOneToManyField;

import com.bobocode.bibernate.annotation.ManyToOne;
import com.bobocode.bibernate.annotation.OneToMany;
import com.bobocode.bibernate.context.PersistenceContext;
import com.bobocode.bibernate.util.EntityKey;
import com.bobocode.bibernate.util.SqlUtils;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.VisibleForTesting;

@Slf4j
public class JDBCRepository {

  private final DataSource dataSource;
  private final PersistenceContext context;

  public JDBCRepository(DataSource dataSource, PersistenceContext context) {
    this.dataSource = dataSource;
    this.context = context;
  }

  public <T> T findOneById(Class<T> entityType, Object id) {
    var key = EntityKey.of(entityType, id);

    if (context.contains(key)) {
      T entity = context.getEntity(key);
      log.info("Returning cached entity {}", entity);
      return entity;
    }

    var idField = extractIdField(entityType);
    var entities = findAllByField(entityType, idField, id);

    checkOnlyOneEntityFound(entityType, id, entities);

    return entities.get(0);
  }

  private void checkOnlyOneEntityFound(Class<?> entityType, Object id, List<?> entities) {
    if (entities.isEmpty()) {
      throw new IllegalStateException(
          "No records found for '%s' entity with id=%s".formatted(entityType.getSimpleName(), id));
    }
    if (entities.size() > 1) {
      throw new IllegalStateException(
          "Found more than 1 record for entity %s with id=%s"
              .formatted(entityType.getSimpleName(), id));
    }
  }

  @VisibleForTesting
  <T> List<T> findAllByField(Class<T> entityType, Field filterField, Object fieldVal) {
    try (var connection = dataSource.getConnection()) {

      String sql = SqlUtils.selectByField(entityType, filterField);
      try (var statement = connection.prepareStatement(sql)) {
        statement.setObject(1, fieldVal);
        log.debug("Executing {}", statement);
        var resultSet = statement.executeQuery();

        List<T> entities = new ArrayList<>();
        while (resultSet.next()) {
          var entity = extractEntityFromResultSet(entityType, resultSet);
          entities.add(entity);
        }

        return entities;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @SneakyThrows
  private <T> T extractEntityFromResultSet(Class<T> entityType, ResultSet resultSet) {
    log.debug(
        "Parsing the result set to create instance of '{}' entity", entityType.getSimpleName());
    var instance = entityType.getConstructor().newInstance();

    for (var field : entityType.getDeclaredFields()) {
      field.setAccessible(true);
      String fieldName = extractFieldName(field);

      if (field.isAnnotationPresent(ManyToOne.class)) {
        var fieldValue = findOneById(field.getType(), resultSet.getObject(fieldName));
        log.trace("Setting '{}' field with value '{}'", fieldName, fieldValue);
        field.set(instance, fieldValue);
      } else if (field.isAnnotationPresent(OneToMany.class)) {
        var annotation = field.getAnnotation(OneToMany.class);
        Class<?> owningEntityType = extractCollectionType(field);

        if (annotation.fetch() == LAZY) {
          Field joinColumn = findJoinColumn(owningEntityType, entityType);
          var referencedEntityId = extractId(instance);

          var fieldValue =
              createLazyCollection(field, owningEntityType, joinColumn, referencedEntityId);

          log.trace(
              "Setting '{}' field to '{}'",
              field.getName(),
              "lazy collection of %s<%s>"
                  .formatted(field.getType().getSimpleName(), owningEntityType.getSimpleName()));

          field.set(instance, fieldValue);
        } else {
          log.trace(
              "Fetching eagerly {}<{}> collection",
              field.getType().getSimpleName(),
              owningEntityType.getSimpleName());

          Field idField = extractIdField(owningEntityType);
          var columnValue = resultSet.getObject(extractFieldName(idField));
          return createEntityEagerly(entityType, owningEntityType, columnValue);
        }
      } else {
        var fieldValue = resultSet.getObject(fieldName);
        log.trace("Setting '{}' field to '{}'", fieldName, fieldValue);
        field.set(instance, fieldValue);
      }
    }

    context.addEntity(instance);
    return instance;
  }

  @SneakyThrows
  public <T> T createEntityEagerly(Class<T> parentType, Class<?> childType, Object parentId) {
    try (var connection = dataSource.getConnection()) {

      String sql = SqlUtils.createSelectWithLeftJoin(parentType, childType);
      try (var statement = connection.prepareStatement(sql)) {
        statement.setObject(1, parentId);
        log.debug("Executing:\n'{}'", statement);

        var resultSet = statement.executeQuery();

        var parentEntities = new ArrayList<>();
        var childEntities = new ArrayList<>();

        while (resultSet.next()) {
          var parentInstance = extractEntityWithBasicFieldsFromResultSet(parentType, resultSet);
          var childInstance = extractEntityWithBasicFieldsFromResultSet(childType, resultSet);

          parentEntities.add(parentInstance);
          childEntities.add(childInstance);
        }

        T parentEntity = setOneToManyField(parentType, parentEntities.get(0), childEntities);
        childEntities.forEach(e -> setManyToOneField(childType, e, parentEntity));
        childEntities.forEach(context::addEntity);
        context.addEntity(parentEntity);

        return parentType.cast(parentEntity);
      }
    }
  }

  @SneakyThrows
  private Object extractEntityWithBasicFieldsFromResultSet(Class<?> type, ResultSet resultSet) {
    var instance = type.getConstructor().newInstance();
    for (Field f : type.getDeclaredFields()) {
      if (f.isAnnotationPresent(ManyToOne.class) || f.isAnnotationPresent(OneToMany.class)) {
        continue;
      }

      f.setAccessible(true);
      String fieldName = extractFieldName(f);

      String tableAlias = extractTableAlias(type);
      var value = resultSet.getObject(tableAlias + fieldName);
      f.set(instance, value);
    }

    return instance;
  }

  private Object createLazyCollection(
      Field oneToManyField, Class<?> elementType, Field joinColumn, Object referencedEntityId) {
    InvocationHandler handler =
        (proxy, method, methodArgs) -> {
          log.debug("Fetching lazy collection");
          return findAllByField(elementType, joinColumn, referencedEntityId);
        };

    var collectionType = oneToManyField.getType();
    return Proxy.newProxyInstance(
        Thread.currentThread().getContextClassLoader(), new Class[] {collectionType}, handler);
  }
}
