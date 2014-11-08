package com.alanrussian.networkingproject.in.audio.math;

import org.jtransforms.fft.DoubleFFT_1D;

/**
 * Various math functions for interpreting sound.
 */
public class SoundMath {
  
  /**
   * Does an FFT on data.
   */
  public static double[] applyFft(byte[] soundData) {
    int dataLength = soundData.length;
    double[] data = new double[dataLength * 2];

    for (int i = 0; i < dataLength; i++) {
      data[i] = (double) soundData[i];
    }
    
    DoubleFFT_1D transformation = new DoubleFFT_1D(dataLength);
    transformation.realForwardFull(data);
    
    return data;
  }

  /**
   * Computes the absolute magnitude of a frequency.
   * 
   * @see http://stackoverflow.com/a/7675171
   */
  public static double getMagnitudeOfFrequency(
      double frequency,
      double[] transformedData,
      int sampleRate) {
    
    int originalDataLength = transformedData.length / 2;
    
    int indexOfFrequency = (int) (frequency * (double) originalDataLength / (double) sampleRate);
    
    double real = transformedData[2 * indexOfFrequency];
    double imaginary = transformedData[2 * indexOfFrequency + 1];
    double magnitude = Math.sqrt(Math.pow(real, 2) + Math.pow(imaginary, 2));
    
    return magnitude;
  }
}
