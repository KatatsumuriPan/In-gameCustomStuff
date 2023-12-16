package kpan.ig_custom_stuff.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.item.ItemEntry.ItemEntryJson.ItemType;
import kpan.ig_custom_stuff.registry.DynamicServerRegistryManager;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.ids.ItemId;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JsonUtils;

import java.io.IOException;
import java.lang.reflect.Type;

public class ItemEntry {

	public static ItemEntry fromByteBuf(ByteBuf buf) {
		var itemId = ItemId.formByteBuf(buf);
		var propertyEntry = ItemPropertyEntry.fromByteBuf(buf);
		return new ItemEntry(itemId, propertyEntry);
	}

	public final ItemId itemId;
	public final ItemPropertyEntry propertyEntry;

	public ItemEntry(ItemId itemId, ItemPropertyEntry propertyEntry) {
		this.itemId = itemId;
		this.propertyEntry = propertyEntry;
	}

	public void writeTo(ByteBuf buf) {
		itemId.writeTo(buf);
		propertyEntry.writeTo(buf);
	}

	public void register(boolean isRemote) throws IOException {
		if (isRemote) {
			MCRegistryUtil.register(this, isRemote);
		} else {
			DynamicServerRegistryManager.register(this);
		}
	}

	public void update(boolean isRemote) throws IOException {
		if (isRemote) {
			MCRegistryUtil.update(this, isRemote);
		} else {
			DynamicServerRegistryManager.update(this);
		}
	}

	public String toJson() {
		return new ItemEntryJson(ItemType.NORMAL, propertyEntry).toJson();
	}

	public static class ItemEntryJson {
		public final ItemType type;
		public final ItemPropertyEntry propertyEntry;

		public ItemEntryJson(ItemType type, ItemPropertyEntry propertyEntry) {
			this.type = type;
			this.propertyEntry = propertyEntry;
		}

		public static ItemEntryJson fromJson(String json) {
			return JsonUtils.gsonDeserialize(Serializer.GSON, json, ItemEntryJson.class, false);
		}

		public String toJson() {
			return Serializer.GSON.toJson(this);
		}

		private static class Serializer implements JsonDeserializer<ItemEntryJson>, JsonSerializer<ItemEntryJson> {

			private static final Gson GSON;

			static {
				GsonBuilder gsonbuilder = new GsonBuilder();
				gsonbuilder.registerTypeHierarchyAdapter(ItemEntryJson.class, new Serializer());
				gsonbuilder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
				GSON = gsonbuilder.create();
			}

			@Override
			public ItemEntryJson deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				JsonObject jsonObject = json.getAsJsonObject();
				ItemType type = context.deserialize(jsonObject.get("type"), ItemType.class);
				var propertyEntry = ItemPropertyEntry.deserialize(JsonUtils.getJsonObject(jsonObject, "basic_property"), typeOfT, context);

				return new ItemEntryJson(type, propertyEntry);
			}

			@Override
			public JsonElement serialize(ItemEntryJson object, Type type, JsonSerializationContext context) {
				JsonObject jsonobject = new JsonObject();
				jsonobject.add("type", context.serialize(object.type));
				jsonobject.add("basic_property", object.propertyEntry.serialize(type, context));
				return jsonobject;
			}
		}


		public enum ItemType {
			NORMAL,
		}
	}
}
