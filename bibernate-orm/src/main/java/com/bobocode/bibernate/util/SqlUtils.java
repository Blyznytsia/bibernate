package com.bobocode.bibernate.util;

import static com.bobocode.bibernate.util.EntityUtils.*;
import static java.lang.String.join;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Field;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
// TODO: think about having a fluent builder for construction of SQL statements
public final class SqlUtils {
  private static final String LEFT_JOIN_QUERY =
      """
        SELECT
        %s,
        %s
        FROM %s as %s
        LEFT JOIN %s as %s
        ON %s = %s
        WHERE %s = ?;
      """;
  public static final String INSERT_QUERY = "insert into %s(%s) values (%s)";
  public static final String DELETE_QUERY = "delete from %s where id = ?";

  public static String createSelectWithLeftJoin(Class<?> leftTableType, Class<?> rightTableType) {
    var leftTableName = extractTableName(leftTableType);
    var rightTableName = extractTableName(rightTableType);

    var leftTableAlias = extractTableAlias(leftTableType);
    var rightTableAlias = extractTableAlias(rightTableType);

    var leftTablePrimaryKey =
        join(".", leftTableAlias, extractFieldName(extractIdField(leftTableType)));
    var rightTableForeignKey =
        join(".", rightTableAlias, extractFieldName(findManyToOneField(rightTableType)));

    var leftTableColumnNames = getColumnAliasesForSimpleFields(leftTableType, leftTableAlias);
    var rightTableColumnNames = getColumnAliasesForSimpleFields(rightTableType, rightTableAlias);

    return LEFT_JOIN_QUERY.formatted(
        leftTableColumnNames,
        rightTableColumnNames,
        leftTableName,
        leftTableAlias,
        rightTableName,
        rightTableAlias,
        leftTablePrimaryKey,
        rightTableForeignKey,
        leftTablePrimaryKey);
  }

  public static String selectByField(Class<?> entityType, Field filterField) {
    var tableName = extractTableName(entityType);
    var filterFieldName = extractFieldName(filterField);
    return "SELECT * FROM %s WHERE %s = ?;".formatted(tableName, filterFieldName);
  }

  private static String getColumnAliasesForSimpleFields(Class<?> entityType, String tableAlias) {
    return findAllSimpleFields(entityType)
        .map(EntityUtils::extractFieldName)
        .map(name -> tableAlias + "." + name)
        .map(name -> name + " AS " + name.replace(".", ""))
        .collect(joining(","));
  }

  /**
   * example:
   * <pre>
   *      insert into table_names(firstName, lastName) values (?, ?);
   * </pre>
   *
   * @param entity - entity object
   * @return built update query string for {@link java.sql.PreparedStatement}
   * @param <T> type of entity
   */
  public static <T> String createUpdateQuery(T entity) {
    Class<?> entityType = entity.getClass();

    String tableName = extractTableName(entityType);
    Field[] fields = extractSortedFieldsStream(entityType)
            .toArray(Field[]::new);

    var fieldNames = new StringBuilder();
    var questionMarksPlaceHolder = new StringBuilder();

    for (int i = 0; i < fields.length; i++) {
      var field = fields[i];
      var fieldName = extractFieldName(field);
      fieldNames.append(fieldName);
      questionMarksPlaceHolder.append("?");

      if (i + 1 != fields.length) {
        fieldNames.append(",");
        questionMarksPlaceHolder.append(",");
      }

    }

    return INSERT_QUERY.formatted(tableName, fieldNames, questionMarksPlaceHolder);
  }

  public static <T> String createDeleteQuery(T entity) {
    String tableName = extractTableName(entity.getClass());
    return DELETE_QUERY.formatted(tableName);
  }
}
