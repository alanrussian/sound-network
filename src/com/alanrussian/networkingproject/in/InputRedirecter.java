package com.alanrussian.networkingproject.in;

import java.io.PrintStream;

import com.alanrussian.networkingproject.common.Constants;

/**
 * Redirects data from an {@link Input} by outputting its data to a {@link PrintStream} prefixed by
 * "Received: ".
 */
public class InputRedirecter {
  
  private final PrintStream out;
  
  private final Input.Listener listener = new Input.Listener() {
    @Override
    public void onDataReceived(byte[] data) {
      handleDataReceived(data);
    }

    @Override
    public void onAckReceived(int recipient) {
      // Do not care here.
    }
  };

  public InputRedirecter(Input in, PrintStream out) {
    this.out = out;
    
    in.addListener(listener);
  }
  
  private void handleDataReceived(byte[] data) {
    String message = new String(data, Constants.CHARSET);

    out.println("Received: " + message);
  }
}
