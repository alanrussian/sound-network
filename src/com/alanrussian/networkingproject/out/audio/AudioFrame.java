package com.alanrussian.networkingproject.out.audio;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.out.audio.wave.Wave;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * A frame of data transmitted over audio.
 */
public class AudioFrame {
  
  private final Wave waveOff;
  private final Wave waveOn;
  private final byte[] data;

  public AudioFrame(Wave waveOff, Wave waveOn, byte[] data) {
    if (data.length > Constants.AUDIO_FRAME_MAX_DATA_LENGTH) {
      throw new IllegalArgumentException(
          "Data must be less than " + Constants.AUDIO_FRAME_MAX_DATA_LENGTH + " bytes.");
    }

    this.waveOff = waveOff;
    this.waveOn = waveOn;
    this.data = data;
  }
  
  /**
   * Sends the frame.
   */
  public void send() {
    AudioPlayer player;
    try {
      player = new AudioPlayer();

      playOutput(
          ImmutableList.<Boolean>builder()
              .addAll(Constants.AUDIO_FRAME_START)
              .addAll(createOutput(
                  createBooleanListFromNumber(data.length, Constants.AUDIO_FRAME_SIZE_BITS)))
              .addAll(createOutput(byteArrayToBooleanList(data)))
              .addAll(Constants.AUDIO_FRAME_CHECKSUM)
              .addAll(Constants.AUDIO_FRAME_END)
              .build(),
          player);

      player.play();
    } catch (LineUnavailableException e) {
      // TODO: Handle error.
      e.printStackTrace();
      return;
    }
  }
  
  /**
   * Creates an output list of booleans representing whether the sound should be on or off for every
   * {@link Constants#BIT_DURATION}. This adds the Manchester encoding.
   */
  private List<Boolean> createOutput(List<Boolean> input) {
    ImmutableList.Builder<Boolean> builder = ImmutableList.builder();

    for (int i = 0; i < input.size(); i++) {
      boolean bit = input.get(i);
      
      builder.add(bit);
      builder.add(!bit);
    }
    
    return builder.build();
  }
  
  /**
   * Converts a list of booleans into sound by treating true values as a sound being on and visa
   * versa with false values.
   */
  private void playOutput(List<Boolean> output, AudioPlayer player) {
    if (output.isEmpty()) {
      return;
    }
    
    int length = 1;
    boolean lastValue = output.get(0);
    
    for (int i = 1; i < output.size(); i++) {
      boolean value = output.get(i);
      
      if (value == lastValue) {
        length++;
        continue;
      }
      
      player.add(lastValue ? waveOn : waveOff, Constants.BIT_DURATION * length);
      length = 1;
      lastValue = value;
    }
    
    player.add(lastValue ? waveOn : waveOff, Constants.BIT_DURATION * length);
  }
  
  private static List<Boolean> byteArrayToBooleanList(byte[] array) {
    ImmutableList.Builder<Boolean> builder = ImmutableList.<Boolean>builder();
    
    BitSet bitSet = BitSet.valueOf(array);
    for (int i = 0, bits = array.length * 8; i < bits; i++) {
      builder.add(bitSet.get(i));
    }
    
    return builder.build();
  }
  
  /**
   * Creates a boolean list representing a number using exactly {@code bits}.
   */
  private static List<Boolean> createBooleanListFromNumber(int number, int bits) {
    Preconditions.checkArgument(number >= 0);
    Preconditions.checkArgument(
        number <= Math.pow(bits, 2) - 1,
        "Number cannot fit in given number of bits");

    LinkedList<Boolean> list = new LinkedList<>();
    
    while (number > 0) {
      list.addFirst(number % 2 == 1);
      number /= 2;
    }
    
    // Fill in zero bits.
    while (list.size() < bits) {
      list.addFirst(false);
    }
    
    return list;
  }
}
