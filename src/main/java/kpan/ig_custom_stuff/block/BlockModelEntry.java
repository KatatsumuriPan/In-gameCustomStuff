package kpan.ig_custom_stuff.block;

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
import kpan.ig_custom_stuff.resource.JsonUtil;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector3f;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BlockModelEntry {

	public static BlockModelEntry fromByteBuf(ByteBuf buf) {
		ModelType modelType = MyByteBufUtil.readEnum(buf, ModelType.class);
		BlockModelFaceEntry[] faces = new BlockModelFaceEntry[6];
		for (int i = 0; i < faces.length; i++) {
			faces[i] = BlockModelFaceEntry.fromByteBuf(buf);
		}
		Map<String, ResourceLocation> textureIds = new HashMap<>();
		int count = MyByteBufUtil.readVarInt(buf);
		for (int i = 0; i < count; i++) {
			String texture = MyByteBufUtil.readString(buf);
			ResourceLocation textureId = new ResourceLocation(MyByteBufUtil.readString(buf));
			textureIds.put(texture, textureId);
		}
		return new BlockModelEntry(modelType, faces, textureIds);
	}
	public static BlockModelEntry fromJson(String json) {
		return Serializer.GSON.fromJson(json, BlockModelEntry.class);
	}

	public final ModelType modelType;
	public final BlockModelFaceEntry[] faces;
	public final Map<String, ResourceLocation> textureIds;

	public BlockModelEntry(ModelType modelType, BlockModelFaceEntry[] faces, Map<String, ResourceLocation> textureIds) {
		this.modelType = modelType;
		this.faces = faces;
		this.textureIds = textureIds;
		if (faces.length != 6)
			throw new IllegalArgumentException("The length of faces must be 6!");
	}

	public boolean isCage() {
		return modelType == ModelType.CAGE;
	}

	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeEnum(buf, modelType);
		for (BlockModelFaceEntry face : faces) {
			face.writeTo(buf);
		}
		MyByteBufUtil.writeVarInt(buf, textureIds.size());
		for (Entry<String, ResourceLocation> e : textureIds.entrySet()) {
			MyByteBufUtil.writeString(buf, e.getKey());
			MyByteBufUtil.writeString(buf, e.getValue().toString());
		}
	}

	public String toJson() {
		return Serializer.GSON.toJson(this);
	}

	public String getString() {
		return modelType.getString();
	}

	public enum ModelType {
		NORMAL,
		CAGE,
		;
		public static ModelType getFromName(String name) {
			for (ModelType value : values()) {
				if (value.name().equalsIgnoreCase(name))
					return value;
			}
			throw new IllegalArgumentException("Unknown ModelType!:" + name);
		}
		public String getString() {
			switch (this) {
				case NORMAL -> {
					return I18n.format("ingame_custom_stuff.block_model.normal");
				}
				case CAGE -> {
					return I18n.format("ingame_custom_stuff.block_model.cage");
				}
				default -> throw new AssertionError();
			}
		}
	}

	private static class Serializer implements JsonDeserializer<BlockModelEntry>, JsonSerializer<BlockModelEntry> {

		private static final Gson GSON;

		static {
			GsonBuilder gsonbuilder = new GsonBuilder();
			gsonbuilder.registerTypeHierarchyAdapter(BlockModelEntry.class, new Serializer());
			gsonbuilder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
			GSON = gsonbuilder.create();
		}

		@Override
		public BlockModelEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			ModelType type = ModelType.getFromName(JsonUtils.getString(jsonObject, "ics_block_model_type"));
			String parent = jsonObject.getAsJsonPrimitive("parent").getAsString();

			switch (type) {
				case NORMAL -> {
					BlockModelFaceEntry[] faces1 = new BlockModelFaceEntry[6];
					Map<String, ResourceLocation> textureIds = new HashMap<>();
					switch (parent) {
						case "block/cube" -> {
							if (jsonObject.has("elements")) {
								throw new JsonParseException("block/cube with elements is not supported yet.");
							} else {
								//texturesのみ
								JsonObject textures = JsonUtils.getJsonObject(jsonObject, "textures");
								for (Entry<String, JsonElement> entry : textures.entrySet()) {
									textureIds.put(entry.getKey(), new ResourceLocation(entry.getValue().getAsString()));
								}
							}
							for (int i = 0; i < EnumFacing.VALUES.length; i++) {
								EnumFacing face = EnumFacing.VALUES[i];
								faces1[i] = new BlockModelFaceEntry(face.getName2(), TextureUV.DEFAULT, 0, face);
							}
						}
						case "block/block" -> {
							JsonObject textures = JsonUtils.getJsonObject(jsonObject, "textures");
							for (Entry<String, JsonElement> entry : textures.entrySet()) {
								textureIds.put(entry.getKey(), new ResourceLocation(entry.getValue().getAsString()));
							}

							JsonArray elements = JsonUtils.getJsonArray(jsonObject, "elements");
							if (elements.size() != 1)
								throw new JsonParseException("elements size is not 1:" + elements.size());
							JsonObject element = elements.get(0).getAsJsonObject();
							Vector3f from = JsonUtil.parsePosition(element, "from");
//					if (vector3f.x >= -16.0F && vector3f.y >= -16.0F && vector3f.z >= -16.0F && vector3f.x <= 32.0F && vector3f.y <= 32.0F && vector3f.z <= 32.0F)
							if (from.x != 0 || from.y != 0 || from.z != 0)
								throw new JsonParseException("from is not [0, 0, 0]:" + from);
							Vector3f to = JsonUtil.parsePosition(element, "to");
							if (to.x != 16 || to.y != 16 || to.z != 16)
								throw new JsonParseException("to is not [16, 16, 16]:" + to);
							JsonObject faces = JsonUtils.getJsonObject(element, "faces");
							for (int i = 0; i < EnumFacing.VALUES.length; i++) {
								EnumFacing face = EnumFacing.VALUES[i];
								faces1[i] = BlockModelFaceEntry.deserialize(JsonUtils.getJsonObject(faces, face.getName2()), from, to, face);
							}
						}
						default -> throw new JsonParseException("invalid parent:" + parent);
					}

					return new BlockModelEntry(type, faces1, textureIds);
				}
				case CAGE -> {
					if (!parent.equals("block/block"))
						throw new JsonParseException("invalid parent:" + parent);

					Map<String, ResourceLocation> textureIds = new HashMap<>();
					JsonObject textures = JsonUtils.getJsonObject(jsonObject, "textures");
					for (Entry<String, JsonElement> entry : textures.entrySet()) {
						textureIds.put(entry.getKey(), new ResourceLocation(entry.getValue().getAsString()));
					}

					JsonArray elements = JsonUtils.getJsonArray(jsonObject, "elements");
					if (elements.size() != 6)
						throw new JsonParseException("elements size is not 6:" + elements.size());
					BlockModelFaceEntry[] faces1 = new BlockModelFaceEntry[6];
					for (int i = 0; i < elements.size(); i++) {
						JsonObject element = elements.get(i).getAsJsonObject();
						EnumFacing face = JsonUtils.deserializeClass(element, "cage_face", context, EnumFacing.class);
						Vector3f from = JsonUtil.parsePosition(element, "from");
						Vector3f to = JsonUtil.parsePosition(element, "to");
//						if (from.x != 0 || from.y != 0 || from.z != 0)
//							throw new JsonParseException("from is not [0, 0, 0]:" + from);
//						if (to.x != 16 || to.y != 16 || to.z != 16)
//							throw new JsonParseException("to is not [16, 16, 16]:" + to);
						JsonObject faces = JsonUtils.getJsonObject(element, "faces");
						faces1[face.getIndex()] = BlockModelFaceEntry.deserialize(JsonUtils.getJsonObject(faces, face.getName2()), from, to, face);
					}

					return new BlockModelEntry(type, faces1, textureIds);
				}
				default -> throw new AssertionError();
			}
		}

		@Override
		public JsonElement serialize(BlockModelEntry object, Type type, JsonSerializationContext context) {
			JsonObject jsonobject = new JsonObject();
			jsonobject.add("ics_block_model_type", context.serialize(object.modelType));
			switch (object.modelType) {
				case NORMAL -> {
					jsonobject.addProperty("parent", "block/block");
					{
						JsonObject textures = new JsonObject();
						for (Entry<String, ResourceLocation> entry : object.textureIds.entrySet()) {
							textures.addProperty(entry.getKey().replace("#", ""), entry.getValue().toString());
						}
						jsonobject.add("textures", textures);
					}
					{
						JsonArray elements = new JsonArray();
						{
							JsonObject element = new JsonObject();
							{
								JsonArray from = new JsonArray();
								from.add(0);
								from.add(0);
								from.add(0);
								element.add("from", from);
							}
							{
								JsonArray to = new JsonArray();
								to.add(16);
								to.add(16);
								to.add(16);
								element.add("to", to);
							}
							{
								JsonObject faces = new JsonObject();
								for (int i = 0; i < object.faces.length; i++) {
									EnumFacing face = EnumFacing.byIndex(i);
									faces.add(face.getName2(), object.faces[i].serialize());
								}
								element.add("faces", faces);
							}
							elements.add(element);
						}
						jsonobject.add("elements", elements);
					}
				}
				case CAGE -> {
					jsonobject.addProperty("parent", "block/block");
					{
						JsonObject textures = new JsonObject();
						for (Entry<String, ResourceLocation> entry : object.textureIds.entrySet()) {
							textures.addProperty(entry.getKey().replace("#", ""), entry.getValue().toString());
						}
						jsonobject.add("textures", textures);
					}
					{
						JsonArray elements = new JsonArray();
						{
							for (int i = 0; i < object.faces.length; i++) {
								EnumFacing face = EnumFacing.byIndex(i);
								JsonObject element = new JsonObject();
								element.add("cage_face", context.serialize(face));
								{
									JsonArray from = new JsonArray();
									switch (face) {
										case DOWN -> {
											from.add(0);
											from.add(0);
											from.add(0);
										}
										case UP -> {
											from.add(0);
											from.add(15.9f);
											from.add(0);
										}
										case NORTH -> {
											from.add(0);
											from.add(0);
											from.add(0);
										}
										case SOUTH -> {
											from.add(0);
											from.add(0);
											from.add(15.9f);
										}
										case WEST -> {
											from.add(0);
											from.add(0);
											from.add(0);
										}
										case EAST -> {
											from.add(15.9f);
											from.add(0);
											from.add(0);
										}
									}
									element.add("from", from);
								}
								{
									JsonArray to = new JsonArray();
									switch (face) {
										case DOWN -> {
											to.add(16);
											to.add(0.1f);
											to.add(16);
										}
										case UP -> {
											to.add(16);
											to.add(16);
											to.add(16);
										}
										case NORTH -> {
											to.add(16);
											to.add(16);
											to.add(0.1f);
										}
										case SOUTH -> {
											to.add(16);
											to.add(16);
											to.add(16);
										}
										case WEST -> {
											to.add(0.1f);
											to.add(16);
											to.add(16);
										}
										case EAST -> {
											to.add(16);
											to.add(16);
											to.add(16);
										}
									}
									element.add("to", to);
								}
								{
									JsonObject faces = new JsonObject();
									BlockModelFaceEntry faceEntry = object.faces[i];
									faces.add(face.getName2(), faceEntry.serialize());
									TextureUV uv = faceEntry.uv;
									TextureUV opposite_uv;
									if ((faceEntry.rotation / 90 % 2 == 0) == (face.getAxis() != Axis.Y))
										opposite_uv = new TextureUV(uv.maxU, uv.minV, uv.minU, uv.maxV);
									else
										opposite_uv = new TextureUV(uv.minU, uv.maxV, uv.maxU, uv.minV);
									BlockModelFaceEntry opposite = new BlockModelFaceEntry(faceEntry.textureTag, opposite_uv, faceEntry.rotation, null);
									faces.add(face.getOpposite().getName2(), opposite.serialize());
									element.add("faces", faces);
								}
								elements.add(element);
							}
						}
						jsonobject.add("elements", elements);
					}
				}
			}
			return jsonobject;
		}
	}

}
