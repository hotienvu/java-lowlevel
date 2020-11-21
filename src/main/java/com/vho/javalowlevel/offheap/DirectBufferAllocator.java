package com.vho.javalowlevel.offheap;


import java.nio.ByteBuffer;

/**
 *  A program that allocates a large buffer outside the Java heap.
 */
public class DirectBufferAllocator
{
  public static void main(String[] argv)
    throws Exception
  {
    ByteBuffer myBuffer = ByteBuffer.allocateDirect(100 * 1024 * 1024);

    // now that we've got the buffer, we'll sleep so that you can see
    // it in pmap(): look for a 102408K "anon" block with permissions "rwx"
    Thread.sleep(60000L);
  }
}
