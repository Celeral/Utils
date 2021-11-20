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

import java.io.Serializable;
import java.util.Iterator;

import javax.validation.constraints.NotNull;

/**
 * Performs a depth first iteration of a hierarchical structure.
 *
 * @param <T> Type of the elements iterated.
 * @author Chetan Narsude {@literal <chetan@celeral.com>}
 */
public class Traverser<T> implements Iterator<T>, Serializable {
  @FunctionalInterface
  public interface ChildrenEnumerator<T> {
    T[] getChildren(T element);
  }

  private static class ArrayIterator<T> implements Iterator<T> {
    private final T[] array;
    private int index;
    private ArrayIterator<T> next;

    public ArrayIterator(T[] array) {
      this.array = array;
    }

    @Override
    public boolean hasNext() {
      return index < array.length;
    }

    @Override
    public T next() {
      return array[index++];
    }
  }

  private final ChildrenEnumerator<T> childrenEnumerator;
  private ArrayIterator<T> currentIterator;
  private T previous;
  private boolean isPrevious;

  public Traverser(
      @NotNull T[] elements, @NotNull Traverser.ChildrenEnumerator<T> childrenEnumerator) {
    this(new ArrayIterator<>(elements.clone()), childrenEnumerator);
  }

  private Traverser(ArrayIterator<T> iterator, ChildrenEnumerator<T> childrenEnumerator) {
    currentIterator = iterator;
    this.childrenEnumerator = childrenEnumerator;
  }

  @Override
  public boolean hasNext() {
    if (isPrevious) {
      final T[] children = childrenEnumerator.getChildren(previous);
      if (children != null && children.length != 0) {
        final ArrayIterator<T> arrayIterator = new ArrayIterator<>(children);
        arrayIterator.next = currentIterator;
        currentIterator = arrayIterator;
      }

      isPrevious = false;
    }

    do {
      if (currentIterator.hasNext()) {
        return true;
      }

      currentIterator = currentIterator.next;
    } while (currentIterator != null);

    return false;
  }

  @Override
  public T next() {
    final T next = currentIterator.next();
    isPrevious = true;
    previous = next;
    return next;
  }
}
