package com.alanrussian.networkingproject.in.audio;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.in.audio.frame.FrameWatcher;
import com.alanrussian.networkingproject.in.audio.math.SoundMath;
import com.alanrussian.networkingproject.out.Output;

/**
 * Listens to the microphone and tries to detect data sent by other devices. Sends an ACK when
 * frames are received.
 */
public class AudioDecoder {

  /**
   * Listener for changes to {@link Input}.
   *
   */
  public static interface Listener {

    /**
     * Triggered when new data is received.
     */
    void onDataReceived(byte[] data);
    
    /**
     * Triggered when an ACK is received.
     */
    void onAckReceived();
  }

  /**
   * The number of partitions per {@link Constants#BIT_DURATION} to evaluate.
   */
  private static final int SOUND_PARTITIONS = 16;
  
  private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
        Constants.SAMPLE_RATE,
        8 /* sampleSizeInBits */,
        1, /* channels */
        true, /* signed */ 
        true /* bigEndian */);
  
  private final Listener listener;
  private final TargetDataLine line;
  private final FrameWatcher frameWatcher;
  private final AudioSignalParser audioSignalParser;
  
  private final FrameWatcher.Listener frameWatcherListener = new FrameWatcher.Listener() {
    @Override
    public void onDataFrameFound(byte[] data) {
      handleFrameFound(data);
    }
    
    @Override
    public void onAckFrameFound() {
      handleAckFound();
    }
  };
  
  private final AudioSignalParser.Listener audioSignalParserListener =
      new AudioSignalParser.Listener() {
        @Override
        public void onBitReceived(boolean value) {
          handleBitReceived(value);
        }
      };
  
  private final Runnable runnable = new Runnable() {
    @Override
    public void run() {
      processSound();
    }
  };
  
  private boolean isEnabled;
  
  public AudioDecoder(Listener listener) throws LineUnavailableException {
    this.listener = listener;

    this.line = AudioSystem.getTargetDataLine(AUDIO_FORMAT);
    line.open(AUDIO_FORMAT);

    line.start();
    
    this.frameWatcher = new FrameWatcher(frameWatcherListener);
    this.audioSignalParser = new AudioSignalParser(SOUND_PARTITIONS, audioSignalParserListener);
    
    isEnabled = true;
    
    scheduleTasks();
  }
  
  /**
   * Sets whether sounds are processed.
   */
  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }
  
  /**
   * Returns whether all listened to frequencies are clear.
   */
  public boolean isLineClear() {
    // TODO: Implement.
    return true;
  }
  
  private void scheduleTasks() {
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(
        runnable,
        0 /* initialDelay */,
        Constants.BIT_DURATION,
        TimeUnit.MILLISECONDS);
  }
  
  /**
   * Processes the next bit of sound.
   */
  private void processSound() {
    if (!isEnabled) {
      return;
    }

    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

    byte[] data = new byte[line.available()];
    if (data.length == 0) {
      return;
    }

    line.read(data, 0, data.length);
    
    double[][] partitionedAndTransformedData =
        getPartitionedAndTransformedData(SOUND_PARTITIONS, data);
    
    double[] offMagnitudes = getPartitionedFrequencyMagnitudes(
        Constants.FREQUENCY_OFF,
        partitionedAndTransformedData,
        Constants.SAMPLE_RATE);
    double[] offOffsetMagnitudes = getPartitionedFrequencyMagnitudes(
        Constants.FREQUENCY_OFF + Constants.FREQUENCY_SECOND_OFFSET,
        partitionedAndTransformedData,
        Constants.SAMPLE_RATE);
    double[] onMagnitudes = getPartitionedFrequencyMagnitudes(
        Constants.FREQUENCY_ON,
        partitionedAndTransformedData,
        Constants.SAMPLE_RATE);
    double[] onOffsetMagnitudes = getPartitionedFrequencyMagnitudes(
        Constants.FREQUENCY_ON + Constants.FREQUENCY_SECOND_OFFSET,
        partitionedAndTransformedData,
        Constants.SAMPLE_RATE);
    
    for (int i = 0; i < SOUND_PARTITIONS; i++) {
      double offMagnitude = offMagnitudes[i] + offOffsetMagnitudes[i];
      double onMagnitude = onMagnitudes[i] + onOffsetMagnitudes[i];
      
//      System.out.printf("%.2f %.2f%n", offMagnitude, onMagnitude);

      boolean value = onMagnitude > offMagnitude;
      
      audioSignalParser.addSignal(value);
    }
  }
  
  /**
   * Splits up a sound into chunks and applies the FFT to the chunks.
   */
  private double[][] getPartitionedAndTransformedData(int partitions, byte[] soundData) {
    double[][] transformedData = new double[partitions][];
    
    int partitionSize = soundData.length / partitions;
    for (int i = 0; i < partitions; i++) {
      int partitionSizeHere = partitionSize;
      if (i == (partitions - 1)) {
        partitionSizeHere += soundData.length % partitions;
      }

      int start = partitionSize * i;
      byte[] partitionSoundData =
          Arrays.copyOfRange(soundData, start, start + partitionSizeHere);
      
      transformedData[i] = SoundMath.applyFft(partitionSoundData);
    }
    
    return transformedData;
  }
  
  /**
   * Returns the frequency magnitudes of chunks of FFT transformed data.
   */
  private double[] getPartitionedFrequencyMagnitudes(
      double frequency,
      double[][] partitionedAndTransformedData,
      int sampleRate) {

    double[] magnitudes = new double[partitionedAndTransformedData.length];
    
    for (int i = 0; i < magnitudes.length; i++) {
      magnitudes[i] = SoundMath.getMagnitudeOfFrequency(
          frequency,
          partitionedAndTransformedData[i],
          sampleRate);
    }
    
    return magnitudes;
  }
  
  /**
   * Handles a frame with data found by the {@link FrameWatcher}.
   */
  private void handleFrameFound(byte[] data) {
    Output.getInstance().sendAck();
    listener.onDataReceived(data);
  }
  
  /**
   * Handles an ACK frame being found by the {@link FrameWatcher}.
   */
  private void handleAckFound() {
    listener.onAckReceived();
  }
  
  /**
   * Handles a bit received from the {@link AudioSignalParser}.
   */
  private void handleBitReceived(boolean value) {
    frameWatcher.addBit(value);
  }
}
