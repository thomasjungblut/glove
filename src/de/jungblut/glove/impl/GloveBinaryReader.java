package de.jungblut.glove.impl;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.WritableUtils;

import com.google.common.collect.AbstractIterator;

import de.jungblut.glove.GloveStreamReader;
import de.jungblut.glove.util.StringVectorPair;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class GloveBinaryReader implements GloveStreamReader {

  @Override
  public Stream<StringVectorPair> stream(Path gloveBinaryFolder)
      throws IOException {

    Path dict = gloveBinaryFolder.resolve(GloveBinaryWriter.DICT_FILE);
    Path vectors = gloveBinaryFolder.resolve(GloveBinaryWriter.VECTORS_FILE);

    final DataInputStream in = new DataInputStream(new BufferedInputStream(
        new FileInputStream(dict.toFile())));
    final BufferedInputStream vec = new BufferedInputStream(
        new FileInputStream(vectors.toFile()));

    FilesIterator filesIterator = new FilesIterator(in, vec);

    Stream<StringVectorPair> stream = StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(filesIterator,
            Spliterator.ORDERED), false);

    stream.onClose(() -> {
      IOUtils.closeStream(filesIterator);
    });

    return stream;
  }

  private class FilesIterator extends AbstractIterator<StringVectorPair>
      implements Closeable {

    final DataInputStream dict;
    final BufferedInputStream vec;

    long blockSize = -1;
    String second = null;

    public FilesIterator(DataInputStream dict, BufferedInputStream vec) {
      this.dict = dict;
      this.vec = vec;
    }

    @Override
    protected StringVectorPair computeNext() {
      try {

        if (second != null) {
          String tmp = second;
          second = null;
          return new StringVectorPair(tmp, readVec());
        }

        String word = dict.readUTF();
        @SuppressWarnings("unused")
        long off = WritableUtils.readVLong(dict);
        if (blockSize == -1) {
          String word2 = dict.readUTF();
          long off2 = WritableUtils.readVLong(dict);
          blockSize = off2;
          second = word2;
        }

        return new StringVectorPair(word, readVec());

      } catch (EOFException e) {
        // expected eod
        return endOfData();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private DoubleVector readVec() throws IOException {
      int dim = (int) (blockSize / 4);
      DoubleVector v = new DenseDoubleVector(dim);

      byte[] buf = new byte[4];
      for (int i = 0; i < v.getDimension(); i++) {
        vec.read(buf);
        int n = ByteBuffer.wrap(buf).getInt();
        v.set(i, Float.intBitsToFloat(n));
      }

      return v;
    }

    @Override
    public void close() throws IOException {
      IOUtils.closeStream(dict);
      IOUtils.closeStream(vec);
    }

  }

}
