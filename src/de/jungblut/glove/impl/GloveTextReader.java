package de.jungblut.glove.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.jungblut.glove.GloveStreamReader;
import de.jungblut.glove.util.StringVectorPair;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

public class GloveTextReader implements GloveStreamReader {

  private static final Pattern SPLIT_WHITESPACE = Pattern.compile(" ");

  @Override
  public Stream<StringVectorPair> stream(Path input) throws IOException {
    return Files.lines(input).map((line) -> process(line));
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
