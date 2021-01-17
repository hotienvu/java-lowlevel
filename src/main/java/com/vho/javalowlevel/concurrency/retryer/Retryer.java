package com.vho.javalowlevel.concurrency.retryer;

import java.util.concurrent.Callable;

public interface Retryer<T> {

  T call(Callable<T> callable) throws RetryException;
}
