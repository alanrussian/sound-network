package com.alanrussian.networkingproject.out.audio;

import java.util.Arrays;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.out.audio.wave.MixedWave;
import com.alanrussian.networkingproject.out.audio.wave.SineWave;
import com.alanrussian.networkingproject.out.audio.wave.Wave;
import com.google.common.collect.ImmutableList;

/**
 * Encodes and sends data over audio.
 */
public class AudioEncoder {
  
  private final Wave waveOff;
  private final Wave waveOn;
  
  public AudioEncoder() {
    this.waveOff = new MixedWave(ImmutableList.of(
        new SineWave(Constants.FREQUENCY_OFF),
        new SineWave(Constants.FREQUENCY_OFF + Constants.FREQUENCY_SECOND_OFFSET)));
    this.waveOn = new MixedWave(ImmutableList.of(
        new SineWave(Constants.FREQUENCY_ON),
        new SineWave(Constants.FREQUENCY_ON + Constants.FREQUENCY_SECOND_OFFSET)));
  }
  
  /**
   * Sends {@code data} over audio.
   */
  public void send(byte[] data) {
    try {
      int offset = 0;
      
      while (offset < data.length) {
        int length = Math.min(Constants.AUDIO_FRAME_MAX_DATA_LENGTH, data.length - offset + 1);
        
        AudioFrame frame = new AudioFrame(
            waveOff,
            waveOn,
            Arrays.copyOfRange(data, offset, offset + length));
        frame.send();
        
        offset += length;
      }
    } catch (Exception e) {
      // TODO: Handle error.
      e.printStackTrace();
    }
  }
}
