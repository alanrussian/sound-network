package com.alanrussian.networkingproject;

import java.util.Scanner;

import com.alanrussian.networkingproject.common.Constants;
import com.alanrussian.networkingproject.in.Input;
import com.alanrussian.networkingproject.in.InputRedirecter;
import com.alanrussian.networkingproject.out.Output;
import com.alanrussian.networkingproject.out.OutputRedirector;

/**
 * Launches the application.
 */
public class Launcher {
  
  public Launcher() {
    int computerId = requestComputerId();
    
    Input in = Input.getInstance(computerId);
    Output out = Output.getInstance(computerId);

    new InputRedirecter(in, System.out);
    new OutputRedirector(out, System.in);
    System.out.println("Now reading and broadcasting.");
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
    
    int smallestId = 0;
    int largestId = ((int) Math.pow(2, Constants.COMPUTER_ID_BITS)) - 1;
    
    // If the scanner is closed, we won't be able to read from System.in later.
    @SuppressWarnings("resource")
    Scanner scanner = new Scanner(System.in);

    while (true) {
      System.out.printf("Please enter a unique computer ID [%d-%d] ", smallestId, largestId);

      int response = scanner.nextInt();

      if ((response >= smallestId) && (response <= largestId)) {
        return response;
      }
    }
  }
}
