package net.powermatcher.api.monitoring;

public interface Observer {
	void update(UpdateEvent event);
}
