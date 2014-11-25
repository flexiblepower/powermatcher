package net.powermatcher.visualisation;

import java.util.ArrayList;
import java.util.List;

public class VisualElement {

    private static int counter;

    private Kind kind;
    private String name;
    private String id;
    private List<VisualElement> children;

    public VisualElement(Kind kind, String name) {
        this.kind = kind;
        this.name = name;
        this.id = Integer.toString(counter++);
        this.children = new ArrayList<>();
    }

    public void addChild(VisualElement visualElement) {
        children.add(visualElement);
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<VisualElement> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (VisualElement v : children) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(v.getId());
        }

        return "{\"selected\":false,\"mouse\":false,\"x\":100,\"y\":100,\"mx\":100,\"my\":-46,\"childBlocks\":["
                + sb.toString() + "],\"kind\":\"" + this.kind.getDescription() + "\"," + "\"name\":\"" + this.name
                + "\",\"clss\":0,\"clssName\":\"" + this.name + "\",\"id\":\"" + this.id
                + "\",\"depth\":0,\"treeSortValue\":0}";
    }
}
