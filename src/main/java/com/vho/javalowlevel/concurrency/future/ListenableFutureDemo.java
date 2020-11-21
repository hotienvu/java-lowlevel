package com.vho.javalowlevel.concurrency.future;

import com.google.common.util.concurrent.*;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ListenableFutureDemo {
  public static void main(String[] args) {
    ListeningExecutorService es = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));
    ListenableFuture<Void> f = es.submit(() -> {
      System.out.println("pushed");
      int x = 1 / 0;
      return null;
    });
    Futures.addCallback(f, new FutureCallback<Void>() {
      @Override
      public void onSuccess(@Nullable Void unused) {
        System.out.println("push completed");
      }

      @Override
      public void onFailure(Throwable throwable) {
        System.out.println("push failed: " + throwable.getMessage());
      }
    }, es);
    f.addListener(() -> System.out.println("done 1"), es);
    f.addListener(() -> System.out.println("done 2"), es);

    es.shutdown();
    try {
      es.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      es.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
