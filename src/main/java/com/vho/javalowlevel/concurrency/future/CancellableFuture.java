package com.vho.javalowlevel.concurrency.future;

import java.util.concurrent.CompletableFuture;

public class CancellableFuture<T> extends CompletableFuture<T> {
  CompletableFuture<T> future;

  public CancellableFuture(CompletableFuture<T> future) {
    this.future = future;
  }

  public static <T> CancellableFuture<T> create(CompletableFuture<T> future) {
    return new CancellableFuture<>(future);
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return super.cancel(mayInterruptIfRunning);
  }
}
