package com.vho.javalowlevel.concurrency.future;

import java.util.concurrent.*;

public class CancellableTaskDemo {
  public static void main(String[] args) throws InterruptedException {
    FutureTask<Integer> task = new FutureTask<>(() -> {
      try {
        System.out.println("Sleeping...");
        Thread.sleep(5000);
        System.out.println("Done");
      } catch (InterruptedException e) {
        System.out.println("Cancelling...");
        Thread.currentThread().interrupt();
      }
      return 1;
    });
    ExecutorService es = Executors.newFixedThreadPool(2);
    es.submit(task);
    Thread.sleep(1000);
    System.out.println("is task cancelled " + task.isCancelled());
    task.cancel(true);
    es.shutdown();
    try {
      System.out.println("is task cancelled " + task.isCancelled());
      es.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      es.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
