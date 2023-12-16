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
import kpan.ig_custom_stuff.block.BlockStateEntry.BlockStateType;
import kpan.ig_custom_stuff.registry.DynamicServerRegistryManager;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.ids.BlockId;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JsonUtils;

import java.io.IOException;
import java.lang.reflect.Type;

public class BlockEntry {

	public static BlockEntry fromByteBuf(ByteBuf buf) {
		var blockId = BlockId.formByteBuf(buf);
		BlockStateType blockStateType = MyByteBufUtil.readEnum(buf, BlockStateType.class);
		var option = BlockPropertyEntry.fromByteBuf(buf);
		return new BlockEntry(blockId, blockStateType, option);
	}

	public final BlockId blockId;
	public final BlockStateType blockStateType;
	public final BlockPropertyEntry basicProperty;

	public BlockEntry(BlockId blockId, BlockStateType blockStateType, BlockPropertyEntry basicProperty) {
		this.blockId = blockId;
		this.blockStateType = blockStateType;
		this.basicProperty = basicProperty;
	}

	public void writeTo(ByteBuf buf) {
		blockId.writeTo(buf);
		MyByteBufUtil.writeEnum(buf, blockStateType);
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
		return new BlockEntryJson(BlockType.NORMAL, blockStateType, basicProperty).toJson();
	}

	public static class BlockEntryJson {
		public final BlockType type;
		public final BlockStateType blockStateType;
		public final BlockPropertyEntry propertyEntry;

		public BlockEntryJson(BlockType type, BlockStateType blockStateType, BlockPropertyEntry propertyEntry) {
			this.type = type;
			this.blockStateType = blockStateType;
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
				BlockStateType blockStateType;
				if (jsonObject.has("block_state"))
					blockStateType = context.deserialize(jsonObject.get("block_state"), BlockStateType.class);
				else
					blockStateType = BlockStateType.SIMPLE;
				BlockPropertyEntry basicPropertyEntry = BlockPropertyEntry.deserialize(jsonObject.getAsJsonObject("basic_property"), typeOfT, context);

				return new BlockEntryJson(type, blockStateType, basicPropertyEntry);
			}

			@Override
			public JsonElement serialize(BlockEntryJson object, Type type, JsonSerializationContext context) {
				JsonObject jsonobject = new JsonObject();
				jsonobject.add("type", context.serialize(object.blockStateType));
				jsonobject.add("block_state", context.serialize(object.blockStateType));
				jsonobject.add("basic_property", object.propertyEntry.serialize(type, context));
				return jsonobject;
			}
		}


		public enum BlockType {
			NORMAL,
		}
	}
}
