/*
 * Copyright 2019 Celeral.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WeakIdentityHashMapTest
{
  @Test
  public void testRemovalOfKeys() throws InterruptedException
  {
    String name = "Weak Identity";
    String[] parts = name.split(" ");

    WeakIdentityHashMap<String, String> wih = new WeakIdentityHashMap<>();
    wih.put(parts[0], parts[1]);
    assertEquals("Stored Value", parts[1], wih.get(parts[0]));
    parts = null;

    for (int i = 0; i < 100; i++) {
      System.gc();
      if (wih.isEmpty()) {
        break;
      }

      Thread.sleep(5);
    }

    assertEquals("Map Empty", 0, wih.size());
  }
}