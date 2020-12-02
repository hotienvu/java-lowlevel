package com.vho.javalowlevel.concurrency.linearization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Linearizer {
  private static final long WAIT_TIMEOUT_SECONDS = 300;
  Logger LOG = LoggerFactory.getLogger(Linearizer.class);

  private AtomicReference<LinearizationPoint> lastLinearPoint;
  private AtomicInteger numLinearPoints;

  /**
   * creates an lock-object such that other objects of this type may interleave their start/complete
   * calls.
   *
   * <p>calling start() on the resulting ConcurrentPoint will block until the previous
   * LinearizationPoint calls complete()
   *
   */
  public synchronized ConcurrentPoint createConcurrentPoint() {
    return new ConcurrentPointImpl();
  }

  /**
   * calling start() on the resulting LinearizationPoint will block until all previously generated
   * Points call complete()
   *
   */
  public synchronized LinearizationPoint createLinearizationPoint() {
    return new LinearizationPointImpl();
  }

  private class ConcurrentPointImpl implements ConcurrentPoint {

    public ConcurrentPointImpl() {
    }

    @Override
    public void start() {

    }

    @Override
    public void complete() {

    }
  }

  private class LinearizationPointImpl implements LinearizationPoint {
    LinearizationPoint previousPoint;
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch completeLatch = new CountDownLatch(1);

    public LinearizationPointImpl() {
      previousPoint = lastLinearPoint.getAndSet(this);
    }

    @Override
    public void start() {
      if (previousPoint != null) {
        try {
          previousPoint.waitForCompletion();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
      startLatch.countDown();
    }

    @Override
    public void complete() {
      completeLatch.countDown();
    }

    @Override
    public void waitForStart() throws InterruptedException {
      while (!waitForStart(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        LOG.info("Waited {} seconds for LinearPoint to start", WAIT_TIMEOUT_SECONDS);
      }
    }

    @Override
    public boolean waitForStart(long timeout, TimeUnit unit) throws InterruptedException {
      return startLatch.await(timeout, unit);
    }

    @Override
    public void waitForCompletion() throws InterruptedException {
      while (!waitForCompletion(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        LOG.info("Waited {} seconds for LinearPoint to complete", WAIT_TIMEOUT_SECONDS);
      }
    }

    @Override
    public boolean waitForCompletion(long timeout, TimeUnit unit) throws InterruptedException {
      return completeLatch.await(timeout, unit);
    }
  }
}
