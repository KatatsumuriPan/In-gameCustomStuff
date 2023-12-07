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
import kpan.ig_custom_stuff.ModTagsGenerated;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import kpan.ig_custom_stuff.resource.IdConverter;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.lang.reflect.Type;

public class BlockStateEntry {

	public static BlockStateEntry fromByteBuf(ByteBuf buf) {
		BlockstateType blockstateType = MyByteBufUtil.readEnum(buf, BlockstateType.class);
		ResourceLocation textureId = new ResourceLocation(MyByteBufUtil.readString(buf));
		return new BlockStateEntry(blockstateType, textureId);
	}
	public static BlockStateEntry fromJson(String json) {
		return Serializer.GSON.fromJson(json, BlockStateEntry.class);
	}

	public static BlockStateEntry defaultBlockState() {
		return new BlockStateEntry(BlockstateType.SIMPLE, new ResourceLocation(ModTagsGenerated.MODID, "not_configured_blockstate"));
	}

	public final BlockstateType blockstateType;
	public final ResourceLocation blockModelId;

	public BlockStateEntry(BlockstateType blockstateType, ResourceLocation blockModelId) {
		this.blockstateType = blockstateType;
		this.blockModelId = blockModelId;
	}
	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeEnum(buf, blockstateType);
		MyByteBufUtil.writeString(buf, blockModelId.toString());
	}

	public void register(ResourceLocation blockId, boolean isRemote) throws IOException {
		if (isRemote) {
			ClientCache.INSTANCE.addBlockstate(blockId, this);
		} else {
			Server.INSTANCE.addBlockstate(blockId, this);
		}
	}

	public void update(ResourceLocation blockId, boolean isRemote) throws IOException {
		if (isRemote) {
			ClientCache.INSTANCE.replaceBlockstate(blockId, this);
		} else {
			Server.INSTANCE.replaceBlockstate(blockId, this);
		}
	}

	public String toJson() {
		return Serializer.GSON.toJson(this);
	}

	public String getString() {
		return blockstateType.getString() + "(" + blockModelId + ")";
	}

	public enum BlockstateType {
		SIMPLE,
		;
		public static BlockstateType getFromName(String name) {
			for (BlockstateType value : values()) {
				if (value.name().equalsIgnoreCase(name))
					return value;
			}
			throw new IllegalArgumentException("Unknown BlockStateType!:" + name);
		}
		public String getString() {
			switch (this) {
				case SIMPLE -> {
					return I18n.format("ingame_custom_stuff.block_state.simple");
				}
				default -> throw new AssertionError();
			}
		}
	}

	private static class Serializer implements JsonDeserializer<BlockStateEntry>, JsonSerializer<BlockStateEntry> {

		private static final Gson GSON;

		static {
			GsonBuilder gsonbuilder = new GsonBuilder();
			gsonbuilder.registerTypeHierarchyAdapter(BlockStateEntry.class, new Serializer());
			gsonbuilder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
			GSON = gsonbuilder.create();
		}

		@Override
		public BlockStateEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			BlockstateType type = BlockstateType.getFromName(JsonUtils.getString(jsonObject, "ics_block_state_type"));
			JsonObject variants = JsonUtils.getJsonObject(jsonObject, "variants");
			JsonObject normal = JsonUtils.getJsonObject(variants, "normal");
			String blockModelFile = JsonUtils.getString(normal, "model");

			return new BlockStateEntry(type, IdConverter.blockModelFile2modelId(new ResourceLocation(blockModelFile)));
		}

		@Override
		public JsonElement serialize(BlockStateEntry object, Type type, JsonSerializationContext context) {
			JsonObject jsonobject = new JsonObject();
			jsonobject.add("ics_block_state_type", context.serialize(object.blockstateType));
			JsonObject variants = new JsonObject();
			{
				JsonObject normal = new JsonObject();
				normal.addProperty("model", IdConverter.modelId2BlockModelFile(object.blockModelId).toString());
				variants.add("normal", normal);
			}
			jsonobject.add("variants", variants);
			return jsonobject;
		}
	}

}
