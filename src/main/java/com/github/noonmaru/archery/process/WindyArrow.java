package com.github.noonmaru.archery.process;

import com.github.noonmaru.archery.ArcheryConfig;
import com.github.noonmaru.math.Vector;
import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.block.TapBlock;
import com.github.noonmaru.tap.block.TapBlockData;
import com.github.noonmaru.tap.entity.TapArmorStand;
import com.github.noonmaru.tap.firework.FireworkEffect;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.math.BlockPoint;
import com.github.noonmaru.tap.math.RayTraceResult;
import com.github.noonmaru.tap.packet.Packet;
import com.github.noonmaru.tap.world.TapWorld;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.NumberConversions;

import java.util.Collection;

/**
 * @author Nemo
 */
public class WindyArrow
{
    private final Wind wind;

    private final World world;

    private final TapArmorStand arrow;

    private final Vector pos;

    private final Vector move;

    private final Archer archer;

    private int ticks;

    private boolean flying;

    public WindyArrow(Wind wind, World world, Vector pos, Vector move, Archer archer, TapItemStack arrowItem)
    {
        this.wind = wind;
        this.world = world;
        this.arrow = Tap.ENTITY.createEntity(ArmorStand.class);
        this.pos = pos;
        this.move = move;
        this.archer = archer;
        this.flying = true;

        arrow.setInvisible(true);
        arrow.setGlowing(true);
        arrow.setEquipment(EquipmentSlot.HEAD, arrowItem);
        updatePosition(false);
    }

    public void spawnTo(Collection<? extends Player> players)
    {
        Packet.ENTITY.spawnMob(arrow.getBukkitEntity()).sendTo(players);
        Packet.ENTITY.metadata(arrow.getBukkitEntity()).sendTo(players);
        Packet.ENTITY.equipment(arrow.getId(), EquipmentSlot.HEAD, arrow.getEquipment(EquipmentSlot.HEAD)).sendTo(players);
    }

    public Vector getPos()
    {
        return pos;
    }

    public Vector getMove()
    {
        return move;
    }

    public Archer getArcher()
    {
        return archer;
    }

    public boolean isFlying()
    {
        return flying;
    }

    public void onUpdate()
    {
        ticks++;
        move.multiply(0.99F);
        move.y -= ArcheryConfig.gravity / 20.0D;
        Vector v = wind.get().copy().subtract(this.move);
        move.add(v.multiply(0.01D));

        Vector from = pos;
        Vector to = from.copy().add(move);
        RayTraceResult hitResult = Tap.MATH.rayTraceBlock(world, from, to, 0);

        if (hitResult != null)
        {
            flying = false;
            to.set(hitResult.getX(), hitResult.getY(), hitResult.getZ()).subtract(move.copy().normalize().multiply(0.5)); // 박힌 자리에서 반칸 뒤로

            BlockPoint bp = hitResult.getBlockPoint();
            TapBlockData blockData = Tap.WORLD.fromWorld(world).getBlock(bp.x, bp.y, bp.z);

            if (blockData.getBlock() == Tap.BLOCK.getBlock("concrete"))
            {
                int data = blockData.toLegacyData();
                int point;
                FireworkEffect.Builder builder = FireworkEffect.builder().type(FireworkEffect.Type.SMALL_BALL);

                switch (data)
                {
                    case 0: //white
                        point = 20;
                        builder.color(0xa6aaab);
                        break;
                    case 7: //grey
                        point = 40;
                        builder.color(0x2c2e32);
                        break;
                    case 3:
                        point = 60;
                        builder.color(0x1c6d9e);
                        break;
                    case 14:
                        point = 80;
                        builder.color(0x721a1a);
                        break;
                    case 4: //yellow
                        point = 100;
                        builder.color(0xc18c11).trail(true).type(FireworkEffect.Type.STAR);
                        break;
                    default:
                        point = 0;
                }

                if (point > 0)
                {
                    Score score = archer.getTeam().getScore();
                    score.setScore(score.getScore() + point);
                    Packet.EFFECT.firework(builder.build(), to.x, to.y, to.z).sendAll();
                    Packet.TITLE.compound("", String.valueOf(point), 0, 20, 10).sendAll();
                }
            }
        }

        if (flying && ticks >= ArcheryConfig.maxArrowFlyingTick)
            flying = false;

        pos.set(to);
        updatePosition(true);
    }

    private void updatePosition(boolean packet)
    {
        Vector v = move.copy().normalize();
        float yaw = (float) -Math.toDegrees(Math.atan2(v.x, v.z));
        float pitch = (float) -Math.toDegrees(Math.asin(v.y));

        arrow.setPositionAndRotation(pos.x, pos.y - 0.5D, pos.z, yaw + 90, 0F);
        arrow.setHeadPose(0, 0F, pitch + 45);

        if (packet)
        {
            Packet.ENTITY.metadata(arrow.getBukkitEntity()).sendAll();
            Packet.ENTITY.teleport(arrow.getBukkitEntity(), arrow.getPosX(), arrow.getPosY(), arrow.getPosZ(), arrow.getYaw(), 0F, false).sendAll();
        }

        if (!flying)
        {
            arrow.setGlowing(false);

            Packet.ENTITY.metadata(arrow.getBukkitEntity()).sendAll();
        }
    }

    public void destroy()
    {
        Packet.ENTITY.destroy(arrow.getId()).sendAll();
    }
}
