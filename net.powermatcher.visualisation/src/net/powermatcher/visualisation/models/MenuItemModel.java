package net.powermatcher.visualisation.models;

import java.util.ArrayList;
import java.util.List;

public class MenuItemModel {

    String title;
    List<SubMenuItemModel> items;

    public MenuItemModel(String title) {
        this.title = title;
        this.items = new ArrayList<SubMenuItemModel>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<SubMenuItemModel> getItemts() {
        return items;
    }

    public void setItemts(List<SubMenuItemModel> subItemts) {
        this.items = subItemts;
    }

    public void addSubMenuItem(SubMenuItemModel subMenuItemModel) {
        this.items.add(subMenuItemModel);
    }
}
