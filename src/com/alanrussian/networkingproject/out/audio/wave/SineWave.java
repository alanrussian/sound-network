package com.alanrussian.networkingproject.out.audio.wave;

/**
 * Generates a sine wave at a given frequency and sample rate.
 */
public class SineWave implements Wave {
  
  private final double frequency;
  
  public SineWave(double frequency) {
    this.frequency = frequency;
  }
  
  @Override
  public byte[] getData(int sampleRate, int duration) {
    double angularFrequency = 2.0 * Math.PI * frequency;
    double indexMultiplier = angularFrequency / sampleRate;
    
    byte[] data = new byte[sampleRate / 1000 * duration];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) (Math.sin(indexMultiplier * i) * 127.0);
    }
    
    return data;
  }
}
