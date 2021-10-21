package com.runelitetts.npc;

/**
 * This class stores NPC metadata scraped from the OSRS wiki. This class is necessary, as the RuneLite API and original
 * game don't provide this information.
 */
public class NPCMetaData {

    /**
     * An enumerated type for the NPC.
     */
    public enum NPCType {
        MAN,
        WOMAN,
        CHILD_BOY,
        CHILD_GIRL,
        MONSTER
    }

    private String name;
    private NPCType npcType;

    /**
     * Construct a new type using parameters.
     * @param name
     * @param npcType
     */
    public NPCMetaData(final String name, final NPCType npcType) {
        this.name = name;
        this.npcType = npcType;
    }

    /**
     * Returns the string name of the NPC.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the string name of the NPC.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
}
