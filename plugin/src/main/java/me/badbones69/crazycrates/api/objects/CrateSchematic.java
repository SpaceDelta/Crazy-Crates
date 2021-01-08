package me.badbones69.crazycrates.api.objects;

import java.io.File;

public class CrateSchematic {

    private String schematicName;
    private File schematicFile;

    public CrateSchematic(String schematicName, File schematicFile) {
        this.schematicName = schematicName;
        this.schematicFile = schematicFile;
    }

    /**
     * @return Returns the name of the schematic.
     */
    public String getSchematicName() {
        return schematicName;
    }

    /**
     * @return Returns the file where the schematic is located.
     */
    public File getSchematicFile() {
        return schematicFile;
    }

}