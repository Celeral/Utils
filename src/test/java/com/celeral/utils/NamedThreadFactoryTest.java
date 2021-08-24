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

import static org.junit.Assert.*;

import org.junit.Test;

public class NamedThreadFactoryTest {

  @Test
  public void testThreadName() {
    ThreadGroup group = new ThreadGroup("Celeral");

    NamedThreadFactory factory =
        new NamedThreadFactory(
            group,
            String.format(
                "{CI}-{%s}-{%s}",
                NamedThreadFactory.POOL_SEQUENCE, NamedThreadFactory.THREAD_SEQUENCE));
    final Runnable runnable =
        new Runnable() {
          @Override
          public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
          }
        };

    Thread t1 = factory.newThread(runnable);
    assertEquals("First Thread Name", "{CI}-1-1", t1.getName());
    assertEquals("First Thread Daemonity", group.isDaemon(), t1.isDaemon());

    group.setDaemon(!group.isDaemon());

    Thread t2 = factory.newThread(runnable);
    assertEquals("Second Thread Name", "{CI}-1-2", t2.getName());
    assertEquals("Second Thread Daemonity", group.isDaemon(), t2.isDaemon());

    assertNotEquals("Daemonity for 2 threads", t1.isDaemon(), t2.isDaemon());
  }
}
