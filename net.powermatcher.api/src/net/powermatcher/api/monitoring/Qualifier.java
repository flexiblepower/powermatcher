package net.powermatcher.api.monitoring;

public enum Qualifier {

    MATCHER("matcher"), AGENT("agent");
    
    private String description;

    private Qualifier(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
