package com.vho.javalowlevel.concurrency.producerconsumer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ProducerConsumerTest {
  private int numProducers;
  private int numConsumers;
  private ExecutorService es;
  private Class<? extends AbstractProducer> producerClass;
  private Class<? extends AbstractConsumer> consumerClass;

  /** shared buffer between consumer and producers */
  private Deque<String> buffer;

  /** statistics */
  private Map<String, Integer> stats = new HashMap<>();
  private static final AtomicInteger totalConsumed = new AtomicInteger();
  private static final AtomicInteger totalProduced = new AtomicInteger();



  public ProducerConsumerTest(int numProducers, int numConsumers, Deque<String> buffer, ExecutorService es) {
    this.numProducers = numProducers;
    this.numConsumers = numConsumers;
    this.buffer = buffer;
    this.es = es;
  }

  public void setProducerClass(Class<? extends AbstractProducer> producerClass) {
    this.producerClass = producerClass;
  }

  public void setConsumerClass(Class<? extends AbstractConsumer> consumerClass) {
    this.consumerClass = consumerClass;
  }

  public void run() throws Exception {
    AbstractProducer[] producers = new AbstractProducer[numProducers];
    for (int i=0;i<numProducers;++i) {
      producers[i] = producerClass.getConstructor(String.class, Deque.class).newInstance("Producer-" + i, buffer);
      es.submit(producers[i]);
      producers[i].addObserver((src, evnt) -> {
        totalProduced.incrementAndGet();
        String producerName = ((AbstractProducer)src).getName();
        System.out.println(producerName + " produced data " + evnt);
        stats.put(producerName, stats.getOrDefault(producerName, 0) + 1);
      });
    }

    AbstractConsumer[] consumers = new AbstractConsumer[numConsumers];
    for (int i=0;i<numConsumers;++i) {
      consumers[i] = consumerClass.getConstructor(String.class, Deque.class).newInstance("Consumer-" + i, buffer);
      es.submit(consumers[i]);
      consumers[i].addObserver((src, evnt) -> {
        totalConsumed.incrementAndGet();
        String consumerName = ((AbstractConsumer)src).getName();
        System.out.println(consumerName + " consumed data " + evnt);
        stats.put(consumerName, stats.getOrDefault(consumerName, 0) + 1);
      });
    }

    Thread.sleep(1000);
    for (AbstractProducer p : producers) {
      p.stop();
    }
    for (AbstractConsumer c : consumers) {
      c.stop();
    }
    es.shutdown();
    es.awaitTermination(5, TimeUnit.SECONDS);
    System.out.println("========== Producer/Consumer statistics ========");
    for (Map.Entry<String, Integer> e : stats.entrySet()) {
      System.out.println(e.getKey() + " => " + e.getValue());
    }
    System.out.println("Total produced = " + totalProduced.get());
    System.out.println("Total consumed = " + totalConsumed.get());
    System.out.println("Unconsumed = " + buffer.size());
  }
}
