package com.alanrussian.networkingproject.in;

import java.io.PrintStream;

import com.alanrussian.networkingproject.common.Constants;

/**
 * Redirects data from an {@link Input} by outputting its data to a {@link PrintStream} prefixed by
 * "Received from #: ".
 */
public class InputRedirecter {
  
  private final PrintStream out;
  
  private final Input.Listener listener = new Input.Listener() {
    @Override
    public void onDataReceived(int source, byte[] data) {
      handleDataReceived(source, data);
    }

    @Override
    public void onAckReceived(int source) {
      // Do not care here.
    }
  };

  public InputRedirecter(Input in, PrintStream out) {
    this.out = out;
    
    in.addListener(listener);
  }
  
  private void handleDataReceived(int source, byte[] data) {
    String message = new String(data, Constants.CHARSET);

    out.printf("Received from %d: %s%n", source, message);
  }
}
