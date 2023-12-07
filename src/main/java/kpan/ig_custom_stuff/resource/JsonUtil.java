package kpan.ig_custom_stuff.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.JsonUtils;

import javax.vecmath.Vector3f;

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
}
