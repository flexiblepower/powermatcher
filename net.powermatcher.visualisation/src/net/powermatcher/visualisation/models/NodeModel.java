package net.powermatcher.visualisation.models;

import net.powermatcher.api.Agent;

/**
 * A data object representing a node in the visualizer tree. A node represents a running {@link Agent} instance.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
public class NodeModel implements Comparable<NodeModel> {

    /**
     * The factory pid of this node.
     */
    private String fpid;

    /**
     * The pid of this node.
     */
    private String pid;

    /**
     * The id of this node.
     */
    private String agentId;

    /**
     * The id of the desired parent of this node.
     */
    private String desiredParentId;

    /**
     * A constructor to create an instance of a nodeModel.
     * 
     * @param fpid
     *            the factory pid of this node.
     * @param pid
     *            The pid of this node.
     * 
     * @param agentId
     *            the id of this node.
     * @param desiredParentId
     *            The id of the desired parent of this node.
     */
    public NodeModel(String fpid, String pid, String agentId, String desiredParentId) {
        this.fpid = fpid;
        this.pid = pid;
        this.agentId = agentId;
        this.desiredParentId = desiredParentId;
    }

    /**
     * @return the current fpid <code>String</code>.
     */
    public String getFpid() {
        return fpid;
    }

    /**
     * @param the
     *            new fpid <code>String</code>.
     */
    public void setFpid(String fpid) {
        this.fpid = fpid;
    }

    /**
     * 
     * @return the current pid <code>String</code>.
     */
    public String getPid() {
        return pid;
    }

    /**
     * @param the
     *            new pid <code>String</code>.
     */
    public void setPid(String pid) {
        this.pid = pid;
    }

    /**
     * @return the current agentId <code>String</code>.
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * @param the
     *            new AgentId <code>String</code>.
     */
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * @return the current desiredParentId <code>String</code>.
     */
    public String getDesiredParentId() {
        return desiredParentId;
    }

    /**
     * @param the
     *            new desiredParentId <code>String</code>.
     */
    public void setDesiredParentId(String desiredParentId) {
        this.desiredParentId = desiredParentId;
    }

    /**
     * This method determines which instance is "bigger". The nodes are first sorted alphabetically by desiredParentId.
     * If those are the same, they are sorted by alphabetically by agentId (to group them alphabetically under their
     * parent).
     * 
     * @param that
     *            The {@link NodeModel} instance you want to compare with this one.
     * 
     * @return a negative value if this < that, 0 if this = that and a positive value if this > that
     * 
     */
    @Override
    public int compareTo(NodeModel that) {

        int output = 0;

        // The desiredParentId is null for the Auctioneer, so it you have to check this first.
        if (this.desiredParentId == null ^ that.desiredParentId == null) {
            output = (that.desiredParentId == null) ? -1 : 1;
        } else if (this.desiredParentId == null && that.desiredParentId == null) {
            output = 0;
        } else {
            output = this.desiredParentId.compareTo(that.desiredParentId);
        }

        if (output == 0) {
            output = this.agentId.compareTo(that.agentId);
        }

        return output;
    }

    /**
     * @param obj
     *            the {@link Object} you want to check the equality of.
     * 
     * @return <code>true</code> if the instances are equals and <code>false</code> if they are not.
     */
    @Override
    public boolean equals(Object obj) {
        NodeModel that = (NodeModel) ((obj instanceof NodeModel) ? obj : null);
        if (that == null) {
            return false;
        }

        if (this == that) {
            return true;
        }

        return this.agentId.equals(that.agentId) && this.desiredParentId.equals(that.desiredParentId)
                && this.fpid.equals(that.fpid) && this.pid.equals(that.pid);
    }

    /**
     * @return a hash of this instance.
     */
    @Override
    public int hashCode() {
        return 211 * (agentId.hashCode() + desiredParentId.hashCode() + fpid.hashCode() + pid.hashCode());
    }
}
