package studio.avis.galaxywars.playground;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import studio.avis.galaxywars.playground.listeners.GeneralListener;
import studio.avis.galaxywars.playground.listeners.MissileListener;
import studio.avis.galaxywars.playground.listeners.SpaceshipListener;
import studio.avis.galaxywars.playground.listeners.ZeroGravityItemListener;

public class GalaxyWarsPlayground extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        super.onEnable();

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(new MissileListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ZeroGravityItemListener(this), this);
        this.getServer().getPluginManager().registerEvents(new GeneralListener(), this);
        this.getServer().getPluginManager().registerEvents(new SpaceshipListener(), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
