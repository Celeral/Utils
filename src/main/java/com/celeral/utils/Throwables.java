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

import java.lang.reflect.Constructor;

import static org.slf4j.helpers.MessageFormatter.arrayFormat;

/**
 * Collection of helper methods to ensure that exceptions are
 * properly thrown. If the cause is of type Error or
 * RuntimeException then the cause it thrown as it is.
 * Otherwise the cause is wrapped in a RuntimeException and
 * the later is thrown.
 */
public class Throwables
{

  /**
   * Throws cause as it is if it is RuntimeException or Error,
   * otherwise wraps it in RuntimeException and throws it.
   *
   * @param cause throwable to be wrapped if cannot be thrown directly
   * @return the function does not return
   * @see #wrapIfChecked(java.lang.Exception)
   */
  public static RuntimeException wrapIfChecked(Throwable cause)
  {
    if (cause instanceof Error) {
      throw (Error)cause;
    }

    if (cause instanceof RuntimeException) {
      throw (RuntimeException)cause;
    }

    throw new RuntimeException(cause);
  }

  /**
   * Throws exception as it is if it is RuntimeException,
   * otherwise wraps it in RuntimeException and throws it.
   *
   * @param exception exception to be wrapped if cannot be thrown directly
   * @return the function does not return
   * @see #wrapIfChecked(java.lang.Throwable)
   */
  public static RuntimeException wrapIfChecked(Exception exception)
  {
    if (exception instanceof RuntimeException) {
      throw (RuntimeException)exception;
    }

    throw new RuntimeException(exception);
  }

  /**
   * Throws the error as it is. This method is added to statically
   * warn the developer that this call is redundant by marking the
   * call deprecated. The developer should be able to throw the
   * error as it is directly.
   *
   * @param error the error which needs to be wrapped
   * @return the function does not return
   * @deprecated Error does not need to be wrapped; Instead of {@code throw Throwables.wrapIfChecked(error);} use {@code throw error;} directly.
   */
  @Deprecated
  public static RuntimeException wrapIfChecked(Error error)
  {
    throw error;
  }

  /**
   * Throws the runtime exception as it is. This method is added to
   * statically warn the developer that this call is redundant by
   * marking the call deprecated. The developer should be able to
   * throw the unchecked exception as it is directly.
   *
   * @param exception the runtime exception which needs to be wrapped
   * @return the function does not return
   * @deprecated Unchecked exception (subclass of RuntimeException) does not need to be wrapped; Instead of {@code Throwables.rethrow(runtime_exception);} use {@code throw runtime_exception;} directly.
   */
  @Deprecated
  public static RuntimeException wrapIfChecked(RuntimeException exception)
  {
    throw exception;
  }

  /**
   * Throws the requested throwable after initializing it with the formatted message.
   * This function uses reflection on the passed subclass of Throwable or Throwable
   * class itself if it was passed to identify a constructor which takes a single
   * argument of type String. If the constructor gets identified, the function invokes
   * it with the formatted message string. The message is formatted using
   * <a href="https://www.slf4j.org/api/org/slf4j/helpers/MessageFormatter.html">
   * MessageFormatter</a> provided by slf4j by directly passing the messagePattern
   * and all the arguments following it to MessageFormatter. The instance of throwable
   * class thus obtained is thrown.
   *
   * @param <T>            Specific subtype of Throwable that's wished to be thrown
   * @param clazz          Class object for type T
   * @param messagePattern formatting pattern for the message
   * @param args           arguments used to fill the placeholders in the formatting pattern
   * @return this method does not return
   * @throws T                if the method is successful
   * @throws RuntimeException if clazz could not be instantiated for any reason
   * @see #throwFormatted(java.lang.Throwable, java.lang.Class, java.lang.String, java.lang.Object...)
   * @see <a href="https://www.slf4j.org/api/org/slf4j/helpers/MessageFormatter.html#arrayFormat(java.lang.String,%20java.lang.Object[])">org.slf4j.helpers.MessageFormatter.arrayFormat</a>
   */
  @SuppressWarnings("UseSpecificCatch")
  public static <T extends Throwable> T throwFormatted(Class<T> clazz, String messagePattern, Object... args) throws T
  {
    throw Throwables.throwFormatted(null, clazz, messagePattern, args);
  }

  /**
   * Throws the requested throwable after initializing it with the formatted message and
   * the passed cause.This function uses reflection on the passed subclass of Throwable
   * or Throwable class itself if it was passed to identify a constructor which takes two
   * arguments - the first one of type String, and the second one of type Throwable.
   * If the constructor gets identified, the function invokes it with the formatted
   * message string and passed cause. The message is formatted using
   * <a href="https://www.slf4j.org/api/org/slf4j/helpers/MessageFormatter.html">
   * MessageFormatter</a> provided by slf4j by directly passing the messagePattern
   * and all the arguments following it to MessageFormatter. The instance of throwable
   * class thus obtained is thrown.
   *
   * @param <T>            Specific subtype of Throwable that's wished to be thrown
   * @param cause          root cause throwable for invoking this method
   * @param clazz          Class object for type T
   * @param messagePattern formatting pattern for the message
   * @param args           arguments used to fill the placeholders in the formatting pattern
   * @return this method does not return
   * @throws T                if the method is successful
   * @throws RuntimeException if clazz could not be instantiated for any reason
   * @see #throwFormatted(java.lang.Class, java.lang.String, java.lang.Object...)
   * @see <a href="https://www.slf4j.org/api/org/slf4j/helpers/MessageFormatter.html#arrayFormat(java.lang.String,%20java.lang.Object[])">org.slf4j.helpers.MessageFormatter.arrayFormat</a>
   */
  @SuppressWarnings({"UseSpecificCatch", "InfiniteRecursion"})
  public static <T extends Throwable> T throwFormatted(final Throwable cause, final Class<T> clazz, String messagePattern, Object... args) throws T
  {
    ThrowableFactory<T> factory = new ThrowableFactory<T>()
    {
      @Override
      public T createThrowable(String message) throws Exception
      {
        Constructor<T> constructor = clazz.getConstructor(String.class, Throwable.class);
        return constructor.newInstance(message, cause);
      }
    };

    throw Throwables.throwFormatted(factory, messagePattern, args);
  }

  /**
   * Throws the throwable created by the factory upon invoking it with the formatted message
   * and the null cause. The function invokes the createThrowable method on the factory with
   * the formatted message string and null cause. The message is formatted using
   * <a href="https://www.slf4j.org/api/org/slf4j/helpers/MessageFormatter.html">
   * MessageFormatter</a> provided by slf4j by directly passing the messagePattern
   * and all the arguments following it to MessageFormatter. The instance of throwable
   * class thus obtained is thrown.
   *
   * @param <T>            Specific subtype of Throwable that's wished to be thrown
   * @param factory        Factory for creating an instance of the requested Throwable
   * @param messagePattern formatting pattern for the message
   * @param args           arguments used to fill the placeholders in the formatting pattern
   * @return this method does not return
   * @throws T                if the method is successful
   * @throws RuntimeException if clazz could not be instantiated for any reason
   * @see <a href="https://www.slf4j.org/api/org/slf4j/helpers/MessageFormatter.html#arrayFormat(java.lang.String,%20java.lang.Object[])">org.slf4j.helpers.MessageFormatter.arrayFormat</a>
   */
  public static <T extends Throwable> T throwFormatted(ThrowableFactory<T> factory, String messagePattern, Object... args) throws T
  {
    String message = arrayFormat(messagePattern, args).getMessage();

    T instance;
    try {
      instance = factory.createThrowable(message);
    }
    catch (Exception ex) {
      throw Throwables.throwFormatted(ex, RuntimeException.class,
                                      "Couldn't throw exception with message {}!",
                                      message);
    }

    throw instance;
  }

  /**
   * An interface to model the factory for creation of the Throwable objects which do not follow
   * the standard constructor or require additional processing besides invocation of the standard
   * constructors.
   *
   * @param <T> common supertype for all different types of throwables created by this factory
   */
  public interface ThrowableFactory<T extends Throwable>
  {
    /**
     * Create a throwable of type T using the message and the root cause.
     *
     * @param message message for the throwable
     * @return the requested throwable
     * @throws Exception for any reason if the throwable cannot be created, the exception is thrown.
     */
    T createThrowable(String message) throws Exception;
  }
}
