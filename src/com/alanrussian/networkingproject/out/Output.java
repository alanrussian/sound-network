package com.alanrussian.networkingproject.out;

import com.alanrussian.networkingproject.out.audio.AudioEncoder;

/**
 * Sends data that can be read by an {@link Input}.
 */
public class Output {
  
  private final AudioEncoder encoder;
  
  public Output() {
    encoder = new AudioEncoder();
  }

  public void send(byte[] data) {
    encoder.send(data);
  }
}
