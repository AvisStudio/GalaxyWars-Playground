package studio.avis.galaxywars.playground.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorUtils {

    private VectorUtils() {
    }

    public static Vector trajectory(Location from, Location to) {
        return trajectory(from.toVector(), to.toVector());
    }

    public static Vector trajectory(Vector from, Vector to) {
        return to.subtract(from).normalize();
    }

    public static float yaw(Vector vector) {
        double x = vector.getX();
        double z = vector.getZ();
        double yaw = Math.toDegrees(Math.atan(-x / z));
        if(z < 0d) {
            yaw += 180d;
        }
        return (float) yaw;
    }

    public static float pitch(Vector vector) {
        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();
        double xz = Math.sqrt(x * x + z * z);
        double pitch = Math.toDegrees(Math.atan(xz / y));
        if(y <= 0d) {
            pitch += 90d;
        } else {
            pitch -= 90d;
        }
        return (float) pitch;
    }

}
