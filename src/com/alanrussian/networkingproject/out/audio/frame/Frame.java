package com.alanrussian.networkingproject.out.audio.frame;

import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.out.audio.AudioPlayer;
import com.alanrussian.networkingproject.out.audio.wave.Wave;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Abstract class containing an audio frame.
 */
public abstract class Frame {

  protected final int target;
  
  private final Wave waveOff;
  private final Wave waveOn;

  protected Frame(int target, Wave waveOff, Wave waveOn) {
    this.target = target;
    this.waveOff = waveOff;
    this.waveOn = waveOn;
  }
  
  /**
   * Returns the target of the frame.
   */
  public int getTarget() {
    return target;
  }
  
  /**
   * Sends the frame.
   * 
   * @param callback executes after frame is finished sending
   */
  public void send(Runnable callback) {
    Runnable runnable = createPlaySoundRunnable(callback);
    
    new Thread(runnable).start();
  }
  
  /**
   * Returns the on/off signals to be sent.
   */
  protected abstract List<Boolean> getSignals();
  
  /**
   * Creates an output list of booleans representing whether the sound should be on or off for every
   * {@link Constants#BIT_DURATION}. This adds the Manchester encoding.
   */
  protected List<Boolean> createOutput(List<Boolean> input) {
    ImmutableList.Builder<Boolean> builder = ImmutableList.builder();

    for (int i = 0; i < input.size(); i++) {
      boolean bit = input.get(i);
      
      builder.add(bit);
      builder.add(!bit);
    }
    
    return builder.build();
  }
  
  /**
   * Creates a boolean list representing a number using exactly {@code bits}.
   */
  protected static List<Boolean> createBooleanListFromNumber(int number, int bits) {
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
  
  /**
   * Creates a runnable to play sounds and executes {@code callback} after completion.
   */
  private Runnable createPlaySoundRunnable(final Runnable callback) {
    return new Runnable() {
      @Override
      public void run() {
        playSound();

        callback.run();
      }
    };
  }
  
  /**
   * Plays the sound from the frame's signals.
   */
  private void playSound() {
    AudioPlayer player;
    try {
      player = new AudioPlayer();

      playOutput(getSignals(), player);

      player.play();
    } catch (LineUnavailableException e) {
      // TODO: Handle error.
      e.printStackTrace();
      return;
    }
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
}
