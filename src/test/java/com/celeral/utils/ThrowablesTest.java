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

import static com.celeral.utils.Throwables.wrapIfChecked;

public class ThrowablesTest
{
  @Test
  public void testRethrow_Throwable()
  {
    //noinspection EmptyTryBlock
    try {
    }
    catch (Throwable th) {
      throw wrapIfChecked(th);
    }
  }

  @Test
  public void testRethrow_Exception()
  {
    //noinspection EmptyTryBlock
    try {
    }
    catch (Exception th) {
      throw wrapIfChecked(th);
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testRethrow_Error()
  {
    //noinspection EmptyTryBlock
    try {
    }
    catch (Error th) {
      throw wrapIfChecked(th);
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testRethrow_RuntimeException()
  {
    //noinspection EmptyTryBlock
    try {
    }
    catch (RuntimeException th) {
      throw wrapIfChecked(th);
    }
  }

}
