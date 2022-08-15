package com.bobocode.bibernate.util;

import lombok.Value;

@Value(staticConstructor = "of")
public class EntityKey<T> {

  Class<T> type;
  Object id;
}
