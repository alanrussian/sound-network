package com.alanrussian.networkingproject.in.audio.math;

import java.util.LinkedList;

/**
 * Efficiently computes a running average of a given number of doubles in constant time independent
 * on the given number.
 */
public class RunningAverage {
  
  private final int maximumNumbers;
  private final LinkedList<Double> numbers;

  private double average;

  public RunningAverage(int maximumNumbers) {
    this.maximumNumbers = maximumNumbers;
    this.numbers = new LinkedList<>();
    
    average = 0.0;
  }
  
  /**
   * Returns whether the desired number of doubles has been collected yet.
   */
  public boolean haveAverage() {
    return numbers.size() == maximumNumbers;
  }
  
  /**
   * Adds a number to the running average. Pops off the oldest number if all doubles have been
   * collected.
   */
  public void add(double number) {
    int numbersSize = numbers.size();

    if (numbersSize == maximumNumbers) {
      double removedNumber = numbers.removeFirst();
      average = (average * numbersSize - removedNumber) / (numbersSize - 1);
      numbersSize--;
    }
    
    numbers.addLast(number);
    average = (average * numbersSize + number) / (numbersSize + 1);
  }
  
  /**
   * Clears the current numbers.
   */
  public void clear() {
    numbers.clear();
    average = 0.0;
  }
  
  /**
   * Returns the average of the numbers.
   *
   * @throws IllegalStateException if no numbers are present
   */
  public double getAverage() {
    if (numbers.isEmpty()) {
      throw new IllegalStateException();
    }
    
    return average;
  }
  
  /**
   * Returns the standard deviation of the numbers.
   */
  public double getStandardDeviation() {
    // TODO: Compute in constant time.
    double squaredDifferences = 0.0;
    for (double number : numbers) {
      squaredDifferences += Math.pow(number - average, 2);
    }
    
    return Math.sqrt(squaredDifferences / (double) numbers.size());
  }
}
