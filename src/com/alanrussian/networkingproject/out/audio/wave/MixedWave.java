package com.alanrussian.networkingproject.out.audio.wave;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

/**
 * A waved mixed together (by average) from multiple waves.
 */
public class MixedWave implements Wave {
  
  private final List<Wave> waves;

  public MixedWave(List<Wave> waves) {
    this.waves = waves;
  }

  /**
   * Returns the average of the waves data.
   */
  @Override
  public byte[] getData(int sampleRate, int duration) {
    byte[] data = new byte[sampleRate / 1000 * duration];

    FluentIterable<byte[]> waveBytes = FluentIterable.from(waves)
        .transform(new Function<Wave, byte[]>() {
          @Override
          public byte[] apply(Wave wave) {
            return wave.getData(sampleRate, duration);
          }
        });
    
    for (int i = 0; i < data.length; i++) {
      double sum = 0;

      for (byte[] bytes : waveBytes) {
        sum += bytes[i];
      }

      data[i] = (byte) (sum / waves.size());
    }
    
    return data;
  }
}
