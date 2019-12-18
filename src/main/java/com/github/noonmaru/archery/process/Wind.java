package com.github.noonmaru.archery.process;

import com.github.noonmaru.archery.ArcheryConfig;
import com.github.noonmaru.customentity.CustomEntityPacket;
import com.github.noonmaru.math.Vector;
import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.entity.TapArmorStand;
import com.github.noonmaru.tap.packet.Packet;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Collection;

/**
 * @author Nemo
 */
public class Wind
{
    private final TapArmorStand indi;

    private final Vector vector = new Vector();

    private float size = 1;

    public Wind()
    {
        indi = Tap.ENTITY.createEntity(ArmorStand.class);
        indi.setInvisible(true);
        indi.setEquipment(EquipmentSlot.HEAD, Tap.ITEM.newItemStack("arrow", 1, 0));

        Vector pos = ArcheryConfig.indicatorPos;
        indi.setPosition(pos.x, pos.y, pos.z);
    }

    public void spawnTo(Collection<? extends Player> players)
    {
        Packet.ENTITY.spawnMob(indi.getBukkitEntity()).sendTo(players);
        Packet.ENTITY.metadata(indi.getBukkitEntity()).sendTo(players);
        Packet.ENTITY.equipment(indi.getId(), EquipmentSlot.HEAD, indi.getEquipment(EquipmentSlot.HEAD)).sendTo(players);
    }

    public void updateCustomEntity(Collection<? extends Player> players)
    {
        CustomEntityPacket.register(indi.getId()).sendTo(players);
        CustomEntityPacket.scale(indi.getId(), size, size, size, 1).sendAll();
    }

    private void updateDirection()
    {
        double length = vector.length();
        Vector v = vector.copy().devide(vector.length());
        float yaw = (float) -Math.toDegrees(Math.atan2(v.x, v.z));
        float pitch = (float) -Math.toDegrees(Math.asin(v.y));

        indi.setPositionAndRotation(indi.getPosX(), indi.getPosY(), indi.getPosZ(), yaw + 90, 0F);
        indi.setHeadPose(0, 0.0F, pitch + 45);
        size = Math.max((float) length, 0.5F);
        Packet.ENTITY.metadata(indi.getBukkitEntity()).sendAll();
        Vector pos = ArcheryConfig.indicatorPos;
        Packet.ENTITY.teleport(indi.getBukkitEntity(), pos.x, pos.y - size / 2D, pos.z, indi.getYaw(), 0F, false).sendAll();
        CustomEntityPacket.scale(indi.getId(), size, size, size, 1).sendAll();
    }

    public void next()
    {
        vector.random().normalize().multiply(Math.random() * ArcheryConfig.maxWindSpeed);
        updateDirection();
    }

    public Vector get()
    {
        return vector;
    }

    public double getSpeed()
    {
        return vector.length();
    }

    public void destroy()
    {
        Packet.ENTITY.destroy(indi.getId()).sendAll();
    }


}
