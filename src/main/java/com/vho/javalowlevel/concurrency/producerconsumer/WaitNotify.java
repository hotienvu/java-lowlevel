package com.vho.javalowlevel.concurrency.producerconsumer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executors;

public class WaitNotify {

  private static final int MAX_CAPACITY = 10;

  private static class Consumer extends AbstractConsumer {

    private volatile boolean finished;

    public Consumer(String name, Deque<String> buffer) {
      super(name, buffer);
    }

    @Override
    public String consume() {
      synchronized (buffer) {
        while (buffer.isEmpty()) {
          try {
            buffer.wait();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(name + " got interrupted");
          }
        }
        String data = buffer.remove();
        if (buffer.size() == MAX_CAPACITY - 1) {
          buffer.notifyAll();
        }
        return data;
      }
    }
  }

  private static class Producer extends AbstractProducer {

    public Producer(String name, Deque<String> buffer) {
      super(name, buffer);
    }

    @Override
    public void produce(String data) {
      synchronized (buffer) {
        while (buffer.size() >= MAX_CAPACITY) {
          try {
            buffer.wait();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(name + " got interrupted");
          }
        }
        buffer.offer(data);
        if (buffer.size() == 1) {
          buffer.notifyAll();
        }
      }

    }
  }

  public static void main(String[] args) throws Exception {
    ProducerConsumerTest test = new ProducerConsumerTest(
      2,
      10,
      new ArrayDeque<>(MAX_CAPACITY),
      Executors.newCachedThreadPool());

    test.setConsumerClass(Consumer.class);
    test.setProducerClass(Producer.class);
    test.run();
    System.exit(0);
  }
}
