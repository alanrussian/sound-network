package com.alanrussian.networkingproject.out.audio;

import java.util.Arrays;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.out.audio.wave.SineWave;

/**
 * Encodes and sends data over audio.
 */
public class AudioEncoder {
  
  private final SineWave sineWave;
  
  public AudioEncoder() {
    this.sineWave = new SineWave(Constants.FREQUENCY);
  }
  
  /**
   * Sends {@code data} over audio.
   */
  public void send(byte[] data) {
    try {
      int offset = 0;
      
      while (offset < data.length) {
        int length = Math.min(Constants.AUDIO_FRAME_MAX_DATA_LENGTH, data.length - offset + 1);
        
        AudioFrame frame =
            new AudioFrame(sineWave, Arrays.copyOfRange(data, offset, offset + length));
        frame.send();
        
        offset += length;
      }
    } catch (Exception e) {
      // TODO: Handle error.
      e.printStackTrace();
    }
  }
}
