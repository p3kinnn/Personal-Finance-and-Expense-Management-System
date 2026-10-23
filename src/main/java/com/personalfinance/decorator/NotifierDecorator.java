package com.personalfinance.decorator;

import java.util.Objects;

public abstract class NotifierDecorator implements Notifier {
    protected final Notifier wrapped;
    protected NotifierDecorator(Notifier wrapped) { this.wrapped = Objects.requireNonNull(wrapped); }
}
