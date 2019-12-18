package com.github.noonmaru.archery;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * @author Nemo
 */
public class ConfigReloader implements Runnable
{
    private final File file;

    private long lastModified;

    public ConfigReloader(File file)
    {
        this.file = file;
    }

    @Override
    public void run()
    {
        long last = file.lastModified();

        if (last != this.lastModified)
        {
            lastModified = last;
            YamlConfiguration config =YamlConfiguration.loadConfiguration(file);
            ArcheryConfig.load(config);
            System.out.println("Config reloaded!");
        }
    }
}
