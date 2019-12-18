package com.github.noonmaru.archery.process;

import com.github.noonmaru.tap.packet.Packet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * @author Nemo
 */
public class ArcheryScheduler implements Runnable
{
    private final ArcheryProcess process;

    private Task task;

    public ArcheryScheduler(ArcheryProcess process)
    {
        this.process = process;
        this.task = new TitleTask();
    }

    @Override
    public void run()
    {
        task = task.execute();

        if (task == null)
            process.stop();
    }

    interface Task
    {
        Task execute();
    }

    private class TitleTask implements Task
    {
        private int ticks;

        @Override
        public Task execute()
        {
            if (ticks == 0)
            {
                Packet.TITLE.compound(ChatColor.AQUA + "양궁", ChatColor.STRIKETHROUGH + "바람은 계산하는 것이다", 5, 50, 5).sendAll();
            }

            if (++ticks < 60)
                return this;

            return new ReadyTask();
        }
    }

    private class ReadyTask implements Task
    {
        @Override
        public Task execute()
        {
            Archer archer = process.nextArcher();

            if (archer == null)
            {
                return new ResultTask();
            }
            else
            {
                archer.ready();
                String teamName = archer.getTeam().getTeam().getName();
                String archerName = archer.getName();
                Packet.TITLE.compound(teamName.equals(archerName) ? "" : archer.getTeam().getDisplayName(), archerName, 0, 40, 10).sendAll();

                return new ArcherTask();
            }
        }
    }

    private class ArcherTask implements Task
    {
        @Override
        public Task execute()
        {
            Archer archer = process.getCurrentArcher();

            if (archer.isOnline())
            {
                archer.onUpdate();

                if (archer.isEnd())
                {
                    return new ReadyTask();
                }
            }

            return this;
        }
    }

    private class ResultTask implements Task
    {
        @Override
        public Task execute()
        {
            ArrayList<ArcherTeam> teams = new ArrayList<>(process.getTeams());
            teams.sort((o1, o2) -> Integer.compare(o2.getScore().getScore(), o1.getScore().getScore()));

            Packet.TITLE.compound(ChatColor.RED + "게임종료!", "우승: " + teams.get(0).getDisplayName(), 5, 60, 10).sendAll();

            int i = 1;

            for (ArcherTeam team : teams)
            {
                Bukkit.broadcastMessage(i++ + ".  " + team.getDisplayName());
            }

            return null;
        }
    }
}
