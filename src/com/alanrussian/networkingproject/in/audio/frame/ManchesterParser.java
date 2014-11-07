package com.alanrussian.networkingproject.in.audio.frame;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

/**
 * Listens to a bits parsed from a Manchester Encoded audio signal and tries to find data from it.
 */
class ManchesterParser {
  
  /**
   * Exception indicating that two bits that should have been oppostie were found to be the same.
   */
  public static class ManchesterEncodingException extends Exception {

    private static final long serialVersionUID = 1L;
  }
  
  private final List<Boolean> data;
  
  private Optional<Boolean> lastValue;
  
  public ManchesterParser() {
    this.data = new ArrayList<>();

    lastValue = Optional.absent();
  }

  public void addBit(boolean value) throws ManchesterEncodingException {
    if (!lastValue.isPresent()) {
      lastValue = Optional.of(value);
      return;
    }

    if (lastValue.get() == value) {
      throw new ManchesterEncodingException();
    } else {
      data.add(lastValue.get());
      lastValue = Optional.absent();
    }
  }
  
  /**
   * Returns the decoded data.
   */
  public List<Boolean> getData() {
    return data;
  }

  /**
   * Returns the size of the decoded data.
   */
  public Integer size() {
    return data.size();
  }
}
