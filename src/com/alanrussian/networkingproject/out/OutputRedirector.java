package com.alanrussian.networkingproject.out;

import java.io.InputStream;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alanrussian.networkingproject.common.Constants;

/**
 * Redirects data from an {@link InputStream} to an {@link Output}. Data must be in the format of
 * "ComputerId: Message".
 */
public class OutputRedirector {
  
  /**
   * Listener for {@link OutputRedirector}.
   */
  public interface Listener {
    public enum InputError {
      FORMAT,
      OUT_OF_RANGE_TARGET,
      MESSAGE_TO_SELF
    };

    void onBadInput(InputError error);
  }
  
  /**
   * Period in millaseconds in which the input is read.
   */
  private static final long PERIOD = 100;
  
  private static final Pattern LINE_PATTERN = Pattern.compile("(\\d+): ?(.+)");
  
  private final Output out;
  private final Scanner scanner;
  private final Listener listener;
  
  private final TimerTask redirectTask = new TimerTask() {
    @Override
    public void run() {
      while (true) {
        Matcher lineMatcher = LINE_PATTERN.matcher(scanner.nextLine());

        if (!lineMatcher.matches()) {
          listener.onBadInput(Listener.InputError.FORMAT);
          return;
        }
        
        int target = Integer.valueOf(lineMatcher.group(1));
        String message = lineMatcher.group(2);
        
        if ((target > Constants.LARGEST_COMPUTER_ID) || (target < Constants.SMALLEST_COMPUTER_ID)) {
          listener.onBadInput(Listener.InputError.OUT_OF_RANGE_TARGET);
          return;
        }

        try {
          out.sendData(target, message.getBytes(Constants.CHARSET));
        } catch (IllegalArgumentException e) {
          listener.onBadInput(Listener.InputError.MESSAGE_TO_SELF);
        }
      }
    }
  };

  public OutputRedirector(Output out, InputStream in, Listener listener) {
    this.out = out;
    this.scanner = new Scanner(in);
    this.listener = listener;
    
    scheduleTasks();
  }
  
  private void scheduleTasks() {
    new Timer().scheduleAtFixedRate(redirectTask, 0, PERIOD);
  }
}
