package studio.avis.galaxywars.playground.objects;

import net.minecraft.server.EntityArmorStand;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.EnumItemSlot;
import net.minecraft.server.EnumMoveType;
import net.minecraft.server.ItemElytra;
import net.minecraft.server.ItemStack;
import net.minecraft.server.Items;
import net.minecraft.server.MathHelper;
import net.minecraft.server.Vector3f;
import net.minecraft.server.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Vector;

public class ZeroGravityArmorStand extends EntityArmorStand {

    static {
        try {
            // register the new armor stand type into the registry
            EntityTypes.d.stream()
                    .filter(key -> key.getKey().contains("armor_stand"))
                    .findFirst()
                    .ifPresent(minecraftKey -> EntityTypes.b.a(EntityType.ARMOR_STAND.getTypeId(), minecraftKey, ZeroGravityArmorStand.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int bD = 0;
    public int speed = 0;

    public ZeroGravityArmorStand(World world) {
        super(world);

        setSize(2f, 2f);
    }

    @Override
    public void n() {
        if (this.bD > 0) {
            --this.bD;
        }

        if (this.bi > 0 && !this.bI()) {
            // patch - zero gravitation y axis
            double d0 = this.locX + (this.bj - this.locX) / (double)this.bi;
            double d1 = this.locY + (this.bk - this.locY) / (double)this.bi;
            double d2 = this.locZ + (this.bl - this.locZ) / (double)this.bi;
            double d3 = MathHelper.g(this.bm - (double)this.yaw);
            this.yaw = (float)((double)this.yaw + d3 / (double)this.bi);
            this.pitch = (float)((double)this.pitch + (this.bn - (double)this.pitch) / (double)this.bi);
            this.setPosition(d0, d1, d2);
            this.setYawPitch(this.yaw, this.pitch);
        } else if (!this.cC()) {
            this.motX *= 1;
            this.motY *= 1;
            this.motZ *= 1;
        }

        if (Math.abs(this.motX) < 0.003D) {
            this.motX = 0.0D;
        }

        if (Math.abs(this.motY) < 0.003D) {
            this.motY = 0.0D;
        }

        if (Math.abs(this.motZ) < 0.003D) {
            this.motZ = 0.0D;
        }

        this.world.methodProfiler.a("ai");
        if (this.isFrozen()) {
            this.bd = false;
            this.be = 0.0F;
            this.bg = 0.0F;
            this.bh = 0.0F;
        } else if (this.cC()) {
            this.world.methodProfiler.a("newAi");
            this.doTick();
            this.world.methodProfiler.b();
        }

        this.world.methodProfiler.b();
        this.world.methodProfiler.a("jump");
        if (this.bd) {
            if (this.isInWater()) {
                this.cv();
            } else if (this.au()) {
                this.cw();
            } else if (this.onGround && this.bD == 0) {
                this.cu();
                this.bD = 10;
            }
        } else {
            this.bD = 0;
        }

        this.world.methodProfiler.b();
        this.world.methodProfiler.a("travel");
        // patch - no air resistance
        this.r();
        this.a(this.be, this.bf, this.bg);
        this.world.methodProfiler.b();
        this.world.methodProfiler.a("push");
        this.cB();
        this.world.methodProfiler.b();
    }

    /* Override */
    public void r() {
        boolean flag = this.getFlag(7);
        if (flag && !this.onGround && !this.isPassenger()) {
            ItemStack itemstack = this.getEquipment(EnumItemSlot.CHEST);
            if (itemstack.getItem() == Items.cS && ItemElytra.d(itemstack)) {
                flag = true;
                if (!this.world.isClientSide && (this.bq + 1) % 20 == 0) {
                    itemstack.damage(1, this);
                }
            } else {
                flag = false;
            }
        } else {
            flag = false;
        }

        if (!this.world.isClientSide && flag != this.getFlag(7) && !CraftEventFactory.callToggleGlideEvent(this, flag).isCancelled()) {
            this.setFlag(7, flag);
        }
    }

    @Override
    public void a(float f, float f1, float f2) {
        if(this.passengers.isEmpty() || !(this.passengers.get(0) instanceof EntityPlayer)) {
            // keep the entity fly away straightly
            Vector velocity = this.getBukkitEntity().getVelocity();
            this.motX = velocity.getX();
            this.motY = velocity.getY();
            this.motZ = velocity.getZ();
            this.move(EnumMoveType.SELF, this.motX, this.motY, this.motZ);
            return;
        }

        EntityLiving entityLiving = (EntityLiving) this.passengers.get(0);

        float keyForwardBack = entityLiving.bg;
        float keyLeftRight = entityLiving.be;

        Vector3f pos = this.headPose;

        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();
        if(keyForwardBack < 0f) {
            if(pos.getX() < -30f) {
                x = -30f;
            } else if(pos.getX() > 0) {
                x -= 3f;
            } else {
                x -= 1.5f;
            }
        } else if(keyForwardBack > 0f) {
            if(pos.getX() >= 30f) {
                x = 30f;
            } else if(pos.getX() < 0) {
                x += 3f;
            } else {
                x += 1.5f;
            }
        }

        if(keyLeftRight > 0f) {
            if(pos.getZ() >= 20f) {
                z = 20f;
            } else if(pos.getZ() < 0f) {
                z += 3f;
            } else {
                z += 2f;
            }
        } else if(keyLeftRight < 0f) {
            if(pos.getZ() <= -20f) {
                z = -20f;
            } else if(pos.getZ() > 0f) {
                z -= 3f;
            } else {
                z -= 2f;
            }
        } else {
            if(pos.getZ() > 2f) {
                z -= 0.5f;
            } else if(pos.getZ() < -8f) {
                z += 0.5f;
            } else {
                z = 0;
            }
        }

        setHeadPose(new Vector3f(x, y, z));

        // handle the spaceship direction
        float yaw = this.yaw - (keyLeftRight * 7);

        this.lastYaw = this.yaw = yaw;
        this.pitch = pos.getX();
        this.setYawPitch(this.yaw, this.pitch);

        Vector vel = this.getBukkitEntity().getLocation().getDirection();
        vel.multiply(Math.min((double) this.speed / 10, 2.5));

        this.motX = vel.getX();
        this.motY = vel.getY();
        this.motZ = vel.getZ();
        super.a(f, f1, f2);
    }

    public static ZeroGravityArmorStand spawn(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();

        ZeroGravityArmorStand entity = new ZeroGravityArmorStand(world);
        entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        if(entity.getBukkitEntity() instanceof CraftLivingEntity) {
            ((CraftLivingEntity) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
        }
        entity.setInvisible(true);
        world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return entity;
    }
}
