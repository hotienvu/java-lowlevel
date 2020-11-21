package com.vho.javalowlevel.concurrency.future;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CompletableFutureCancelDemo {
  public static void main(String[] args) throws InterruptedException {
    ExecutorService es = Executors.newFixedThreadPool(2);
    AtomicBoolean stopped = new AtomicBoolean(false);
    // cancel test
    CompletableFuture<Void> cf = CompletableFuture.supplyAsync(() -> {
      try {
        while (!stopped.get()) {
          System.out.println("Sleeping...");
          Thread.sleep(2000);
        }
        System.out.println("Done");
      } catch (InterruptedException | CancellationException e) {
        // CompletableFuture.cancel() doesn't interrupt underlying thread.
        System.out.println("This will never get called");
        Thread.currentThread().interrupt();
      }
      return null;
    }, es);
    cf.thenAccept(x -> {
      System.out.println("upstream cancelled thus this would never be called");
    });
    System.out.println("Stopping sleeping thread  " + cf.cancel(true));
    stopped.compareAndSet(false, true);
    es.shutdown();
    try {
      es.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      es.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
