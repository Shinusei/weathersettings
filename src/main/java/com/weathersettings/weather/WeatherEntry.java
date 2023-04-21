package com.weathersettings.weather;

import com.google.gson.JsonObject;

public class WeatherEntry
{
    public final String name;
    public final String command;
    public final int    duration;
    public final int    weight;
    public final int    clearDuration;

    public WeatherEntry(final String name, final String command, final int weight, final int duration, final int clearDuration)
    {
        this.name = name;
        this.command = command;
        this.duration = duration;
        this.weight = weight;
        this.clearDuration = clearDuration;
    }

    public JsonObject serialize(final JsonObject worldEntryJson)
    {
        final JsonObject entry = new JsonObject();
        entry.addProperty("command", command);
        entry.addProperty("weight", weight);
        entry.addProperty("duration", duration);
        entry.addProperty("clearDuration", clearDuration);

        worldEntryJson.add(name, entry);
        return worldEntryJson;
    }

    public WeatherEntry(final String name, final JsonObject jsonObject)
    {
        this.name = name;
        command = jsonObject.get("command").getAsString();
        weight = jsonObject.get("weight").getAsInt();
        duration = jsonObject.get("duration").getAsInt();
        clearDuration = jsonObject.get("clearDuration").getAsInt();
    }
}
