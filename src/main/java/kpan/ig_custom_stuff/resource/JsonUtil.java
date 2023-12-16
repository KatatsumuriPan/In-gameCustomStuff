package kpan.ig_custom_stuff.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import net.minecraft.util.JsonUtils;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.io.StringReader;

public class JsonUtil {

	public static Vector3f parsePosition(JsonObject object, String memberName) {
		JsonArray jsonarray = JsonUtils.getJsonArray(object, memberName);

		if (jsonarray.size() != 3) {
			throw new JsonParseException("Expected 3 " + memberName + " values, found: " + jsonarray.size());
		} else {
			float[] afloat = new float[3];

			for (int i = 0; i < afloat.length; ++i) {
				afloat[i] = JsonUtils.getFloat(jsonarray.get(i), memberName + "[" + i + "]");
			}

			return new Vector3f(afloat[0], afloat[1], afloat[2]);
		}
	}

	public static JsonElement toJsonElement(String json) {
		try (JsonReader reader = new JsonReader(new StringReader(json))) {
			reader.setLenient(true);
			return Streams.parse(reader);
		} catch (IOException e) {
			throw new JsonSyntaxException(e);
		}
	}
}
