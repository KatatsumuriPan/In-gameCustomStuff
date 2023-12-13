package kpan.ig_custom_stuff.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
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
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.Server;
import kpan.ig_custom_stuff.resource.IdConverter;
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

public class BlockStateEntry {

	public static BlockStateEntry fromByteBuf(ByteBuf buf) {
		BlockStateType blockstateType = MyByteBufUtil.readEnum(buf, BlockStateType.class);
		BlockStateModelEntry blockStateModelEntry = BlockStateModelEntry.fromByteBuf(buf);
		if (buf.readBoolean()) {
			int size = MyByteBufUtil.readVarInt(buf);
			Builder<BlockStateModelEntry> builder = ImmutableList.builder();
			for (int i = 0; i < size; i++) {
				builder.add(BlockStateModelEntry.fromByteBuf(buf));
			}
			ImmutableList<BlockStateModelEntry> customizedModelEntries = builder.build();
			return new BlockStateEntry(blockstateType, blockStateModelEntry, true, customizedModelEntries);
		} else {
			return new BlockStateEntry(blockstateType, blockStateModelEntry);
		}
	}
	public static BlockStateEntry fromJson(String json) {
		return Serializer.GSON.fromJson(json, BlockStateEntry.class);
	}

	public static BlockStateEntry defaultBlockState() {
		return new BlockStateEntry(BlockStateType.SIMPLE, new BlockStateModelEntry(new ResourceLocation(ModTagsGenerated.MODID, "not_configured_blockstate")));
	}

	public final BlockStateType blockstateType;
	public final BlockStateModelEntry blockStateModelEntry;
	public final boolean customizeEach;
	public final ImmutableList<BlockStateModelEntry> customizedModelEntries;

	public BlockStateEntry(BlockStateType blockstateType, BlockStateModelEntry blockStateModelEntry) {
		this(blockstateType, blockStateModelEntry, false, ImmutableList.of());
	}
	public BlockStateEntry(BlockStateType blockstateType, BlockStateModelEntry blockStateModelEntry, boolean customizeEach, ImmutableList<BlockStateModelEntry> customizedModelEntries) {
		this.blockstateType = blockstateType;
		this.blockStateModelEntry = blockStateModelEntry;
		this.customizeEach = customizeEach && blockstateType != BlockStateType.SIMPLE;
		this.customizedModelEntries = customizedModelEntries;
		if (customizeEach) {
			switch (blockstateType) {
				case SIMPLE -> {
					//無視
				}
				case FACE6 -> {
					if (customizedModelEntries.size() != 6)
						throw new IllegalArgumentException("size of blockModelIds is not 6:" + customizedModelEntries.size());
				}
				case HORIZONTAL4 -> {
					if (customizedModelEntries.size() != 4)
						throw new IllegalArgumentException("size of blockModelIds is not 4:" + customizedModelEntries.size());
				}
				case XYZ -> {
					if (customizedModelEntries.size() != 3)
						throw new IllegalArgumentException("size of blockModelIds is not 3:" + customizedModelEntries.size());
				}
			}
		}
	}

	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeEnum(buf, blockstateType);
		blockStateModelEntry.writeTo(buf);
		if (customizeEach) {
			buf.writeBoolean(true);
			MyByteBufUtil.writeVarInt(buf, customizedModelEntries.size());
			for (BlockStateModelEntry blockStateModelEntry : customizedModelEntries) {
				blockStateModelEntry.writeTo(buf);
			}
		} else {
			buf.writeBoolean(false);
		}
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
		return blockstateType.getString() + "(" + blockStateModelEntry + ")";
	}


	public static class BlockStateModelEntry {

		public static BlockStateModelEntry fromByteBuf(ByteBuf buf) {
			ResourceLocation blockModelId = new ResourceLocation(MyByteBufUtil.readString(buf));
			int rotationX = buf.readByte() * 90;
			int rotationY = buf.readByte() * 90;
			return new BlockStateModelEntry(blockModelId, rotationX, rotationY);
		}

		public static BlockStateModelEntry deserialize(JsonObject variant) {
			ResourceLocation blockModelId = IdConverter.blockModelFile2modelId(new ResourceLocation(JsonUtils.getString(variant, "model")));
			int rotationX = JsonUtils.getInt(variant, "x", 0);
			int rotationY = JsonUtils.getInt(variant, "y", 0);
			return new BlockStateModelEntry(blockModelId, rotationX, rotationY);
		}

		public final ResourceLocation blockModelId;
		public final int rotationX;
		public final int rotationY;
		public BlockStateModelEntry(ResourceLocation blockModelId) {
			this(blockModelId, 0, 0);
		}
		public BlockStateModelEntry(ResourceLocation blockModelId, int rotationX, int rotationY) {
			this.blockModelId = blockModelId;
			this.rotationX = rotationX;
			this.rotationY = rotationY;
		}

		public BlockStateModelEntry with(ResourceLocation blockModelId) {
			return new BlockStateModelEntry(blockModelId, rotationX, rotationY);
		}

		public BlockStateModelEntry addRotation(int rotationX, int rotationY) {
			return new BlockStateModelEntry(blockModelId, (this.rotationX + rotationX) % 360, (this.rotationY + rotationY) % 360);
		}

		public void writeTo(ByteBuf buf) {
			MyByteBufUtil.writeString(buf, blockModelId.toString());
			buf.writeByte(rotationX / 90);
			buf.writeByte(rotationY / 90);
		}

		public void serialize(JsonObject jsonObject) {
			jsonObject.addProperty("model", IdConverter.modelId2BlockModelFile(blockModelId).toString());
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
			BlockStateModelEntry blockStateModelEntry;
			switch (type) {
				case SIMPLE -> {
					JsonObject variant = JsonUtils.getJsonObject(variants, "normal");
					blockStateModelEntry = BlockStateModelEntry.deserialize(variant);
				}
				case FACE6 -> {
					JsonObject variant = JsonUtils.getJsonObject(variants, "facing=north");
					blockStateModelEntry = BlockStateModelEntry.deserialize(variant);
				}
				case HORIZONTAL4 -> {
					JsonObject variant = JsonUtils.getJsonObject(variants, "facing=north");
					blockStateModelEntry = BlockStateModelEntry.deserialize(variant);
				}
				case XYZ -> {
					JsonObject variant = JsonUtils.getJsonObject(variants, "axis=y");
					blockStateModelEntry = BlockStateModelEntry.deserialize(variant);
				}
				default -> throw new AssertionError();
			}

			if (jsonObject.has("model_customized") && type != BlockStateType.SIMPLE) {
				Builder<BlockStateModelEntry> builder = ImmutableList.builder();
				switch (type) {
					case FACE6 -> {
						for (EnumFacing facing : EnumFacing.VALUES) {
							JsonObject variant = JsonUtils.getJsonObject(variants, "facing=" + facing);
							builder.add(BlockStateModelEntry.deserialize(variant));
						}
					}
					case HORIZONTAL4 -> {
						for (EnumFacing facing : EnumFacing.HORIZONTALS) {
							JsonObject variant = JsonUtils.getJsonObject(variants, "facing=" + facing);
							builder.add(BlockStateModelEntry.deserialize(variant));
						}
					}
					case XYZ -> {
						builder.add(BlockStateModelEntry.deserialize(JsonUtils.getJsonObject(variants, "axis=x")));
						builder.add(BlockStateModelEntry.deserialize(JsonUtils.getJsonObject(variants, "axis=y")));
						builder.add(BlockStateModelEntry.deserialize(JsonUtils.getJsonObject(variants, "axis=z")));
					}
					default -> throw new AssertionError();
				}
				ImmutableList<BlockStateModelEntry> customizedModelEntries = builder.build();
				return new BlockStateEntry(type, blockStateModelEntry, true, customizedModelEntries);
			} else {
				return new BlockStateEntry(type, blockStateModelEntry);
			}
		}

		@Override
		public JsonElement serialize(BlockStateEntry object, Type type, JsonSerializationContext context) {
			JsonObject jsonobject = new JsonObject();
			jsonobject.add("ics_block_state_type", context.serialize(object.blockstateType));
			if (object.customizeEach)
				jsonobject.addProperty("model_customized", true);
			JsonObject variants = new JsonObject();
			switch (object.blockstateType) {
				case SIMPLE -> {
					JsonObject normal = new JsonObject();
					object.blockStateModelEntry.serialize(normal);
					variants.add("normal", normal);
				}
				case FACE6 -> {
					if (object.customizeEach) {
						for (EnumFacing facing : EnumFacing.VALUES) {
							JsonObject obj = new JsonObject();
							object.customizedModelEntries.get(facing.getIndex()).serialize(obj);
							variants.add("facing=" + facing, obj);
						}
					} else {
						for (EnumFacing facing : EnumFacing.VALUES) {
							JsonObject variant = new JsonObject();
							switch (facing) {
								case DOWN -> object.blockStateModelEntry.addRotation(90, 0).serialize(variant);
								case UP -> object.blockStateModelEntry.addRotation(270, 0).serialize(variant);
								case NORTH -> object.blockStateModelEntry.addRotation(0, 0).serialize(variant);
								case SOUTH -> object.blockStateModelEntry.addRotation(0, 180).serialize(variant);
								case WEST -> object.blockStateModelEntry.addRotation(0, 270).serialize(variant);
								case EAST -> object.blockStateModelEntry.addRotation(0, 90).serialize(variant);
							}
							variants.add("facing=" + facing, variant);
						}
					}
				}
				case HORIZONTAL4 -> {
					if (object.customizeEach) {
						for (EnumFacing facing : EnumFacing.HORIZONTALS) {
							JsonObject obj = new JsonObject();
							object.customizedModelEntries.get(facing.getHorizontalIndex()).serialize(obj);
							variants.add("facing=" + facing, obj);
						}
					} else {
						for (EnumFacing facing : EnumFacing.HORIZONTALS) {
							JsonObject variant = new JsonObject();
							switch (facing) {
								case NORTH -> object.blockStateModelEntry.addRotation(0, 0).serialize(variant);
								case SOUTH -> object.blockStateModelEntry.addRotation(0, 180).serialize(variant);
								case WEST -> object.blockStateModelEntry.addRotation(0, 270).serialize(variant);
								case EAST -> object.blockStateModelEntry.addRotation(0, 90).serialize(variant);
							}
							variants.add("facing=" + facing, variant);
						}
					}
				}
				case XYZ -> {
					if (object.customizeEach) {
						JsonObject x = new JsonObject();
						object.customizedModelEntries.get(0).serialize(x);
						variants.add("axis=x", x);
						JsonObject y = new JsonObject();
						object.customizedModelEntries.get(1).serialize(y);
						variants.add("axis=y", y);
						JsonObject z = new JsonObject();
						object.customizedModelEntries.get(2).serialize(z);
						variants.add("axis=z", z);
					} else {
						JsonObject x = new JsonObject();
						object.blockStateModelEntry.addRotation(90, 90).serialize(x);
						variants.add("axis=x", x);
						JsonObject y = new JsonObject();
						object.blockStateModelEntry.addRotation(0, 0).serialize(y);
						variants.add("axis=y", y);
						JsonObject z = new JsonObject();
						object.blockStateModelEntry.addRotation(90, 0).serialize(z);
						variants.add("axis=z", z);
					}
				}
				default -> throw new AssertionError();
			}
			jsonobject.add("variants", variants);
			return jsonobject;
		}
	}

}
