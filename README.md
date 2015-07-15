This project is a convenience Java wrapper around GloVe word vectors and converter to more space efficient binary files, which also includes a random access lookup for very large amount of vectors on disk.

Caution: There isn't any unit tests yet.


Examples of use
===================

Build the library first, you'll need the fat jar (glove*-shaded.jar) in the next sections.


Converting the text files to binary
-----------------------------------

To use the power of the library and save some disk space you should rewrite the file to binary first.
This can be done by supplying the text file and an output folder:

> java -cp glove-0.1-SNAPSHOT-shaded.jar de.jungblut.glove.examples.TextToBinaryConverterMain glove-vectors.txt glove-binary

Now you should have a "glove-binary" folder with two files in it, a smaller "dict.bin" and a bigger "vectors.bin".


Using it for random access reads
-----------------------------------

You can use the GloveRandomAccessReader to get the vector for a string fast and without loading all the vectors into memory.

Using my math library it is also easy to do the typical vector computations.

```java

    GloveRandomAccessReader db = new GloveBinaryRandomAccessReader(
        Paths.get("glove-binary"));

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

```

You can execute the above using

> java -cp glove-0.1-SNAPSHOT-shaded.jar de.jungblut.glove.examples.VectorLookupMain glove-binary

Output is:

```
dist queen = 0.24690873337939978
dist royal = 0.30120073399624914

```

Nearest Neighbour Queries
-------------------------

You can also do efficient nearest neighbour queries using a KD-Tree. The full code can be found in de.jungblut.glove.examples.NearestNeighbourMain.

You can also run it with an "interactive" menu like this:

> java -cp glove-0.1-SNAPSHOT-shaded.jar de.jungblut.glove.examples.NearestNeighbourMain glove-binary

Keep in mind that this takes up quite some memory since the KD-Tree needs some space, but the queries are fast.

Some example output on the small twitter file:

```
Reading...
Balancing the KD tree...
Finished, input your word to find its nearest neighbours
rt
Searching....done. Took 850 millis.
<user>  1.3282014981649486
:       2.0140673085358696
?       2.439117425083601
---     2.441400270469679
"       2.45020842228818

yolo
Searching....done. Took 758 millis.
wtf     2.1469371352219953
swag    2.1752410454311986
lolz    2.2263784996001705
loser   2.2666981122295806
ew      2.308925723645761

```


Binary File Layout
==================

Dictionary
----------

The dictionary writes (per string-vector pair): 
 - UTF-8 string
 - vlong offset of where the start of the vector is in the vector file
 
 
Vectors
-------  

The vector file contains (per string-vector pair):
 - the vector content encoded as a sequence of 4 byte floats 
 - each float is encoded using Float.floatToIntBits


License
===================

Since I am Apache committer, I consider everything inside of this repository 
licensed by Apache 2.0 license, although I haven't put the usual header into the source files.

If something is not licensed via Apache 2.0, there is a reference or an additional licence header included in the specific source file.


Build
===================

To build locally, you will need at least Java 8 to build this library.

You can simply build with:
 
> mvn clean package install

The created jars contains debuggable code + sources + javadocs.

If you want to skip testcases you can use:

> mvn clean package install -DskipTests
