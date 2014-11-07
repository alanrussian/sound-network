package com.alanrussian.networkingproject.out.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.out.audio.wave.Wave;

/**
 * Plays different waves.
 */
public class AudioPlayer {
  
  private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
        Constants.SAMPLE_RATE,
        8 /* sampleSizeInBits */,
        1, /* channels */
        true, /* signed */ 
        true /* bigEndian */);
  
  private final SourceDataLine line;
  
  public AudioPlayer() throws LineUnavailableException {
    line = AudioSystem.getSourceDataLine(AUDIO_FORMAT);
    line.open(AUDIO_FORMAT, Constants.SAMPLE_RATE);
    line.start();
  }
  
  /**
   * Plays all waves that have been queued up.
   */
  public void play() throws LineUnavailableException {
    // TODO: Make threaded.
    line.drain();
  }
  
  /**
   * Adds a {@link Wave} to the play queue.
   */
  public void add(Wave wave, int duration) {
    byte[] data = wave.getData(Constants.SAMPLE_RATE, duration);

    line.write(data, 0, data.length);
  }
}
