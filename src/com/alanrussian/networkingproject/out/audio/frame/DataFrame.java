package com.alanrussian.networkingproject.out.audio.frame;

import java.util.BitSet;
import java.util.List;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.out.audio.wave.Wave;
import com.google.common.collect.ImmutableList;

/**
 * A frame of data transmitted over audio.
 */
public class DataFrame extends Frame {
  
  private final byte[] data;

  public DataFrame(Wave waveOff, Wave waveOn, byte[] data) {
    super(waveOff, waveOn);

    if (data.length > Constants.AUDIO_FRAME_MAX_DATA_LENGTH) {
      throw new IllegalArgumentException(
          "Data must be less than " + Constants.AUDIO_FRAME_MAX_DATA_LENGTH + " bytes.");
    }

    this.data = data;
  }
  
  @Override
  protected List<Boolean> getSignals() {
    return ImmutableList.<Boolean>builder()
        .addAll(Constants.AUDIO_FRAME_START)
        .addAll(createOutput(
            createBooleanListFromNumber(data.length, Constants.AUDIO_FRAME_SIZE_BITS)))
        .addAll(createOutput(byteArrayToBooleanList(data)))
        .addAll(Constants.AUDIO_FRAME_CHECKSUM)
        .addAll(Constants.AUDIO_FRAME_END)
        .build();
  }
  
  private static List<Boolean> byteArrayToBooleanList(byte[] array) {
    ImmutableList.Builder<Boolean> builder = ImmutableList.<Boolean>builder();
    
    BitSet bitSet = BitSet.valueOf(array);
    for (int i = 0, bits = array.length * 8; i < bits; i++) {
      builder.add(bitSet.get(i));
    }
    
    return builder.build();
  }
}
