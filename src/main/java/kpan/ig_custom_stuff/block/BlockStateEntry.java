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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.lang.reflect.Type;

public class BlockStateEntry {

	public static BlockStateEntry fromByteBuf(ByteBuf buf) {
		BlockStateType blockstateType = MyByteBufUtil.readEnum(buf, BlockStateType.class);
		ResourceLocation textureId = new ResourceLocation(MyByteBufUtil.readString(buf));
		return new BlockStateEntry(blockstateType, textureId);
	}
	public static BlockStateEntry fromJson(String json) {
		return Serializer.GSON.fromJson(json, BlockStateEntry.class);
	}

	public static BlockStateEntry defaultBlockState() {
		return new BlockStateEntry(BlockStateType.SIMPLE, new ResourceLocation(ModTagsGenerated.MODID, "not_configured_blockstate"));
	}

	public final BlockStateType blockstateType;
	public final ResourceLocation blockModelId;

	public BlockStateEntry(BlockStateType blockstateType, ResourceLocation blockModelId) {
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

	public enum BlockStateType {
		SIMPLE,
		FACE6,
		HORIZONTAL4,
		XYZ,
		;
		public static BlockStateType getFromName(String name) {
			for (BlockStateType value : values()) {
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
				case FACE6 -> {
					return I18n.format("ingame_custom_stuff.block_state.6faces");
				}
				case HORIZONTAL4 -> {
					return I18n.format("ingame_custom_stuff.block_state.4horizontals");
				}
				case XYZ -> {
					return I18n.format("ingame_custom_stuff.block_state.xyz_axes");
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
			BlockStateType type = BlockStateType.getFromName(JsonUtils.getString(jsonObject, "ics_block_state_type"));
			JsonObject variants = JsonUtils.getJsonObject(jsonObject, "variants");
			String blockModelFile;
			switch (type) {
				case SIMPLE -> {
					JsonObject normal = JsonUtils.getJsonObject(variants, "normal");
					blockModelFile = JsonUtils.getString(normal, "model");
				}
				case FACE6 -> {
					JsonObject normal = JsonUtils.getJsonObject(variants, "facing=north");
					blockModelFile = JsonUtils.getString(normal, "model");
				}
				case HORIZONTAL4 -> {
					JsonObject normal = JsonUtils.getJsonObject(variants, "facing=north");
					blockModelFile = JsonUtils.getString(normal, "model");
				}
				case XYZ -> {
					JsonObject normal = JsonUtils.getJsonObject(variants, "axis=y");
					blockModelFile = JsonUtils.getString(normal, "model");
				}
				default -> throw new AssertionError();
			}

			return new BlockStateEntry(type, IdConverter.blockModelFile2modelId(new ResourceLocation(blockModelFile)));
		}

		@Override
		public JsonElement serialize(BlockStateEntry object, Type type, JsonSerializationContext context) {
			JsonObject jsonobject = new JsonObject();
			jsonobject.add("ics_block_state_type", context.serialize(object.blockstateType));
			JsonObject variants = new JsonObject();
			switch (object.blockstateType) {
				case SIMPLE -> {
					JsonObject normal = new JsonObject();
					normal.addProperty("model", IdConverter.modelId2BlockModelFile(object.blockModelId).toString());
					variants.add("normal", normal);
				}
				case FACE6 -> {
					for (EnumFacing facing : EnumFacing.VALUES) {
						JsonObject obj = new JsonObject();
						obj.addProperty("model", IdConverter.modelId2BlockModelFile(object.blockModelId).toString());
						switch (facing) {
							case DOWN -> obj.addProperty("x", 90);
							case UP -> obj.addProperty("x", 270);
							case NORTH -> {
							}
							case SOUTH -> obj.addProperty("y", 180);
							case WEST -> obj.addProperty("y", 270);
							case EAST -> obj.addProperty("y", 90);
						}
						variants.add("facing=" + facing, obj);
					}
				}
				case HORIZONTAL4 -> {
					for (EnumFacing facing : EnumFacing.HORIZONTALS) {
						JsonObject obj = new JsonObject();
						obj.addProperty("model", IdConverter.modelId2BlockModelFile(object.blockModelId).toString());
						switch (facing) {
							case NORTH -> {
							}
							case SOUTH -> obj.addProperty("y", 180);
							case WEST -> obj.addProperty("y", 270);
							case EAST -> obj.addProperty("y", 90);
						}
						variants.add("facing=" + facing, obj);
					}
				}
				case XYZ -> {
					JsonObject x = new JsonObject();
					x.addProperty("model", IdConverter.modelId2BlockModelFile(object.blockModelId).toString());
					x.addProperty("x", 90);
					x.addProperty("y", 90);
					variants.add("axis=x", x);
					JsonObject y = new JsonObject();
					y.addProperty("model", IdConverter.modelId2BlockModelFile(object.blockModelId).toString());
					variants.add("axis=y", y);
					JsonObject z = new JsonObject();
					z.addProperty("model", IdConverter.modelId2BlockModelFile(object.blockModelId).toString());
					z.addProperty("x", 90);
					variants.add("axis=z", z);
				}
				default -> throw new AssertionError();
			}
			jsonobject.add("variants", variants);
			return jsonobject;
		}
	}

}
