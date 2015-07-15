package de.jungblut.glove.impl;

import java.io.IOException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.math.DoubleVector;

public class CachedGloveBinaryRandomAccessReader implements
    GloveRandomAccessReader {

  private final GloveRandomAccessReader reader;
  private Cache<String, DoubleVector> cache;

  public CachedGloveBinaryRandomAccessReader(GloveRandomAccessReader reader,
      long maxCacheSize) {
    this.reader = reader;
    this.cache = CacheBuilder.newBuilder().maximumSize(maxCacheSize).build();
  }

  @Override
  public boolean contains(String word) {
    return reader.contains(word);
  }

  @Override
  public DoubleVector get(String word) throws IOException {

    DoubleVector ret = cache.getIfPresent(word);

    if (ret == null) {
      if (reader.contains(word)) {
        ret = reader.get(word);
        cache.put(word, ret);
      }
    }

    return ret;
  }

}
