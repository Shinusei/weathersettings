package com.weathersettings.weather;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.weathersettings.WeatherSettingsMod;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WeatherHandler
{
    /**
     * Weather config data
     */
    private final static List<WeatherCommandEntry> weatherEntries = new ArrayList<>();
    private static       int                       weightSum      = 0;
    private static       String                    clearCommand   = "";

    /**
     * Current weather
     */
    private static long                nextChangeMilis = 0;
    private static WeatherCommandEntry currentWeather  = null;

    private static WeatherCommandEntry NONE = new WeatherCommandEntry("", 0, 0, 1000000);

    /**
     * Called on world tick
     *
     * @param server
     */
    public static void onServerTick(final MinecraftServer server) throws CommandSyntaxException
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
                    server
                      .getCommands().getDispatcher().execute(currentWeather.command + " " + duration, createStack(server));
                }
            }
            else
            {
                if (currentWeather.clearDuration > 10)
                {
                    int clearDuration = (int) (WeatherSettingsMod.rand.nextInt(currentWeather.clearDuration / 2) + currentWeather.clearDuration * 0.75);
                    nextChangeMilis = System.currentTimeMillis() + clearDuration * 1000L;
                    server
                      .getCommands().getDispatcher().execute(clearCommand + " " + clearDuration * 2, createStack(server));
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
    private static WeatherCommandEntry chooseNextWeather()
    {
        if (weightSum == 0)
        {
            return NONE;
        }

        int currentWeight = 0;
        final int chosenWeight = WeatherSettingsMod.rand.nextInt(weightSum);

        for (final WeatherCommandEntry entry : weatherEntries)
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
     * Reset saved values
     */
    public static void reset()
    {
        weatherEntries.clear();
        weightSum = 0;
        nextChangeMilis = System.currentTimeMillis() + 1000 * 120;
        currentWeather = null;
    }

    /**
     * Parses the config settings
     */
    public static void parseConfig()
    {
        reset();
        for (final String data : WeatherSettingsMod.config.getCommonConfig().weatherEntries)
        {
            final String[] splitData = data.split(";");
            if (splitData.length != 4)
            {
                WeatherSettingsMod.LOGGER.warn("Bad config entry:" + data, " seperate entries by ; and make sure you have 4 in total");
                continue;
            }

            final String command = splitData[0].trim();

            final int weight;
            try
            {
                weight = Integer.parseInt(splitData[1]);
            }
            catch (Exception e)
            {
                WeatherSettingsMod.LOGGER.warn("Bad input, expected number for:" + data + " at:" + splitData[1]);
                continue;
            }

            final int duration;
            try
            {
                duration = Integer.parseInt(splitData[2]);
            }
            catch (Exception e)
            {
                WeatherSettingsMod.LOGGER.warn("Bad input, expected number for:" + data + " at:" + splitData[2]);
                continue;
            }

            final int clearDuration;
            try
            {
                clearDuration = Integer.parseInt(splitData[3]);
            }
            catch (Exception e)
            {
                WeatherSettingsMod.LOGGER.warn("Bad input, expected number for:" + data + " at:" + splitData[3]);
                continue;
            }

            weatherEntries.add(new WeatherCommandEntry(command, duration, weight, clearDuration));
            weightSum += weight;
        }

        clearCommand = WeatherSettingsMod.config.getCommonConfig().clearWeatherCommand.trim();
        WeatherSettingsMod.LOGGER.info("Loaded config entries");
    }

    /**
     * Initial delay on server start
     */
    public static void onServerStart()
    {
        final WeatherCommandEntry rndWeather = chooseNextWeather();
        int clearDuration = (int) (WeatherSettingsMod.rand.nextInt(rndWeather.clearDuration / 2) + rndWeather.clearDuration * 0.75);
        nextChangeMilis = System.currentTimeMillis() + clearDuration * 1000L;
    }

    /**
     * Data class for weather command entries
     */
    static class WeatherCommandEntry
    {
        final String command;
        final int    duration;
        final int    weight;
        final int    clearDuration;

        private WeatherCommandEntry(final String command, final int duration, final int weight, final int clearDuration)
        {
            this.command = command;
            this.duration = duration;
            this.weight = weight;
            this.clearDuration = clearDuration;
        }
    }
}
