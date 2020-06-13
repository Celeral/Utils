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

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 *
 */
public class AttributeMapTest
{
  @Test
  public void testGetAttributes()
  {
    assertTrue("Identity of Interface", iface.serialVersionUID != 0);
    Set<Attribute<Object>> result = com.celeral.utils.Attribute.AttributeMap.AttributeInitializer.getAttributes(iface.class);
    assertTrue("Attributes Collection", !result.isEmpty());
    for (Attribute<Object> attribute : result) {
      logger.debug("{}", attribute);
    }
  }

  enum Greeting
  {
    hello,
    howdy
  };

  interface iface
  {
    Attribute<Greeting> greeting = new Attribute<>(Greeting.hello);
    long serialVersionUID = com.celeral.utils.Attribute.AttributeMap.AttributeInitializer.initialize(iface.class);
  }

  @Test
  public void testEnumAutoCodec()
  {
    Greeting howdy = iface.greeting.codec.fromString(Greeting.howdy.name());
    assertSame("Attribute", Greeting.howdy, howdy);
  }

  private static final Logger logger = LogManager.getLogger();
}
