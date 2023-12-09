package kpan.ig_custom_stuff.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

public class MyJsonUtil {

	public static <E extends Enum<E>> E deserializeEnum(JsonObject jsonObject, String memberName, JsonDeserializationContext context, E fallback, Class<E> clazz) {
		return jsonObject.has(memberName) ? context.deserialize(jsonObject.get(memberName), clazz) : fallback;
	}
}
