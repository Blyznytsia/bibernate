package com.bobocode.bibernate.util;

import static com.bobocode.bibernate.util.EntityUtils.extractFieldName;
import static com.bobocode.bibernate.util.EntityUtils.extractIdField;
import static com.bobocode.bibernate.util.EntityUtils.extractTableAlias;
import static com.bobocode.bibernate.util.EntityUtils.extractTableName;
import static com.bobocode.bibernate.util.EntityUtils.findAllSimpleFields;
import static com.bobocode.bibernate.util.EntityUtils.findManyToOneField;
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
}
