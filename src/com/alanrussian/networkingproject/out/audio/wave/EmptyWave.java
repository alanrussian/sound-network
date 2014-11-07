package com.alanrussian.networkingproject.out.audio.wave;

/**
 * A wave that is empty (i.e., no sound).
 */
public class EmptyWave implements Wave {
  
  private static EmptyWave instance = null;

  private EmptyWave() {}

  public static EmptyWave getInstance() {
    if (instance == null) {
      instance = new EmptyWave();
    }
    
    return instance;
  }
  
  @Override
  public byte[] getData(int sampleRate, int duration) {
    return new byte[sampleRate / 1000 * duration];
  }
}
