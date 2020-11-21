package com.vho.javalowlevel.concurrency;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class BackgroundTaskExecutor {

  public static interface OnInterruption<T> {
    void accept(Future<T> t, Exception exception);
  }

  public static interface OnShutdownError {
    void accept(ExecutorService executor, Exception exception);
  }

  private final ExecutorService executor;

  public BackgroundTaskExecutor(int threadsForTasks) {
    this.executor = Executors.newFixedThreadPool(threadsForTasks);
  }

  public <T> Future<T> execute(Callable<T> task) {
    return executor.submit(task);
  }

  public <T> List<Future<T>> execute(List<Callable<T>> tasks) {
    return tasks.stream()
      .map(executor::submit)
      .collect(Collectors.toList());
  }

  public <T> boolean cancel(Future<T> task) {
    return task.cancel(true);
  }

  public <T> boolean cancel(List<FutureTask<T>> tasks) {
    return tasks.stream()
      .map(it -> it.cancel(true))
      .allMatch(it -> it.equals(true));
  }

  public <T> List<Optional<T>> completeTasks(List<Future<T>> tasks, OnInterruption<T> onInterruption) {
    return
      tasks.stream()
      .map(f -> {
        try {
          return Optional.of(f.get());
        } catch (InterruptedException | ExecutionException e) {
          Thread.currentThread().interrupt();
          onInterruption.accept(f, e);
          return Optional.<T>empty();
        }
      }).collect(Collectors.toList());
  }

  public <T> Optional<T> completeTask(Future<T> task, OnInterruption<T> onInterruption) {
    try {
      return Optional.of(task.get());
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      onInterruption.accept(task, e);
      return Optional.empty();
    }
  }

  public void shutdownTasks(long timeout, TimeUnit timeUnit, OnShutdownError onShutdownError) {
    executor.shutdown();
    try {
      executor.awaitTermination(timeout, timeUnit);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      onShutdownError.accept(executor, e);
      executor.shutdownNow();
    }
  }

  public List<Runnable> shutdownNowTasks(long timeout, TimeUnit timeUnit, OnShutdownError onShutdownError) {
    List<Runnable> remainingTasks = executor.shutdownNow();
    try {
      executor.awaitTermination(timeout, timeUnit);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      onShutdownError.accept(executor, e);
    }
    return remainingTasks;
  }
}
