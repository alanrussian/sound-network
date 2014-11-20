package com.alanrussian.networkingproject.out;

import java.io.InputStream;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.alanrussian.networkingproject.common.Constants;

/**
 * Redirects data from an {@link InputStream} to an {@link Output}.
 */
public class OutputRedirector {
  
  /**
   * Period in millaseconds in which the input is read.
   */
  private static final long PERIOD = 100;
  
  private final Output out;
  private final Scanner scanner;
  
  private final TimerTask redirectTask = new TimerTask() {
    @Override
    public void run() {
      while (scanner.hasNextLine()) {
        out.sendData(scanner.nextLine().getBytes(Constants.CHARSET));
      }
    }
  };

  public OutputRedirector(Output out, InputStream in) {
    this.out = out;
    this.scanner = new Scanner(in);
    
    scheduleTasks();
  }
  
  private void scheduleTasks() {
    new Timer().scheduleAtFixedRate(redirectTask, 0, PERIOD);
  }
}
