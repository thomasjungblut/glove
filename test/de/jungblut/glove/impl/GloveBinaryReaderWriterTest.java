package de.jungblut.glove.impl;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.hadoop.io.WritableUtils;
import org.junit.Test;

import de.jungblut.glove.GloveTestUtils;
import de.jungblut.glove.util.StringVectorPair;

public class GloveBinaryReaderWriterTest {

  @Test
  public void testNormalFileWritingAndReading() throws IOException {

    int dim = 10;
    int numElements = 100;
    Path folder = GloveTestUtils
        .createTemporaryTestBinaryFile(dim, numElements);
    GloveBinaryReader reader = new GloveBinaryReader();
    List<StringVectorPair> list = reader.stream(folder).collect(
        Collectors.toList());

    GloveTestUtils.checkWordVectorResult(dim, numElements, list);

  }

  @Test(expected = IllegalArgumentException.class)
  public void testWritingCorruptedStream() throws IOException {
    GloveTestUtils.createTemporaryCorruptedTestBinaryFiles();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReadingCorruptedFileTooSmall() throws IOException {
    int dim = 10;
    int numElements = 100;
    Path folder = GloveTestUtils
        .createTemporaryTestBinaryFile(dim, numElements);
    corruptVectorFile(folder, 2048);

    GloveBinaryReader reader = new GloveBinaryReader();
    reader.stream(folder).forEach((v) -> v.vector.sum());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReadingCorruptedFileTooBig() throws IOException {
    int dim = 10;
    int numElements = 100;
    Path folder = GloveTestUtils
        .createTemporaryTestBinaryFile(dim, numElements);
    corruptVectorFile(folder, 1024 * 512);

    GloveBinaryReader reader = new GloveBinaryReader();
    reader.stream(folder).forEach((v) -> v.vector.sum());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReadingCorruptedDictionaryDifferentNumElements()
      throws IOException {
    int dim = 10;
    int numElements = 100;
    Path folder = GloveTestUtils
        .createTemporaryTestBinaryFile(dim, numElements);
    corruptDictionaryFile(folder, dim, 50, false, false);
    GloveBinaryReader reader = new GloveBinaryReader();
    reader.stream(folder).forEach((v) -> v.vector.sum());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReadingCorruptedDictionaryNegativeOffset() throws IOException {
    int dim = 10;
    int numElements = 100;
    Path folder = GloveTestUtils
        .createTemporaryTestBinaryFile(dim, numElements);
    corruptDictionaryFile(folder, dim, numElements, true, true);
    GloveBinaryReader reader = new GloveBinaryReader();
    reader.stream(folder).forEach((v) -> v.vector.sum());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testReadingCorruptedDictionaryCorruptedOffset()
      throws IOException {
    int dim = 10;
    int numElements = 100;
    Path folder = GloveTestUtils
        .createTemporaryTestBinaryFile(dim, numElements);
    corruptDictionaryFile(folder, dim, numElements, true, false);
    GloveBinaryReader reader = new GloveBinaryReader();
    reader.stream(folder).forEach((v) -> v.vector.sum());
  }

  private void corruptDictionaryFile(Path folder, int dim, int items,
      boolean corruptOffsets, boolean negativeOffset) throws IOException {
    try (DataOutputStream dict = new DataOutputStream(new BufferedOutputStream(
        new FileOutputStream(folder.resolve(GloveBinaryWriter.DICT_FILE)
            .toFile())))) {
      for (int i = 0; i < items; i++) {
        dict.writeUTF(i + "");
        long off = i * dim * 4;
        if (corruptOffsets) {
          if (negativeOffset) {
            off = -115;
          } else {
            off = off + 2;
          }
        }
        WritableUtils.writeVLong(dict, off);
      }
    }

  }

  public void corruptVectorFile(Path folder, int size) throws IOException {
    // corrupt the binary file
    try (BufferedOutputStream vec = new BufferedOutputStream(
        new FileOutputStream(folder.resolve(GloveBinaryWriter.VECTORS_FILE)
            .toFile()))) {

      // write some random garbage
      byte[] buf = new byte[size];
      Random r = new Random();
      for (int i = 0; i < buf.length; i++) {
        buf[i] = (byte) r.nextInt(Byte.MAX_VALUE);
      }
      vec.write(buf);

    }
  }

}
