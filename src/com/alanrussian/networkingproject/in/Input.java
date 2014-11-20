package com.alanrussian.networkingproject.in;

import java.util.ArrayList;
import java.util.List;

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
    void onDataReceived(byte[] data);
    
    /**
     * Triggered when an ACK is received.
     */
    void onAckReceived();
  }
  
  private static Input instance;
  
  private final AudioDecoder audioDecoder;
  private final List<Listener> listeners;
  
  private final AudioDecoder.Listener decoderListener = new AudioDecoder.Listener() {
    @Override
    public void onDataReceived(byte[] data) {
      handleDataReceived(data);
    }
    
    @Override
    public void onAckReceived() {
      handleAckReceived();
    }
  };
  
  private Input() {
    this.listeners = new ArrayList<>();

    try {
      this.audioDecoder = new AudioDecoder(decoderListener);
    } catch (LineUnavailableException e) {
      // TODO: Handle error.
      e.printStackTrace();
      
      throw new RuntimeException("Could not initialize audio decoder.");
    }
  }
  
  /**
   * Returns the instance of the Input class.
   */
  public static Input getInstance() {
    if (instance == null) {
      instance = new Input();
    }

    return instance;
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
  private void handleDataReceived(byte[] data) {
    for (Listener listener : listeners) {
      listener.onDataReceived(data);
    }
  }
  
  /**
   * Handles an ACK being received from the {@link AudioDecoder}.
   */
  private void handleAckReceived() {
    for (Listener listener : listeners) {
      listener.onAckReceived();
    }
  }
}
