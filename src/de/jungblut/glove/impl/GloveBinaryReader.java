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

import com.google.common.base.Preconditions;
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
    long currentOffset = -1;
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
        long off = WritableUtils.readVLong(dict);
        Preconditions.checkArgument(off >= 0,
            "Offset was negative! Dictionary seems corrupted.");
        if (blockSize == -1) {
          String word2 = dict.readUTF();
          long off2 = WritableUtils.readVLong(dict);
          blockSize = off2;
          second = word2;
          currentOffset = off2;
        } else {
          // check block size consistency
          Preconditions.checkArgument((currentOffset + blockSize) == off,
              String.format(
                  "Can't read different block sizes! Expected %d but was %d.",
                  blockSize, off - currentOffset));
          currentOffset = off;
        }

        return new StringVectorPair(word, readVec());

      } catch (EOFException e) {
        // expected eod from the dictionary
        try {
          // check if the vector file has some bytes we were missing
          Preconditions
              .checkArgument(
                  vec.read() == -1,
                  "Vector file has more bytes than expected, dictionary seems inconsistent to the vector file");
        } catch (IOException e1) {
          // expect errors here for checking stuff
        }
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
        try {
          int read = vec.read(buf);
          Preconditions
              .checkArgument(read == 4,
                  "Couldn't read the next four bytes from the file, vector file seems truncated");
        } catch (EOFException e) {
          throw new IOException(
              "Unexpected end of file found while reading a vector of size "
                  + dim);
        }
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
