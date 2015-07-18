package de.jungblut.glove.impl;

import gnu.trove.map.hash.TObjectLongHashMap;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;

import org.apache.hadoop.io.WritableUtils;

import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class GloveBinaryRandomAccessReader implements GloveRandomAccessReader {

  private final TObjectLongHashMap<String> dictMap = new TObjectLongHashMap<>();
  private RandomAccessFile raf;
  private long size;

  public GloveBinaryRandomAccessReader(Path gloveBinaryFolder)
      throws IOException {

    Path dict = gloveBinaryFolder.resolve(GloveBinaryWriter.DICT_FILE);
    Path vectors = gloveBinaryFolder.resolve(GloveBinaryWriter.VECTORS_FILE);

    initLookup(dict);
    initBufferedFile(vectors);
  }

  private void initBufferedFile(Path vectors) throws FileNotFoundException {
    raf = new RandomAccessFile(vectors.toFile(), "r");
  }

  private void initLookup(Path dict) throws IOException {
    try (DataInputStream in = new DataInputStream(new BufferedInputStream(
        new FileInputStream(dict.toFile())))) {

      long lastBlock = -1;
      size = -1;
      while (true) {
        String s = in.readUTF();
        long off = WritableUtils.readVLong(in);

        if (lastBlock == -1) {
          lastBlock = off;
        } else {
          if (size == -1) {
            size = off;
          }
          if (off - lastBlock != size) {
            throw new IOException(
                "Dictionary is corrupted, blocking isn't exact. Expected blocks of "
                    + size);
          }

          lastBlock = off;
        }

        dictMap.put(s, off);

      }

    } catch (EOFException e) {
      // expected
    }
  }

  @Override
  public boolean contains(String word) {
    return dictMap.containsKey(word);
  }

  @Override
  public DoubleVector get(String word) throws IOException {

    if (!contains(word)) {
      return null;
    }

    long offset = dictMap.get(word);

    // page the block in, read from it and wrap as a vector
    MappedByteBuffer buf = raf.getChannel()
        .map(MapMode.READ_ONLY, offset, size);

    return parse(buf);
  }

  private DoubleVector parse(MappedByteBuffer buf) {
    int dim = (int) (size / 4);
    DoubleVector v = new DenseDoubleVector(dim);

    for (int i = 0; i < v.getDimension(); i++) {
      int n = buf.getInt();
      v.set(i, Float.intBitsToFloat(n));
    }

    return v;
  }
}
