/*
 * Copyright 2020 Celeral.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.celeral.utils.function;

import java.util.function.Function;

import static com.celeral.utils.Throwables.throwSneaky;

/**
 *
 * @author Chetan Narsude {@literal <chetan@celeral.com>}
 */
@FunctionalInterface
public interface AutoFunction<T, R>
{

  /**
   * Applies this function to the given argument.
   *
   * @param t the function argument
   *
   * @return the function result
   *
   * @throws java.lang.Exception
   */
  R apply(T t) throws Exception;

  default Function<T, R> toFunction()
  {
    return t -> {
      try {
        return apply(t);
      }
      catch (Exception ex) {
        throw throwSneaky(ex);
      }
    };
  }

}
