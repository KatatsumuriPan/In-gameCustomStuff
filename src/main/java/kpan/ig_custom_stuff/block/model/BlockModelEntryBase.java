package kpan.ig_custom_stuff.block.model;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.block.BlockModelFaceJson;
import kpan.ig_custom_stuff.block.EnumSlabType;
import kpan.ig_custom_stuff.block.TextureUV;
import kpan.ig_custom_stuff.block.model.BlockModelEntryStair.StairModelType;
import kpan.ig_custom_stuff.resource.ids.BlockModelGroupId.BlockModelGroupType;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector3f;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public abstract class BlockModelEntryBase {

	public static final String VARIANT_MARKER = "-";
	public static BlockModelEntryBase fromJson(String json) {
		return Serializer.GSON.fromJson(json, BlockModelEntryBase.class);
	}
	public static BlockModelEntryBase fromByteBuf(ByteBuf buf) {
		ModelType modelType = MyByteBufUtil.readEnum(buf, ModelType.class);
		BlockModelEntryBase blockModelEntry;
		switch (modelType) {
			case NORMAL -> blockModelEntry = BlockModelEntryNormal.fromBuf(buf);
			case CAGE -> blockModelEntry = BlockModelEntryCage.fromBuf(buf);
			case SLAB_TOP -> {
				blockModelEntry = BlockModelEntrySlab.fromBuf(buf, EnumSlabType.TOP);
			}
			case SLAB_BOTTOM -> {
				blockModelEntry = BlockModelEntrySlab.fromBuf(buf, EnumSlabType.BOTTOM);
			}
			case SLAB_DOUBLE -> {
				blockModelEntry = BlockModelEntrySlab.fromBuf(buf, EnumSlabType.DOUBLE);
			}
			case STAIR_STRAIGHT -> {
				blockModelEntry = BlockModelEntryStair.fromBuf(buf, StairModelType.STRAIGHT);
			}
			case STAIR_INNER -> {
				blockModelEntry = BlockModelEntryStair.fromBuf(buf, StairModelType.INNER);
			}
			case STAIR_OUTER -> {
				blockModelEntry = BlockModelEntryStair.fromBuf(buf, StairModelType.OUTER);
			}
			default -> throw new AssertionError();
		}
		int count = MyByteBufUtil.readVarInt(buf);
		for (int i = 0; i < count; i++) {
			String textureTag = MyByteBufUtil.readString(buf);
			BlockModelTextureEntry blockModelTextureEntry = BlockModelTextureEntry.fromByteBuf(buf);
			blockModelEntry.setTexture(textureTag, blockModelTextureEntry);
		}
		return blockModelEntry;
	}

	public final ModelType modelType;
	protected final Map<String, BlockModelTextureEntry> textures = new HashMap<>();
	protected BlockModelEntryBase(ModelType modelType) {
		this.modelType = modelType;
	}

	//textureTag->face
	public final ImmutableMap<String, BlockModelTextureEntry> getTextures() {
		return ImmutableMap.copyOf(textures);
	}
	public final BlockModelTextureEntry getTexture(String textureTag) {
		return textures.get(textureTag);
	}
	public final void setTexture(String textureTag, BlockModelTextureEntry blockModelTextureEntry) {
		textures.put(textureTag, blockModelTextureEntry);
	}

	public final void replaceTextureId(String textureTag, ResourceLocation textureId) {
		textures.put(textureTag, textures.get(textureTag).with(textureId));
	}

	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeEnum(buf, modelType);
		writeToInternal(buf);
		MyByteBufUtil.writeVarInt(buf, textures.size());
		for (Entry<String, BlockModelTextureEntry> e : textures.entrySet()) {
			MyByteBufUtil.writeString(buf, e.getKey());
			e.getValue().writeTo(buf);
		}
	}
	public String toJson() {
		return Serializer.GSON.toJson(this);
	}

	protected abstract void writeToInternal(ByteBuf buf);
	protected abstract void serializeInternal(JsonObject jsonObject);

	protected static void deserializeFaces(JsonObject element, Vector3f from, Vector3f to, BlockModelEntryBase blockModelEntry, JsonObject textures) {
		JsonObject faces = JsonUtils.getJsonObject(element, "faces");
		for (int i = 0; i < EnumFacing.VALUES.length; i++) {
			EnumFacing face = EnumFacing.VALUES[i];
			BlockModelFaceJson faceJson = BlockModelFaceJson.deserialize(JsonUtils.getJsonObject(faces, face.getName2()), from, to, face);
			blockModelEntry.setTexture(face.getName2(), new BlockModelTextureEntry(new ResourceLocation(JsonUtils.getString(textures, faceJson.textureTag)), faceJson.uv, faceJson.rotation));
		}
		blockModelEntry.setTexture("particle", new BlockModelTextureEntry(new ResourceLocation(JsonUtils.getString(textures, "particle")), TextureUV.FULL, 0));
	}
	protected void serializeFaces(JsonObject element) {
		JsonObject faces = new JsonObject();
		for (EnumFacing facing : EnumFacing.values()) {
			faces.add(facing.getName2(), textures.get(facing.getName2()).serialize(facing.getName2(), facing));
		}
		element.add("faces", faces);
	}

	protected static JsonElement createDisplay(int rotationX, int rotationY, int rotationZ, int translationX, int translationY, int translationZ, double scale) {
		JsonObject jsonObject = new JsonObject();
		JsonArray rotation = new JsonArray();
		rotation.add(rotationX);
		rotation.add(rotationY);
		rotation.add(rotationZ);
		jsonObject.add("rotation", rotation);
		JsonArray translation = new JsonArray();
		translation.add(translationX);
		translation.add(translationY);
		translation.add(translationZ);
		jsonObject.add("translation", translation);
		JsonArray s = new JsonArray();
		s.add(scale);
		s.add(scale);
		s.add(scale);
		jsonObject.add("scale", s);
		return jsonObject;
	}

	public enum ModelType {
		NORMAL,
		CAGE,
		SLAB_TOP,
		SLAB_BOTTOM,
		SLAB_DOUBLE,
		STAIR_STRAIGHT,
		STAIR_INNER,
		STAIR_OUTER,
		/*
		追加されうるもの
		normal
		slab
		縦slab
		stair
		fence
		fence-gate
		wall
		pressure-plate
		button
		sign
		trap-door
		rail
		plant
		plant-double
		glass-pane
		coral-fan
		ladder
		floating-leave
		carpet
		torch
		door
		 */;
		public static ModelType getFromName(String name) {
			for (ModelType value : values()) {
				if (value.name().equalsIgnoreCase(name))
					return value;
			}
			throw new IllegalArgumentException("Unknown ModelType!:" + name);
		}
		public String getString() {
			switch (this) {
				case NORMAL, CAGE -> {
					return I18n.format("ingame_custom_stuff.block_model." + name().toLowerCase(Locale.ROOT));
				}
				case SLAB_DOUBLE, SLAB_TOP, SLAB_BOTTOM -> {
					return I18n.format("ingame_custom_stuff.block_model.slab");
				}
				case STAIR_STRAIGHT, STAIR_INNER, STAIR_OUTER -> {
					return I18n.format("ingame_custom_stuff.block_model.stair");
				}
				default -> throw new AssertionError();
			}
		}

		public BlockModelGroupType toBlockModelGroupType() {
			switch (this) {
				case NORMAL, CAGE -> {
					return BlockModelGroupType.NORMAL;
				}
				case SLAB_DOUBLE, SLAB_TOP, SLAB_BOTTOM -> {
					return BlockModelGroupType.SLAB;
				}
				case STAIR_STRAIGHT, STAIR_INNER, STAIR_OUTER -> {
					return BlockModelGroupType.STAIR;
				}
				default -> throw new AssertionError();
			}
		}
	}

	private static class Serializer implements JsonDeserializer<BlockModelEntryBase>, JsonSerializer<BlockModelEntryBase> {

		private static final Gson GSON;

		static {
			GsonBuilder gsonbuilder = new GsonBuilder();
			gsonbuilder.registerTypeHierarchyAdapter(BlockModelEntryBase.class, new Serializer());
			gsonbuilder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
			GSON = gsonbuilder.create();
		}

		@Override
		public BlockModelEntryBase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			ModelType type = ModelType.getFromName(JsonUtils.getString(jsonObject, "ics_block_model_type"));
			switch (type) {
				case NORMAL -> {
					return BlockModelEntryNormal.deserialize(jsonObject);
				}
				case CAGE -> {
					return BlockModelEntryCage.deserialize(jsonObject);
				}
				case SLAB_TOP -> {
					return BlockModelEntrySlab.deserialize(jsonObject, EnumSlabType.TOP);
				}
				case SLAB_BOTTOM -> {
					return BlockModelEntrySlab.deserialize(jsonObject, EnumSlabType.BOTTOM);
				}
				case SLAB_DOUBLE -> {
					return BlockModelEntrySlab.deserialize(jsonObject, EnumSlabType.DOUBLE);
				}
				case STAIR_STRAIGHT -> {
					return BlockModelEntryStair.deserialize(jsonObject, StairModelType.STRAIGHT);
				}
				case STAIR_INNER -> {
					return BlockModelEntryStair.deserialize(jsonObject, StairModelType.INNER);
				}
				case STAIR_OUTER -> {
					return BlockModelEntryStair.deserialize(jsonObject, StairModelType.OUTER);
				}
				default -> throw new AssertionError();
			}
		}

		@Override
		public JsonElement serialize(BlockModelEntryBase object, Type type, JsonSerializationContext context) {
			JsonObject jsonobject = new JsonObject();
			jsonobject.add("ics_block_model_type", context.serialize(object.modelType));
			object.serializeInternal(jsonobject);
			return jsonobject;
		}
	}

}
