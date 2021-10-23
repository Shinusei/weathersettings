package com.weathersettings.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class CommonConfiguration
{
    public final ForgeConfigSpec                                         ForgeConfigSpecBuilder;
    public final ForgeConfigSpec.ConfigValue<Boolean>                    skipWeatherOnSleep;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> weatherEntries;
    public final ForgeConfigSpec.ConfigValue<String> cleanWeatherCommand;

    protected CommonConfiguration(final ForgeConfigSpec.Builder builder)
    {
        builder.push("Config category");

        builder.comment("Whether to skip weather after sleeping: default:false");
        skipWeatherOnSleep = builder.define("skipWeatherOnSleep", false);

        builder.comment("Command for clean weather");
        cleanWeatherCommand = builder.define("cleanWeatherCommand", "weather clear");

        builder.comment("Weather entries, format: [\"command;weight;duration in seconds;clear weather afterwards duration in seconds\"].");
        weatherEntries = builder.defineList("weatherEntries",
          Arrays.asList("weather rain;100;700;3000",
            "weather thunder;20;500;2000")
          , e -> e instanceof String);

        // Escapes the current category level
        builder.pop();
        ForgeConfigSpecBuilder = builder.build();
    }
}
