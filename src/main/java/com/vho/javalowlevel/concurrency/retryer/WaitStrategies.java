package com.vho.javalowlevel.concurrency.retryer;

public final class WaitStrategies {

  private WaitStrategies() {
  }

  public static WaitStrategy threadSleepConstantDurationStrategy(long durationInMillies) {
    return attempt -> {
      System.out.println("Sleeping " + durationInMillies + " ms...");
      Thread.sleep(durationInMillies);
    };
  }
}
