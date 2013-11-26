package com.gopivotal.pushlib;

public class LogItem {

    public final String timestamp;
    public final String message;
    public final int baseRowColour;

    public LogItem(String timestamp, String message, int baseRowColour) {
        this.timestamp = timestamp;
        this.message = message;
        this.baseRowColour = baseRowColour;
    }
}
