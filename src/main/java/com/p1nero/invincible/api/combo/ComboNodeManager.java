package com.p1nero.invincible.api.combo;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ComboNodeManager {

    private static int currentId = 0;

    private static final Map<Integer, ComboNode> NODES = new HashMap<>();

    public static int getNodeSize() {
        return NODES.size();
    }

    public static ComboNode get(int id) {
        return NODES.get(id);
    }

    public static Collection<ComboNode> getNodes() {
        return NODES.values();
    }

    @ApiStatus.Internal
    public static void assignId(ComboNode node) {
        if(node.isAssigned()){
            return;
        }
        currentId++;
        NODES.put(currentId, node);
        node.assign(currentId);
    }

}
