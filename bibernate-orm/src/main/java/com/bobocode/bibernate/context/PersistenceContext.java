package com.bobocode.bibernate.context;

import com.bobocode.bibernate.action.Action;
import com.bobocode.bibernate.util.EntityKey;
import com.bobocode.bibernate.util.EntityUtils;

import java.util.*;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersistenceContext {

  private final Map<EntityKey<?>, Object> entitiesMap = new HashMap<>();
  private final Map<EntityKey<?>, Object[]> entitiesSnapshots = new HashMap<>();
  private final List<Action> actionQueue = new ArrayList<>();

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

  public void addAction(Action action) {
    actionQueue.add(action);
  }

  public <T> void saveDirtyEntity(EntityKey<T> entityKey, T t) {
    Object[] sortedFieldValues = EntityUtils.extractSortedFieldValues(entityKey.getType(), t);
    entitiesSnapshots.put(entityKey, sortedFieldValues);
  }

  public void performAction() {
    for (Action action : actionQueue) {
      action.perform();
    }
  }
}
