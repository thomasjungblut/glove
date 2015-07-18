package de.jungblut.glove.impl;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

import de.jungblut.glove.GloveTestUtils;
import de.jungblut.glove.util.StringVectorPair;
import de.jungblut.math.DoubleVector;

public class GloveBinaryRandomAccessReaderTest {

  @Test
  public void testNormalRandomReading() throws IOException {
    int dim = 10;
    int numElements = 100;
    Path folder = GloveTestUtils
        .createTemporaryTestBinaryFile(dim, numElements);

    GloveBinaryRandomAccessReader reader = new GloveBinaryRandomAccessReader(
        folder);

    for (int i = 0; i < numElements; i++) {
      String s = i + "";
      Assert.assertTrue("didn't contain word=" + s, reader.contains(s));
      DoubleVector vec = reader.get(s);
      Assert.assertNotNull("vector null, despite contains returned true! word="
          + s, vec);

      GloveTestUtils
          .checkSingleWordVector(dim, i, new StringVectorPair(s, vec));

    }

  }

  @Test
  public void testNotContainedValues() throws IOException {
    int dim = 10;
    int numElements = 100;
    Path folder = GloveTestUtils
        .createTemporaryTestBinaryFile(dim, numElements);

    GloveBinaryRandomAccessReader reader = new GloveBinaryRandomAccessReader(
        folder);

    Assert.assertNull("contained lolol", reader.get("lolol"));
    Assert.assertNull("contained omg", reader.get("omg"));
    Assert.assertNull("contained 101", reader.get("101"));
    Assert.assertNull("contained -1", reader.get("-1"));
  }

}
