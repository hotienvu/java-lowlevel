package com.vho.javalowlevel.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class ByteBufferTest {
  /**
   * Display state of channel/buffer.
   *
   * @param where description of where we are in the program to label the state snapzhot
   * @param fc    FileChannel reading/writing.
   * @param b     Buffer to display state of:
   *
   * @throws java.io.IOException if I/O problems.
   */
  private static void showStats( String where, FileChannel fc, Buffer b ) throws IOException
  {
    System.out.println( where +
      " \nchannelPosition: " + fc.position() +
      " \nbufferPosition: " + b.position() +
      " \nlimit: " + b.limit() +
      " \nremaining: " + b.remaining() +
      " \ncapacity: " + b.capacity() + "\n======\n");
  }

  public static void main(String[] args) throws IOException {
    readRawBytes();
    writeRawBytes();
    byteBufferSliceTest();
  }

  private static void byteBufferSliceTest() {
    byte[] data = new byte[256];
    for (int ii = 0 ; ii < data.length ; ii++)
      data[ii] = (byte)ii;

    ByteBuffer buf1 = ByteBuffer.wrap(data);

    buf1.position(128);
    ByteBuffer buf2 = buf1.slice();

    System.out.println(String.format(
      "buf2[0], before update = %08x",
      buf2.getInt(0)));

    // note that we're changing the original buffer
    buf1.putInt(128, 0x12345678);

    System.out.println(String.format(
      "buf2[0], after update  = %08x",
      buf2.getInt(0)));
  }

  private static void writeRawBytes() throws IOException {
    final FileOutputStream fos = new FileOutputStream("test");
    final FileChannel fc = fos.getChannel();
    final byte[] output = "Hello, world! this is a test asdf asdf\nThis is second line\nThis is third line.".getBytes();
    ByteBuffer buffer = ByteBuffer.allocate(8);
    showStats("after created", fc, buffer);
    for (int offset=0;offset < output.length;offset += buffer.capacity()) {
      buffer.clear();
      showStats("before read", fc, buffer);
      buffer.put(output, offset, Math.min(output.length-offset, buffer.capacity()));
      showStats("after read", fc, buffer);
      buffer.flip();
      showStats("before write", fc, buffer);
      fc.write(buffer);
      showStats("after write", fc, buffer);
    }
    fc.close();
  }

  private static void readRawBytes() throws IOException {
    final FileInputStream fis = new FileInputStream("test");
    final FileChannel fc = fis.getChannel();
    ByteBuffer buffer = ByteBuffer.allocate(16);
    showStats("Newly allocated read", fc, buffer);
    // read up to 16Kb, -1 means eof
    byte[] receive = new byte[8];
    int readBytes;
    do {
      buffer.clear();
      readBytes = fc.read(buffer);
      if (readBytes > -1) {
        showStats("after read from channel", fc, buffer);
        buffer.flip();
        showStats("before read from buffer", fc, buffer);
        while (buffer.remaining() > 0) {
          final int len = Math.min(buffer.remaining(), receive.length);
          buffer.get(receive, 0, len);
          String readStr = len == receive.length ? new String(receive) : new String(Arrays.copyOf(receive, len));
          System.out.println("read = " + readStr);
        }
        showStats("after read from buffer", fc, buffer);
      }
    } while (readBytes > -1);
  }
}
