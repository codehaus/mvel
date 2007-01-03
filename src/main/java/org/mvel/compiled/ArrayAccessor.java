package org.mvel.compiled;

import org.mvel.AccessorNode;

import java.util.Map;

public class ArrayAccessor implements AccessorNode {
    private AccessorNode nextNode;

    private int index;

    public Object getValue(Object ctx, Object elCtx, Map vars) throws Exception {
        if (nextNode != null) {
            return nextNode.getValue(((Object[])ctx)[index], elCtx, vars);
        }
        else {
            return ((Object[])ctx)[index];
        }
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public AccessorNode getNextNode() {
        return nextNode;
    }

    public AccessorNode setNextNode(AccessorNode nextNode) {
        return this.nextNode = nextNode;
    }


    public String toString() {
        return "Array Accessor -> [" + index + "]";
    }
}
