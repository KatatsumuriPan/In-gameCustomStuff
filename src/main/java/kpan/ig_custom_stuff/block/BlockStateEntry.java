package kpan.ig_custom_stuff.block;

import com.google.common.collect.ImmutableMap;
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
import kpan.ig_custom_stuff.resource.DynamicResourceLoader.SingleBlockModelLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import kpan.ig_custom_stuff.resource.IdConverter;
import kpan.ig_custom_stuff.resource.ids.BlockId;
import kpan.ig_custom_stuff.resource.ids.BlockModelGroupId;
import kpan.ig_custom_stuff.resource.ids.BlockModelGroupId.BlockModelGroupType;
import kpan.ig_custom_stuff.resource.ids.BlockModelId;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import kpan.ig_custom_stuff.util.RenderUtil;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

public class BlockStateEntry {

	public static BlockStateEntry fromByteBuf(ByteBuf buf) {
		BlockStateType blockstateType = MyByteBufUtil.readEnum(buf, BlockStateType.class);
		BlockModelGroupId blockModelGroupId = BlockModelGroupId.formByteBuf(buf);
		int rotationX = buf.readByte() * 90;
		int rotationY = buf.readByte() * 90;
		int count = MyByteBufUtil.readVarInt(buf);
		ImmutableMap.Builder<String, BlockStateModelEntry> builder = ImmutableMap.builder();
		for (int i = 0; i < count; i++) {
			String variant = MyByteBufUtil.readString(buf);
			BlockStateModelEntry blockStateModelEntry = BlockStateModelEntry.fromByteBuf(buf);
			builder.put(variant, blockStateModelEntry);
		}
		return new BlockStateEntry(blockstateType, blockModelGroupId, rotationX, rotationY, builder.build());
	}
	public static BlockStateEntry fromJson(String json) {
		return Serializer.GSON.fromJson(json, BlockStateEntry.class);
	}

	public static BlockStateEntry defaultBlockState() {
		return new BlockStateEntry(BlockStateType.SIMPLE, new BlockModelGroupId(BlockModelGroupType.NORMAL, ModTagsGenerated.MODID, "not_configured_blockstate"));
	}

	public final BlockStateType blockstateType;
	public final BlockModelGroupId blockModelGroupId;
	public final int rotationX;
	public final int rotationY;
	public final ImmutableMap<String, BlockStateModelEntry> overrides;//variant->

	public BlockStateEntry(BlockStateType blockstateType, BlockModelGroupId blockModelGroupId) {
		this(blockstateType, blockModelGroupId, 0, 0, ImmutableMap.of());
	}
	public BlockStateEntry(BlockStateType blockstateType, BlockModelGroupId blockModelGroupId, int rotationX, int rotationY, ImmutableMap<String, BlockStateModelEntry> overrides) {
		this.blockstateType = blockstateType;
		this.blockModelGroupId = blockModelGroupId;
		this.rotationX = rotationX;
		this.rotationY = rotationY;
		this.overrides = overrides;
	}

	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeEnum(buf, blockstateType);
		blockModelGroupId.writeTo(buf);
		buf.writeByte(rotationX / 90);
		buf.writeByte(rotationY / 90);
		MyByteBufUtil.writeVarInt(buf, overrides.size());
		for (Entry<String, BlockStateModelEntry> entry : overrides.entrySet()) {
			MyByteBufUtil.writeString(buf, entry.getKey());
			entry.getValue().writeTo(buf);
		}
	}

	public void register(BlockId blockId, boolean isRemote) throws IOException {
		if (isRemote) {
			ClientCache.INSTANCE.addBlockstate(blockId, this);
		} else {
			Server.INSTANCE.addBlockstate(blockId, this);
		}
	}

	public void update(BlockId blockId, boolean isRemote) throws IOException {
		if (isRemote) {
			ClientCache.INSTANCE.replaceBlockstate(blockId, this);
		} else {
			Server.INSTANCE.replaceBlockstate(blockId, this);
		}
	}

	public String getItemModelJson(DynamicResourceManager dynamicResourceManager) {
		switch (blockstateType) {
			case SIMPLE, FACE6, HORIZONTAL4, XYZ -> {
				return "{\n" +
						"    \"ics_itemblock_model_type\": \"block\",\n" +
						"    \"parent\": \"" + blockModelGroupId.getRenderModelId() + "\"\n" +
						"}\n";
			}
			case SLAB -> {
				return "{\n" +
						"    \"ics_itemblock_model_type\": \"block\",\n" +
						"    \"parent\": \"" + blockModelGroupId.getRenderModelId() + "\"\n" +
						"}\n";
			}
			default -> throw new AssertionError();
		}
	}

	public String toJson() {
		return Serializer.GSON.toJson(this);
	}

	public String getString() {
		return blockstateType.getString() + "(" + blockModelGroupId.namespace + ":" + blockModelGroupId.path + ")";
	}


	public static class BlockStateModelEntry {

		public static BlockStateModelEntry fromByteBuf(ByteBuf buf) {
			BlockModelId blockModelId = BlockModelId.formByteBuf(buf);
			int rotationX = buf.readByte() * 90;
			int rotationY = buf.readByte() * 90;
			return new BlockStateModelEntry(blockModelId, rotationX, rotationY);
		}

		public static BlockStateModelEntry deserialize(JsonObject variant) {
			BlockModelId blockModelId = new BlockModelId(IdConverter.blockModelFile2modelId(new ResourceLocation(JsonUtils.getString(variant, "model"))));
			int rotationX = JsonUtils.getInt(variant, "x", 0);
			int rotationY = JsonUtils.getInt(variant, "y", 0);
			return new BlockStateModelEntry(blockModelId, rotationX, rotationY);
		}

		public final BlockModelId blockModelId;
		public final int rotationX;
		public final int rotationY;
		public BlockStateModelEntry(BlockModelId blockModelId) {
			this(blockModelId, 0, 0);
		}
		public BlockStateModelEntry(BlockModelId blockModelId, int rotationX, int rotationY) {
			this.blockModelId = blockModelId;
			this.rotationX = Math.floorMod(rotationX, 360);
			this.rotationY = Math.floorMod(rotationY, 360);
		}

		public BlockStateModelEntry with(BlockModelId blockModelId) {
			return new BlockStateModelEntry(blockModelId, rotationX, rotationY);
		}

		public BlockStateModelEntry addRotation(int rotationX, int rotationY) {
			return new BlockStateModelEntry(blockModelId, this.rotationX + rotationX, this.rotationY + rotationY);
		}

		public void writeTo(ByteBuf buf) {
			blockModelId.writeTo(buf);
			buf.writeByte(rotationX / 90);
			buf.writeByte(rotationY / 90);
		}

		public void serialize(JsonObject jsonObject) {
			jsonObject.addProperty("model", IdConverter.modelId2BlockModelFile(blockModelId.toResourceLocation()).toString());
			if (rotationX != 0)
				jsonObject.addProperty("x", rotationX);
			if (rotationY != 0)
				jsonObject.addProperty("y", rotationY);
		}

		@SideOnly(Side.CLIENT)
		public void render(int x, int y, float scale, float yaw, float pitch) {
			IBakedModel model = SingleBlockModelLoader.getModel(blockModelId);
			if (model != null)
				RenderUtil.renderModel(x, y, scale, yaw - rotationY, pitch, rotationX, model);
		}

		@Override
		public String toString() {
			if (rotationX == 0 && rotationY == 0)
				return blockModelId.toString();
			else
				return blockModelId + "(rotX: " + rotationX + ", rotY: " + rotationY + ')';
		}
	}

	public enum BlockStateType {
		SIMPLE,
		FACE6,
		HORIZONTAL4,
		XYZ,
		SLAB,
		;
		public static BlockStateType getFromName(String name) {
			for (BlockStateType value : values()) {
				if (value.name().equalsIgnoreCase(name))
					return value;
			}
			throw new IllegalArgumentException("Unknown BlockStateType!:" + name);
		}
		public BlockModelGroupType getModelGroupType() {
			switch (this) {
				case SIMPLE, FACE6, HORIZONTAL4, XYZ -> {
					return BlockModelGroupType.NORMAL;
				}
				case SLAB -> {
					return BlockModelGroupType.SLAB;
				}
				default -> throw new AssertionError();
			}
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
				case SLAB -> {
					return I18n.format("ingame_custom_stuff.block_state.slab");
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
			BlockModelGroupType blockModelGroupType;
			BlockStateModelEntry blockStateModelEntry;
			switch (type) {
				case SIMPLE -> {
					blockModelGroupType = BlockModelGroupType.NORMAL;
					JsonObject variant = JsonUtils.getJsonObject(variants, "normal");
					blockStateModelEntry = BlockStateModelEntry.deserialize(variant);
				}
				case FACE6 -> {
					blockModelGroupType = BlockModelGroupType.NORMAL;
					JsonObject variant = JsonUtils.getJsonObject(variants, "facing=north");
					blockStateModelEntry = BlockStateModelEntry.deserialize(variant);
				}
				case HORIZONTAL4 -> {
					blockModelGroupType = BlockModelGroupType.NORMAL;
					JsonObject variant = JsonUtils.getJsonObject(variants, "facing=north");
					blockStateModelEntry = BlockStateModelEntry.deserialize(variant);
				}
				case XYZ -> {
					blockModelGroupType = BlockModelGroupType.NORMAL;
					JsonObject variant = JsonUtils.getJsonObject(variants, "axis=y");
					blockStateModelEntry = BlockStateModelEntry.deserialize(variant);
				}
				case SLAB -> {
					blockModelGroupType = BlockModelGroupType.SLAB;
					JsonObject variant = JsonUtils.getJsonObject(variants, "slab=double");
					blockStateModelEntry = BlockStateModelEntry.deserialize(variant);
				}
				default -> throw new AssertionError();
			}

			return new BlockStateEntry(type, new BlockModelGroupId(blockModelGroupType, blockStateModelEntry.blockModelId), blockStateModelEntry.rotationX, blockStateModelEntry.rotationY, ImmutableMap.of());
		}

		@Override
		public JsonElement serialize(BlockStateEntry object, Type type, JsonSerializationContext context) {
			JsonObject jsonobject = new JsonObject();
			jsonobject.add("ics_block_state_type", context.serialize(object.blockstateType));
			JsonObject variants = new JsonObject();
			switch (object.blockstateType) {
				case SIMPLE -> {
					JsonObject normal = new JsonObject();
					new BlockStateModelEntry(object.blockModelGroupId.getRenderModelId(), object.rotationX, object.rotationY).serialize(normal);
					variants.add("normal", normal);
				}
				case FACE6 -> {
					BlockStateModelEntry baseModel = new BlockStateModelEntry(object.blockModelGroupId.getRenderModelId(), object.rotationX, object.rotationY);
					for (EnumFacing facing : EnumFacing.VALUES) {
						JsonObject variant = new JsonObject();
						switch (facing) {
							case DOWN -> baseModel.addRotation(90, 0).serialize(variant);
							case UP -> baseModel.addRotation(270, 0).serialize(variant);
							case NORTH -> baseModel.addRotation(0, 0).serialize(variant);
							case SOUTH -> baseModel.addRotation(0, 180).serialize(variant);
							case WEST -> baseModel.addRotation(0, 270).serialize(variant);
							case EAST -> baseModel.addRotation(0, 90).serialize(variant);
						}
						variants.add("facing=" + facing, variant);
					}
				}
				case HORIZONTAL4 -> {
					BlockStateModelEntry baseModel = new BlockStateModelEntry(object.blockModelGroupId.getRenderModelId(), object.rotationX, object.rotationY);
					for (EnumFacing facing : EnumFacing.HORIZONTALS) {
						JsonObject variant = new JsonObject();
						switch (facing) {
							case NORTH -> baseModel.addRotation(0, 0).serialize(variant);
							case SOUTH -> baseModel.addRotation(0, 180).serialize(variant);
							case WEST -> baseModel.addRotation(0, 270).serialize(variant);
							case EAST -> baseModel.addRotation(0, 90).serialize(variant);
						}
						variants.add("facing=" + facing, variant);
					}
				}
				case XYZ -> {
					BlockStateModelEntry baseModel = new BlockStateModelEntry(object.blockModelGroupId.getRenderModelId(), object.rotationX, object.rotationY);
					JsonObject x = new JsonObject();
					baseModel.addRotation(90, 90).serialize(x);
					variants.add("axis=x", x);
					JsonObject y = new JsonObject();
					baseModel.addRotation(0, 0).serialize(y);
					variants.add("axis=y", y);
					JsonObject z = new JsonObject();
					baseModel.addRotation(90, 0).serialize(z);
					variants.add("axis=z", z);
				}
				case SLAB -> {
					Map<String, BlockModelId> map = object.blockModelGroupId.getBlockModelIds();
					JsonObject top = new JsonObject();
					new BlockStateModelEntry(map.get("top"), object.rotationX, object.rotationY).serialize(top);
					variants.add("slab=top", top);
					JsonObject bottom = new JsonObject();
					new BlockStateModelEntry(map.get("bottom"), object.rotationX, object.rotationY).serialize(bottom);
					variants.add("slab=bottom", bottom);
					JsonObject dbl = new JsonObject();
					new BlockStateModelEntry(map.get("double"), object.rotationX, object.rotationY).serialize(dbl);
					variants.add("slab=double", dbl);
				}
				default -> throw new AssertionError();
			}
			jsonobject.add("variants", variants);
			return jsonobject;
		}
	}

}
