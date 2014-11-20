package com.alanrussian.networkingproject.out.audio;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.in.Input;
import com.alanrussian.networkingproject.out.audio.wave.MixedWave;
import com.alanrussian.networkingproject.out.audio.wave.SineWave;
import com.alanrussian.networkingproject.out.audio.wave.Wave;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Encodes and sends data over audio. This also implements the stop and wait mechanism.
 */
public class AudioEncoder {
  
  /**
   * The time it takes to send an ACK frame (in millaseconds).
   */
  private static final long ACK_FRAME_DURATION =
      Constants.BIT_DURATION * (Constants.AUDIO_FRAME_START.size()
          + (Constants.AUDIO_FRAME_SIZE_BITS * 2 /* Manchester encoded */)
          + Constants.AUDIO_FRAME_END.size());
  
  /**
   * Time buffer for the ACK timeout (in millaseconds).
   */
  private static final long ACK_TIME_BUFFER = 50;
  
  private final Wave waveOff;
  private final Wave waveOn;
  
  private final Input input;
  private final Queue<AudioFrame> frameQueue;
  private final ScheduledExecutorService executor;
  
  private final Input.Listener inputListener = new Input.Listener() {
    @Override
    public void onDataReceived(byte[] data) {
      // Don't care here.
    }

    @Override
    public void onAckReceived() {
      handleAckReceived();
    }};
    
  private final Callable<Void> timeoutRunnable = new Callable<Void>() {
    @Override
    public Void call() {
      onTimeout();

      return null;
    }};
    
  private ScheduledFuture<Void> timeoutFuture;
  
  public AudioEncoder() {
    this.waveOff = new MixedWave(ImmutableList.of(
        new SineWave(Constants.FREQUENCY_OFF),
        new SineWave(Constants.FREQUENCY_OFF + Constants.FREQUENCY_SECOND_OFFSET)));
    this.waveOn = new MixedWave(ImmutableList.of(
        new SineWave(Constants.FREQUENCY_ON),
        new SineWave(Constants.FREQUENCY_ON + Constants.FREQUENCY_SECOND_OFFSET)));
    
    this.input = Input.getInstance();
    this.frameQueue = new LinkedList<>();
    this.executor = Executors.newSingleThreadScheduledExecutor();
    
    input.addListener(inputListener);
  }
  
  /**
   * Sends {@code data} over audio.
   */
  public void send(byte[] data) {
    try {
      int offset = 0;
      
      while (offset < data.length) {
        int length = Math.min(Constants.AUDIO_FRAME_MAX_DATA_LENGTH, data.length - offset + 1);
        
        AudioFrame frame = new AudioFrame(
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
   * Sends the next frame in the queue.
   */
  private void sendNextFrame() {
    Preconditions.checkArgument(!frameQueue.isEmpty());

    AudioFrame nextFrame = frameQueue.peek();
    nextFrame.send();
    
    timeoutFuture = executor.schedule(
        timeoutRunnable,
        ACK_FRAME_DURATION + ACK_TIME_BUFFER,
        TimeUnit.MILLISECONDS);
  }
  
  /**
   * Handles the receipt of an ACK.
   */
  private synchronized void handleAckReceived() {
    if (frameQueue.isEmpty()) {
      throw new IllegalStateException("Received ACK without sending data.");
    }
    
    timeoutFuture.cancel(false);
    
    frameQueue.remove();
    if (!frameQueue.isEmpty()) {
      sendNextFrame();
    }
  }
  
  /**
   * Handles a timeout occurring.
   */
  private synchronized void onTimeout() {
    sendNextFrame();
  }
}
