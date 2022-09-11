package com.bobocode.bibernate.util;

import static java.util.function.Predicate.not;

import com.bobocode.bibernate.annotation.Column;
import com.bobocode.bibernate.annotation.Id;
import com.bobocode.bibernate.annotation.JoinColumn;
import com.bobocode.bibernate.annotation.ManyToOne;
import com.bobocode.bibernate.annotation.OneToMany;
import com.bobocode.bibernate.annotation.Table;
import com.bobocode.bibernate.exception.NoSuchFieldException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityUtils {

  public static Field extractIdField(Class<?> entityType) {
    return Arrays.stream(entityType.getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(Id.class))
        .findFirst()
        .orElseThrow(
            () ->
                new NoSuchFieldException(
                    "%s entity has no field annotated with @Id".formatted(entityType)));
  }

  @SneakyThrows
  public static Object extractId(Object entity) {
    var idField = extractIdField(entity.getClass());
    idField.setAccessible(true);
    return idField.get(entity);
  }

  public static String extractTableName(Class<?> entityType) {
    return Optional.ofNullable(entityType.getAnnotation(Table.class))
        .map(Table::name)
        .orElse(entityType.getSimpleName().toLowerCase());
  }

  public static String extractTableAlias(Class<?> type) {
    String tableName = extractTableName(type);
    //  TODO: add check for table length, make alias more collision prone
    return tableName.substring(0, 3);
  }

  public static String extractFieldName(Field field) {
    if (field.isAnnotationPresent(Column.class)) {
      return field.getAnnotation(Column.class).name();
    } else if (field.isAnnotationPresent(JoinColumn.class)) {
      return field.getAnnotation(JoinColumn.class).name();
    } else {
      return field.getName();
    }
  }

  public static Class<?> extractCollectionType(Field field) {
    var parameterizedType = (ParameterizedType) field.getGenericType();
    var typeArguments = parameterizedType.getActualTypeArguments();
    var actualTypeArgument = typeArguments[0];
    return (Class<?>) actualTypeArgument;
  }

  public static Field findJoinColumn(Class<?> owningEntityType, Class<?> referencedEntityType) {
    return Arrays.stream(owningEntityType.getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(JoinColumn.class))
        .filter(f -> f.getType().equals(referencedEntityType))
        .findFirst()
        .orElseThrow();
  }

  public static Stream<Field> findAllSimpleFields(Class<?> entityType) {
    return Arrays.stream(entityType.getDeclaredFields())
        .filter(
            not(
                f ->
                    f.isAnnotationPresent(ManyToOne.class)
                        || f.isAnnotationPresent(OneToMany.class)));
  }

  public static Field findFieldAnnotatedWith(
      Class<?> type, Class<? extends Annotation> annotation) {
    return Arrays.stream(type.getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(annotation))
        .findFirst()
        .orElseThrow(
            () ->
                new NoSuchFieldException(
                    "No field annotated with %s found for type %s"
                        .formatted(annotation.getTypeName(), type.getTypeName())));
  }

  public static Field findOneToManyField(Class<?> type) {
    return findFieldAnnotatedWith(type, OneToMany.class);
  }

  public static Field findManyToOneField(Class<?> type) {
    return findFieldAnnotatedWith(type, ManyToOne.class);
  }

  @SneakyThrows
  public static <T> T setOneToManyField(Class<T> type, Object object, List<?> value) {
    var oneToManyField = findOneToManyField(type);
    oneToManyField.setAccessible(true);
    oneToManyField.set(object, value);
    return type.cast(object);
  }

  @SneakyThrows
  public static void setManyToOneField(Class<?> type, Object object, Object value) {
    var manyToOneField = findManyToOneField(type);
    manyToOneField.setAccessible(true);
    manyToOneField.set(object, value);
  }

  public static <T> Object[] extractSortedFieldValues(Class<T> type, T t) {
    return extractSortedFieldsStream(type)
            .map(field -> {
              try {
                field.setAccessible(true);
                return field.get(t);
              } catch (IllegalAccessException e) {
                throw new RuntimeException("Can't extract value form field", e);
              }
            })
            .toArray();
  }

  public static <T> Stream<Field> extractSortedFieldsStream(Class<T> type) {
    return findAllSimpleFields(type)
            .filter(field -> !field.isAnnotationPresent(Id.class))
            .sorted(Comparator.comparing(Field::getName));
  }
}
