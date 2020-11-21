package com.vho.javalowlevel.concurrency.retryer;

public class Attempt {
  private final int numAttempts;

  public Attempt(int numAttempts) {
    this.numAttempts = numAttempts;
  }

  public int getNumAttempts() {
    return numAttempts;
  }
}
