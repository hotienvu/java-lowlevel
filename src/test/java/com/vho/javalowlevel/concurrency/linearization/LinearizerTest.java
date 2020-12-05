package com.vho.javalowlevel.concurrency.linearization;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class LinearizerTest {


  @Test
  public void testLinearizationPoints() {
    Linearizer linearizer = new Linearizer();
    List<Integer> res = new ArrayList<>();
    List<Runnable> tasks = IntStream.rangeClosed(1, 1000)
      .mapToObj(i -> createLinearizationTask(linearizer, i, res))
      .collect(Collectors.toList());
    executeTasks(tasks);
    final List<Integer> ans = IntStream.rangeClosed(1, 1000).boxed().collect(Collectors.toList());
    assertEquals(ans, res);
  }

  @Test
  public void testStressMixedPoints() {
    Linearizer linearizer = new Linearizer();
    List<Integer> res = new ArrayList<>();
    final AtomicInteger numLinear = new AtomicInteger();
    List<Runnable> tasks = IntStream.rangeClosed(1, 1000)
      .mapToObj(i -> {
        if (i % 10 == 0) {
          numLinear.incrementAndGet();
          return createLinearizationTask(linearizer, numLinear.get(), res);
        } else {
          return createConcurrentTask(linearizer, numLinear.get(), res);
        }
      })
      .collect(Collectors.toList());
    executeTasks(tasks);
    for (int i = 0; i < res.size() - 1; ++i) {
      assertTrue(res.get(i) <= res.get(i + 1));
    }
  }

  @Test
  public void testConcurrentPoints() throws InterruptedException {
    Linearizer linearizer = new Linearizer();
    List<Integer> res = new ArrayList<>();
    List<Runnable> tasks = new ArrayList<>();
    tasks.add(createLinearizationTask(linearizer, 1, res));
    tasks.add(createConcurrentTask(linearizer, 2, res));
    tasks.add(createConcurrentTask(linearizer, 2, res));
    tasks.add(createConcurrentTask(linearizer, 2, res));
    tasks.add(createLinearizationTask(linearizer, 3, res));
    executeTasks(tasks);
    assertEquals(Arrays.asList(1, 2, 2, 2, 3), res);
  }

  private Runnable createConcurrentTask(Linearizer linearizer, int id, List<Integer> list) {
    ConcurrentPoint point = linearizer.createConcurrentPoint();
    return () -> {
      point.start();
      synchronized (list) {
        list.add(id);
      }
      point.complete();
    };
  }

  private Runnable createLinearizationTask(Linearizer linearizer, final int id, List<Integer> list) {
    LinearizationPoint point = linearizer.createLinearizationPoint();
    return () -> {
      point.start();
      list.add(id);
      point.complete();
    };
  }


  private void executeTasks(List<Runnable> tasks) {
    ExecutorService es = Executors.newCachedThreadPool();

    Collections.shuffle(tasks);
    tasks.forEach(es::execute);
    es.shutdown();
    try {
      es.awaitTermination(20, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      es.shutdownNow();
    }
  }
}