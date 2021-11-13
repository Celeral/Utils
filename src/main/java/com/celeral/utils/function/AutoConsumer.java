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
package com.celeral.utils.function;

import static com.celeral.utils.Throwables.throwSneaky;

import java.util.function.Consumer;

/**
 * Represents an operation that accepts a single input argument and returns no result.
 *
 * @param <T> the type of the consumable
 * @author Chetan Narsude {@literal <chetan@celeral.com>}
 */
@FunctionalInterface
public interface AutoConsumer<T> {
  /**
   * Performs the consume operation on the given argument.
   *
   * @param t the input argument
   * @throws Exception if this consumable cannot be consumed
   */
  void accept(T t) throws Exception;

  default Consumer<T> toConsumer() {
    return t -> {
      try {
        accept(t);
      } catch (Exception ex) {
        throw throwSneaky(ex);
      }
    };
  }
}
