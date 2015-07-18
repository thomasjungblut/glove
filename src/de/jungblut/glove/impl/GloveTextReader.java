package de.jungblut.glove.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

import de.jungblut.glove.GloveStreamReader;
import de.jungblut.glove.util.StringVectorPair;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class GloveTextReader implements GloveStreamReader {

  private static final Pattern SPLIT_WHITESPACE = Pattern.compile(" ");

  @Override
  public Stream<StringVectorPair> stream(Path input) throws IOException {

    final Stream<String> lines = Files.lines(input);
    int[] expectedSize = new int[] { -1 };
    Stream<StringVectorPair> pairs = lines.map((line) -> process(line)).map(
        (pair) -> {
          Preconditions.checkNotNull(pair.value, "word was null");
          if (expectedSize[0] == -1) {
            expectedSize[0] = pair.vector.getDimension();
          } else {
            Preconditions.checkArgument(
                expectedSize[0] == pair.vector.getDimension(),
                "found inconsistency. Expected size " + expectedSize[0]
                    + " but found " + pair.vector.getDimension());
          }
          return pair;
        });

    pairs.onClose(() -> lines.close());

    return pairs;
  }

  private StringVectorPair process(String line) {
    String[] split = SPLIT_WHITESPACE.split(line);
    String name = split[0];

    DoubleVector vec = new DenseDoubleVector(split.length - 1);
    for (int i = 1; i < split.length; i++) {
      vec.set(i - 1, Double.parseDouble(split[i]));
    }

    return new StringVectorPair(name, vec);
  }
}
