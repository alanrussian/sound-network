package com.alanrussian.networkingproject.out.audio;

import java.util.ArrayList;
import java.util.List;

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
  
  private final List<byte[]> waveBytes;
  
  private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
        Constants.SAMPLE_RATE,
        8 /* sampleSizeInBits */,
        1, /* channels */
        true, /* signed */ 
        true /* bigEndian */);
  
  private final SourceDataLine line;
  
  public AudioPlayer() throws LineUnavailableException {
    this.waveBytes = new ArrayList<>();

    line = AudioSystem.getSourceDataLine(AUDIO_FORMAT);
    line.open(AUDIO_FORMAT, Constants.SAMPLE_RATE);
    line.start();
  }
  
  /**
   * Plays all waves that have been queued up.
   */
  public void play() throws LineUnavailableException {
    for (byte[] data : waveBytes) {
      line.write(data, 0, data.length);
    }

    line.drain();
    
    waveBytes.clear();
  }
  
  /**
   * Adds a {@link Wave} to the play queue.
   */
  public void add(Wave wave, int duration) {
    waveBytes.add(wave.getData(Constants.SAMPLE_RATE, duration));
  }
}
