package com.alanrussian.networkingproject.in.audio;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Parses the interpreted sound-on / sound-off signals by smoothing them and returns bits found from
 * within them.
 */
class AudioSignalParser {
  
  /**
   * Listener for {@link AudioSignalParser}.
   */
  public interface Listener {
    
    /**
     * Triggered when a bit value is found from the provided audio signals.
     */
    void onBitReceived(boolean value);
  }
  
  /**
   * Threshold of signals being the same in order to consider a bit present.
   */
  private static final double SIGNAL_THRESHOLD = 7.0/8.0;

  /**
   * The number of signals that will be sent per bit.
   */
  private final int signalsPerActualBit;
  
  private final Listener listener;
  private final LinkedList<Boolean> signals;

  public AudioSignalParser(int signalsPerActualBit, Listener listener) {
    this.signalsPerActualBit = signalsPerActualBit;
    this.listener = listener;
    this.signals = new LinkedList<>();
  }
  
  /**
   * Adds a signal. One of {@link #signalsPerActualBit} signals from an actual bit.
   * 
   * @param isOn whether the signal was interpreted as on.
   */
  public void addSignal(boolean isOn) {
//    System.out.print(isOn ? 1 : 0);
    signals.add(isOn);
    
    parseSignals();
  }
  
  /**
   * Parses the signals we have and tries to find bits.
   */
  private void parseSignals() {
    if (signals.size() != signalsPerActualBit) {
      return;
    }
    
    List<Boolean> smoothSignals = createSmoothenedArray(signals);
    
    int ons = 0;
    for (int i = 0; i < signalsPerActualBit; i++) {
      ons += smoothSignals.get(i) ? 1 : 0;
    }
    
    double percentage = Math.round((double) ons / (double) signalsPerActualBit);

    if (Math.max(percentage, 1.0 - percentage) >= SIGNAL_THRESHOLD) {
      boolean value = percentage >= 0.5;
      listener.onBitReceived(value);
      
      // Find where the signal changed.
      int i;
      for (i = signals.size() - 2; i < signals.size(); i++) {
        if (signals.get(i) != value) {
          break;
        }
      }
      
      for (int j = 0; j < i; j++) {
        signals.removeFirst();
      }
    } else {
      signals.removeFirst();
    }
  }
  
  /**
   * Replaces elements with two opposite values adjacent to it with the opposite value and replaces
   * the start and end if the next/previous two values are opposite.
   */
  private List<Boolean> createSmoothenedArray(List<Boolean> signals) {
    int size = signals.size();
    if (size < 3) {
      return ImmutableList.copyOf(signals);
    }
    
    ImmutableList.Builder<Boolean> builder = ImmutableList.<Boolean>builder();
    
    // Compare start against next two.
    if ((signals.get(0) != signals.get(1)) && (signals.get(1) == signals.get(2))) {
      builder.add(signals.get(1));
    } else {
      builder.add(signals.get(0));
    }
    
    // Compare middle values against both adjacent values.
    for (int i = 1; i < size - 1; i++) {
      boolean previousValue = signals.get(i - 1);
      boolean value = signals.get(i);
      boolean nextValue = signals.get(i + 1);
      
      boolean newValue = value;
      if ((previousValue == nextValue) && (value != previousValue)) {
        newValue = previousValue;
      }
      
      builder.add(newValue);
    }
    
    // Compare end against previous two.
    if ((signals.get(size - 1) != signals.get(size - 2))
        && (signals.get(size - 2) == signals.get(size - 3))) {
      builder.add(signals.get(size - 2));
    } else {
      builder.add(signals.get(size - 1));
    }
     
    return builder.build();
  }
}
