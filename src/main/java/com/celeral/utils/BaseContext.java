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

public class BaseContext implements Context, Serializable {
  public final Attribute.AttributeMap attributes;
  public final Context parentContext;

  public BaseContext(Attribute.AttributeMap attributes, Context parentContext) {
    this.attributes =
        attributes == null ? new Attribute.AttributeMap.DefaultAttributeMap() : attributes;
    this.parentContext = parentContext;
  }

  @Override
  public Attribute.AttributeMap getAttributes() {
    return attributes;
  }

  @Override
  public <T> T getValue(Attribute<T> key) {
    T attr = attributes.get(key);
    if (attr != null) {
      return attr;
    }
    return parentContext == null ? key.defaultValue : parentContext.getValue(key);
  }

  private static final long serialVersionUID = 201804100403L;
}
