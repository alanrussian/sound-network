package com.alanrussian.networkingproject.in.audio.frame;

import java.util.List;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.in.audio.frame.ManchesterParser.ManchesterEncodingException;
import com.google.common.base.Optional;

/**
 * Listens to a bits parsed from an audio signal after the start of a frame and checks to see if it
 * matches a frame.
 */
class FrameParser {

  public static class FrameLossException extends Exception {
    
    public FrameLossException(String message) {
      super(message);
    }

    private static final long serialVersionUID = 1L;
  }
  
  private final ManchesterParser sizeParser;
  private final ManchesterParser dataParser;
  
  private Optional<Integer> size;
  private int checksumIndex;
  private int endIndex;

  public FrameParser() {
    this.sizeParser = new ManchesterParser();
    this.dataParser = new ManchesterParser();

    size = Optional.absent();
    checksumIndex = 0;
    endIndex = 0;
  }
  
  /**
   * Adds a new bit to the frame.
   * 
   * @return whether a complete frame has been received
   */
  public boolean addBit(boolean value) throws FrameLossException {
    if (!size.isPresent()) {
      try {
        handleNewSizeBit(value);
      } catch (ManchesterEncodingException e) {
        throw new FrameLossException("Misinterpreted size encoding");
      }
      return false;
    }
    
    // Handle ACK case.
    if (size.get() == 0) {
      handleNewFrameEndBit(value);

      return isFrameFinished();
    }
    
    if (dataParser.size() != size.get() * 8 /* bits in byte */) {
      try {
        handleNewDataBit(value);
      } catch (ManchesterEncodingException e) {
        throw new FrameLossException("Misinterpreted data encoding");
      }
      return false;
    }
    
    if (checksumIndex != Constants.AUDIO_FRAME_CHECKSUM.size()) {
      handleNewChecksumBit(value);
      return false;
    }
    
    handleNewFrameEndBit(value);

    return isFrameFinished();
  }
  
  /**
   * Returns the data as a list of booleans.
   * 
   * @throws IllegalStateException if the frame has not yet ended
   */
  public List<Boolean> getData() {
    if (!isFrameFinished()) {
      throw new IllegalStateException();
    }
    
    return dataParser.getData();
  }
  
  /**
   * Returns whether this is an ACK frame.
   * 
   * @throws IllegalStateException if the frame has not yet ended
   */
  public boolean isAckFrame() {
    if (!isFrameFinished()) {
      throw new IllegalStateException();
    }
    
    return size.get() == 0;
  }
  
  /**
   * Handles a new bit while in the size part of the frame.
   */
  private void handleNewSizeBit(boolean value) throws ManchesterEncodingException {
    sizeParser.addBit(value);
    
    if (sizeParser.size() != Constants.AUDIO_FRAME_SIZE_BITS) {
      return;
    }

    size = Optional.of(convertBooleansToNumber(sizeParser.getData()));
  }
  
  /**
   * Handles a new bit while in the data part of the frame.
   */
  private void handleNewDataBit(boolean value) throws ManchesterEncodingException {
    dataParser.addBit(value);
  }
  
  /**
   * Handles a new bit while in the checksum part of the frame.
   */
  private void handleNewChecksumBit(boolean value) throws FrameLossException {
    if (Constants.AUDIO_FRAME_CHECKSUM.get(checksumIndex) != value) {
      throw new FrameLossException("Bad checksum");
    }
    
    checksumIndex++;
  }
  
  /**
   * Handles a new bit while in the end part of the frame.
   */
  private void handleNewFrameEndBit(boolean value) throws FrameLossException {
    if (Constants.AUDIO_FRAME_END.get(endIndex) != value) {
      throw new FrameLossException("Bad end");
    }
    
    endIndex++;
  }
  
  /**
   * Converts a list of booleans into the number it represents.
   */
  private static int convertBooleansToNumber(List<Boolean> booleans) {
    int number = 0;
    
    for (int i = 0; i < booleans.size(); i++) {
      if (booleans.get(i)) {
        number += Math.pow(2, booleans.size() - i - 1);
      }
    }

    return number;
  }
  
  /**
   * Returns whether a frame has ended.
   */
  private boolean isFrameFinished() {
    return endIndex == Constants.AUDIO_FRAME_END.size();
  }
}
