package com.personalfinance.decorator;


public final class PriorityNotifierDecorator extends NotifierDecorator {
    public PriorityNotifierDecorator(Notifier wrapped) { super(wrapped); }
    @Override public void notify(String message) { wrapped.notify("[IMPORTANT] " + message); }
}
