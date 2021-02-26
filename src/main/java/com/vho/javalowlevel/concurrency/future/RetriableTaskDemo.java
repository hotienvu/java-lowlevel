package com.vho.javalowlevel.concurrency.future;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class RetriableTaskDemo {

  private static class RetriableTask<T> implements Callable<T> {
    private Callable<T> runnable;
    private final int maxRetries;
    private final long waitDurationMillis;

    RetriableTask(Callable<T> runnable, int maxRetries, long waitDurationMillis) {
      this.runnable = runnable;
      this.maxRetries = maxRetries;
      this.waitDurationMillis = waitDurationMillis;
    }

    @Override
    public T call() throws Exception {
      int tries = 0;
      Exception lastException = null;
      T res = null;
      while (tries++ < maxRetries) {
        try {
          res = runnable.call();
          return res;
        } catch (Exception e) {
          System.out.println("Failed to get result. Retrying after " + waitDurationMillis + " ms");
          lastException = e;
          Thread.sleep(waitDurationMillis);
        }
      }
      throw new RetryExceedException("Retries limit reached: " + maxRetries, lastException);
    }

    private static class RetryExceedException extends Exception {
      public RetryExceedException(String msg, Exception e) {
        super(msg, e);
      }
    }
  }
  public static void main(String[] args) {
    AtomicInteger counter = new AtomicInteger(0);
    RetriableTask<Boolean> task = new RetriableTask<>(() -> {
      int count = counter.incrementAndGet();
      if (count < 10) {
        throw new IllegalArgumentException("Failed");
      }
      return true;
    }, 5, 1000);
    try {
      System.out.println("Result = " + task.call());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
