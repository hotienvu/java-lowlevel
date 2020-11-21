package com.vho.javalowlevel.concurrency.retryer;

public interface WaitStrategy {
  void block(Attempt attempt) throws InterruptedException;
}
