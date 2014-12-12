package com.alanrussian.networkingproject.in;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.LineUnavailableException;

import com.alanrussian.networkingproject.in.audio.AudioDecoder;

/**
 * Singleton that reads data broadcasted from an {@link Output}.
 */
public class Input {
  
  /**
   * Listener for changes to {@link Input}.
   *
   */
  public interface Listener {

    /**
     * Triggered when new data is received.
     */
    void onDataReceived(int source, byte[] data);
    
    /**
     * Triggered when an ACK is received.
     */
    void onAckReceived(int source);
  }
  
  private static Map<Integer, Input> computerIdsToInstance = new HashMap<>();
  
  private final AudioDecoder audioDecoder;
  private final List<Listener> listeners;
  
  private final AudioDecoder.Listener decoderListener = new AudioDecoder.Listener() {
    @Override
    public void onDataReceived(int source, byte[] data) {
      handleDataReceived(source, data);
    }
    
    @Override
    public void onAckReceived(int source) {
      handleAckReceived(source);
    }
  };
  
  private Input(int computerId) {
    this.listeners = new ArrayList<>();

    try {
      this.audioDecoder = new AudioDecoder(computerId, decoderListener);
    } catch (LineUnavailableException e) {
      // TODO: Handle error.
      e.printStackTrace();
      
      throw new RuntimeException("Could not initialize audio decoder.");
    }
  }
  
  /**
   * Returns the instance of the Input class for the given ID.
   */
  public static Input getInstance(int computerId) {
    if (!computerIdsToInstance.containsKey(computerId)) {
      computerIdsToInstance.put(computerId, new Input(computerId));
    }

    return computerIdsToInstance.get(computerId);
  }
  
  public void addListener(Listener listener) {
    listeners.add(listener);
  }
  
  /**
   * Sets whether input should be enabled. Input should be disabled, for example, if you are sending
   * something.
   */
  public void setEnabled(boolean isEnabled) {
    audioDecoder.setEnabled(isEnabled);
  }
  
  /**
   * Returns whether the line is clear on all frequencies that are used.
   */
  public boolean isLineClear() {
    return audioDecoder.isLineClear();
  }
  
  /**
   * Handles data being received from the {@link AudioDecoder}.
   */
  private void handleDataReceived(int source, byte[] data) {
    for (Listener listener : listeners) {
      listener.onDataReceived(source, data);
    }
  }
  
  /**
   * Handles an ACK being received from the {@link AudioDecoder}.
   */
  private void handleAckReceived(int source) {
    for (Listener listener : listeners) {
      listener.onAckReceived(source);
    }
  }
}
