package io.github.chindeaone.collectiontracker.config.core

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class PositionDeserializer: JsonDeserializer<Position> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Position {
        val obj = json.asJsonObject

        val x = when {
            obj.has("x") && !obj["x"].isJsonNull -> obj["x"].asInt
            obj.has("X") && !obj["X"].isJsonNull -> obj["X"].asInt
            else -> 0
        }

        val y = when {
            obj.has("y") && !obj["y"].isJsonNull -> obj["y"].asInt
            obj.has("Y") && !obj["Y"].isJsonNull -> obj["Y"].asInt
            else -> 0
        }

        val pos = Position(x, y)

        if (obj.has("scale") && !obj["scale"].isJsonNull) pos.scale = obj["scale"].asFloat
        if (obj.has("width") && !obj["width"].isJsonNull) pos.width = obj["width"].asInt
        if (obj.has("height") && !obj["height"].isJsonNull) pos.height = obj["height"].asInt

        return pos
    }
}