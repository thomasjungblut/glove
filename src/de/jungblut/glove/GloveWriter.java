package de.jungblut.glove;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import de.jungblut.glove.util.StringVectorPair;

public interface GloveWriter {

  /**
   * A writer for a stream of StringVectorPairs. This is mainly used to rewrite
   * text to binary files or vice versa.
   * 
   * @param stream the stream of elements to write.
   * @param output depending on the implementation, either a file or a folder.
   * @throws IOException file/directory doesn't exist, isn't writable, or other
   *           io errors.
   */
  public void writeStream(Stream<StringVectorPair> stream, Path output)
      throws IOException;

}
