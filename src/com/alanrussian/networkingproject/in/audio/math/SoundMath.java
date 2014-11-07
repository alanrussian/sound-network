package com.alanrussian.networkingproject.in.audio.math;

import org.jtransforms.fft.DoubleFFT_1D;

/**
 * Various math functions for interpreting sound.
 */
public class SoundMath {

  /**
   * Computes the absolute magnitude of a frequency.
   * 
   * @see http://stackoverflow.com/a/7675171
   */
  public static double getMagnitudeOfFrequency(
      double frequency,
      byte[] soundData,
      int sampleRate) {
    
    int dataLength = soundData.length;
    double[] data = new double[dataLength * 2];

    for (int i = 0; i < dataLength; i++) {
      data[i] = (double) soundData[i];
    }
    
    DoubleFFT_1D transformation = new DoubleFFT_1D(dataLength);
    transformation.realForwardFull(data);
    
    int indexOfFrequency = (int) (frequency * (double) dataLength / (double) sampleRate);
    
    double real = data[2 * indexOfFrequency];
    double imaginary = data[2 * indexOfFrequency + 1];
    double magnitude = Math.sqrt(Math.pow(real, 2) + Math.pow(imaginary, 2));
    
    return magnitude;
  }
}
