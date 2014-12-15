package net.powermatcher.visualisation.models;

import java.util.Set;
import java.util.TreeSet;

/**
 * A data object representing a level for the visualizer. The visualization tree contains n levels. Level 0 is the top
 * level, the bottom level has to be determined.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
public class LevelModel {

    /**
     * The number of the level of this instance.
     */
    private int level;

    /**
     * The set of nodes in this level of the tree.
     */
    private Set<NodeModel> nodes;

    /**
     * A constructor to create an instance with a certain level
     * 
     * @param level
     *            the number of the level of this instance
     */
    public LevelModel(int level) {
        this.level = level;
        this.nodes = new TreeSet<NodeModel>();
    }

    /**
     * 
     * @return the level of this instance.
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param the level you want to set this level to.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @return the set of nodes of this level0
     */
    public Set<NodeModel> getNodes() {
        return nodes;
    }

    /**
     * Adds a node to the node Set of this level.
     * 
     * @param node the node that has to be added.
     */
    public void addNode(NodeModel node) {
        this.nodes.add(node);
    }
}
