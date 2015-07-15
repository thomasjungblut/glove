package de.jungblut.glove.examples;

import java.io.IOException;
import java.nio.file.Paths;

import de.jungblut.distance.CosineDistance;
import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.glove.impl.GloveBinaryRandomAccessReader;
import de.jungblut.math.DoubleVector;

public class VectorLookupMain {

  public static void main(String[] args) throws IOException {

    if (args.length != 1) {
      System.err
          .println("only argument should be the path to the binary glove folder");
      System.exit(1);
    }

    GloveRandomAccessReader db = new GloveBinaryRandomAccessReader(
        Paths.get(args[0]));

    DoubleVector king = db.get("king");
    DoubleVector man = db.get("man");

    DoubleVector queen = db.get("queen");
    DoubleVector woman = db.get("woman");

    CosineDistance cos = new CosineDistance();

    DoubleVector diff = king.subtract(man).add(woman);

    double dist = cos.measureDistance(diff, queen);
    System.out.println("dist queen = " + dist);

    dist = cos.measureDistance(diff, db.get("royal"));
    System.out.println("dist royal = " + dist);

  }

}
