package kpan.ig_custom_stuff.item.model;

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
import kpan.ig_custom_stuff.ModTagsGenerated;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ItemModelEntry {

	public static ItemModelEntry fromByteBuf(ByteBuf buf) {
		ModelType modelType = MyByteBufUtil.readEnum(buf, ModelType.class);
		int count = MyByteBufUtil.readVarInt(buf);
		Set<ResourceLocation> textureIds = new HashSet<>();
		for (int i = 0; i < count; i++) {
			ResourceLocation textureId = new ResourceLocation(MyByteBufUtil.readString(buf));
			textureIds.add(textureId);
		}
		return new ItemModelEntry(modelType, textureIds, "");
	}
	@Nullable
	public static ItemModelEntry fromJson(String json) {
		return Serializer.GSON.fromJson(json, ItemModelEntry.class);
	}

	public static ItemModelEntry normalType(ModelType modelType, ResourceLocation textureId) {
		return normalType(modelType, Collections.singleton(textureId));
	}
	public static ItemModelEntry normalType(ModelType modelType, Set<ResourceLocation> textureIds) {
		return new ItemModelEntry(modelType, textureIds, "");
	}

	public static ItemModelEntry defaultModel() {
		return normalType(ModelType.SIMPLE, new ResourceLocation(ModTagsGenerated.MODID, "not_configured_texture"));
	}

//	public static ItemModelEntry customType(String json) {
//		return new ItemModelEntry(ModelType.CUSTOM, )
//	}

	public final ModelType modelType;
	public final Set<ResourceLocation> textureIds;
	public final String CustomJsonString;

	private ItemModelEntry(ModelType modelType, Set<ResourceLocation> textureIds, String customJsonString) {
		this.modelType = modelType;
		this.textureIds = textureIds;
		CustomJsonString = customJsonString;
	}
	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeEnum(buf, modelType);
		MyByteBufUtil.writeVarInt(buf, textureIds.size());
		for (ResourceLocation textureId : textureIds) {
			MyByteBufUtil.writeString(buf, textureId.toString());
		}
	}

	public void register(ResourceLocation itemId, boolean isRemote) throws IOException {
		if (isRemote) {
			ClientCache.INSTANCE.addModel(itemId, this);
		} else {
			Server.INSTANCE.addModel(itemId, this);
		}
	}

	public void update(ResourceLocation itemId, boolean isRemote) throws IOException {
		register(itemId, isRemote);
	}

	public String toJson() {
		return Serializer.GSON.toJson(this);
	}
	public String getString() {
		return modelType.getString() + "(" + StringUtils.join(textureIds, ",") + ")";
	}

	public enum ModelType {
		SIMPLE,
		HANDHELD,
		CUSTOM,
		;
		public static ModelType getFromName(String name) {
			for (ModelType value : values()) {
				if (value == CUSTOM)
					continue;
				if (value.name().equalsIgnoreCase(name))
					return value;
			}
			throw new IllegalArgumentException("Unknown ModelType!:" + name);
		}
		public String getParent() {
			switch (this) {
				case SIMPLE -> {
					return "item/generated";
				}
				case HANDHELD -> {
					return "item/handheld";
				}
				default -> throw new AssertionError();
			}
		}
		public String getString() {
			switch (this) {
				case SIMPLE -> {
					return I18n.format("ingame_custom_stuff.item_model.simple");
				}
				case HANDHELD -> {
					return I18n.format("ingame_custom_stuff.item_model.handheld");
				}
				case CUSTOM -> {
					return "Custom";
				}
				default -> throw new AssertionError();
			}
		}
	}

	private static class Serializer implements JsonDeserializer<ItemModelEntry>, JsonSerializer<ItemModelEntry> {

		private static final Gson GSON;

		static {
			GsonBuilder gsonbuilder = new GsonBuilder();
			gsonbuilder.registerTypeHierarchyAdapter(ItemModelEntry.class, new Serializer());
			gsonbuilder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
			GSON = gsonbuilder.create();
		}

		@Override
		public ItemModelEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			if (jsonObject.has("ics_itemblock_model_type")) {
				return null;//block側で追加されたもの
			}
			ModelType type = ModelType.getFromName(JsonUtils.getString(jsonObject, "ics_item_model_type"));
//			String parent = jsonObject.getAsJsonPrimitive("parent").getAsString();
			JsonObject textures = JsonUtils.getJsonObject(jsonObject, "textures");
			String layer0 = JsonUtils.getString(textures, "layer0");
			return ItemModelEntry.normalType(type, new ResourceLocation(layer0));
		}

		@Override
		public JsonElement serialize(ItemModelEntry object, Type type, JsonSerializationContext context) {
			JsonObject jsonobject = new JsonObject();
			jsonobject.addProperty("parent", object.modelType.getParent());
			{
				JsonObject textures = new JsonObject();
				textures.addProperty("layer0", object.textureIds.iterator().next().toString());
				jsonobject.add("textures", textures);
			}
			jsonobject.add("ics_item_model_type", context.serialize(object.modelType));
			return jsonobject;
		}
	}

}
