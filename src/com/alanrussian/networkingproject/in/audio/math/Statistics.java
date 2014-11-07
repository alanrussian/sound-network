package com.alanrussian.networkingproject.in.audio.math;

/**
 * Various statistics functions.
 */
public class Statistics {

  /**
   * Returns whether {@code number} is within {@code average} +/- {@code deviations} {@code
   * standardDeviation}s.
   */
  public static boolean isWithinAverage(
      double number,
      double average,
      double standardDeviation,
      int deviations) {
    
    return Math.abs(number - average) <= (standardDeviation * deviations);
  }
}
