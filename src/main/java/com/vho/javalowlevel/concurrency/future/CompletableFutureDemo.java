package com.vho.javalowlevel.concurrency.future;

import java.util.concurrent.*;

public class CompletableFutureDemo {
  public static void main(String[] args) throws InterruptedException {
    ExecutorService es = Executors.newFixedThreadPool(2);
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

    es.shutdown();
    try {
      es.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      es.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
