package net.powermatcher.mock;

import net.powermatcher.api.monitoring.Observer;
import net.powermatcher.api.monitoring.UpdateEvent;

public class MockObserver implements Observer {

    private boolean hasReceivedEvent;

    @Override
    public void update(UpdateEvent event) {
        this.hasReceivedEvent = true;
    }

    public boolean hasReceivedEvent() {
        return hasReceivedEvent;
    }
}
