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
   * Sound frequency at which to transfer off bits. Be sure to avoid harmonics with {@link
   * #FREQUENCY_ON}. Also, it should be a multiple of 50.
   */
  public static final double FREQUENCY_OFF = 1_000.0;
  
  /**
   * Sound frequency at which to transfer on bits. Be sure to avoid harmonics with {@link
   * #FREQUENCY_OFF}. Also, it should be a multiple of 50.
   */
  public static final double FREQUENCY_ON = FREQUENCY_OFF + 400.0;
  
  /**
   * Offset of off and on frequency to mix into off and on wave. For example, if you put 50 here and
   * 10,000 in FREQUENCY_OFF, there will be two sine waves mixed together at 10,000 and 10,050 hz.
   * Make sure this is a multiple of 50 and avoid harmonics.
   */
  public static final double FREQUENCY_SECOND_OFFSET = 100.0;
  
  /**
   * The sample rate to encode and to decode at.
   */
  public static final int SAMPLE_RATE = 48_000;
  
  /**
   * Millaseconds that each bit is transfered for.
   */
  public static final int BIT_DURATION = SAMPLE_RATE / 256;
  
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
  
  /**
   * The number of bits containing the computer ID.
   */
  public static final int COMPUTER_ID_BITS = 3;
}
