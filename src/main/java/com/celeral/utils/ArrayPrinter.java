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

class ArrayPrinter {
  Object[] object;

  ArrayPrinter(Object[] array) {
    this.object = array;
  }

  @Override
  public String toString() {
    boolean copied = false;
    Object[] array = object;

    StringBuilder sb = new StringBuilder("[");
    if (array.length > 0) {
      sb.append("%s");
      if (array[0] != null && array[0].getClass().isArray()) {
        if (!copied) {
          copied = true;
          array = array.clone();
        }
        array[0] = new ArrayPrinter((Object[]) array[0]);
      }

      for (int i = 1; i < array.length; i++) {
        sb.append(", %s");
        if (array[i] != null && array[i].getClass().isArray()) {
          if (!copied) {
            copied = true;
            array = array.clone();
          }
          array[i] = new ArrayPrinter((Object[]) array[i]);
        }
      }
    }
    sb.append("]");

    return String.format(sb.toString(), array);
  }
}
