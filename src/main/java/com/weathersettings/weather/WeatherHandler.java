package com.weathersettings.weather;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.weathersettings.WeatherSettingsMod;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WeatherHandler
{
    /**
     * Weather config data
     */
    private final List<WeatherEntry> weatherEntries;
    private       int                weightSum    = 0;
    private       String             clearCommand = "";

    /**
     * Current weather
     */
    private long         nextChangeMilis = 0;
    private WeatherEntry currentWeather  = null;

    private static WeatherEntry NONE = new WeatherEntry("name", "", 0, 0, 1000000);

    public WeatherHandler(final List<WeatherEntry> entries)
    {
        weatherEntries = entries;

        for (final WeatherEntry weatherEntry : weatherEntries)
        {
            weightSum += weatherEntry.weight;
        }

        clearCommand = WeatherSettingsMod.config.getCommonConfig().clearWeatherCommand;
    }

    /**
     * Called on world tick
     *
     * @param level
     */
    public void onLevelTick(final ServerLevel level) throws CommandSyntaxException
    {
        if (System.currentTimeMillis() > nextChangeMilis)
        {
            // min 5sec delay
            nextChangeMilis = System.currentTimeMillis() + 5 * 1000L;
            if (currentWeather == null)
            {
                currentWeather = chooseNextWeather();
                if (currentWeather == NONE)
                {
                    return;
                }

                if (currentWeather.duration > 10)
                {
                    int duration = (int) (WeatherSettingsMod.rand.nextInt(currentWeather.duration / 2) + currentWeather.duration * 0.75);
                    nextChangeMilis = System.currentTimeMillis() + duration * 1000L;
                    level.getServer().getCommands().getDispatcher().execute(currentWeather.command + " " + duration, createStack(level.getServer()).withLevel(level));
                }
            }
            else
            {
                if (currentWeather.clearDuration > 10)
                {
                    int clearDuration = (int) (WeatherSettingsMod.rand.nextInt(currentWeather.clearDuration / 2) + currentWeather.clearDuration * 0.75);
                    nextChangeMilis = System.currentTimeMillis() + clearDuration * 1000L;
                    level.getServer()
                      .getCommands().getDispatcher().execute(clearCommand + " " + clearDuration * 2, createStack(level.getServer()).withLevel(level));
                }
                currentWeather = null;
            }
        }
    }

    private static CommandSourceStack createStack(final MinecraftServer server)
    {
        return new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, server.overworld(), 4, "Server", Component.literal("weathersettings"), server, (Entity) null);
    }

    /**
     * Random the next weather entry
     *
     * @return next entry
     */
    @NotNull
    private WeatherEntry chooseNextWeather()
    {
        if (weightSum == 0)
        {
            return NONE;
        }

        int currentWeight = 0;
        final int chosenWeight = WeatherSettingsMod.rand.nextInt(weightSum);

        for (final WeatherEntry entry : weatherEntries)
        {
            currentWeight += entry.weight;
            if (chosenWeight < currentWeight)
            {
                return entry;
            }
        }

        return NONE;
    }

    /**
     * Initial delay on server start
     */
    public void onServerStart()
    {
        currentWeather = chooseNextWeather();
        nextChangeMilis = System.currentTimeMillis() + 10 * 1000L;
    }
}
