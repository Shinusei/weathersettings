package com.weathersettings.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.weathersettings.WeatherSettingsMod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommonConfiguration
{
    public boolean      skipWeatherOnSleep  = false;
    public List<String> weatherEntries      =
      Arrays.asList("weather rain;100;500;7000",
        "weather thunder;20;300;6000");
    public String       clearWeatherCommand = "weather clear";

    protected CommonConfiguration()
    {

    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();

        final JsonObject entry = new JsonObject();
        entry.addProperty("desc:", "Whether to skip weather after sleeping: default:false");
        entry.addProperty("skipWeatherOnSleep", skipWeatherOnSleep);
        root.add("skipWeatherOnSleep", entry);

        final JsonObject entry2 = new JsonObject();
        entry2.addProperty("desc:", "Command for clean weather");
        entry2.addProperty("clearWeatherCommand", clearWeatherCommand);
        root.add("clearWeatherCommand", entry2);

        final JsonObject entry3 = new JsonObject();
        entry3.addProperty("desc:", "Weather entries, format: [\"command;weight;duration in seconds;clear weather afterwards duration in seconds\"].");
        final JsonArray list3 = new JsonArray();
        for (final String name : weatherEntries)
        {
            list3.add(name);
        }
        entry3.add("weatherEntries", list3);
        root.add("weatherEntries", entry3);

        return root;
    }

    public void deserialize(JsonObject data)
    {
        if (data == null)
        {
            WeatherSettingsMod.LOGGER.error("Config file was empty!");
            return;
        }

        try
        {
            skipWeatherOnSleep = data.get("skipWeatherOnSleep").getAsJsonObject().get("skipWeatherOnSleep").getAsBoolean();
            clearWeatherCommand = data.get("clearWeatherCommand").getAsJsonObject().get("clearWeatherCommand").getAsString();
            weatherEntries = new ArrayList<>();
            for (final JsonElement element : data.get("weatherEntries").getAsJsonObject().get("weatherEntries").getAsJsonArray())
            {
                weatherEntries.add(element.getAsString());
            }
        }
        catch (Exception e)
        {
            WeatherSettingsMod.LOGGER.error("Could not parse config file", e);
        }
    }
}
