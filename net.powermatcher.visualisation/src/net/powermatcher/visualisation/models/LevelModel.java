package net.powermatcher.visualisation.models;

import java.util.Set;
import java.util.TreeSet;

/**
 * A data object representing a level for the visualizer. The visualization tree contains N levels. Level 0 is the top
 * level, the Nth level has to be determined.
 * 
 * @author FAN
 * @version 2.0
 */
public class LevelModel {

    /**
     * The number of the level of this instance.
     */
    private int level;

    /**
     * The set of {@link NodeModel}'s in this level of the tree.
     */
    private Set<NodeModel> nodes;

    /**
     * A constructor to create an instance with a certain level.
     * 
     * @param level
     *            the number of the level of this instance.
     */
    public LevelModel(int level) {
        this.level = level;
        // a treeSet because they have to be sorted.
        this.nodes = new TreeSet<NodeModel>();
    }

    /**
     * @return the current value of level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param the
     *            level you want to set this level to.
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @return the current value of nodes.
     */
    public Set<NodeModel> getNodes() {
        return nodes;
    }

    /**
     * Adds a {@link NodeModel} to the nodes Set of this level.
     * 
     * @param node
     *            the {@link NodeModel} that has to be added.
     */
    public void addNode(NodeModel node) {
        this.nodes.add(node);
    }
}
