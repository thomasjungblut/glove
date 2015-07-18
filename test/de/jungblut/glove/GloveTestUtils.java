package de.jungblut.glove;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;

import de.jungblut.datastructure.ArrayJoiner;
import de.jungblut.glove.impl.GloveBinaryWriter;
import de.jungblut.glove.util.StringVectorPair;
import de.jungblut.math.dense.DenseDoubleVector;

public class GloveTestUtils {

  public static Path createTemporaryTestBinaryFile(int dimension, int numVectors)
      throws IOException {

    Stream<StringVectorPair> randomStream = GloveTestUtils.getWordVectorStream(
        dimension, numVectors);

    return writeStreamToBinaryFile(randomStream);
  }

  public static Path writeStreamToBinaryFile(
      Stream<StringVectorPair> randomStream) throws IOException {
    GloveBinaryWriter writer = new GloveBinaryWriter();
    Path outputFolder = GloveTestUtils.createTemporaryOutputFolder();

    writer.writeStream(randomStream, outputFolder);
    return outputFolder;
  }

  public static Path createTemporaryCorruptedTestBinaryFiles()
      throws IOException {
    Stream<StringVectorPair> input = IntStream.range(0, 5).mapToObj(
        (i) -> new StringVectorPair("" + i, new DenseDoubleVector(i)));
    return writeStreamToBinaryFile(input);
  }

  public static Path createTemporaryOutputFolder() throws IOException {
    Path tmp = Files.createTempDirectory("glove-test-dir");
    tmp.toFile().deleteOnExit();
    return tmp;
  }

  public static Path createTemporaryTestTextFile(int dimension)
      throws IOException {
    Stream<StringVectorPair> input = getWordVectorStream(dimension);
    return createTemporaryTestTextFileWithContent(input);
  }

  public static Path createTemporaryCorruptedTestTextFile() throws IOException {
    Stream<StringVectorPair> input = IntStream.range(0, 5).mapToObj(
        (i) -> new StringVectorPair("" + i, new DenseDoubleVector(i)));
    return createTemporaryTestTextFileWithContent(input);
  }

  public static Stream<StringVectorPair> getWordVectorStream(int dimension) {
    return getWordVectorStream(dimension, 15);
  }

  public static Stream<StringVectorPair> getWordVectorStream(int dimension,
      int numElements) {
    return IntStream.range(0, numElements).mapToObj((i) -> {
      DenseDoubleVector vec = new DenseDoubleVector(dimension);
      for (int x = 0; x < dimension; x++) {
        vec.set(x, x);
      }
      return new StringVectorPair("" + i, vec);
    });
  }

  public static Path createTemporaryTestTextFileWithContent(
      Stream<StringVectorPair> input) throws IOException {

    Path tmp = Files.createTempFile("glove-test", ".txt");
    Files.write(
        tmp,
        input.map(
            (pair) -> pair.value + " "
                + ArrayJoiner.on(" ").join(pair.vector.toArray())).collect(
            Collectors.toList()), Charset.forName("UTF-8"));

    tmp.toFile().deleteOnExit();

    return tmp;
  }

  public static void checkWordVectorResult(int dim, int numElements,
      List<StringVectorPair> list) {

    Assert.assertEquals(numElements, list.size());
    int start = 0;
    for (StringVectorPair v : list) {
      checkSingleWordVector(dim, start++, v);
    }
  }

  public static void checkSingleWordVector(int dim, int start,
      StringVectorPair v) {
    String name = start + "";
    Assert.assertEquals(name, v.value);
    Assert.assertEquals(dim, v.vector.getDimension());
    for (int i = 0; i < v.vector.getDimension(); i++) {
      Assert.assertEquals(i, (int) v.vector.get(i));
    }
  }

}
