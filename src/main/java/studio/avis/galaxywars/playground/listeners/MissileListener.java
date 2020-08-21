package studio.avis.galaxywars.playground.listeners;

import net.minecraft.server.EntityArmorStand;
import net.minecraft.server.Vector3f;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import studio.avis.galaxywars.playground.objects.ZeroGravityArmorStand;
import studio.avis.galaxywars.playground.objects.MissileInfo;
import studio.avis.galaxywars.playground.utils.BlockUtils;
import studio.avis.galaxywars.playground.utils.ParticleEffect;
import studio.avis.galaxywars.playground.utils.VectorUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MissileListener implements Listener {

    public static final int MISSILE_LIFETIME = 10; // seconds
    public static final double MISSILE_DETONATION_RADIUS = 1.5d; // blocks

    private final JavaPlugin plugin;
    private final Map<ArmorStand, MissileInfo> missiles = new HashMap<>();

    public MissileListener(JavaPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for(Iterator<ArmorStand> iterator = this.missiles.keySet().iterator(); iterator.hasNext(); ) {
                ArmorStand missile = iterator.next();
                MissileInfo info = this.missiles.get(missile);

                boolean done = false;
                // prepare to detonate the missile if there are players who is near to missile
                for(Player player : missile.getWorld().getPlayers()) {
                    if(info.getPlayer() == player) {
                        continue;
                    }
                    if(player.getLocation().distance(missile.getLocation()) < MISSILE_DETONATION_RADIUS) {
                        done = true;
                        break;
                    }
                }
                // prepare to detonate the missile if is getting closer to solid stuffs
                List<Block> blocks = BlockUtils.blocksInRadius(missile.getLocation(), MISSILE_DETONATION_RADIUS);
                if(blocks.stream().anyMatch(block -> block.getType() != Material.AIR)) {
                    done = true;
                }

                if(!missile.isValid() || missile.getTicksLived() > 20 * MISSILE_LIFETIME || done) {
                    // explosion workaround: spawn a tnt and detonate immediately
                    TNTPrimed tnt = missile.getWorld().spawn(missile.getLocation(), TNTPrimed.class);
                    tnt.setFuseTicks(0);

                    missile.remove();
                    iterator.remove();
                } else {
                    info.setB(info.getA());
                    info.setA(missile.getLocation().add(0, 1, 0));

                    if(info.getAimTo() == null) {
                        // keep missile fly if there is no desired target
                        missile.setVelocity(info.getVelocity());
                    }

                    if(info.getA() == null || info.getB() == null) {
                        continue;
                    }

                    Location location = missile.getLocation().add(0, 1, 0);
                    location.add(missile.getLocation().add(0, 1, 0).getDirection().multiply(-1.2d));

                    Vector trajectory = VectorUtils.trajectory(info.getB(), info.getA());
                    EntityArmorStand armorStand = ((CraftArmorStand) missile).getHandle();
                    armorStand.yaw = VectorUtils.yaw(trajectory);
                    armorStand.setHeadPose(new Vector3f(VectorUtils.pitch(trajectory), 0, 0));

                    ParticleEffect.CLOUD.display(location, 0f, 0f, 0f, 0f, 1);
                }
            }
        }, 0, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        // give missile launcher if the player does not have one
        ItemStack missileLauncher = new ItemStack(Material.LONG_GRASS, 1, (short) 1);
        Player player = event.getPlayer();
        if(!player.getInventory().contains(missileLauncher.getType())) {
            ItemMeta meta = missileLauncher.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + ChatColor.BOLD.toString() + "Right Click" + ChatColor.WHITE + ChatColor.BOLD.toString() + " - " + ChatColor.YELLOW + ChatColor.BOLD.toString() + "Launch Missile");
            missileLauncher.setItemMeta(meta);
            player.getInventory().setItem(0, missileLauncher);
        }
    }

    @EventHandler
    public void onRightClick(final PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack inHand = event.getPlayer().getItemInHand();
        if(inHand.getTypeId() != 31 || inHand.getData().getData() != 1) {
            return;
        }

        ArmorStand vehicle = (ArmorStand) event.getPlayer().getVehicle();
        if(vehicle == null) {
            return;
        }

        Location start = vehicle.getLocation().add(0, 1, 0).add(vehicle.getLocation().add(0, 1, 0).getDirection().multiply(1.2d));

        ZeroGravityArmorStand missile = ZeroGravityArmorStand.spawn(start);
        ArmorStand bukkit = (ArmorStand) missile.getBukkitEntity();

        missile.setSize(0.5f, 0.5f);
        bukkit.setHelmet(new ItemStack(Material.DEAD_BUSH));
        bukkit.setVelocity(vehicle.getLocation().add(0, 1, 0).getDirection().multiply(2.5d));

        missile.setHeadPose(new Vector3f(bukkit.getLocation().getPitch(), 0, 0));

        Player player = event.getPlayer();

        double minAngle = Math.PI * 2;
        LivingEntity aimTo = null;
        for(Entity entity : player.getNearbyEntities(64d, 64d, 64d)) {
            if(player.hasLineOfSight(entity) && entity instanceof Player && !entity.isDead()) {
                Vector to = entity.getLocation().toVector().clone().subtract(player.getLocation().toVector());
                double angle = bukkit.getVelocity().angle(to);
                if(angle < minAngle) {
                    minAngle = angle;
                    aimTo = (LivingEntity) entity;
                }
            }
        }
        if(aimTo != null) {
            new MissilePhysicsRunner(this.plugin, bukkit, aimTo);
        }

        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.5f);

        Vector velocity = bukkit.getLocation().getDirection().multiply(3.5d);
        this.missiles.put(bukkit, new MissileInfo(player, aimTo, velocity));
    }

    private static class MissilePhysicsRunner extends BukkitRunnable {

        public static final double MAX_ROTATION_ANGLE = 0.08d;
        public static final double TARGET_SPEED = 5d;

        private final Entity missileEntity;
        private final LivingEntity target;

        public MissilePhysicsRunner(JavaPlugin plugin, Entity missileEntity, LivingEntity target) {
            this.missileEntity = missileEntity;
            this.target = target;
            runTaskTimer(plugin, 1, 1);
        }

        @Override
        public void run() {
            double speed = this.missileEntity.getVelocity().length();
            if(this.missileEntity.isOnGround() || this.missileEntity.isDead() || this.target.isDead() || this.target.getWorld() != this.missileEntity.getWorld()) {
                cancel();
                return;
            }
            Vector to = this.target.getLocation().clone().add(new Vector(0d, 0.5d, 0d)).subtract(this.missileEntity.getLocation()).toVector();

            Vector directionVelocity = this.missileEntity.getVelocity().clone().normalize();
            Vector directionTo = to.clone().normalize();
            double angle = directionVelocity.angle(directionTo);
            double newSpeed = 0.9d * speed + (TARGET_SPEED / 10);
            Vector newVelocity;
            if(angle < MAX_ROTATION_ANGLE) {
                newVelocity = directionVelocity.clone().multiply(newSpeed);
            } else {
                Vector newDirection = directionVelocity.clone().multiply((angle - MAX_ROTATION_ANGLE) / angle).add(directionTo.clone().multiply(MAX_ROTATION_ANGLE / angle));
                newDirection.normalize();
                newVelocity = newDirection.clone().multiply(newSpeed);
            }
            this.missileEntity.setVelocity(newVelocity.add(new Vector(0d, 0.03d, 0d)));
        }
    }


}
