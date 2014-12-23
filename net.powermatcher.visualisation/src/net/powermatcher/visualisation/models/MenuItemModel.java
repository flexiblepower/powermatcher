package net.powermatcher.visualisation.models;

import java.util.ArrayList;
import java.util.List;

/**
 * A data object representing a Menu item for the visualizer. The menu contains {@link MenuItemModel}'s, with a title
 * and one or more {@link SubMenuItemModel}. For example: the Device Agent menu item can have Freezer and PvPanel as
 * {@link SubMenuItemModel}'s
 * 
 * @author FAN
 * @version 2.0
 */
public class MenuItemModel {

    /**
     * The title of this menu item.
     */
    String title;

    /**
     * The {@link SubMenuItemModel} items belonging to this instance.
     */
    List<SubMenuItemModel> items;

    /**
     * The constructor to create an {@link MenuItemModel} instance.
     * 
     * @param title
     *            the title of this menu item.
     */
    public MenuItemModel(String title) {
        this.title = title;
        this.items = new ArrayList<SubMenuItemModel>();
    }

    /**
     * @return the current value of title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param the
     *            new String value for title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the <code>List</code> of {@link SubMenuItemModel}'s.
     */
    public List<SubMenuItemModel> getItemts() {
        return items;
    }

    /**
     * @param the
     *            new <code>List</code> of {@link SubMenuItemModel}'s
     */
    public void setItemts(List<SubMenuItemModel> subItemts) {
        this.items = subItemts;
    }

    /**
     * Adds a new {@link SubMenuItemModel} to items.
     * 
     * @param subMenuItemModel
     *            the {@link SubMenuItemModel} you want to add.
     */
    public void addSubMenuItem(SubMenuItemModel subMenuItemModel) {
        this.items.add(subMenuItemModel);
    }
}
