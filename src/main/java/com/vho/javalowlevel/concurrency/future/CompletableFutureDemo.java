package com.vho.javalowlevel.concurrency.future;

import java.util.concurrent.*;

public class CompletableFutureDemo {
  public static void main(String[] args) throws InterruptedException {
    ExecutorService es = Executors.newFixedThreadPool(2);
    testSimpleAPIs(es);
    testAllSucceed(es);

    es.shutdown();
    try {
      es.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      es.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  private static void testAllSucceed(ExecutorService es) {
    CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
      System.out.println("1");
      return 1;
    }, es);
    CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
      System.out.println("2");
      return 2;
    }, es);
    CompletableFuture<Integer> f3 = CompletableFuture.supplyAsync(() -> {
      System.out.println("3");
      return 3;
    }, es);

    CompletableFuture<Void> all = CompletableFuture.allOf(f1, f2, f3);
    all.exceptionally(e -> {
      System.out.println("error = " + e.getMessage());
      return null;
    }).thenAccept(v -> {
      System.out.println("success");
    });
    all.join();
  }


  private static void testSimpleAPIs(ExecutorService es) {
    CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> {
      System.out.println("pushed");
      return 1;
    }, es);

    f.handleAsync((res, e) -> {
      System.out.println("result = " + res);
      System.out.println("error = " + e);
      return null;
    }, es);

    // plus 1 and then multiply by 10
    f.thenApply(i -> i + 1).thenCombine(CompletableFuture.supplyAsync(() -> 10), (x, y) -> x * y)
      .thenAccept(x -> System.out.println("combined = " + x));

    // compose with another CompletableFuture
    f.thenCompose(x -> CompletableFuture.supplyAsync(() -> x * 2))
      .thenAccept(x -> System.out.println("compose = " + x));
  }
}
