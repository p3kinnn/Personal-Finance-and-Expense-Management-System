package com.personalfinance.decorator;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TimestampNotifierDecorator extends NotifierDecorator {
    private final Clock clock;
    public TimestampNotifierDecorator(Notifier wrapped, Clock clock) { super(wrapped); this.clock = clock; }
    @Override public void notify(String message) {
        wrapped.notify("[" + LocalDateTime.now(clock).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "] " + message);
    }
}
