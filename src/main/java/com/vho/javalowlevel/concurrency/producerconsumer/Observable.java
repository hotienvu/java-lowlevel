package com.vho.javalowlevel.concurrency.producerconsumer;

public interface Observable {

  void addObserver(Observer observer);

  void removeObserver(Observer observer);
}
