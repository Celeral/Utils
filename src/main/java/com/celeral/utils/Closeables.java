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

import java.util.Arrays;

/**
 * Helper class to ensure that when more than one AutoClosable are closed serially, exception thrown
 * by any of the close calls do not hinder closing of the subsequent Autocloseables. The instance of
 * this class, also helps to track various resources allocated within the try-with-resources block
 * and to free then if the try with resource block is exited unexpectedly because of an error. It
 * also supports an intended no-release of the resource for the resources which need to live beyond
 * the scope of the try-with-resources block. When such a functionality is needed, one needs to
 * invoke {@link #protect()} to protect all the tracked resources from being freed. If the protected
 * resources need to be freed, one needs to call {@link #expose()} to expose all the tracked
 * resources before invoking {@link #close()}. By default, all the resources are exposed i.e. they
 * are in unprotected state.
 */
public class Closeables extends Consumables<AutoCloseable> {
  /**
   * Creates closeables instance without any message associated with it.
   *
   * @see #Closeables(String, Object...)
   */
  public Closeables() {
    this(null, (Object[]) null);
  }

  /**
   * Creates closeable instance without any message associated with it and initializes the tracked
   * resource list by adding the passed closeable.
   *
   * @param closeable the first resource to be tracked
   * @see #Closeables(String, Object...)
   */
  public Closeables(AutoCloseable closeable) {
    this(null, (Object[]) null);
    super.add(closeable);
  }

  /**
   * Initializes the closeable with the given message pattern and the arguments for the message
   * pattern. The message pattern and the argument are used to create a message which will be used
   * to create a runtime exception under which all the exceptions thrown while closing the tracked
   * resources will be listed as the suppressed exceptions.
   *
   * @param messagePattern the message pattern for annotating the runtime exception
   * @param args the positional argument to replace the placeholders in the message pattern
   */
  public Closeables(String messagePattern, Object... args) {
    super(AutoCloseable::close, messagePattern, args);
  }

  /**
   * Adds closeable for tracking purpose.
   *
   * @param closeable consumable to be tracked
   */
  @Override
  public void add(AutoCloseable closeable) {
    super.add(closeable);
  }

  /**
   * Attempt to close all the resources in the order provided. This method returns normally if all
   * the closeables passed could be closed without any of the {@link AutoCloseable#close()} throwing
   * exception or error.
   *
   * <p>While closing any particular resource, if an {@link Exception} is thrown, a new wrapping
   * exception of type {@link RuntimeException} is created with the passed message and the former
   * exception is added to it as suppressed exception. The closing of the subsequent resources is
   * attempted in a similar fashion, except in case of exceptions, the previously created runtime
   * exception instance is used to add these exceptions as suppressed ones. However if an {@link
   * Error} is thrown while attempting to close the resource, the error is immediately rethrown but
   * not before adding the runtime exception as suppressed exception to it. If no error is
   * encountered, the runtime exception is thrown.
   *
   * @param message message to annotate the runtime exception
   * @param closeables closeables which need to be closed
   * @see #close(Iterable, String, Object...)
   */
  public static void close(String message, AutoCloseable... closeables) {
    Consumables.consume(AutoCloseable::close, Arrays.asList(closeables), message);
  }

  /**
   * Attempt to close all the resources in the order provided. This method returns normally if all
   * the closeables passed could be closed without any of the {@link AutoCloseable#close()} throwing
   * exception or error.
   *
   * <p>While closing any particular resource, if an {@link Exception} is thrown, a new wrapping
   * exception of type {@link RuntimeException} is created with the message created using
   * substituting args in the passed messagePattern. The former exception is added to this newly
   * created exception as suppressed exception. The closing of the subsequent resources is attempted
   * in a similar fashion, except in case of exceptions, the previously created runtime exception
   * instance is used to add these exceptions as suppressed ones. However if an {@link Error} is
   * thrown while attempting to close the resource, the error is immediately rethrown but not before
   * adding the runtime exception as suppressed exception to it. If no error is encountered, the
   * runtime exception is thrown.
   *
   * @param closeables closeables which need to be closed
   * @param messagePattern template to create message from for runtime exception
   * @param args positional arguments to substitute in the messagePattern
   * @see #close(String, AutoCloseable...)
   */
  public static void close(
      Iterable<AutoCloseable> closeables, String messagePattern, Object... args) {
    Consumables.consume(AutoCloseable::close, closeables, messagePattern, args);
  }
}
