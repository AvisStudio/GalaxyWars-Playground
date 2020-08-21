package studio.avis.galaxywars.playground.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import studio.avis.galaxywars.playground.objects.ZeroGravityArmorStand;

import java.util.List;

public class ZeroGravityItemListener implements Listener {

    public static final int FLOATING_ITEMS_LIFETIME = 10; // seconds

    public ZeroGravityItemListener(JavaPlugin plugin) {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            List<World> worlds = Bukkit.getWorlds();
            worlds.forEach(world -> {
                world.getEntitiesByClass(ArmorStand.class).forEach(item -> {
                    if(!item.getPassengers().isEmpty()) {
                        return;
                    }
                    if(item.getTicksLived() > 20 * FLOATING_ITEMS_LIFETIME) {
                        item.remove();
                    }
                });
            });
        }, 2, 2);
    }

    @EventHandler
    public void onItemSpawn(final ItemSpawnEvent event) {
        Item item = event.getEntity();

        ZeroGravityArmorStand hover = ZeroGravityArmorStand.spawn(event.getLocation());
        ArmorStand armorStand = (ArmorStand) hover.getBukkitEntity();

        armorStand.setItemInHand(item.getItemStack());
        armorStand.setVelocity(item.getVelocity());

        event.setCancelled(true);
    }
}
