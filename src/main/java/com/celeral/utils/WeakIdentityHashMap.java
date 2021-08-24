/*
 * Copyright © 2021 Celeral.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.celeral.utils;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * An implementation of Map interface which provides the combination of IdentityHashMap and
 * WeakHashMap. Such a map is useful to be used as a cache which gets auto cleaned as and when the
 * keys have no more strong references to them anywhere in the system and hence garbage collected by
 * the JVM.
 *
 * @param <K> type of the key
 * @param <V> type of the value
 * @since 1.2.0
 */
public class WeakIdentityHashMap<K, V> implements Map<K, V> {
  private final ReferenceQueue<K> queue;
  private final HashMap<IdentityWeakReference, V> map;
  private final Consumer<V> removalListener;

  /** Wrapper to have a weak reference to the key object. */
  private final class IdentityWeakReference extends WeakReference<K> {
    int hash;

    IdentityWeakReference(K key) {
      super(key, queue);
      hash = System.identityHashCode(key);
    }

    @Override
    public int hashCode() {
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }

      if (obj instanceof WeakIdentityHashMap.IdentityWeakReference) {
        @SuppressWarnings("unchecked")
        final IdentityWeakReference ref = (IdentityWeakReference) obj;
        return super.get() == ref.get();
      }

      return false;
    }
  }

  /** Constructs the map with initial capacity 2. */
  public WeakIdentityHashMap() {
    this(2);
  }

  /**
   * Constructs the map with given initial capacity.
   *
   * @param capacity initial capacity of the map
   */
  public WeakIdentityHashMap(int capacity) {
    this(capacity, v -> {});
  }

  public WeakIdentityHashMap(int capacity, Consumer<V> removalListener) {
    this.queue = new ReferenceQueue<>();
    this.map = new HashMap<>(capacity);
    this.removalListener = removalListener;
  }

  private void reap() {
    Reference<? extends K> poll;
    synchronized (map) {
      while ((poll = queue.poll()) != null) {
        //noinspection SuspiciousMethodCalls
        V v = map.remove(poll);
        removalListener.accept(v);
      }
    }
  }

  @Override
  public int size() {
    reap();
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    reap();
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    reap();
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    reap();
    return map.containsValue(value);
  }

  @Override
  public V get(Object key) {
    reap();
    @SuppressWarnings("unchecked")
    final K k = (K) key;
    return map.get(new IdentityWeakReference(k));
  }

  @Override
  public V put(K key, V value) {
    reap();
    return map.put(new IdentityWeakReference(key), value);
  }

  @Override
  public V remove(Object key) {
    reap();
    @SuppressWarnings("unchecked")
    final K k = (K) key;
    return map.remove(new IdentityWeakReference(k));
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    reap();
    for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
      map.put(new IdentityWeakReference(entry.getKey()), entry.getValue());
    }
  }

  @Override
  public void clear() {
    map.clear();
    reap();
  }

  @Override
  public Set<K> keySet() {
    reap();

    IdentityHashMap<K, Boolean> retval = new IdentityHashMap<>();
    for (IdentityWeakReference ref : map.keySet()) {
      retval.put(ref.get(), Boolean.TRUE);
    }

    return retval.keySet();
  }

  @Override
  public Collection<V> values() {
    reap();
    return map.values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    reap();

    IdentityHashMap<K, V> imap = new IdentityHashMap<>();
    for (Entry<IdentityWeakReference, V> entry : map.entrySet()) {
      imap.put(entry.getKey().get(), entry.getValue());
    }

    return imap.entrySet();
  }
}
