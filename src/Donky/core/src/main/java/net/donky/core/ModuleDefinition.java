package net.donky.core;

/**
 * Describes module definition to subscribe in core module.
 *
 * Created by Marcin Swierczek
 * 17/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class ModuleDefinition {

    private String name;

    private String version;

    /**
     * The module details.
     *
     * @param name The module name.
     * @param version￼The module version, format a.b.c.d
     */
    public ModuleDefinition(String name, String version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Get the module name.
     *
     * @return The module name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the module version, format a.b.c.d
     *
     * @return ￼￼The module version, format a.b.c.d
     */
    public String getVersion() {
        return version;
    }

}
