package studio.avis.galaxywars.playground.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockUtils {

    private BlockUtils() {
    }

    public static List<Block> blocksInRadius(Location center, double rd) {
        List<Block> blocks = new ArrayList<>();
        int radius = (int) rd + 1;
        for(int x = -radius; x <= radius; x++) {
            for(int z = -radius; z <= radius; z++) {
                for(int y = -radius; y <= radius; y++) {
                    Block block = center.getWorld().getBlockAt((int) (center.getX() + x), (int) (center.getY() + y), (int) (center.getZ() + z));
                    double offset = center.distance(block.getLocation().add(0.5d, 0.5d, 0.5d));
                    if(offset <= rd) {
                        blocks.add(block);
                    }
                }
            }
        }
        return blocks;
    }

}
