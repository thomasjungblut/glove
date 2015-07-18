package de.jungblut.glove.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import de.jungblut.glove.GloveTestUtils;
import de.jungblut.glove.util.StringVectorPair;

public class GloveTextReaderTest {

  @Test
  public void testNormalFile() throws IOException {
    final int dim = 5;
    Path in = GloveTestUtils.createTemporaryTestTextFile(5);

    GloveTextReader reader = new GloveTextReader();
    Stream<StringVectorPair> stream = reader.stream(in);
    List<StringVectorPair> collected = stream.collect(Collectors.toList());

    GloveTestUtils.checkWordVectorResult(dim, 15, collected);
  }

  @Test(expected = IllegalArgumentException.class)
  public void readBlowsUpOnCorruption() throws IOException {
    Path in = GloveTestUtils.createTemporaryCorruptedTestTextFile();
    GloveTextReader reader = new GloveTextReader();
    Stream<StringVectorPair> stream = reader.stream(in);
    stream.forEach((v) -> System.out.println(v));
  }
}
