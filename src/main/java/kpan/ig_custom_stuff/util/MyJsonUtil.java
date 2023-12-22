package kpan.ig_custom_stuff.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JsonUtils;

import java.util.Locale;

public class MyJsonUtil {

	public static <E extends Enum<E>> E deserializeEnum(JsonObject jsonObject, String memberName, JsonDeserializationContext context, E fallback, Class<E> clazz) {
		return jsonObject.has(memberName) ? context.deserialize(jsonObject.get(memberName), clazz) : fallback;
	}

	public static <E extends Enum<E>> E deserializeEnum(JsonObject jsonObject, String memberName, E fallback, Class<E> clazz) {
		if (!jsonObject.has(memberName))
			return fallback;
		String enumString = JsonUtils.getString(jsonObject, memberName);
		for (E enumConstant : clazz.getEnumConstants()) {
			if (enumString.equals(enumConstant.name().toLowerCase(Locale.ROOT))) {
				return enumConstant;
			}
		}
		return fallback;
	}

	public static <E extends Enum<E>> E deserializeEnum(JsonObject jsonObject, String memberName, Class<E> clazz) {
		if (!jsonObject.has(memberName))
			throw new JsonSyntaxException("Missing " + memberName);
		String enumString = JsonUtils.getString(jsonObject, memberName);
		for (E enumConstant : clazz.getEnumConstants()) {
			if (enumString.equals(enumConstant.name().toLowerCase(Locale.ROOT))) {
				return enumConstant;
			}
		}
		throw new JsonSyntaxException(String.format("%s is not valid %s.", enumString, clazz.getSimpleName()));
	}

	public static <E extends Enum<E>> JsonElement serializeEnum(E enumValue) {
		return new JsonPrimitive(enumValue.name().toLowerCase(Locale.ROOT));
	}

}
