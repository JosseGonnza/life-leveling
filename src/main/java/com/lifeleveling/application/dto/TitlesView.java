package com.lifeleveling.application.dto;

import com.lifeleveling.domain.title.TitleInventory;
import com.lifeleveling.domain.title.TitleType;

import java.util.ArrayList;
import java.util.List;

/**
 * Read model de Hall of Fame: sellos equipados + colección completa (con estado de cada título).
 */
public record TitlesView(
        List<TitleEntryView> equipped,
        List<TitleEntryView> collection,
        int usedSlots,
        int availableSlots,
        int unlockedCount,
        int totalCount
) {
    public record TitleEntryView(
            String type,
            String name,
            String category,
            String buffs,
            String requirement,
            String lore,
            boolean unlocked,
            boolean equipped
    ) {}

    public static TitlesView from(TitleInventory titles, int playerLevel) {
        List<TitleEntryView> collection = new ArrayList<>();
        List<TitleEntryView> equipped = new ArrayList<>();
        for (TitleType type : TitleType.values()) {
            TitleEntryView entry = new TitleEntryView(
                    type.name(),
                    type.getDisplayName(),
                    type.getCategory().getDisplayName(),
                    type.getBuffsDisplay(),
                    type.getRequirementDisplay(),
                    type.getLore(),
                    titles.hasTitle(type),
                    titles.isEquipped(type));
            collection.add(entry);
            if (entry.equipped()) equipped.add(entry);
        }
        return new TitlesView(equipped, collection,
                titles.getEquippedCount(), titles.getAvailableSlots(playerLevel),
                titles.getUnlockedCount(), TitleType.values().length);
    }
}
