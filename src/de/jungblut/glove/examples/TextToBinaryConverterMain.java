package de.jungblut.glove.examples;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;

import de.jungblut.glove.impl.GloveBinaryWriter;
import de.jungblut.glove.impl.GloveTextReader;
import de.jungblut.glove.util.StringVectorPair;

public class TextToBinaryConverterMain {

  public static void main(String[] args) throws IOException {

    if (args.length != 2) {
      System.err
          .println("first argument needs to be the glove text file, the second needs to be the output folder of the binary files.");
      System.exit(1);
    }

    GloveTextReader reader = new GloveTextReader();
    Stream<StringVectorPair> stream = reader.stream(Paths.get(args[0]));
    GloveBinaryWriter writer = new GloveBinaryWriter();
    writer.writeStream(stream, Paths.get(args[1]));

  }

}
