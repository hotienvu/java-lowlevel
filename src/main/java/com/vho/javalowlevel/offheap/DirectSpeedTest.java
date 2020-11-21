package com.vho.javalowlevel.offheap;


import java.nio.ByteBuffer;
import java.util.Random;


/**
 *  Compares the performance of in-heap and out-of-heap buffers, using
 *  random reads and writes.
 */
public class DirectSpeedTest
{
  public static void main(String[] argv)
    throws Exception
  {
    // source of randomness: replace the ctor param with a known
    // value if you want to ensure the same blocks are read/written
    // in the same order
    Random rnd = new Random(System.currentTimeMillis());

    // change "reps" to get a reasonable runtime on your system
    // change "writePct" to change the percentage of writes vs reads
    int size = 100 * 1024 * 1024;
    int reps = 100000000;

    // pick one of the following
//    ByteBuffer buf = ByteBuffer.allocate(size);
     ByteBuffer buf = ByteBuffer.allocateDirect(size);

    long start = System.currentTimeMillis();
    for (int ii = 0 ; ii < reps ; ii++)
    {
      int offset = rnd.nextInt(size - 4);
      buf.getInt(offset);
    }
    long finish = System.currentTimeMillis();
    long elapsed = finish - start;

    System.out.println(
      reps + " reps = "
        + elapsed + " milliseconds");
  }
}
