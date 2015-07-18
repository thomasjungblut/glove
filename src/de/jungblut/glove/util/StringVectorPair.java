package de.jungblut.glove.util;

import de.jungblut.math.DoubleVector;

public class StringVectorPair {

  public final String value;
  public final DoubleVector vector;

  public StringVectorPair(String value, DoubleVector vector) {
    this.value = value;
    this.vector = vector;
  }

  @Override
  public String toString() {
    return "StringVectorPair [word=" + value + ", vector=" + vector + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StringVectorPair other = (StringVectorPair) obj;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

}
