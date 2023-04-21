package com.weathersettings.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.weathersettings.WeatherSettingsMod;
import com.weathersettings.weather.WeatherEntry;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

import java.util.*;

public class CommonConfiguration
{
    public boolean                         skipWeatherOnSleep   = false;
    public Map<String, List<WeatherEntry>> worldWeatherSettings = new HashMap<>();
    public String                          clearWeatherCommand  = "weather clear";

    protected CommonConfiguration()
    {
        worldWeatherSettings.put(BuiltinDimensionTypes.OVERWORLD.location().toString(), Arrays.asList(new WeatherEntry("rain", "weather rain", 100, 300, 3600),
          new WeatherEntry("thunder", "weather thunder", 20, 200, 3600)));
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
        entry3.addProperty("desc:", "Weather entries, duration in seconds. Weight is the chance to be chosen out of the sum of all weights");
        final JsonArray worldList = new JsonArray();
        for (final Map.Entry<String, List<WeatherEntry>> worldEntry : worldWeatherSettings.entrySet())
        {
            final JsonObject worldEntryJson = new JsonObject();
            worldEntryJson.addProperty("world", worldEntry.getKey());

            if (worldEntry.getValue() != null)
            {
                for (final WeatherEntry weatherEntry : worldEntry.getValue())
                {
                    weatherEntry.serialize(worldEntryJson);
                }
            }

            worldList.add(worldEntryJson);
        }
        entry3.add("weatherEntries", worldList);
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

        skipWeatherOnSleep = data.get("skipWeatherOnSleep").getAsJsonObject().get("skipWeatherOnSleep").getAsBoolean();
        clearWeatherCommand = data.get("clearWeatherCommand").getAsJsonObject().get("clearWeatherCommand").getAsString();
        worldWeatherSettings.clear();

        for (final JsonElement element : data.get("weatherEntries").getAsJsonObject().get("weatherEntries").getAsJsonArray())
        {
            JsonObject jsonObject = ((JsonObject) element);
            String dimension = jsonObject.get("world").getAsString();

            final List<WeatherEntry> weatherEntries = new ArrayList<>();

            for (final String key : jsonObject.keySet())
            {
                if (key.equals("world"))
                {
                    continue;
                }

                weatherEntries.add(new WeatherEntry(key, jsonObject.get(key).getAsJsonObject()));
                WeatherSettingsMod.LOGGER.info("Loaded weather for: " + key);
            }

            worldWeatherSettings.put(dimension, weatherEntries);
        }
    }
}
