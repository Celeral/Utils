/*
 * Copyright 2018 Celeral.
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
package com.celeral.utils;

import com.celeral.utils.Attribute.AttributeMap;

public interface Context
{
  /**
   * Get the attributes associated with this context.
   * The returned map does not contain any attributes that may have been defined in the parent context of this context.
   *
   * @return attributes defined for the current context.
   */
  AttributeMap getAttributes();

  /**
   * Get the value of the attribute associated with the current key by recursively traversing the contexts upwards to
   * the application level. If the attribute is not found, then return the defaultValue.
   *
   * @param <T> - Type of the value stored against the attribute
   * @param key - Attribute to identify the attribute.
   *
   * @return The value for the attribute if found or the defaultValue passed in as argument.
   */
  <T> T getValue(Attribute<T> key);

  /**
   * Context type decides how the values for attributes are resolved in this context
   * with reference to the values that may be present in the parent context.
   */
  enum ContextType
  {
    /**
     * The value if present in the current context overrides
     * any value that may be present in the parent context.
     */
    OVERRIDE,
    /**
     * The value if present in the current context is merged
     * with the value that may be present in the parent context.
     */
    MERGE,
    /**
     * The value if present in the current context or the default
     * value. The value in the parent context is completely ignored.
     */
    MASK,
    /**
     * The value if present in the parent context or the default
     * value. The value in this context is completely ignored.
     */
    PARENT
  };

}
