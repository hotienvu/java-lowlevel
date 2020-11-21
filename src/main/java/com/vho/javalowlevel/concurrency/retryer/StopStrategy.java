package com.vho.javalowlevel.concurrency.retryer;

public interface StopStrategy {
  boolean shouldStop(Attempt attempt);
  String getReason();
}
