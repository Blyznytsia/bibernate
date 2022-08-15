package com.bobocode.bibernate.context;

import com.bobocode.bibernate.util.EntityKey;
import com.bobocode.bibernate.util.EntityUtils;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersistenceContext {

  private final Map<EntityKey<?>, Object> entitiesMap = new HashMap<>();
  private final Map<EntityKey<?>, Object[]> entitiesSnapshots = new HashMap<>();

  public boolean contains(@NonNull EntityKey<?> key) {
    return entitiesMap.containsKey(key);
  }

  public <T> T getEntity(@NonNull EntityKey<T> key) {
    return key.getType().cast(entitiesMap.get(key));
  }

  public void addEntity(@NonNull Object entity) {
    Object id = EntityUtils.extractId(entity);
    EntityKey<?> key = EntityKey.of(entity.getClass(), id);
    entitiesMap.put(key, entity);
  }
}
