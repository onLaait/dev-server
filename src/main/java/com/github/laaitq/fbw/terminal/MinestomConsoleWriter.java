package com.github.laaitq.fbw.terminal;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.fusesource.jansi.AnsiConsole;

import java.text.SimpleDateFormat;

import static com.github.laaitq.fbw.terminal.MinestomTerminal.reader;

public class MinestomConsoleWriter extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent event) {
        Level level = event.getLevel();
        String ANSI_RESET = "\u001B[m";

        StringBuilder stringBuilder = new StringBuilder();
        String message = TerminalColorConverter.format(event.getMessage());
        stringBuilder.append('[')
                .append(new SimpleDateFormat("HH:mm:ss").format(event.getTimeStamp()))
                .append(' ')
                .append(level.levelStr)
                .append("] ")
                .append(message);
        switch (level.levelInt) {
            case Level.WARN_INT -> {
                stringBuilder.insert(0, "\u001B[93m");
                stringBuilder.append(ANSI_RESET);
            }
            case Level.ERROR_INT -> {
                stringBuilder.insert(0, "\u001B[91m");
                stringBuilder.append(ANSI_RESET);
            }
        }
        String formatted = stringBuilder.toString();
        if (reader != null) {
            reader.printAbove(formatted);
        } else {
            AnsiConsole.out().println(formatted);
        }
    }
}