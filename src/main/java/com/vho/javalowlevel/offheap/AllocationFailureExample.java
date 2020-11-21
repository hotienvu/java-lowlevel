package com.vho.javalowlevel.offheap;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *  Allocates large direct buffers until the virtual address space (or available
 *  commit charge) is exhausted.
 */
public class AllocationFailureExample {

  public static void main(String[] argv) throws Exception {
    final int bufSize = 250 * 1024 * 1024;  // 250 MB, adjust upwards for 64 bit
    List<ByteBuffer> refs = new ArrayList<>();
    while (true) {
      System.out.println("allocating buffer " + refs.size());
      refs.add(ByteBuffer.allocateDirect(bufSize));
      Thread.sleep(1000);
    }
  }
}
