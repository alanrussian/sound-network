package com.alanrussian.networkingproject.in.audio.frame;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.in.audio.AudioDecoder;
import com.alanrussian.networkingproject.in.audio.frame.FrameParser.FrameLossException;
import com.google.common.collect.Iterables;

/**
 * Listens for new bits from {@link AudioDecoder} and tries to construct a frame from them.
 */
public class FrameWatcher {
  
  public interface Listener {
    
    /**
     * Triggered when a frame of data has been received.
     */
    void onFrameFound(byte[] data);
  }

  private final Listener listener;
  private final LinkedList<Boolean> frameStartTracker;
  
  private boolean isInFrame;
  private FrameParser frameParser;
  
  public FrameWatcher(Listener listener) {
    this.listener = listener;
    
    this.frameStartTracker = new LinkedList<>();
  }
  
  /**
   * Handles a new bit.
   */
  public void addBit(boolean value) {
//    System.out.print(value ? 1 : 0);
    if (isInFrame) {
      handleNewBitWhileInFrame(value);
    } else {
      handleNewBitWhileNotInFrame(value);
    }
  }
  
  /**
   * Handles a new bit.
   * 
   * @param value the value of the bit
   * @param bitLength how many times the bit occurred
   */
  public void addBit(boolean value, int bitLength) {
    for (int i = 0; i < bitLength; i++) {
      addBit(value);
    }
  }
  
  /**
   * Handles a new bit while not inside a frame.
   */
  private void handleNewBitWhileNotInFrame(boolean value) {
      boolean isTrackerFull = addValueToTracker(value);
      
      if (isTrackerFull
          && Iterables.elementsEqual(frameStartTracker, Constants.AUDIO_FRAME_START)) {
        isInFrame = true;
        frameParser = new FrameParser();

//        System.out.println("Frame start");
      }
  }
  
  /**
   * Handles a new bit while inside a frame.
   */
  private void handleNewBitWhileInFrame(boolean value) {
    try {
      if (!frameParser.addBit(value)) {
        return;
      }
    } catch (FrameLossException e) {
      System.err.println("Frame loss (" + e.getMessage() + ")");
      
      isInFrame = false;
      addValueToTracker(value);

      return;
    }
    
//    System.out.println("Frame end");
    
    List<Boolean> data = frameParser.getData();

    BitSet bitSet = new BitSet(data.size());
    for (int i = 0; i < data.size(); i++) {
      bitSet.set(i, data.get(i));
    }

    listener.onFrameFound(bitSet.toByteArray());
    
    isInFrame = false;
  }
  
  /**
   * Adds a value to the tracker. Makes sure that the tracker size does not exceed the size of the
   * constant.
   * 
   * @return whether the tracker is full.
   */
  private boolean addValueToTracker(boolean value) {

    int sizeDifference = Constants.AUDIO_FRAME_START.size() - frameStartTracker.size();
    if (sizeDifference == 0) {
      frameStartTracker.removeFirst();
    }
    
    frameStartTracker.addLast(value);
    
    return sizeDifference <= 1;
  }
}
