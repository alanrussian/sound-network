package com.alanrussian.networkingproject.in;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import com.alanrussian.networkingproject.in.audio.AudioDecoder;

/**
 * Reads data broadcasted from an {@link Output}.
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
  
  public Input() {
    this.listeners = new ArrayList<>();

    try {
      new AudioDecoder(decoderListener);
    } catch (LineUnavailableException e) {
      // TODO: Handle error.
      e.printStackTrace();
    }
  }
  
  public void addListener(Listener listener) {
    listeners.add(listener);
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
