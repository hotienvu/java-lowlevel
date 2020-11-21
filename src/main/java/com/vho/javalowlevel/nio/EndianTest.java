package com.vho.javalowlevel.nio;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class EndianTest {
  public static void main(String[] args) throws IOException {
    byte[] data = new byte[4];
    FileInputStream fos = new FileInputStream("src/main/java/com/vho/javalowlevel/nio/example.dat");
    if (fos.read(data) < 4) {
      throw new IOException("Failed to read data");
    }
    ByteBuffer buffer = ByteBuffer.wrap(data);
    System.out.println(buffer.hasArray() + " " + buffer.arrayOffset() + " " + Arrays.toString(buffer.array()));
    int orig = 0x12345678;
    System.out.println("orig                 = " + orig);
    System.out.println("data                 = " + buffer.getInt(0));
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    System.out.println("data (little endian) = " + buffer.getInt(0));
    while (true) {

    }
  }
}
