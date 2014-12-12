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
  
  private final ManchesterParser sourceIdParser;
  private final ManchesterParser targetParser;
  private final ManchesterParser sizeParser;
  private final ManchesterParser dataParser;
  
  private Optional<Integer> source;
  private Optional<Integer> target;
  private Optional<Integer> size;
  private int checksumIndex;
  private int endIndex;

  public FrameParser() {
    this.sourceIdParser = new ManchesterParser();
    this.targetParser = new ManchesterParser();
    this.sizeParser = new ManchesterParser();
    this.dataParser = new ManchesterParser();

    source = Optional.absent();
    target = Optional.absent();
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
    if (!source.isPresent()) {
      try {
        handleNewSourceBit(value);
      } catch (ManchesterEncodingException e) {
        throw new FrameLossException("Misinterpreted source encoding");
      }
      return false;
    }

    if (!target.isPresent()) {
      try {
        handleNewTargetBit(value);
      } catch (ManchesterEncodingException e) {
        throw new FrameLossException("Misinterpreted target encoding");
      }
      return false;
    }

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
   * Returns the source of the frame.
   * 
   * @throws IllegalStateException if the frame has not yet ended
   */
  public int getSource() {
    if (!isFrameFinished()) {
      throw new IllegalStateException();
    }
    
    return source.get();
  }
  
  /**
   * Returns the target of the frame.
   * 
   * @throws IllegalStateException if the frame has not yet ended
   */
  public int getTarget() {
    if (!isFrameFinished()) {
      throw new IllegalStateException();
    }
    
    return target.get();
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
   * Handles a new bit while in the source part of the frame.
   */
  private void handleNewSourceBit(boolean value) throws ManchesterEncodingException {
    sourceIdParser.addBit(value);
    
    if (sourceIdParser.size() != Constants.COMPUTER_ID_BITS) {
      return;
    }

    source = Optional.of(convertBooleansToNumber(sourceIdParser.getData()));
  }
  
  /**
   * Handles a new bit while in the target part of the frame.
   */
  private void handleNewTargetBit(boolean value) throws ManchesterEncodingException {
    targetParser.addBit(value);
    
    if (targetParser.size() != Constants.COMPUTER_ID_BITS) {
      return;
    }

    target = Optional.of(convertBooleansToNumber(targetParser.getData()));
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
