package com.alanrussian.networkingproject.out.audio.frame;

import java.util.List;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.out.audio.wave.Wave;
import com.google.common.collect.ImmutableList;

/**
 * An ACK frame transmitted over audio.
 */
public class AckFrame extends Frame {

  public AckFrame(Wave waveOff, Wave waveOn) {
    super(waveOff, waveOn);
  }

  /**
   * Returns the signal for an ACK, which are the start, size zero, and the end.
   */
  @Override
  protected List<Boolean> getSignals() {
    return ImmutableList.<Boolean>builder()
        .addAll(Constants.AUDIO_FRAME_START)
        .addAll(createOutput(
            createBooleanListFromNumber(0, Constants.AUDIO_FRAME_SIZE_BITS)))
        .addAll(Constants.AUDIO_FRAME_END)
        .build();
  }
}
