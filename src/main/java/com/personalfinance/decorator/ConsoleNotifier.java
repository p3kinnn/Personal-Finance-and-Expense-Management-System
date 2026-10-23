package com.personalfinance.decorator;

import java.io.PrintStream;

public final class ConsoleNotifier implements Notifier {
    private final PrintStream output;
    public ConsoleNotifier(PrintStream output) { this.output = output; }
    @Override public void notify(String message) { output.println(message); }
}
