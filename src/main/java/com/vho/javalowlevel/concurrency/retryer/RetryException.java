package com.vho.javalowlevel.concurrency.retryer;

public class RetryException extends Exception {
  public RetryException(String s) {
    super(s);
  }

  public RetryException(String s, InterruptedException e) {
    super(s, e);
  }
}
