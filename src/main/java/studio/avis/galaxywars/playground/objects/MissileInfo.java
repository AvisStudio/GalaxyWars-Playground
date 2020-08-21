package studio.avis.galaxywars.playground.objects;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MissileInfo {

    private final Player player;
    private Location a;
    private Location b;

    private final LivingEntity aimTo;
    private final Vector velocity;

    public MissileInfo(Player player, LivingEntity aimTo, Vector velocity) {
        this.player = player;
        this.aimTo = aimTo;
        this.velocity = velocity;
    }

    public Player getPlayer() {
        return player;
    }

    public void setA(Location a) {
        this.a = a;
    }

    public Location getA() {
        return a;
    }

    public void setB(Location b) {
        this.b = b;
    }

    public Location getB() {
        return b;
    }

    public LivingEntity getAimTo() {
        return aimTo;
    }

    public Vector getVelocity() {
        return velocity;
    }
}
