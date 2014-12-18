package net.powermatcher.visualisation.models;

/**
 * A data object representing a node in the visualizer tree.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
public class NodeModel implements Comparable<NodeModel> {
    /**
     * the factory pid of this node. This will be used by
     */
    private String fpid;
    /**
     * The pid of this node. This will be used by the Felix webconsole to modify and delete this instance
     */
    private String pid;
    /**
     * the agentId of this node.
     */
    private String agentId;
    private String desiredParentId;

    public NodeModel(String fpid, String pid, String agentId, String desiredParentId) {
        this.fpid = fpid;
        this.pid = pid;
        this.agentId = agentId;
        // TODO Gson won't add null values in the serialisation
        this.desiredParentId = (desiredParentId == null ? "null" : desiredParentId);
    }

    public String getFpid() {
        return fpid;
    }

    public void setFpid(String fpid) {
        this.fpid = fpid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getDesiredParentId() {
        return desiredParentId;
    }

    public void setDesiredParentId(String desiredParentId) {
        this.desiredParentId = desiredParentId == null ? "null" : desiredParentId;
    }

    @Override
    public int compareTo(NodeModel that) {
        // Sorts alphabetically

        int output = this.desiredParentId.compareTo(that.desiredParentId);

        // if the desiredParents are the same, they have to be sorted in their own level
        if (output == 0) {
            output = this.agentId.compareTo(that.agentId);
        }

        return output;
    }

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

    @Override
    public int hashCode() {
        return 211 * (agentId.hashCode() + desiredParentId.hashCode() + fpid.hashCode() + pid.hashCode());
    }
}
