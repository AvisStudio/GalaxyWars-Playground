package studio.avis.galaxywars.playground.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class GeneralListener implements Listener {

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        if(event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChange(final WeatherChangeEvent event) {
        event.setCancelled(true);
    }

}
