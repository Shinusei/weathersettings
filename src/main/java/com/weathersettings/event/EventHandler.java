package com.weathersettings.event;

import com.weathersettings.WeatherSettingsMod;
import com.weathersettings.weather.WeatherHandler;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler
{
    @SubscribeEvent
    public static void onPlayerWakeup(final PlayerWakeUpEvent event)
    {
        // Re-inject weather settings into the world here, to make sure sleeping did not change them
    }

    @SubscribeEvent
    public static void onServerTick(final TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            return;
        }

        WeatherHandler.onServerTick(event);
    }

    @SubscribeEvent
    public static void onStart(final ServerStartedEvent event)
    {
        if (!WeatherSettingsMod.config.getCommonConfig().skipWeatherOnSleep.get())
        {
            event.getServer().getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, event.getServer());
        }
        WeatherHandler.onServerStart();
    }
}
