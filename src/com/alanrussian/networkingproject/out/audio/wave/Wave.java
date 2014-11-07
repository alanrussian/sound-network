package com.alanrussian.networkingproject.out.audio.wave;

/**
 * A type of audio wave. Allows you to get wave data at a given sample rate for a given amount of
 * time (see {@link #getData}).
 */
public interface Wave {

  /**
   * Gets the audio data of a wave given a sample rate for the given duration.
   */
  public byte[] getData(int sampleRate, int duration);
}
