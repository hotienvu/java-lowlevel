package com.vho.javalowlevel.concurrency.retryer;

import java.util.concurrent.Callable;

public class GenericRetryer<T> implements Retryer<T> {

  StopStrategy stopStrategy;
  WaitStrategy waitStrategy;
  private final Class<? extends Exception> retryIfExceptionOfType;

  private GenericRetryer(StopStrategy stopStrategy,
                         WaitStrategy waitStrategy,
                         Class<? extends Exception> retryIfExceptionOfType) {

    this.stopStrategy = stopStrategy;
    this.waitStrategy = waitStrategy;
    this.retryIfExceptionOfType = retryIfExceptionOfType;
  }

  @Override
  public T call(Callable<T> callable) throws RetryException {
    for (int tries = 1; ; tries++) {
      try {
        return callable.call();
      } catch (Exception e) {
        Attempt attempt = new Attempt(tries);
        if (!retryIfExceptionOfType.isInstance(e)) {
          throw new RetryException("Exception thrown is not of type " + retryIfExceptionOfType.getName());
        }
        if (stopStrategy.shouldStop(attempt)) {
          throw new RetryException("Retries stopped because " + stopStrategy.getReason());
        }
        try {
          waitStrategy.block(attempt);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new RetryException("Interrupted while waiting for next retry. Attempt " + attempt, ie);
        }
      }
    }
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public static final class Builder<T> {
    StopStrategy stopStrategy = StopStrategies.maxAttemptsStrategy(StopStrategies.DEFAULT_MAX_ATTEMPTS);
    WaitStrategy waitStrategy = WaitStrategies.threadSleepConstantDurationStrategy(100);
    private Class<? extends Exception> retryIfExceptionOfType = RuntimeException.class;

    public GenericRetryer<T> build() {
      return new GenericRetryer<>(stopStrategy, waitStrategy, retryIfExceptionOfType);
    }

    public Builder<T> withStopStrategy(StopStrategy stopStrategy) {
      this.stopStrategy = stopStrategy;
      return this;
    }

    public Builder<T> withWaitStrategy(WaitStrategy waitStrategy) {
      this.waitStrategy = waitStrategy;
      return this;
    }

    public Builder<T> withRetryIfExceptionOfType(Class<? extends Exception> retryIfExceptionOfType) {
      this.retryIfExceptionOfType = retryIfExceptionOfType;
      return this;
    }
  }
}
