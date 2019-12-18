package com.github.noonmaru.archery.process;

import com.github.noonmaru.customentity.CustomEntityPacket;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;

/**
 * @author Nemo
 */
public class ArcheryListener implements Listener
{
    private final ArcheryProcess process;

    public ArcheryListener(ArcheryProcess process)
    {
        this.process = process;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        process.onJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        process.onQuit(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event)
    {
        if (event.getChannel().equalsIgnoreCase(CustomEntityPacket.CHANNEL))
            process.updateCustomEntity(event.getPlayer());
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event)
    {
        Entity entity = event.getEntity();

        if (entity instanceof Player)
        {
            Archer archer = process.getOnlineArcher((Player) entity);

            if (archer != null)
            {
                archer.launchArrow(event.getForce());
                event.setCancelled(true);
            }
        }
    }

    //테러 방지
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();

        if (entity instanceof Player)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE)
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryInteractEvent event)
    {
        event.setCancelled(true);
    }
}
