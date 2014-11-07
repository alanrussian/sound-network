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
    Input in = new Input();
    Output out = new Output();

    new InputRedirecter(in, System.out);
    new OutputRedirector(out, System.in);
    System.out.println("Now reading and broadcasting.");
  }

  public static void main(String[] args) {
    new Launcher();
  }
}
