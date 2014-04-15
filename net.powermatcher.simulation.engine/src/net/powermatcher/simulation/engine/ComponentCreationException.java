package net.powermatcher.simulation.engine;

public class ComponentCreationException extends Exception {
	private static final long serialVersionUID = 1L;

	private String componentFactoryId;

	public ComponentCreationException(String componentFactoryId) {
		super();
		this.componentFactoryId = componentFactoryId;
	}

	public ComponentCreationException(String message, String componentFactoryId) {
		super(message);
		this.componentFactoryId = componentFactoryId;
	}

	public ComponentCreationException(Throwable cause, String componentFactoryId) {
		super(cause);
		this.componentFactoryId = componentFactoryId;
	}

	public ComponentCreationException(String message, Throwable cause, String componentFactoryId) {
		super(message, cause);
		this.componentFactoryId = componentFactoryId;
	}

	public String getComponentFactoryId() {
		return componentFactoryId;
	}
}
