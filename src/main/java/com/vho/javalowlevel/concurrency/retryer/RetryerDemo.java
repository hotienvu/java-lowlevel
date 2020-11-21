package com.vho.javalowlevel.concurrency.retryer;

import java.util.concurrent.atomic.AtomicInteger;

public class RetryerDemo {
  public static void main(String[] args) {
    Retryer<Boolean> tryer = Retryer.<Boolean>builder()
      .withRetryIfExceptionOfType(RuntimeException.class)
      .withStopStrategy(StopStrategies.maxAttemptsStrategy(10))
      .withWaitStrategy(WaitStrategies.threadSleepConstantDurationStrategy(1000))
      .build();

    AtomicInteger counter = new AtomicInteger(1);
    try {
      boolean res = tryer.call(() -> {
        if (counter.getAndIncrement() < 10) {
          throw new IllegalStateException("counter too small");
        }
        return true;
      });
      System.out.println("result = " + res);
    } catch (RetryException e) {
      e.printStackTrace();
    }
  }
}
