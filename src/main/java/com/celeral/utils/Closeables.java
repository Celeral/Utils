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

import java.util.ArrayList;

public class Closeables implements AutoCloseable
{
  private final AutoCloseable[] EMPTY_CLOSEABLES = new AutoCloseable[0];

  private final ArrayList<AutoCloseable> closeables = new ArrayList<>();
  private final String message;
  private boolean frozen;

  public Closeables(String message)
  {
    this.message = message;
  }

  public void add(AutoCloseable closeable)
  {
    closeables.add(closeable);
  }

  public void freeze()
  {
    frozen = true;
  }

  public void thaw()
  {
    frozen = false;
  }

  @SuppressWarnings(value = "ThrowFromFinallyBlock")
  public static void close(String message, AutoCloseable... closeables)
  {
    RuntimeException re = null;
    try {
      for (AutoCloseable closeable : closeables) {
        try {
          closeable.close();
        }
        catch (Exception ex) {
          if (re == null) {
            re = new RuntimeException(message);
          }
          re.addSuppressed(ex);
        }
      }
    }
    finally {
      if (re != null) {
        throw re;
      }
    }
  }

  @Override
  public void close()
  {
    if (frozen) {
      return;
    }

    try {
      Closeables.close(message, closeables.toArray(EMPTY_CLOSEABLES));
    }
    finally {
      closeables.clear();
    }
  }
}
