package com.github.noonmaru.archery.process;

import com.github.noonmaru.archery.ArcheryConfig;
import com.github.noonmaru.math.Vector;
import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.entity.TapPlayer;
import com.github.noonmaru.tap.inventory.TapPlayerInventory;
import com.github.noonmaru.tap.item.TapItem;
import com.github.noonmaru.tap.item.TapItemStack;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author Nemo
 */
public class Archer
{
    private final ArcheryProcess process;

    private final UUID uniqueId;

    private String name;

    private Player player;

    private TapPlayer tapPlayer;

    private final ArcherTeam team;

    private int count;

    private WindyArrow arrow;

    private int afterDelay;

    private int ticks;

    private boolean needToReady;

    public Archer(ArcheryProcess process, Player player, ArcherTeam team)
    {
        this.process = process;
        this.uniqueId = player.getUniqueId();
        this.team = team;
        setPlayer(player);
    }

    public void setPlayer(Player player)
    {
        if (player != null)
        {
            name = player.getName();
            this.player = player;
            this.tapPlayer = TapPlayer.wrapPlayer(player);

            if (needToReady)
                readyUpdate();
        }
        else
        {
            this.player = null;
            this.tapPlayer = null;
        }
    }

    public UUID getUniqueId()
    {
        return uniqueId;
    }

    public String getName()
    {
        return name;
    }

    public Player getPlayer()
    {
        return player;
    }

    public TapPlayer getTapPlayer()
    {
        return tapPlayer;
    }

    public ArcherTeam getTeam()
    {
        return team;
    }

    private static final TapItemStack ITEM_BOW = Tap.ITEM.newItemStack("bow", 1, 0);

    static
    {
        ITEM_BOW.getItemMeta().setUnbreakable(true);
    }

    public void ready()
    {
        this.count = ArcheryConfig.shootCount;
        this.ticks = 0;

        if (isOnline())
        {
            readyUpdate();
        }
        else
        {
            needToReady = true;
        }
    }

    public void readyUpdate()
    {
        needToReady = false;

        TapPlayerInventory inv = tapPlayer.getInventory();
        inv.clear();

        inv.addItem(ITEM_BOW.copy());
        inv.addItem(Tap.ITEM.newItemStack("arrow", count, 0));
        inv.update();
        inv.setHeldItemSlot(0);

        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(ArcheryConfig.archerLoc);
    }

    public void setObserver()
    {
        tapPlayer.getInventory().clear();
        player.setGameMode(GameMode.SPECTATOR);
    }

    public void onUpdate()
    {
        if (player.getLocation().distance(ArcheryConfig.archerLoc) > 5)
            player.teleport(ArcheryConfig.archerLoc);


        if (ticks++ % ArcheryConfig.windChangeTick == 0)
            process.getWind().next();

        if (afterDelay > 0)
        {
            afterDelay--;

            if (afterDelay <= 0)
            {
                player.setGameMode(GameMode.SPECTATOR);
                player.getInventory().clear();
                player.teleport(ArcheryConfig.specLoc);
            }
        }

        if (arrow != null)
        {
            arrow.onUpdate();

            if (!arrow.isFlying())
            {
                arrow = null;
                ticks = 0;

                if (count <= 0)
                    afterDelay = 40;
            }
        }
    }

    private static final Vector OFFSET = new Vector(0.15, 0, 0);

    public boolean launchArrow(float force)
    {
        if (count <= 0 || arrow != null)
            return false;

        count--;

        Location eyeLoc = player.getEyeLocation();
        Vector pos = new Vector(eyeLoc.getX(), eyeLoc.getY(), eyeLoc.getZ()).add(OFFSET.copy().rotateAxisX(eyeLoc.getPitch()).rotateAxisY(eyeLoc.getYaw()));
        org.bukkit.util.Vector v = eyeLoc.getDirection();
        Vector velocity = new Vector(v.getX(), v.getY(), v.getZ()).multiply(ArcheryConfig.maxArrowSpeed / 20 * force);

        List<TapItemStack> arrowItems = ArcheryConfig.arrowItems;

        arrow = new WindyArrow(process.getWind(), player.getWorld(), pos, velocity, this, arrowItems.get(team.getArrowItemIndex() % arrowItems.size()));
        process.addArrow(arrow);


        System.out.println(team.getArrowItemIndex());
        TapPlayerInventory inv = tapPlayer.getInventory();
        TapItem itemArrow = Tap.ITEM.getItem("arrow");

        for (TapItemStack item : inv.getInventoryContents())
        {
            if (item != null && item.getItem() == itemArrow)
            {
                item.setAmount(count);
                break;
            }
        }

        return true;
    }

    public boolean isEnd()
    {
        return this.arrow == null && this.count <= 0 && afterDelay <= 0;
    }

    public boolean isOnline()
    {
        return player != null;
    }
}
