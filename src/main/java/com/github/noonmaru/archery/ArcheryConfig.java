package com.github.noonmaru.archery;

import com.github.noonmaru.math.Vector;
import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.item.TapItem;
import com.github.noonmaru.tap.item.TapItemStack;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * @author Nemo
 */
public class ArcheryConfig
{
    public static int shootCount;

    public static double maxWindSpeed;

    public static double maxArrowSpeed;

    public static int maxArrowFlyingTick;

    public static List<TapItemStack> arrowItems;

    public static Location archerLoc;

    public static Vector indicatorPos;

    public static int windChangeTick;

    public static int roundCount;

    public static List<TeamConfig> teamConfigs;

    public static Location specLoc;

    public static double gravity;

    public static void load(ConfigurationSection config)
    {
        shootCount = config.getInt("shoot-count");
        maxWindSpeed = config.getDouble("max-wind-speed");
        maxArrowSpeed = config.getDouble("max-arrow-speed");
        maxArrowFlyingTick = config.getInt("max-arrow-flying-tick");
        arrowItems = getItems(config, "arrow-items");
        archerLoc = getLoc(config, "archer-loc");
        indicatorPos = getVec(config, "indicator-pos");
        windChangeTick = config.getInt("wind-change-tick");
        roundCount = config.getInt("round-count");
        teamConfigs = getTeamConfigs(config, "teams");
        specLoc = getLoc(config, "spectator-loc");
        gravity = config.getDouble("gravity");
    }

    private static List<TeamConfig> getTeamConfigs(ConfigurationSection config, String key)
    {
        List<TeamConfig> configs = new ArrayList<>();
        ConfigurationSection teamConfig = config.getConfigurationSection(key);

        for (Map.Entry<String, Object> entry : teamConfig.getValues(false).entrySet())
        {
            String name = entry.getKey();
            ConfigurationSection info = (ConfigurationSection) entry.getValue();
            String prefix = info.getString("prefix");
            List<String> entries = info.getStringList("entries");

            configs.add(new TeamConfig(name, prefix, entries));
        }

        return ImmutableList.copyOf(configs);
    }

    private static Vector getVec(ConfigurationSection config, String key)
    {
        ConfigurationSection vecConfig = config.getConfigurationSection(key);

        double x = vecConfig.getDouble("x");
        double y = vecConfig.getDouble("y");
        double z = vecConfig.getDouble("z");

        return new Vector(x, y, z);
    }

    private static List<TapItemStack> getItems(ConfigurationSection config, String key)
    {
        List<String> itemNames = config.getStringList(key);
        List<TapItemStack> items = new ArrayList<>(itemNames.size());

        for (String itemName : itemNames)
        {
            TapItem item = Tap.ITEM.getItem(itemName);

            if (item != null)
                items.add(Tap.ITEM.newItemStack(item, 1, 0));
        }

        return items;
    }

    private static Location getLoc(ConfigurationSection config, String key)
    {
        ConfigurationSection locConfig = config.getConfigurationSection(key);

        World world = Bukkit.getWorld(locConfig.getString("world"));
        double x = locConfig.getDouble("x");
        double y = locConfig.getDouble("y");
        double z = locConfig.getDouble("z");
        float yaw = (float) locConfig.getDouble("yaw");
        float pitch = (float) locConfig.getDouble("pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static class TeamConfig
    {
        private final String name;

        private final String prefix;

        private final LinkedHashSet<String> entries = new LinkedHashSet<>();

        public TeamConfig(String name, String prefix, Collection<String> entries)
        {
            this.name = name;
            this.prefix = prefix;
            this.entries.addAll(entries);
        }

        public String getName()
        {
            return name;
        }

        public String getPrefix()
        {
            return prefix;
        }

        public LinkedHashSet<String> getEntries()
        {
            return entries;
        }
    }
}
