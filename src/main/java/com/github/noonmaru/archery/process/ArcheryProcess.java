package com.github.noonmaru.archery.process;

import com.github.noonmaru.archery.ArcheryConfig;
import com.github.noonmaru.archery.ArcheryPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

/**
 * @author Nemo
 */
public class ArcheryProcess
{
    private final ArcheryPlugin plugin;

    private final Wind wind = new Wind();

    private final ArcheryListener listener;

    private final BukkitTask task;

    private final Set<ArcherTeam> teams = new LinkedHashSet<>();

    private final Map<UUID, Archer> archers = new HashMap<>();

    private final Map<Player, Archer> onlineArchers = new IdentityHashMap<>();

    private final Queue<Archer> order = new LinkedList<>();

    private int count;

    private Archer currentArcher;

    private final ArrayList<WindyArrow> arrows = new ArrayList<>();

    public ArcheryProcess(ArcheryPlugin plugin, Set<Team> teams)
    {
        this.plugin = plugin;

        //objective setting
        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("archery");
        if (objective != null)
            objective.unregister();
        objective = scoreboard.registerNewObjective("archery", "dummy");
        objective.setDisplayName("    " + ChatColor.AQUA + "양궁    ");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (Team team : teams)
        {
            ArcherTeam archerTeam = new ArcherTeam(team, objective.getScore(team.getPrefix() + team.getName()));

            for (String entry : team.getEntries())
            {
                Player player = Bukkit.getPlayerExact(entry);

                if (player != null)
                {
                    if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE)
                        continue;

                    Archer archer = new Archer(this, player, archerTeam);
                    archerTeam.addArcher(archer);
                }
            }

            if (archerTeam.getArchers().isEmpty())
                continue;

            archerTeam.setArrowItemIndex(this.teams.size());
            this.teams.add(archerTeam);

            for (Archer archer : archerTeam.getArchers())
            {
                archers.put(archer.getUniqueId(), archer);
                onlineArchers.put(archer.getPlayer(), archer);
                archer.getPlayer().setGameMode(GameMode.SPECTATOR);
                archer.getPlayer().teleport(ArcheryConfig.specLoc);
            }
        }

        this.listener = new ArcheryListener(this);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, new ArcheryScheduler(this), 0, 1);
        this.count = ArcheryConfig.roundCount;

        //순서 계산 매우 귀찮으므로 통째로 큐에 밀어넣는다
        for (int i = 0; i < ArcheryConfig.roundCount; i++)
        {
            for (ArcherTeam team : this.teams)
            {
                for (Archer archer : team.getArchers())
                {
                    order.offer(archer);
                }
            }
        }

        wind.spawnTo(Bukkit.getOnlinePlayers());
        wind.updateCustomEntity(Bukkit.getOnlinePlayers());
    }

    public void onJoin(Player player)
    {
        Archer archer = this.archers.get(player.getUniqueId());

        if (archer != null)
        {
            archer.setPlayer(player);
            onlineArchers.put(player, archer);
        }

        if (currentArcher != null && currentArcher != archer)
        {
            GameMode mode = player.getGameMode();

            if (mode == GameMode.ADVENTURE || mode == GameMode.SURVIVAL)
            {
                player.setGameMode(GameMode.SPECTATOR);
            }
        }

        Collection<? extends Player> single = Collections.singleton(player);

        for (WindyArrow arrow : arrows)
        {
            arrow.spawnTo(single);
        }

        wind.spawnTo(single);
    }

    public void onQuit(Player player)
    {
        Archer archer = this.onlineArchers.remove(player);

        if (archer != null)
            archer.setPlayer(null);
    }

    public Archer getOnlineArcher(Player player)
    {
        return onlineArchers.get(player);
    }

    public Collection<Archer> getArchers()
    {
        return archers.values();
    }

    public Collection<Archer> getOnlineArchers()
    {
        return onlineArchers.values();
    }

    public void unregister()
    {
        HandlerList.unregisterAll(this.listener);
        task.cancel();

        for (WindyArrow arrow : arrows)
        {
            arrow.destroy();
        }

        wind.destroy();
    }

    public void stop()
    {
        this.plugin.stopProcess();
    }

    public Archer nextArcher()
    {
        return this.currentArcher = order.poll();
    }

    public Archer getCurrentArcher()
    {
        return currentArcher;
    }

    public void addArrow(WindyArrow arrow)
    {
        arrows.add(arrow);
        arrow.spawnTo(Bukkit.getOnlinePlayers());
    }

    public Wind getWind()
    {
        return wind;
    }

    public Set<ArcherTeam> getTeams()
    {
        return teams;
    }

    public void updateCustomEntity(Player player)
    {
        wind.updateCustomEntity(Collections.singleton(player));
    }
}
