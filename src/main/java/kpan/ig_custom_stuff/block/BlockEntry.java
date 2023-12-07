package kpan.ig_custom_stuff.block;

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
import kpan.ig_custom_stuff.block.BlockEntry.BlockEntryJson.BlockType;
import kpan.ig_custom_stuff.registry.DynamicServerRegistryManager;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.lang.reflect.Type;

public class BlockEntry {

	public static BlockEntry fromByteBuf(ByteBuf buf) {
		var itemId = new ResourceLocation(MyByteBufUtil.readString(buf));
		var option = BlockPropertyEntry.fromByteBuf(buf);
		return new BlockEntry(itemId, option);
	}

	public final ResourceLocation blockId;
	public final BlockPropertyEntry basicProperty;

	public BlockEntry(ResourceLocation blockId, BlockPropertyEntry basicProperty) {
		this.blockId = blockId;
		this.basicProperty = basicProperty;
	}

	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeString(buf, blockId.toString());
		basicProperty.writeTo(buf);
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
		return new BlockEntryJson(BlockType.NORMAL, basicProperty).toJson();
	}

	public static class BlockEntryJson {
		public final BlockType type;
		public final BlockPropertyEntry propertyEntry;

		public BlockEntryJson(BlockType type, BlockPropertyEntry propertyEntry) {
			this.type = type;
			this.propertyEntry = propertyEntry;
		}

		public static BlockEntryJson fromJson(String json) {
			return JsonUtils.gsonDeserialize(Serializer.GSON, json, BlockEntryJson.class, false);
		}

		public String toJson() {
			return Serializer.GSON.toJson(this);
		}

		private static class Serializer implements JsonDeserializer<BlockEntryJson>, JsonSerializer<BlockEntryJson> {

			private static final Gson GSON;

			static {
				GsonBuilder gsonbuilder = new GsonBuilder();
				gsonbuilder.registerTypeHierarchyAdapter(BlockEntryJson.class, new Serializer());
				gsonbuilder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
				GSON = gsonbuilder.create();
			}

			@Override
			public BlockEntryJson deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				JsonObject jsonObject = json.getAsJsonObject();
				BlockType type = context.deserialize(jsonObject.get("type"), BlockType.class);
				BlockPropertyEntry basicPropertyEntry = BlockPropertyEntry.deserialize(jsonObject.getAsJsonObject("basic_property"), typeOfT, context);

				return new BlockEntryJson(type, basicPropertyEntry);
			}

			@Override
			public JsonElement serialize(BlockEntryJson object, Type type, JsonSerializationContext context) {
				JsonObject jsonobject = new JsonObject();
				jsonobject.add("type", context.serialize(object.type));
				jsonobject.add("basic_property", object.propertyEntry.serialize(type, context));
				return jsonobject;
			}
		}


		public enum BlockType {
			NORMAL,
		}
	}
}
