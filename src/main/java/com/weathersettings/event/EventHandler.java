package com.weathersettings.event;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.weathersettings.WeatherSettingsMod;
import com.weathersettings.weather.WeatherHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

/**
 * Forge event bus handler, ingame events are fired here
 */
public class EventHandler
{
    public static void onServerTick(final MinecraftServer server)
    {
        try
        {
            WeatherHandler.onServerTick(server);
        }
        catch (CommandSyntaxException e)
        {
            WeatherSettingsMod.LOGGER.warn(e);
        }
    }

    public static void onStart(final MinecraftServer server)
    {
        if (!WeatherSettingsMod.config.getCommonConfig().skipWeatherOnSleep)
        {
            server.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
        }
        WeatherHandler.onServerStart();
    }
}
