package com.alanrussian.networkingproject.out.audio;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.in.Input;
import com.alanrussian.networkingproject.out.audio.frame.AckFrame;
import com.alanrussian.networkingproject.out.audio.frame.DataFrame;
import com.alanrussian.networkingproject.out.audio.frame.Frame;
import com.alanrussian.networkingproject.out.audio.wave.MixedWave;
import com.alanrussian.networkingproject.out.audio.wave.SineWave;
import com.alanrussian.networkingproject.out.audio.wave.Wave;
import com.google.common.collect.ImmutableList;

/**
 * Encodes and sends data over audio. This also implements the stop and wait mechanism.
 */
public class AudioEncoder {
  
  /**
   * The time it takes to send the largest possible frame (in millaseconds).
   */
  private static final long MAX_FRAME_DURATION =
      Constants.BIT_DURATION * (Constants.AUDIO_FRAME_START.size()
          + (Constants.AUDIO_FRAME_SIZE_BITS * 2 /* Manchester encoded */)
          + (Constants.AUDIO_FRAME_MAX_DATA_LENGTH
              * 8 /* bits in byte */
              * 2 /* Manchester encoded */)
          + Constants.AUDIO_FRAME_CHECKSUM.size()
          + Constants.AUDIO_FRAME_END.size());
  
  /**
   * The time it takes to send an ACK frame (in millaseconds).
   */
  private static final long ACK_FRAME_DURATION =
      Constants.BIT_DURATION * (Constants.AUDIO_FRAME_START.size()
          + (Constants.AUDIO_FRAME_SIZE_BITS * 2 /* Manchester encoded */)
          + Constants.AUDIO_FRAME_END.size());
  
  /**
   * Time buffer for the ACK timeout (in millaseconds). Note the high number is due to the slow
   * decoding process.
   */
  private static final long ACK_TIME_BUFFER = Constants.BIT_DURATION * 8;
  
  private final Wave waveOff;
  private final Wave waveOn;
  
  private final Input input;
  private final LinkedList<Frame> frameQueue;
  private final ScheduledExecutorService executor;
  
  private final Input.Listener inputListener = new Input.Listener() {
    @Override
    public void onDataReceived(byte[] data) {
      // Don't care here.
    }

    @Override
    public void onAckReceived() {
      handleAckReceived();
    }
  };
  
  private final Runnable sendNextFrameRunnable = new Runnable() {
    @Override
    public void run() {
      sendNextFrame();
    }
  };
    
  private final Runnable onFrameSentRunnable = new Runnable() {
    @Override
    public void run() {
      handleFrameSent();
    }
  };
    
  private final Callable<Void> timeoutRunnable = new Callable<Void>() {
    @Override
    public Void call() {
      onTimeout();

      return null;
    }};
    
  private boolean isFrameSending;
  private int exponentialBackoffNumber;
  private ScheduledFuture<Void> timeoutFuture;
  
  public AudioEncoder(int computerId) {
    this.waveOff = new MixedWave(ImmutableList.of(
        new SineWave(Constants.FREQUENCY_OFF),
        new SineWave(Constants.FREQUENCY_OFF + Constants.FREQUENCY_SECOND_OFFSET)));
    this.waveOn = new MixedWave(ImmutableList.of(
        new SineWave(Constants.FREQUENCY_ON),
        new SineWave(Constants.FREQUENCY_ON + Constants.FREQUENCY_SECOND_OFFSET)));
    
    this.input = Input.getInstance(computerId);
    this.frameQueue = new LinkedList<>();
    this.executor = Executors.newSingleThreadScheduledExecutor();
    
    input.addListener(inputListener);
  }
  
  /**
   * Sends {@code data} over audio.
   */
  public void sendData(byte[] data) {
    try {
      int offset = 0;
      
      while (offset < data.length) {
        int length = Math.min(Constants.AUDIO_FRAME_MAX_DATA_LENGTH, data.length - offset + 1);
        
        DataFrame frame = new DataFrame(
            waveOff,
            waveOn,
            Arrays.copyOfRange(data, offset, offset + length));
        frameQueue.add(frame);
        
        offset += length;
      }
    } catch (Exception e) {
      // TODO: Handle error.
      e.printStackTrace();
      return;
    }
    
    sendNextFrame();
  }
  
  /**
   * Sends an ACK over audio.
   */
  public void sendAck() {
    AckFrame frame = new AckFrame(waveOff, waveOn);
    
    // Must maintain the currently sending frame as the first item in the queue.
    frameQueue.add(isFrameSending ? 1 : 0, frame);
    
    sendNextFrame();
  }
  
  /**
   * Sends the next frame in the queue.
   */
  private void sendNextFrame() {
    if (isFrameSending || frameQueue.isEmpty()) {
      return;
    }
    
    if (!input.isLineClear()) {
      executor.schedule(sendNextFrameRunnable, Constants.BIT_DURATION / 2, TimeUnit.MILLISECONDS);
      return;
    }

    isFrameSending = true;
    input.setEnabled(false);
    exponentialBackoffNumber = 1;

    Frame nextFrame = frameQueue.getFirst();
    nextFrame.send(onFrameSentRunnable);
  }
  
  /**
   * Handles a frame finishing being sent. This re-enables input and sends the next frame if it is
   * an {@link AckFrame} or awaits for the ACK if it is another frame (e.g., {@link DataFrame}).
   */
  private void handleFrameSent() {
    isFrameSending = false;
    input.setEnabled(true);

    Frame lastFrame = frameQueue.getFirst();
    if (lastFrame instanceof AckFrame) {
      frameQueue.removeFirst();
      sendNextFrame();
    } else {
      timeoutFuture = executor.schedule(
          timeoutRunnable,
          ACK_FRAME_DURATION + ACK_TIME_BUFFER,
          TimeUnit.MILLISECONDS);
    }
  }
  
  /**
   * Handles the receipt of an ACK.
   */
  private synchronized void handleAckReceived() {
    if (frameQueue.isEmpty()) {
      throw new IllegalStateException("Received ACK without sending data.");
    }
    
    timeoutFuture.cancel(false);
    
    frameQueue.removeFirst();
    sendNextFrame();
  }
  
  /**
   * Handles a timeout occurring.
   */
  private synchronized void onTimeout() {
    long exponentialBackoffMultiple =
        (long) Math.floor(Math.random() * Math.pow(2, exponentialBackoffNumber));

    executor.schedule(
        sendNextFrameRunnable,
        (long) MAX_FRAME_DURATION * exponentialBackoffMultiple,
        TimeUnit.MILLISECONDS);
    exponentialBackoffNumber++;
  }
}
