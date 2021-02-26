package com.vho.javalowlevel.concurrency.producerconsumer;

import java.util.Deque;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockingQueues {

  private static final int MAX_CAPACITY = 10;
  private static final ConcurrentMap<String, Integer> stats = new ConcurrentHashMap<>();
  private static final AtomicInteger totalConsumed = new AtomicInteger();
  private static final AtomicInteger totalProduced = new AtomicInteger();

  private static class Consumer extends AbstractConsumer {

    public Consumer(String name, Deque<String> buffer) {
      super(name, buffer);
      if (!(buffer instanceof BlockingDeque)) {
        throw new IllegalArgumentException("Buffer must be of type BlockingDeque");
      }
    }

    @Override
    public String consume() {
      String data;
      try {
        data = ((BlockingDeque<String>) buffer).take();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(name + " got interrupted");
      }
      return data;
    }
  }

  private static class Producer extends AbstractProducer {

    public Producer(String name, Deque<String> buffer) {
      super(name, buffer);
      if (!(buffer instanceof BlockingDeque)) {
        throw new IllegalArgumentException("Buffer must be of type BlockingDeque");
      }
    }

    @Override
    public void produce(String data) {
      try {
        ((BlockingDeque<String>) buffer).put(data);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(name + " got interrupted");
      }
      totalProduced.incrementAndGet();
      stats.put(name, stats.getOrDefault(name, 0) + 1);
    }
  }

  public static void main(String[] args) throws Exception {
    ProducerConsumerTest test = new ProducerConsumerTest(
      2,
      10,
      new LinkedBlockingDeque<>(MAX_CAPACITY),
      Executors.newCachedThreadPool());

    test.setConsumerClass(Consumer.class);
    test.setProducerClass(Producer.class);
    test.run();
    System.exit(0);
  }
}
