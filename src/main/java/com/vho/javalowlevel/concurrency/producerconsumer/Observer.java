package com.vho.javalowlevel.concurrency.producerconsumer;

@FunctionalInterface
public interface Observer {
  void onNotify(Object source, Object event);
}
