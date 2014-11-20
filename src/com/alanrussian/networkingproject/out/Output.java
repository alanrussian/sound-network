package com.alanrussian.networkingproject.out;

import com.alanrussian.networkingproject.out.audio.AudioEncoder;

/**
 * Sends data that can be read by an {@link Input}.
 */
public class Output {
  
  private static Output instance;
  
  private final AudioEncoder encoder;
  
  private Output() {
    encoder = new AudioEncoder();
  }
  
  /**
   * Returns the instance of the Output class.
   */
  public static Output getInstance() {
    if (instance == null) {
      instance = new Output();
    }

    return instance;
  }

  public void sendData(byte[] data) {
    encoder.sendData(data);
  }
  
  public void sendAck() {
    encoder.sendAck();
  }
}
