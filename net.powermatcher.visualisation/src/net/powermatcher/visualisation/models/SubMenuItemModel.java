package net.powermatcher.visualisation.models;

import net.powermatcher.api.Agent;

/**
 * A data object representing a SubMenu item for the visualizer.
 * 
 * @author FAN
 * @version 1.0
 */
public class SubMenuItemModel {

    /**
     * The title of this instance.
     */
    private String title;

    /**
     * The fpid of the {@link Agent} type you want to create.
     */
    private String fpid;

    /**
     * A constructor to create an instance of a SubMenuItemModel.
     * 
     * @param title
     *            The title of this instance.
     * @param fpid
     *            The fpid of the {@link Agent} type you want to create.
     * 
     */
    public SubMenuItemModel(String title, String fpid) {
        this.title = title;
        this.fpid = fpid;
    }

    /**
     * @return the current title <code>String</code>
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param the
     *            new title <code>String</code>
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the current fpid <code>String</code>
     */
    public String getFpid() {
        return fpid;
    }

    /**
     * @param the
     *            new fpid <code>String</code>
     */
    public void setFpid(String fpid) {
        this.fpid = fpid;
    }
}
