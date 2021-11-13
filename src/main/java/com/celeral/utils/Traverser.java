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
 * @param <T> Type of the elements iterated.
 *
 * @author Chetan Narsude {@literal <chetan@celeral.com>}
 */
public class Traverser<T> implements Iterator<T>, Serializable {
  private final ChildEnumerator<T> childEnumerator;
  private final LeafChecker<T> leafChecker;

  static class Visitor<T> implements Iterator<T>, Serializable {
    T element;

    @Override
    public boolean hasNext() {
      return element != null;
    }

    @Override
    public T next() {
      try {
        return element;
      } finally {
        element = null;
      }
    }

    void setElement(T element) {
      this.element = element;
    }
  }

  @FunctionalInterface
  public interface LeafChecker<T> {
    boolean isLeaf(T element);
  }

  @FunctionalInterface
  public interface ChildEnumerator<T> {
    T[] getChildren(T element);
  }

  private final T[] elements;
  private final Visitor<T> visitor;
  private Iterator<T> currentIterator;
  private int currentIndex;

  public Traverser(
      T[] elements,
      @NotNull LeafChecker<T> leafChecker,
      @NotNull ChildEnumerator<T> childEnumerator) {
    this(elements, leafChecker, childEnumerator, new Visitor<>());
  }

  private Traverser(
      T[] elements,
      LeafChecker<T> leafChecker,
      ChildEnumerator<T> childEnumerator,
      Visitor<T> visitor) {
    if (elements == null) {
      @SuppressWarnings("unchecked")
      final T[] ts = (T[]) new Object[0];
      this.elements = ts;
    } else {
      this.elements = elements.clone();
    }

    this.leafChecker = leafChecker;
    this.childEnumerator = childEnumerator;
    this.visitor = visitor;
  }

  @Override
  public boolean hasNext() {
    do {
      if (currentIterator == null) {
        if (currentIndex < elements.length) {
          T element = elements[currentIndex];
          if (leafChecker.isLeaf(element)) {
            visitor.setElement(element);
            currentIterator = visitor;
            return true;
          } else {
            currentIterator =
                new Traverser<>(childEnumerator.getChildren(element), leafChecker, childEnumerator);
          }
        } else {
          return false;
        }
      } else {
        if (currentIterator.hasNext()) {
          return true;
        }

        currentIterator = null;
        currentIndex++;
      }
    } while (true);
  }

  @Override
  public T next() {
    return currentIterator.next();
  }
}
