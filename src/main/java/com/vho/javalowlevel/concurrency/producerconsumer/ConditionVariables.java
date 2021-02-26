package com.vho.javalowlevel.concurrency.producerconsumer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionVariables {

  private static final int MAX_CAPACITY = 10;

  private static final Lock lock = new ReentrantLock();
  private static final Condition bufferNotEmpty = lock.newCondition();
  private static final Condition bufferNotFull = lock.newCondition();

  private static class Consumer extends AbstractConsumer {

    public Consumer(String name, Deque<String> buffer) {
      super(name, buffer);
    }

    @Override
    public String consume() {
      lock.lock();
      try {
        while (buffer.isEmpty()) {
          bufferNotEmpty.await();
        }
        String data = buffer.remove();
        bufferNotFull.signal();
        return data;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(name + " got interrupted");
      } finally {
        lock.unlock();
      }
    }
  }

  private static class Producer extends AbstractProducer {

    public Producer(String name, Deque<String> buffer) {
      super(name, buffer);
    }

    @Override
    public void produce(String data) {
      lock.lock();
      try {
        while (buffer.size() >= MAX_CAPACITY) {
          bufferNotFull.await();
        }
        buffer.offer(data);
        bufferNotEmpty.signal();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(name + " got interrupted");
      } finally {
        lock.unlock();
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