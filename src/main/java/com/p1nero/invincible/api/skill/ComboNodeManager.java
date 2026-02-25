package com.p1nero.invincible.api.skill;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public class ComboNodeManager {

    private static int currentId = 0;

    private static final Map<Integer, ComboNode> NODES = new HashMap<>();
    private static final Map<String, ComboNode> NODES_BY_NAME = new HashMap<>();

    public static int getNodeSize() {
        return NODES.size();
    }

    public static ComboNode get(int id) {
        return NODES.get(id);
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

    public static void assignName(ComboNode node, String name) {
        if(NODES_BY_NAME.containsKey(name)) {
            throw new IllegalStateException("Node name ["+name+"] is already exist!");
        }
        NODES_BY_NAME.put(name, node);
    }

    public static ComboNode getNodesByName(String name) {
        return NODES_BY_NAME.get(name);
    }
}
