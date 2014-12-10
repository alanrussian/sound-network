package com.alanrussian.networkingproject.out;

import java.util.HashMap;
import java.util.Map;

import com.alanrussian.networkingproject.out.audio.AudioEncoder;

/**
 * Sends data that can be read by an {@link Input}.
 */
public class Output {
  
  private static Map<Integer, Output> computerIdsToInstance = new HashMap<>();
  
  private final AudioEncoder encoder;
  
  private Output(int computerId) {
    encoder = new AudioEncoder(computerId);
  }
  
  /**
   * Returns the instance of the Output class.
   */
  public static Output getInstance(int computerId) {
    if (!computerIdsToInstance.containsKey(computerId)) {
      computerIdsToInstance.put(computerId, new Output(computerId));
    }

    return computerIdsToInstance.get(computerId);
  }

  public void sendData(byte[] data) {
    encoder.sendData(data);
  }
  
  public void sendAck() {
    encoder.sendAck();
  }
}
