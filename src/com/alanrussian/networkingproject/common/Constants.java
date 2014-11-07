package com.alanrussian.networkingproject.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Global constants shared between input and output.
 */
public class Constants {

  /**
   * {@link Charset} used for text.
   */
  public static final Charset CHARSET = StandardCharsets.UTF_8;
  
  /**
   * Frequency at which to transfer sound.
   */
  public static final double FREQUENCY = 300.0;
  
  /**
   * The sample rate to encode and to decode at.
   */
  public static final int SAMPLE_RATE = 44_100;
  
  /**
   * Millaseconds that each bit is transfered for.
   */
  public static final int BIT_DURATION = 256;
  
  /**
   * The start of an audio frame.
   */
  public static final List<Boolean> AUDIO_FRAME_START =
      ImmutableList.of(true, true, true, false, false, false);
  
  /**
   * The end of an audio frame.
   */
  public static final List<Boolean> AUDIO_FRAME_END =
      ImmutableList.of(false, false, false, true, true, true);
  
  /**
   * The checksum of an audio frame.
   */
  public static final List<Boolean> AUDIO_FRAME_CHECKSUM =
      ImmutableList.of(true, false, true, false);
  
  /**
   * The number of bits containing the size.
   */
  public static final int AUDIO_FRAME_SIZE_BITS = 3;

  /**
   * The maximum size in bytes of the data part of a frame.
   */
  public static final int AUDIO_FRAME_MAX_DATA_LENGTH =
      ((int) Math.pow(2, AUDIO_FRAME_SIZE_BITS)) - 1;
}
