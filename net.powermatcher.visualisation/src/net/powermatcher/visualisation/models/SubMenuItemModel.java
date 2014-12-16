package net.powermatcher.visualisation.models;

public class SubMenuItemModel {

    private String title;
    private String fpid;

    public SubMenuItemModel(String title, String fpid) {
        this.title = title;
        this.fpid = fpid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFpid() {
        return fpid;
    }

    public void setFpid(String fpid) {
        this.fpid = fpid;
    }
}
