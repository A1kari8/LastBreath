package org.a1kari8.mc.lastbreath;

import java.util.*;

public class ServerDyingManager {
    private static final Set<UUID> dyingList = new HashSet<UUID>();

    public static void addDying(UUID uuid) {
        dyingList.add(uuid);
    }

    public static void removeDying(UUID uuid) {
        dyingList.remove(uuid);
    }

    public static List<UUID> getDying(){
        return dyingList.stream().toList();
    }
}
