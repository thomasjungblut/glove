package de.jungblut.glove.examples;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import de.jungblut.datastructure.KDTree;
import de.jungblut.datastructure.KDTree.VectorDistanceTuple;
import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.glove.impl.CachedGloveBinaryRandomAccessReader;
import de.jungblut.glove.impl.GloveBinaryRandomAccessReader;
import de.jungblut.glove.impl.GloveBinaryReader;
import de.jungblut.glove.util.StringVectorPair;
import de.jungblut.math.DoubleVector;

public class NearestNeighbourMain {

  public static void main(String[] args) throws IOException {

    if (args.length != 1) {
      System.err.println("first argument needs to be the binary glove folder");
      System.exit(1);
    }

    Path dir = Paths.get(args[0]);

    System.out.println("Reading...");
    GloveRandomAccessReader reader = new CachedGloveBinaryRandomAccessReader(
        new GloveBinaryRandomAccessReader(dir), 100l);
    final KDTree<String> tree = new KDTree<>();

    try (Stream<StringVectorPair> stream = new GloveBinaryReader().stream(dir)) {
      stream.forEach((pair) -> {
        tree.add(pair.vector, pair.value);
      });

    }

    System.out.println("Balancing the KD tree...");
    tree.balanceBySort();

    System.out
        .println("Finished, input your word to find its nearest neighbours");

    @SuppressWarnings("resource")
    Scanner scanner = new Scanner(System.in);

    while (true) {
      String nextLine = scanner.nextLine();

      if (nextLine.equals("q")) {
        return;
      }

      DoubleVector v = reader.get(nextLine);
      if (v == null) {
        System.err.println("doesn't exist");
      } else {
        System.out.print("Searching....");
        long start = System.currentTimeMillis();
        List<VectorDistanceTuple<String>> nearestNeighbours = tree
            .getNearestNeighbours(v, 6);

        // sort and remove the one that we searched for
        Collections.sort(nearestNeighbours, Collections.reverseOrder());
        // the best hit is usually the same item with distance 0
        if (nearestNeighbours.get(0).getValue().equals(nextLine)) {
          nearestNeighbours.remove(0);
        }

        System.out.println("done. Took " + (System.currentTimeMillis() - start)
            + " millis.");
        for (VectorDistanceTuple<String> tuple : nearestNeighbours) {
          System.out.println(tuple.getValue() + "\t" + tuple.getDistance());
        }
        System.out.println();
      }

    }

  }
}
