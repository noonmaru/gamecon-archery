package com.github.noonmaru.archery.process;

import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

import java.util.LinkedHashSet;

/**
 * @author Nemo
 */
public class ArcherTeam
{
    private final LinkedHashSet<Archer> archers = new LinkedHashSet<>();

    private final Team team;

    private final Score score;

    private int arrowItemIndex;

    public ArcherTeam(Team team, Score score)
    {
        this.team = team;
        this.score = score;
    }

    public Team getTeam()
    {
        return team;
    }

    public Score getScore()
    {
        return score;
    }

    public void setArrowItemIndex(int arrowItemIndex)
    {
        this.arrowItemIndex = arrowItemIndex;
    }

    public int getArrowItemIndex()
    {
        return arrowItemIndex;
    }

    public void addArcher(Archer archer)
    {
        archers.add(archer);
    }

    public LinkedHashSet<Archer> getArchers()
    {
        return archers;
    }

    public String getDisplayName()
    {
        return team.getPrefix() + team.getDisplayName() + team.getSuffix();
    }
}
