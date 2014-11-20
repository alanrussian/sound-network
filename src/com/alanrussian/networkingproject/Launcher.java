package com.alanrussian.networkingproject;

import com.alanrussian.networkingproject.in.Input;
import com.alanrussian.networkingproject.in.InputRedirecter;
import com.alanrussian.networkingproject.out.Output;
import com.alanrussian.networkingproject.out.OutputRedirector;

/**
 * Launches the application.
 */
public class Launcher {
  
  public Launcher() {
    Input in = Input.getInstance();
    Output out = Output.getInstance();

    new InputRedirecter(in, System.out);
    new OutputRedirector(out, System.in);
    System.out.println("Now reading and broadcasting.");
  }

  public static void main(String[] args) {
    new Launcher();
  }
}
