package com.softwaremagico.kt.utils;

import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.persistence.entities.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GroupUtils {
    private GroupUtils() {

    }

    public static Map<Integer, List<Group>> orderByLevel(List<Group> groups) {
        final Map<Integer, List<Group>> sortedGroups = new HashMap<>();
        groups.forEach(group -> {
            sortedGroups.computeIfAbsent(group.getLevel(), k -> new ArrayList<>());
            sortedGroups.get(group.getLevel()).add(group);
        });
        return sortedGroups;
    }

    public static Map<Integer, List<GroupDTO>> orderDTOByLevel(List<GroupDTO> groups) {
        final Map<Integer, List<GroupDTO>> sortedGroups = new HashMap<>();
        groups.forEach(group -> {
            sortedGroups.computeIfAbsent(group.getLevel(), k -> new ArrayList<>());
            sortedGroups.get(group.getLevel()).add(group);
        });
        return sortedGroups;
    }
}
