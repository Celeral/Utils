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

import static com.celeral.utils.Throwables.wrapIfChecked;

import org.junit.Assert;
import org.junit.Test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;

public class ThrowablesTest {
  @Test
  public void testRethrow_Throwable() {
    //noinspection EmptyTryBlock
    try {
    } catch (Throwable th) {
      throw wrapIfChecked(th);
    }
  }

  @Test
  public void testRethrow_Exception() {
    //noinspection EmptyTryBlock
    try {
    } catch (Exception th) {
      throw wrapIfChecked(th);
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testRethrow_Error() {
    //noinspection EmptyTryBlock
    try {
    } catch (Error th) {
      throw wrapIfChecked(th);
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testRethrow_RuntimeException() {
    //noinspection EmptyTryBlock
    try {
    } catch (RuntimeException th) {
      throw wrapIfChecked(th);
    }
  }

  @Test
  public void testMessageFormatting() {
    final String messagePattern =
        "integer: {}, double: {}, string: {}, null: {}, object: {}, array: {}";
    final Object[] arguments =
        new Object[] {1, 2.0, "3", null, new RuntimeException(), new Object[] {1, 2.0, "3"}};
    try {
      throw Throwables.throwFormatted(RuntimeException.class, messagePattern, arguments);
    } catch (Exception ex) {
      String message =
          ParameterizedMessageFactory.INSTANCE
              .newMessage(messagePattern, arguments)
              .getFormattedMessage();
      Assert.assertEquals("formatting compatible", message, ex.getMessage());
    }
  }

  public static final Logger logger = LogManager.getLogger(ThrowablesTest.class);
}
