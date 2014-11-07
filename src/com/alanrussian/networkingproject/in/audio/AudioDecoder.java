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
import com.alanrussian.networkingproject.in.audio.math.RunningAverage;
import com.alanrussian.networkingproject.in.audio.math.SoundMath;
import com.alanrussian.networkingproject.in.audio.math.Statistics;

/**
 * Listens to the microphone and tries to detect data sent by other devices.
 */
public class AudioDecoder {

  /**
   * The number of samples to collect for the {@link RunningAverage}s.
   */
  private static final int RUNNING_AVERAGE_SAMPLES = 8;
  
  /**
   * The number of standard deviations to check whether a frequency magnitude is within of a {@link
   * #runningLow} or of a {@link #runningHigh}.
   */
  private static final int RUNNING_DEVIATIONS = 2;

  /**
   * The number of partitions per {@link Constants#BIT_DURATION} to evaluate.
   */
  private static final int SOUND_PARTITIONS = 8;
  
  private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
        Constants.SAMPLE_RATE,
        8 /* sampleSizeInBits */,
        1, /* channels */
        true, /* signed */ 
        true /* bigEndian */);
  
  private final FrameWatcher.Listener frameWatcherListener = new FrameWatcher.Listener() {
    @Override
    public void onFrameFound(byte[] data) {
      handleFrameFound(data);
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

  /**
   * Listener for changes to {@link Input}.
   *
   */
  public interface Listener {

    /**
     * Triggered when new data is received.
     */
    void onDataReceived(byte[] data);
  }
  
  private final Listener listener;
  private final TargetDataLine line;
  private final RunningAverage runningLow;
  private final RunningAverage runningHigh;
  private final FrameWatcher frameWatcher;
  private final AudioSignalParser audioSignalParser;
  
  public AudioDecoder(Listener listener) throws LineUnavailableException {
    this.listener = listener;

    this.line = AudioSystem.getTargetDataLine(AUDIO_FORMAT);
    line.open(AUDIO_FORMAT);

    line.start();
    
    this.runningLow = new RunningAverage(RUNNING_AVERAGE_SAMPLES);
    this.runningHigh = new RunningAverage(RUNNING_AVERAGE_SAMPLES);
    
    this.frameWatcher = new FrameWatcher(frameWatcherListener);
    this.audioSignalParser = new AudioSignalParser(SOUND_PARTITIONS, audioSignalParserListener);
    
    scheduleTasks();
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
    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

    byte[] data = new byte[line.available()];
    if (data.length == 0) {
      return;
    }

    line.read(data, 0, data.length);
    
    double[] frequencyMagnitudes =
        getPartitionedFrequencyMagnitudes(
            SOUND_PARTITIONS,
            Constants.FREQUENCY,
            data,
            Constants.SAMPLE_RATE);
    
    for (int i = 0; i < frequencyMagnitudes.length; i++) {
      double frequencyMagnitude = frequencyMagnitudes[i];

      if (!runningLow.haveAverage()) {
        runningLow.add(frequencyMagnitude);
        runningHigh.add(frequencyMagnitude);

        continue;
      }

      boolean value;
      boolean withinLow = Statistics.isWithinAverage(
          frequencyMagnitude,
          runningLow.getAverage(),
          runningLow.getStandardDeviation(),
          RUNNING_DEVIATIONS);
      boolean withinHigh = Statistics.isWithinAverage(
          frequencyMagnitude,
          runningHigh.getAverage(),
          runningHigh.getStandardDeviation(),
          RUNNING_DEVIATIONS);

      if (withinLow && (frequencyMagnitude < runningHigh.getAverage())) {
        value = false;
      } else if (withinHigh && (frequencyMagnitude > runningLow.getAverage())) {
        value = true;
      } else {
        double lowDifference = Math.abs(runningLow.getAverage() - frequencyMagnitude);
        double highDifference = Math.abs(runningHigh.getAverage() - frequencyMagnitude);
        
        value = lowDifference > highDifference;
        
      }
      
      if (value) {
        runningHigh.add(frequencyMagnitude);
      } else {
        runningLow.add(frequencyMagnitude);
      }
      
      audioSignalParser.addSignal(value);
    }
  }
  
  /**
   * Splits up a sound into chunks and returns the frequency magnitudes of each chunk.
   */
  private double[] getPartitionedFrequencyMagnitudes(
      int partitions,
      double frequency,
      byte[] soundData,
      int sampleRate) {

    double[] magnitudes = new double[partitions];
    
    int partitionSize = soundData.length / partitions;
    for (int i = 0; i < partitions; i++) {
      int partitionSizeHere = partitionSize;
      if (i == (partitions - 1)) {
        partitionSizeHere += soundData.length % partitions;
      }

      int start = partitionSize * i;
      byte[] partitionSoundData =
          Arrays.copyOfRange(soundData, start, start + partitionSizeHere);
      
      magnitudes[i] = SoundMath.getMagnitudeOfFrequency(frequency, partitionSoundData, sampleRate);
    }
    
    return magnitudes;
  }
  
  /**
   * Handles a frame with data found from the {@link FrameWatcher}.
   */
  private void handleFrameFound(byte[] data) {
    listener.onDataReceived(data);
  }
  
  /**
   * Handles a bit received from the {@link AudioSignalParser}.
   */
  private void handleBitReceived(boolean value) {
    frameWatcher.addBit(value);
  }
}
