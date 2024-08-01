package com.weathersettings.event;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.weathersettings.WeatherSettingsMod;
import com.weathersettings.weather.WeatherEntry;
import com.weathersettings.weather.WeatherHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler
{
    private static Map<ResourceKey<Level>, WeatherHandler> handlers = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(final ServerTickEvent.Post event)
    {
        try
        {
            for (final ServerLevel level : event.getServer().getAllLevels())
            {
                WeatherHandler handler = handlers.get(level.dimension());

                if (handler != null)
                {
                    handler.onLevelTick(level);
                }
            }
        }
        catch (CommandSyntaxException e)
        {
            WeatherSettingsMod.LOGGER.warn(e);
        }
    }

    @SubscribeEvent
    public static void onStart(final ServerStartedEvent event)
    {
        if (!WeatherSettingsMod.config.getCommonConfig().skipWeatherOnSleep)
        {
            event.getServer().getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, event.getServer());
        }
        else
        {
            event.getServer().getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(true, event.getServer());
        }

        handlers.clear();
        for (final Level level : event.getServer().getAllLevels())
        {
            final List<WeatherEntry> entries = WeatherSettingsMod.config.getCommonConfig().worldWeatherSettings.get(level.dimension().location().toString());
            if (entries != null && !entries.isEmpty())
            {
                handlers.put(level.dimension(), new WeatherHandler(entries));
            }
        }

        for (final WeatherHandler handler : handlers.values())
        {
            handler.onServerStart();
        }
    }
}
