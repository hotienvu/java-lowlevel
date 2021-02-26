package com.vho.javalowlevel.concurrency.producerconsumer;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public abstract class AbstractProducer implements Runnable, Observable {
  protected final String name;
  protected final Deque<String> buffer;
  protected volatile boolean stopped = false;
  private List<Observer> observers = new ArrayList<>();


  public AbstractProducer(String name, Deque<String> buffer) {
    this.name = name;
    this.buffer = buffer;
  }

  @Override
  public void run() {
    System.out.println(name + " started.");
    while (!stopped) {
      String data = "data-" + name;
      produce(data);
      notifyObservers(data);
    }
  }

  private void notifyObservers(String data) {
    for (Observer observer: observers) {
      observer.onNotify(this, "produced data " + data);
    }
  }

  @Override
  public void addObserver(Observer observer) {
    observers.add(observer);
  }

  @Override
  public void removeObserver(Observer observer) {
    observers.remove(observer);
  }

  public abstract void produce(String data);

  public void stop() {
    stopped = true;
  }

  public String getName() {
    return name;
  }
}
