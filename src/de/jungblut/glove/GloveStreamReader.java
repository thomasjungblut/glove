package de.jungblut.glove;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import de.jungblut.glove.util.StringVectorPair;

public interface GloveStreamReader {

  /**
   * Streams over the glove file/directory in the given path.
   * 
   * @param input the path to the glove files or directory (defined by the
   *          implementation).
   * @return a lazy evaluated stream of the glove file.
   * @throws IOException file not found, or io errors.
   */
  public Stream<StringVectorPair> stream(Path input) throws IOException;

}
