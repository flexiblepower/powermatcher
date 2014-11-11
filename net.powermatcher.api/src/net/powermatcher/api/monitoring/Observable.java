package net.powermatcher.api.monitoring;

public interface Observable {
    String getObserverId();

    void addObserver(Observer observer);

    void removeObserver(Observer observer);
}
