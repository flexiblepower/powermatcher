package net.powermatcher.visualisation;

public enum Kind {

    CONCENTRATOR("Concentrator"), AUCTIONEER("Auctioneer"), DEVICEAGENT("Device");

    private String description;

    private Kind(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
