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

import java.util.ArrayDeque;
import java.util.Arrays;

import org.apache.logging.log4j.message.ParameterizedMessageFactory;

import com.celeral.utils.function.AutoConsumer;

/**
 * Helper class to ensure that when more than one consumables are consumed serially, exception
 * thrown by any of the consume calls do not hinder the consumption of the subsequent consumables.
 * The instance of this class, also helps to track various consumables allocated within the
 * try-with-resources block and to free then if the try with resource block is exited unexpectedly
 * because of an error. It also prevents an unintended consumption of the consumable which need to
 * live beyond the scope of the try-with-resources block. When such a functionality is needed, one
 * needs to invoke {@link #protect()} to protect all the tracked consumables from being consumed. If
 * the protected consumables need to be consumed, one needs to call {@link #expose()} to expose all
 * the tracked consumables before invoking {@link #close()}. By default, all the consumables are
 * exposed i.e. they are in unprotected state.
 *
 * @param <T> the type of the consumable
 */
public class Consumables<T> implements AutoCloseable {
  private final AutoConsumer<T> consumer;
  private final ArrayDeque<T> consumables;
  private final String messagePattern;
  private final Object[] args;
  private boolean isProtected;

  /**
   * Initializes the closeable with the given message pattern and the arguments for the message
   * pattern. The message pattern and the argument are used to create a message which will be used
   * to create a runtime exception under which all the exceptions thrown while closing the tracked
   * resources will be listed as the suppressed exceptions.
   *
   * @param consumer common consumer to consume all the consumables
   * @param messagePattern the message pattern for annotating the runtime exception
   * @param args the positional argument to replace the placeholders in the message pattern
   */
  public Consumables(AutoConsumer<T> consumer, String messagePattern, Object... args) {
    this.consumer = consumer;
    this.consumables = new ArrayDeque<>(1);
    this.messagePattern = messagePattern;
    this.args = args;
  }

  /**
   * Adds consumable for tracking purpose.
   *
   * @param consumable consumable to be tracked
   */
  public void add(T consumable) {
    consumables.addFirst(consumable);
  }

  /**
   * Protects the tracked resources from getting freed if {@link #close()} is called. The tracked
   * resources stay in the protected state until {@link #expose()} is called.
   */
  public void protect() {
    isProtected = true;
  }

  /**
   * Unprotects the tracked resources if they were in protected state due to call to {@link
   * #protect()}.
   */
  public void expose() {
    isProtected = false;
  }

  /**
   * Attempt to consume all the consumables in the order provided. This method returns normally if
   * all the consumables passed could be consumed without any of the {@link
   * AutoConsumer#accept(Object)} throwing exception or error.
   *
   * <p>While consuming any particular consumable, if an {@link Exception} is thrown, a new wrapping
   * exception of type {@link RuntimeException} is created with the message created using
   * substituting args in the passed messagePattern. The former exception is added to this newly
   * created exception as suppressed exception. The consumption of the subsequent consumable is
   * attempted in a similar fashion, except in case of exceptions, the previously created runtime
   * exception instance is used to add these exceptions as suppressed ones. However if an {@link
   * Error} is thrown while attempting to consume any of the consumables, the error is immediately
   * rethrown but not before adding the runtime exception as suppressed exception to it. If no error
   * is encountered, the runtime exception is thrown.
   *
   * @param <T> the type of the consumable
   * @param message message to annotate the runtime exception
   * @param consumer common consumer to consume all the consumables
   * @param consumables consumables which need to be consumed
   * @see #consume(com.celeral.utils.function.AutoConsumer, java.lang.Iterable, java.lang.String,
   *     java.lang.Object...)
   */
  @SafeVarargs
  public static <T> void consume(String message, AutoConsumer<T> consumer, T... consumables) {
    Consumables.consume(consumer, Arrays.asList(consumables), message);
  }

  /**
   * Consumes all the tracked consumables only if they are not protected. Once the consumption of
   * the consumables is attempted using {@link #consume(java.lang.String,
   * com.celeral.utils.function.AutoConsumer, java.lang.Object...) } irrespective of outcome, all
   * the consumables are removed from list of tracked consumables.
   *
   * @see #protect()
   * @see #expose()
   */
  @Override
  public void close() {
    if (isProtected) {
      return;
    }

    try {
      Consumables.consume(consumer, consumables, messagePattern, args);
    } finally {
      consumables.clear();
    }
  }

  /**
   * Attempt to consume all the consumables in the order provided. This method returns normally if
   * all the consumables passed could be consumed without any of the {@link
   * AutoConsumer#accept(Object)} throwing exception or error.
   *
   * <p>While consuming any particular consumable, if an {@link Exception} is thrown, a new wrapping
   * exception of type {@link RuntimeException} is created with the message created using
   * substituting args in the passed messagePattern. The former exception is added to this newly
   * created exception as suppressed exception. The consumption of the subsequent consumable is
   * attempted in a similar fashion, except in case of exceptions, the previously created runtime
   * exception instance is used to add these exceptions as suppressed ones. However if an {@link
   * Error} is thrown while attempting to consume any of the consumables, the error is immediately
   * rethrown but not before adding the runtime exception as suppressed exception to it. If no error
   * is encountered, the runtime exception is thrown.
   *
   * @param <T> the type of the consumable
   * @param consumer common consumer to consume all the consumables
   * @param consumables consumables which need to be consumed
   * @param messagePattern template to create message from for runtime exception
   * @param args positional arguments to substitute in the messagePattern
   */
  public static <T> void consume(
      AutoConsumer<T> consumer, Iterable<T> consumables, String messagePattern, Object... args) {
    RuntimeException re = null;

    try {
      for (T consumable : consumables) {
        try {
          consumer.accept(consumable);
        } catch (Exception ex) {
          if (re == null) {
            re =
                messagePattern == null
                    ? new RuntimeException()
                    : new RuntimeException(
                        ParameterizedMessageFactory.INSTANCE
                            .newMessage(messagePattern, args)
                            .getFormattedMessage());
          }

          re.addSuppressed(ex);
        }
      }
    } catch (Error er) {
      if (re != null) {
        er.addSuppressed(re);
      }

      throw er;
    }

    if (re != null) {
      throw re;
    }
  }
}
