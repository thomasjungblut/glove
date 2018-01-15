package de.jungblut.glove.util;

import java.io.IOException;


/**
 * An utility class for I/O related functionality.
 */
/* Taken from org.apache.hadoop.commons and modified */
public class IOUtils {


  /**
   * Close the Closeable objects and <b>ignore</b> any {@link IOException} or null pointers. Must
   * only be used for cleanup in exception handlers.
   *
   * @param closeables the objects to close
   */
  public static void cleanup(java.io.Closeable... closeables) {
    for (java.io.Closeable c : closeables) {
      if (c != null) {
        try {
          c.close();
        } catch (IOException e) {
          System.err.println("Exception in closing " + c);
          System.err.println(e.getMessage());
        }
      }
    }
  }

}
