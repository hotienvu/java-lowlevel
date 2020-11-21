package com.vho.javalowlevel.concurrency.retryer;

public class StopStrategies {
  public static final int DEFAULT_MAX_ATTEMPTS = 10;

  public static StopStrategy maxAttemptsStrategy(int maxRetries) {
    return new StopStrategy() {
      @Override
      public boolean shouldStop(Attempt attempt) {
        return attempt.getNumAttempts() >= maxRetries;
      }

      @Override
      public String getReason() {
        return String.format("max number of attempts(%d) reached", maxRetries);
      }
    };
  }
}
