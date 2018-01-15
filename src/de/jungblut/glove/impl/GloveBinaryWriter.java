package de.jungblut.glove.impl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

import de.jungblut.glove.GloveWriter;
import de.jungblut.glove.util.StringVectorPair;
import de.jungblut.glove.util.WritableUtils;
import de.jungblut.math.DoubleVector;

public class GloveBinaryWriter implements GloveWriter {

  public static final String VECTORS_FILE = "vectors.bin";
  public static final String DICT_FILE = "dict.bin";

  @Override
  public void writeStream(Stream<StringVectorPair> stream, Path outputFolder)
      throws IOException {

    Files.createDirectories(outputFolder);

    try (DataOutputStream dict = new DataOutputStream(new BufferedOutputStream(
        new FileOutputStream(outputFolder.resolve(DICT_FILE).toFile())))) {

      try (BufferedOutputStream vec = new BufferedOutputStream(
          new FileOutputStream(outputFolder.resolve(VECTORS_FILE).toFile()))) {

        Iterator<StringVectorPair> iterator = stream.iterator();

        long blockSize = -1;
        long offset = 0;
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        while (iterator.hasNext()) {
          byteBuffer.reset();
          StringVectorPair pair = iterator.next();
          dict.writeUTF(pair.value);
          WritableUtils.writeVLong(dict, offset);

          try (DataOutputStream out = new DataOutputStream(byteBuffer)) {
            writeVectorData(pair.vector, out);
          }

          byte[] buf = byteBuffer.toByteArray();
          if (blockSize == -1) {
            blockSize = buf.length;
          }

          if (blockSize != buf.length) {
            Preconditions
                .checkArgument(
                    blockSize == buf.length,
                    String
                        .format(
                            "Can't write different block size! Expected %d but was %d. "
                                + "This happened because the vectors in the stream had different dimensions.",
                            blockSize, buf.length));
          }

          vec.write(buf);

          offset += buf.length;
        }
      }
    }
  }

  private void writeVectorData(DoubleVector v, DataOutput out)
      throws IOException {

    for (int i = 0; i < v.getDimension(); i++) {
      float f = (float) v.get(i);
      int var = Float.floatToIntBits(f);
      out.writeInt(var);
    }

  }
}
