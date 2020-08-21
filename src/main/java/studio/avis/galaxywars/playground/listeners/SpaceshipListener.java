package studio.avis.galaxywars.playground.listeners;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.server.Entity;
import net.minecraft.server.PacketPlayInSteerVehicle;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import studio.avis.galaxywars.playground.objects.ZeroGravityArmorStand;

import java.lang.reflect.Field;

public class SpaceshipListener implements Listener {

    @EventHandler
    public void spawnSpaceship(final PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().trim().toLowerCase();
        if(message.equals("/spaceship")) {
            ArmorStand spaceship = (ArmorStand) ZeroGravityArmorStand.spawn(event.getPlayer().getLocation()).getBukkitEntity();

            spaceship.setHelmet(new ItemStack(Material.MONSTER_EGGS));
            spaceship.addPassenger(event.getPlayer());
            event.setCancelled(true);
            event.getPlayer().sendMessage("Spawning a new spaceship to " + event.getPlayer().getName());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                onPacketRead(event.getPlayer(), msg);
                super.channelRead(ctx, msg);
            }
        };
        ChannelPipeline pipeline = ((CraftPlayer) event.getPlayer()).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", event.getPlayer().getName(), channelDuplexHandler);
    }

    private void onPacketRead(Player player, Object packet) {
        if(packet instanceof PacketPlayInSteerVehicle) {
            try {
                PacketPlayInSteerVehicle steerVehicle = (PacketPlayInSteerVehicle) packet;
                Entity vehicle = ((CraftPlayer) player).getHandle().getVehicle();
                if(!(vehicle instanceof ZeroGravityArmorStand)) {
                    return;
                }
                ZeroGravityArmorStand spaceship = (ZeroGravityArmorStand) vehicle;

                if(steerVehicle.d()) {
                    Field field = PacketPlayInSteerVehicle.class.getDeclaredField("d");
                    field.setAccessible(true);
                    field.set(steerVehicle, false);
                    field.setAccessible(false);

                    spaceship.speed = Math.max(0, spaceship.speed - 1);
                }

                if(steerVehicle.c()) {
                    Field field = PacketPlayInSteerVehicle.class.getDeclaredField("c");
                    field.setAccessible(true);
                    field.set(steerVehicle, false);
                    field.setAccessible(false);

                    spaceship.speed = Math.min(50, spaceship.speed + 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
