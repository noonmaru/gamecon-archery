package com.github.noonmaru.archery;

import com.github.noonmaru.archery.process.ArcheryProcess;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public final class ArcheryPlugin extends JavaPlugin
{
    private ArcheryProcess process;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();

        getServer().getScheduler().runTaskTimer(this, new ConfigReloader(new File(getDataFolder(), "config.yml")), 0, 1);
    }

    @Override
    public void onDisable()
    {
        stopProcess();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0)
            return false;

        String sub = args[0];

        if ("start".equalsIgnoreCase(sub))
        {
            if (process != null)
            {
                sender.sendMessage("게임이 이미 진행중입니다.");
                return true;
            }

            if (args.length < 2)
            {
                sender.sendMessage("/" + label + " " + sub + " team | solo");
            }
            else
            {
                String match = args[1];

                if ("team".equalsIgnoreCase(match))
                {
                    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

                    for (Team team : scoreboard.getTeams())
                    {
                        team.unregister();
                    }

                    Set<Team> teams = new LinkedHashSet<>();

                    for (ArcheryConfig.TeamConfig teamConfig : ArcheryConfig.teamConfigs)
                    {
                        List<Player> players = new ArrayList<>();

                        for (String entry : teamConfig.getEntries())
                        {
                            Player player = Bukkit.getPlayerExact(entry);

                            if (player != null)
                                players.add(player);
                        }

                        if (players.size() > 0)
                        {
                            Team team = scoreboard.registerNewTeam(teamConfig.getName());
                            team.setPrefix(ChatColor.translateAlternateColorCodes('&', teamConfig.getPrefix()));

                            for (Player player : players)
                            {
                                team.addEntry(player.getName());
                            }

                            teams.add(team);
                        }
                    }

                    if (teams.isEmpty())
                    {
                        sender.sendMessage("게임 참가자가 없습니다. config.yml을 확인하세요");
                        return true;
                    }

                    startProcess(teams);
                }
                else if ("solo".equalsIgnoreCase(match))
                {
                    Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                    Set<Team> teams = new LinkedHashSet<>();

                    for (Team team : scoreboard.getTeams())
                    {
                        team.unregister();
                    }

                    for (Player player : Bukkit.getOnlinePlayers())
                    {
                        GameMode gameMode = player.getGameMode();

                        if (gameMode == GameMode.ADVENTURE || gameMode == GameMode.SURVIVAL)
                        {
                            String name = player.getName();
                            Team team = scoreboard.registerNewTeam(name);
                            team.setPrefix("§" + Integer.toHexString(name.hashCode() & 0xF));
                            team.addEntry(name);
                            teams.add(team);
                        }
                    }

                    if (teams.isEmpty())
                    {
                        sender.sendMessage("게임 참가자가 없습니다. config.yml을 확인하세요");
                        return true;
                    }

                    startProcess(teams);
                }
                else
                {
                    sender.sendMessage("/" + label + " " + sub + " team | solo");
                }
            }
        }
        else if ("stop".equalsIgnoreCase(sub))
            stopProcess();
        else
            sender.sendMessage("Unknown command");

        return true;
    }

    public void startProcess(Set<Team> teams)
    {
        if (process != null)
            return;

        process = new ArcheryProcess(this, teams);

        //print entry
        Logger logger = getLogger();
        for (Team team : teams)
        {
            Set<String> entries = team.getEntries();
            StringBuilder builder = new StringBuilder();
            builder.append(team.getName()).append(" (").append(entries.size()).append("):");

            for (String entry : entries)
            {
                builder.append(" ").append(entry);
            }

            logger.info(builder.toString());
        }
    }

    public void stopProcess()
    {
        if (process == null)
            return;

        process.unregister();
        process = null;
    }
}
