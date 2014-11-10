package net.powermatcher.core.mock;

import net.powermatcher.core.monitoring.BaseObservable;

public class MockBaseObservable extends BaseObservable {

    @Override
    public String getObserverId() {
        return "ObservableMock";
    }

}
