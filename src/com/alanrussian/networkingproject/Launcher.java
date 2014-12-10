package com.alanrussian.networkingproject;

import java.util.Scanner;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.in.Input;
import com.alanrussian.networkingproject.in.InputRedirecter;
import com.alanrussian.networkingproject.out.Output;
import com.alanrussian.networkingproject.out.OutputRedirector;
import com.alanrussian.networkingproject.out.OutputRedirector.Listener.InputError;

/**
 * Launches the application.
 */
public class Launcher {
  
  private final OutputRedirector.Listener outputRedirectorListener =
      new OutputRedirector.Listener() {
        @Override
        public void onBadInput(InputError error) {
          handleBadInput(error);
        }
      };
  
  public Launcher() {
    int computerId = requestComputerId();
    
    System.out.println();
    
    Input in = Input.getInstance(computerId);
    Output out = Output.getInstance(computerId);

    new InputRedirecter(in, System.out);
    new OutputRedirector(out, System.in, outputRedirectorListener);

    System.out.println("Now reading and broadcasting. Please send messages like this:");
    System.out.println("2: Message to computer 2.");
    System.out.println();
  }

  public static void main(String[] args) {
    new Launcher();
  }
  
  /**
   * Prompts the user for a computer ID and returns their response.
   */
  private int requestComputerId() {
    System.out.println(
        "Before you can start receiving/sending messages, you must enter a computer ID.");
    System.out.println(
        "There should not be more than one computer with the same computer ID within a room (unless"
        + " you like \n  chaos).");
    
    // If the scanner is closed, we won't be able to read from System.in later.
    @SuppressWarnings("resource")
    Scanner scanner = new Scanner(System.in);

    while (true) {
      System.out.printf(
          "Please enter a unique computer ID [%d-%d] ",
          Constants.SMALLEST_COMPUTER_ID,
          Constants.LARGEST_COMPUTER_ID);

      int response = scanner.nextInt();

      if ((response >= Constants.SMALLEST_COMPUTER_ID)
          && (response <= Constants.LARGEST_COMPUTER_ID)) {
        return response;
      }
    }
  }
  
  private void handleBadInput(InputError error) {
    switch (error) {
      case FORMAT:
        System.out.printf(
            "Input must be in the format: \"[%d-%d]: Message\".%n",
            Constants.SMALLEST_COMPUTER_ID,
            Constants.LARGEST_COMPUTER_ID);
        break;
    
      case OUT_OF_RANGE_TARGET:
        System.out.printf(
            "Target computer ID must be between %d and %d.%n",
            Constants.SMALLEST_COMPUTER_ID,
            Constants.LARGEST_COMPUTER_ID);
        break;
        
      case MESSAGE_TO_SELF:
        System.out.println("Cannot send message to self.");
        break;
        
      default:
        throw new IllegalStateException(
            "Unhandled " + InputError.class.getSimpleName() + " received.");
    }
  }
}
