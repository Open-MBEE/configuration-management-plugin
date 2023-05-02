package org.openmbee.plugin.cfgmgmt.integration.threedx.json;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ThreeDxQueryJsonSerializer implements JsonSerializer<ThreeDxQueryJson> {
    @Override
    public JsonElement serialize(ThreeDxQueryJson threeDxQueryJson, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = (JsonObject) new GsonBuilder().create().toJsonTree(threeDxQueryJson);
        if(threeDxQueryJson.getStart() == null) {
            jsonObject.remove("start");
        } else if(threeDxQueryJson.getNextStart() == null) {
            jsonObject.remove("next_start");
        }
        if(threeDxQueryJson.getQuery() != null) {
            // so we have \" instead of the serializer turning it into \\\"
            jsonObject.addProperty("query", threeDxQueryJson.getQuery().replaceAll("\\\\", ""));
        }
        return jsonObject;
    }
}
