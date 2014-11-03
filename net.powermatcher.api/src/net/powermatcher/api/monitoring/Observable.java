package net.powermatcher.api.monitoring;

public interface Observable {
	void addObserver(Observer observer);
	
	void removeObserver(Observer observer);
}
