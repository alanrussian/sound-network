package com.alanrussian.networkingproject.out;

import java.util.HashMap;
import java.util.Map;

import com.alanrussian.networkingproject.out.audio.AudioEncoder;
import com.google.common.base.Preconditions;

/**
 * Sends data that can be read by an {@link Input}.
 */
public class Output {
  
  private static Map<Integer, Output> computerIdsToInstance = new HashMap<>();
  
  private final int computerId;
  private final AudioEncoder encoder;
  
  private Output(int computerId) {
    this.computerId = computerId;
    this.encoder = new AudioEncoder(computerId);
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

  /**
   * Sends some {@code data} to {@code target}.
   * 
   * @throws IllegalArgumentException if the target is the same as this device
   */
  public void sendData(int target, byte[] data) {
    Preconditions.checkArgument(target != computerId, "Cannot send message to self.");

    encoder.sendData(target, data);
  }
  
  public void sendAck(int target) {
    encoder.sendAck(target);
  }
}
