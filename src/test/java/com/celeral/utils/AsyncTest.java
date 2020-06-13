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

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Chetan Narsude {@literal <chetan@celeral.com>}
 */
public class AsyncTest
{

  @Test
  public void testBasicApplyWith() throws Exception
  {
    CompletableFuture<Boolean> applyWith = Async.applyWith(
            () -> {
              logger.debug("supplier true");
              return true;
            },
            bool -> CompletableFuture.supplyAsync(() -> {
              logger.debug("function {}", bool);
              return bool;
            }),
            bool -> {
              logger.debug("releaser {}", bool);
            });

    Assert.assertTrue("Response", applyWith.get());
  }

  @Test
  public void testBasicApplyWithAsync() throws Exception
  {
    CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
            () -> {
              boolean bool = true;
              logger.debug("supplier {}", bool);
              return bool;
            },
            bool -> CompletableFuture.supplyAsync(() -> {
              logger.debug("function {}", bool);
              return bool;
            }),
            bool -> {
              logger.debug("releaser {}", bool);
            });

    Assert.assertTrue("Response", applyWith.get());
  }

  @Test
  public void testBasicApplyWithAsyncWithExecutor() throws Exception
  {
    ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
    try {
      CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
              () -> {
                boolean bool = true;
                logger.debug("supplier {}", bool);
                return bool;
              },
              bool -> CompletableFuture.supplyAsync(() -> {
                logger.debug("function {}", bool);
                return bool;
              }, newCachedThreadPool),
              bool -> {
                logger.debug("releaser {}", bool);
              }, newCachedThreadPool);

      Assert.assertTrue("Response", applyWith.get());
    }
    finally {
      newCachedThreadPool.shutdown();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithFailedSupplier() throws Throwable
  {
    @SuppressWarnings("ThrowFromFinallyBlock")
    CompletableFuture<Boolean> applyWith = Async.applyWith(
            () -> {
              logger.debug("supplier true");
              try {
                return true;
              }
              finally {
                throw new IOException("supplier");
              }
            },
            bool -> CompletableFuture.supplyAsync(() -> {
              logger.debug("function {}", bool);
              return bool;
            }),
            bool -> {
              logger.debug("releaser {}", bool);
            });

    try {
      applyWith.get();
    }
    catch (ExecutionException ex) {
      throw ex.getCause();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithAsyncFailedSupplier() throws Throwable
  {
    @SuppressWarnings("ThrowFromFinallyBlock")
    CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
            () -> {
              logger.debug("supplier true");
              try {
                return true;
              }
              finally {
                throw new IOException("supplier");
              }
            },
            bool -> CompletableFuture.supplyAsync(() -> {
              logger.debug("function {}", bool);
              return bool;
            }),
            bool -> {
              logger.debug("releaser {}", bool);
            });

    try {
      applyWith.get();
    }
    catch (ExecutionException ex) {
      throw ex.getCause();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithAsyncExecutorFailedSupplier() throws Throwable
  {
    ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
    try {
      @SuppressWarnings("ThrowFromFinallyBlock")
      CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
              () -> {
                logger.debug("supplier true");
                try {
                  return true;
                }
                finally {
                  throw new IOException("supplier");
                }
              },
              bool -> CompletableFuture.supplyAsync(() -> {
                logger.debug("function {}", bool);
                return bool;
              }, newCachedThreadPool),
              bool -> {
                logger.debug("releaser {}", bool);
              }, newCachedThreadPool);

      try {
        applyWith.get();
      }
      catch (ExecutionException ex) {
        throw ex.getCause();
      }
    }
    finally {
      newCachedThreadPool.shutdown();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithFailedFunction() throws Throwable
  {
    @SuppressWarnings("ThrowFromFinallyBlock")
    CompletableFuture<Boolean> applyWith = Async.applyWith(
            () -> {
              logger.debug("supplier true");
              return true;
            },
            bool -> {
              throw new IOException("function");
            },
            bool -> {
              logger.debug("releaser {}", bool);
            });

    try {
      applyWith.get();
    }
    catch (ExecutionException ex) {
      throw ex.getCause();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithAsyncFailedFunction() throws Throwable
  {
    @SuppressWarnings("ThrowFromFinallyBlock")
    CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
            () -> {
              logger.debug("supplier true");
              return true;
            },
            bool -> {
              throw new IOException("function");
            },
            bool -> {
              logger.debug("releaser {}", bool);
            });

    try {
      applyWith.get();
    }
    catch (ExecutionException ex) {
      throw ex.getCause();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithAsyncExecutorFailedFunction() throws Throwable
  {
    ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
    try {
      @SuppressWarnings("ThrowFromFinallyBlock")
      CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
              () -> {
                logger.debug("supplier true");
                return true;
              },
              bool -> {
                throw new IOException("function");
              },
              bool -> {
                logger.debug("releaser {}", bool);
              }, newCachedThreadPool);

      try {
        applyWith.get();
      }
      catch (ExecutionException ex) {
        throw ex.getCause();
      }
    }
    finally {
      newCachedThreadPool.shutdown();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithFailedFunctionReturn() throws Throwable
  {
    @SuppressWarnings("ThrowFromFinallyBlock")
    CompletableFuture<Boolean> applyWith = Async.applyWith(
            () -> {
              logger.debug("supplier true");
              return true;
            },
            bool -> CompletableFuture.supplyAsync(() -> {
              throw Throwables.throwSneaky(new IOException("function"));
            }),
            bool -> {
              logger.debug("releaser {}", bool);
            });

    try {
      applyWith.get();
    }
    catch (ExecutionException ex) {
      throw ex.getCause();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithAsyncFailedFunctionReturn() throws Throwable
  {
    @SuppressWarnings("ThrowFromFinallyBlock")
    CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
            () -> {
              logger.debug("supplier true");
              return true;
            },
            bool -> CompletableFuture.supplyAsync(() -> {
              throw Throwables.throwSneaky(new IOException("function"));
            }),
            bool -> {
              logger.debug("releaser {}", bool);
            });

    try {
      applyWith.get();
    }
    catch (ExecutionException ex) {
      throw ex.getCause();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithAsyncExecutorFailedFunctionReturn() throws Throwable
  {
    ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
    try {
      @SuppressWarnings("ThrowFromFinallyBlock")
      CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
              () -> {
                logger.debug("supplier true");
                return true;
              },
              bool -> CompletableFuture.supplyAsync(() -> {
                throw Throwables.throwSneaky(new IOException("function"));
              }, newCachedThreadPool),
              bool -> {
                logger.debug("releaser {}", bool);
              }, newCachedThreadPool);

      try {
        applyWith.get();
      }
      catch (ExecutionException ex) {
        throw ex.getCause();
      }
    }
    finally {
      newCachedThreadPool.shutdown();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithFailedReleaser() throws Throwable
  {
    @SuppressWarnings("ThrowFromFinallyBlock")
    CompletableFuture<Boolean> applyWith = Async.applyWith(
            () -> {
              return true;
            },
            bool -> CompletableFuture.supplyAsync(() -> {
              return bool;
            }),
            bool -> {
              throw new IOException("releaser");
            });

    try {
      applyWith.get();
    }
    catch (ExecutionException ex) {
      throw ex.getCause();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithAsyncFailedReleaser() throws Throwable
  {
    @SuppressWarnings("ThrowFromFinallyBlock")
    CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
            () -> {
              return true;
            },
            bool -> CompletableFuture.supplyAsync(() -> {
              return bool;
            }),
            bool -> {
              throw new IOException("releaser");
            });

    try {
      applyWith.get();
    }
    catch (ExecutionException ex) {
      throw ex.getCause();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithAsyncExecutorFailedReleaser() throws Throwable
  {
    ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
    try {
      @SuppressWarnings("ThrowFromFinallyBlock")
      CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
              () -> {
                return true;
              },
              bool -> CompletableFuture.supplyAsync(() -> {
                return bool;
              }, newCachedThreadPool),
              bool -> {
                throw new IOException("releaser");
              }, newCachedThreadPool);

      try {
        applyWith.get();
      }
      catch (ExecutionException ex) {
        throw ex.getCause();
      }
    }
    finally {
      newCachedThreadPool.shutdown();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithFailedFunctionReleaser() throws Throwable
  {
    @SuppressWarnings("ThrowFromFinallyBlock")
    CompletableFuture<Boolean> applyWith = Async.applyWith(
            () -> {
              return true;
            },
            bool -> {
              throw new IOException("function");
            },
            bool -> {
              throw new IOException("releaser");
            });

    try {
      applyWith.get();
    }
    catch (ExecutionException ex) {
      Throwable cause = ex.getCause();
      Assert.assertEquals("message", "function", cause.getMessage());
      Assert.assertEquals("suppressed count", 1, cause.getSuppressed().length);
      Assert.assertSame("Suppressed Type", IOException.class, cause.getSuppressed()[0].getClass());
      Assert.assertEquals("message", "releaser", cause.getSuppressed()[0].getMessage());
      throw cause;
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithAsyncFailedFunctionReleaser() throws Throwable
  {
    @SuppressWarnings("ThrowFromFinallyBlock")
    CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
            () -> {
              return true;
            },
            bool -> {
              throw new IOException("function");
            },
            bool -> {
              throw new IOException("releaser");
            });

    try {
      applyWith.get();
    }
    catch (ExecutionException ex) {
      Throwable cause = ex.getCause();
      Assert.assertEquals("message", "function", cause.getMessage());
      Assert.assertEquals("suppressed count", 1, cause.getSuppressed().length);
      Assert.assertSame("Suppressed Type", IOException.class, cause.getSuppressed()[0].getClass());
      Assert.assertEquals("message", "releaser", cause.getSuppressed()[0].getMessage());
      throw cause;
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithAsyncExecutorFailedFunctionReleaser() throws Throwable
  {
    ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
    try {
      @SuppressWarnings("ThrowFromFinallyBlock")
      CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
              () -> {
                return true;
              },
              bool -> {
                throw new IOException("function");
              },
              bool -> {
                throw new IOException("releaser");
              }, newCachedThreadPool);

      try {
        applyWith.get();
      }
      catch (ExecutionException ex) {
        Throwable cause = ex.getCause();
        Assert.assertEquals("message", "function", cause.getMessage());
        Assert.assertEquals("suppressed count", 1, cause.getSuppressed().length);
        Assert.assertSame("Suppressed Type", IOException.class, cause.getSuppressed()[0].getClass());
        Assert.assertEquals("message", "releaser", cause.getSuppressed()[0].getMessage());
        throw cause;
      }
    }
    finally {
      newCachedThreadPool.shutdown();
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithFailedFunctionReturnReleaser() throws Throwable
  {
    @SuppressWarnings("ThrowFromFinallyBlock")
    CompletableFuture<Boolean> applyWith = Async.applyWith(
            () -> {
              return true;
            },
            bool -> CompletableFuture.supplyAsync(() -> {
              throw Throwables.throwSneaky(new IOException("function"));
            }),
            bool -> {
              throw new IOException("releaser");
            });

    try {
      applyWith.get();
    }
    catch (ExecutionException ex) {
      Throwable cause = ex.getCause();
      Assert.assertEquals("message", "function", cause.getMessage());
      Assert.assertEquals("suppressed count", 1, cause.getSuppressed().length);
      Assert.assertSame("Suppressed Type", IOException.class, cause.getSuppressed()[0].getClass());
      Assert.assertEquals("message", "releaser", cause.getSuppressed()[0].getMessage());
      throw cause;
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithAsyncFailedFunctionReturnReleaser() throws Throwable
  {
    @SuppressWarnings("ThrowFromFinallyBlock")
    CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
            () -> {
              return true;
            },
            bool -> CompletableFuture.supplyAsync(() -> {
              throw Throwables.throwSneaky(new IOException("function"));
            }),
            bool -> {
              throw new IOException("releaser");
            });

    try {
      applyWith.get();
    }
    catch (ExecutionException ex) {
      Throwable cause = ex.getCause();
      Assert.assertEquals("message", "function", cause.getMessage());
      Assert.assertEquals("suppressed count", 1, cause.getSuppressed().length);
      Assert.assertSame("Suppressed Type", IOException.class, cause.getSuppressed()[0].getClass());
      Assert.assertEquals("message", "releaser", cause.getSuppressed()[0].getMessage());
      throw cause;
    }
  }

  @Test(expected = IOException.class)
  public void testApplyWithAsyncExecutorFailedFunctionReturnReleaser() throws Throwable
  {
    ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
    try {
      @SuppressWarnings("ThrowFromFinallyBlock")
      CompletableFuture<Boolean> applyWith = Async.applyWithAsync(
              () -> {
                return true;
              },
            bool -> CompletableFuture.supplyAsync(() -> {
              throw Throwables.throwSneaky(new IOException("function"));
            }),
              bool -> {
                throw new IOException("releaser");
              }, newCachedThreadPool);

      try {
        applyWith.get();
      }
      catch (ExecutionException ex) {
        Throwable cause = ex.getCause();
        Assert.assertEquals("message", "function", cause.getMessage());
        Assert.assertEquals("suppressed count", 1, cause.getSuppressed().length);
        Assert.assertSame("Suppressed Type", IOException.class, cause.getSuppressed()[0].getClass());
        Assert.assertEquals("message", "releaser", cause.getSuppressed()[0].getMessage());
        throw cause;
      }
    }
    finally {
      newCachedThreadPool.shutdown();
    }
  }

  private static final Logger logger = LogManager.getLogger();
}
