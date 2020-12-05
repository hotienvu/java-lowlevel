package com.vho.javalowlevel.concurrency.linearization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Inspired by facebook j-common's Linearizer
 */
public class Linearizer {
  private static final long WAIT_TIMEOUT_SECONDS = 300;
  Logger LOG = LoggerFactory.getLogger(Linearizer.class);

  private AtomicReference<LinearizationPoint> lastLinearPoint = new AtomicReference<>(null);
  private AtomicReference<AtomicInteger> numOngoingPoints = new AtomicReference<>(new AtomicInteger(0));

  /**
   * creates an lock-object such that other objects of this type may interleave their start/complete
   * calls.
   *
   * <p>calling start() on the resulting ConcurrentPoint will block until the previous
   * LinearizationPoint calls complete()
   */
  public synchronized ConcurrentPoint createConcurrentPoint() {
    numOngoingPoints.get().incrementAndGet();
    return new ConcurrentPointImpl(lastLinearPoint.get(), numOngoingPoints.get());
  }

  /**
   * calling start() on the resulting LinearizationPoint will block until all previously generated
   * Points call complete()
   */
  public synchronized LinearizationPoint createLinearizationPoint() {
    AtomicInteger numNextPoints = new AtomicInteger(1);
    AtomicInteger numPreviousPoints = numOngoingPoints.getAndSet(numNextPoints);
    LinearizationPoint newLinearPoint = new LinearizationPointImpl(numPreviousPoints, numNextPoints);
    lastLinearPoint.set(newLinearPoint);
    return newLinearPoint;
  }

  private static class ConcurrentPointImpl implements ConcurrentPoint {
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentPointImpl.class);

    private final LinearizationPoint previousLinearPoint;
    private final AtomicInteger numConcurrentPoints;

    ConcurrentPointImpl(LinearizationPoint previousLinearPoint, AtomicInteger numConcurrentPoints) {
      this.previousLinearPoint = previousLinearPoint;
      this.numConcurrentPoints = numConcurrentPoints;
    }

    @Override
    public void start() {
      LOG.info("waiting for previous linearization points");
      if (previousLinearPoint != null) {
        try {
          previousLinearPoint.waitForCompletion();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Interrupted while waiting to start");
        }
      }
      LOG.info("done waiting for previous linearization points");
    }

    @Override
    public void complete() {
      LOG.info("completed");
      int pointsRemained = numConcurrentPoints.decrementAndGet();
      if (pointsRemained == 0) {
        synchronized (numConcurrentPoints) {
          numConcurrentPoints.notifyAll();
        }
      }
    }
  }

  private static class LinearizationPointImpl implements LinearizationPoint {
    private static final Logger LOG = LoggerFactory.getLogger(LinearizationPointImpl.class);

    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final CountDownLatch completeLatch = new CountDownLatch(1);
    private final AtomicInteger numPreviousConcurrentPoints;
    private final AtomicInteger numNextConcurrentPoints;

    LinearizationPointImpl(AtomicInteger numPreviousConcurrentPoints, AtomicInteger numNextConcurrentPoints) {
      this.numPreviousConcurrentPoints = numPreviousConcurrentPoints;
      this.numNextConcurrentPoints = numNextConcurrentPoints;
    }

    @Override
    public void start() {
      LOG.info("waiting for previous points");
      waitForPreviousPoints();
      LOG.info("done waiting for previous points");
      startLatch.countDown();
    }

    private void waitForPreviousPoints() {
      synchronized (numPreviousConcurrentPoints) {
        while (numPreviousConcurrentPoints.get() > 0) {
          try {
            numPreviousConcurrentPoints.wait();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting to start");
          }
        }
      }
    }

    @Override
    public void complete() {
      LOG.info("completed");
      completeLatch.countDown();
      synchronized (numNextConcurrentPoints) {
        int pointsRemained = numNextConcurrentPoints.decrementAndGet();
        if (pointsRemained == 0) {
          numNextConcurrentPoints.notifyAll();
        }
      }
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
