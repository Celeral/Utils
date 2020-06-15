/*
 * Copyright 2020 Celeral.
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import com.celeral.utils.function.AutoConsumer;
import com.celeral.utils.function.AutoFunction;
import com.celeral.utils.function.AutoSupplier;

import static com.celeral.utils.Throwables.throwSneaky;

/**
 *
 * @author Chetan Narsude {@literal <chetan@celeral.com>}
 */
public interface Async
{
  /**
   * Returns a new CompletionStage with the same result or exception as the CompletionStage
   * returned by the function. The return value is different if the releaser throws an
   * exception signifying that the resource could not be released. If the supplier itself
   * fails then neither function nor the releaser is invoked and the returned stage fails
   * with the exception thrown by the supplier.
   *
   * The resource is supplied by the supplier argument before the function is scheduled to
   * be invoked asynchronously and is released immediately after the function has finished
   * execution and the result or exception is ready. Ideally the releaser is not supposed to
   * throw an exception, but if it does, the returned completion stage fails as follows:
   * If the completion stage returned by the function is successful, then the stage fails
   * with the exception thrown by the releaser. If the completion stage returned by the
   * function itself fails, then the exception thrown by the releaser is added to the list
   * of suppressed exceptions of the exception which caused the failure of completion stage
   * returned by the function. This behavior akin to try-with-resource feature of Java but
   * for the resource dependent functions which are asynchronous in nature.
   *
   * @param <T>      type of the resource
   * @param <R>      type of the return value for the function
   * @param supplier supplies the resource
   * @param function computes the output using the resource
   * @param releaser releases the resource
   *
   * @return Completion stage which completes after releaser has finished execution
   */
  @SuppressWarnings("UseSpecificCatch")
  public static <T, R> CompletableFuture<R> applyWith(AutoSupplier<T> supplier,
                                                      AutoFunction<T, CompletableFuture<R>> function,
                                                      AutoConsumer<T> releaser)
  {
    try {
      T resource = supplier.get();
      try {
        return function.apply(resource).whenComplete((r, th) -> {
          release(resource, releaser, th);
        });
      }
      catch (Throwable th) {
        try {
          releaser.accept(resource);
        }
        catch (Throwable rth) {
          th.addSuppressed(rth);
        }

        throw th;
      }
    }
    catch (Throwable th) {
      CompletableFuture<R> future = new CompletableFuture<>();
      future.completeExceptionally(th instanceof CompletionException ? th : new CompletionException(th));
      return future;
      // the following is only available in java 9 and is equivalent of above 3 lines
      //return CompletableFuture.failedFuture(th instanceof CompletionException ? th : new CompletionException(th));
    }
  }

  /**
   * Returns a new CompletionStage with the same result or exception as the CompletionStage
   * returned by the function. The return value is different if the releaser throws an
   * exception signifying that the resource could not be released. If the supplier itself
   * fails then neither function nor the releaser is invoked and the returned stage fails
   * with the exception thrown by the supplier. The
   *
   * This variant of {@link #applyWith(com.celeral.utils.function.AutoSupplier, com.celeral.utils.function.AutoFunction, com.celeral.utils.function.AutoConsumer)}
   * is meant to be used with the resource supplier which are blocking in nature and the
   * caller does not wish to block the calling thread while the resource allocation operation
   * concludes.
   *
   * The resource is supplied by the supplier argument before the function is scheduled to
   * be invoked asynchronously and is released immediately after the function has finished
   * execution and the result or exception is ready. Ideally the releaser is not supposed to
   * throw an exception, but if it does, the returned completion stage fails as follows:
   * If the completion stage returned by the function is successful, then the stage fails
   * with the exception thrown by the releaser. If the completion stage returned by the
   * function itself fails, then the exception thrown by the releaser is added to the list
   * of suppressed exceptions of the exception which caused the failure of completion stage
   * returned by the function. This behavior akin to try-with-resource feature of Java but
   * for the resource dependent functions which are asynchronous in nature.
   *
   * @param <T>      type of the resource
   * @param <R>      type of the return value for the function
   * @param supplier supplies the resource
   * @param function computes the output using the resource
   * @param releaser releases the resource
   *
   * @return the completion stage which completes after releaser has finished execution
   */
  @SuppressWarnings("UseSpecificCatch")
  public static <T, R> CompletableFuture<R> applyWithAsync(AutoSupplier<T> supplier,
                                                           AutoFunction<T, CompletableFuture<R>> function,
                                                           AutoConsumer<T> releaser)
  {
    CompletableFuture<T> resourceFuture = CompletableFuture.supplyAsync(supplier.toSupplier());
    return resourceFuture.thenCompose(function.toFunction()).whenComplete((r, ex) -> {
      if (!resourceFuture.isCompletedExceptionally()) {
        release(resourceFuture.join(), releaser, ex);
      }
    });
  }

  /**
   * Returns a new CompletionStage with the same result or exception as the CompletionStage
   * returned by the function.The return value is different if the releaser throws an
   * exception signifying that the resource could not be released. If the supplier itself
   * fails then neither function nor the releaser is invoked and the returned stage fails
   * with the exception thrown by the supplier.
   *
   * This variant of {@link #applyWith(com.celeral.utils.function.AutoSupplier, com.celeral.utils.function.AutoFunction, com.celeral.utils.function.AutoConsumer)}
   * is meant to be used with the resource supplier which are blocking in nature and the
   * caller does not wish to block the calling thread while the resource allocation operation
   * concludes.
   *
   * The resource is supplied by the supplier argument before the function is scheduled to
   * be invoked asynchronously and is released immediately after the function has finished
   * execution and the result or exception is ready. Ideally the releaser is not supposed to
   * throw an exception, but if it does, the returned completion stage fails as follows:
   * If the completion stage returned by the function is successful, then the stage fails
   * with the exception thrown by the releaser. If the completion stage returned by the
   * function itself fails, then the exception thrown by the releaser is added to the list
   * of suppressed exceptions of the exception which caused the failure of completion stage
   * returned by the function. This behavior akin to try-with-resource feature of Java but
   * for the resource dependent functions which are asynchronous in nature.
   *
   * @param <T>      type of the resource
   * @param <R>      type of the return value for the function
   * @param supplier supplies the resource
   * @param function computes the output using the resource
   * @param releaser releases the resource
   * @param executor the executor to use for asynchronous execution
   *
   * @return Completion stage which completes after releaser has finished execution
   */
  @SuppressWarnings("UseSpecificCatch")
  public static <T, R> CompletableFuture<R> applyWithAsync(AutoSupplier<T> supplier,
                                                           AutoFunction<T, CompletableFuture<R>> function,
                                                           AutoConsumer<T> releaser,
                                                           Executor executor)
  {
    CompletableFuture<T> resourceFuture = CompletableFuture.supplyAsync(supplier.toSupplier(), executor);
    return resourceFuture.thenComposeAsync(function.toFunction(), executor).whenCompleteAsync((r, ex) -> {
      if (!resourceFuture.isCompletedExceptionally()) {
        release(resourceFuture.join(), releaser, ex);
      }
    }, executor);
  }

  @SuppressWarnings("UseSpecificCatch")
  static <T> void release(T resource, AutoConsumer<T> releaser, Throwable th)
  {
    try {
      releaser.accept(resource);
    }
    catch (Throwable rth) {
      if (th == null) {
        throw throwSneaky(rth);
      }

      if (th instanceof CompletionException) {
        th = th.getCause();
      }

      th.addSuppressed(rth);
    }
  }

}
