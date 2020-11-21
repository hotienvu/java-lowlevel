package com.vho.javalowlevel.concurrency.nonblocking;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicRefDemo {
  private static class Point {
    int x;
    int y;

    public Point(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Point point = (Point) o;

      if (x != point.x) return false;
      return y == point.y;
    }

    @Override
    public int hashCode() {
      int result = x;
      result = 31 * result + y;
      return result;
    }
  }

  private static class Robot {
    private static final int MAX_TRIES = 100;
    AtomicReference<Point> position;

    public Robot(Point position) {
      this.position = new AtomicReference<>(position);
    }

    public int update(int x, int y) {
      Point newPos = new Point(x, y);
      int tries = 0;
      while (tries++ < MAX_TRIES) {
        Point old = position.get();
        if (position.compareAndSet(old, newPos)) {
          break;
        }
      }
      return tries;
    }

    public Point getPos() {
      return position.get();
    }
  }

  public static void main(String[] args) {
    Robot robot = new Robot(new Point(0, 0));
    final int NUM_EXECUTORS = 10;
    ExecutorService es = Executors.newFixedThreadPool(NUM_EXECUTORS);
    CountDownLatch latch = new CountDownLatch(NUM_EXECUTORS);
    for (int i = 0; i < NUM_EXECUTORS; ++i) {
      es.submit(() -> {
        Random rand = ThreadLocalRandom.current();
        long totalRetries = 0;
        for (int run = 0; run < 10000; ++run) {
          totalRetries += robot.update(rand.nextInt(100), rand.nextInt(100));
          robot.getPos();
        }
        System.out.println(Thread.currentThread().getName() + " completed. Total retries = " + totalRetries);
        latch.countDown();
      });
    }

    try {
      latch.await();
      System.out.println("All completed." + latch.getCount());
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
    es.shutdown();
    try {
      es.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      es.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
