package de.jungblut.glove;

import java.io.IOException;

import de.jungblut.math.DoubleVector;

public interface GloveRandomAccessReader {

  /**
   * @return true if the glove reader contains this word.
   */
  public boolean contains(String word);

  /**
   * @return the word or null if it doesn't exists.
   */
  public DoubleVector get(String word) throws IOException;

}
