package kpan.ig_custom_stuff.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.block.BlockModelFaceJson;
import kpan.ig_custom_stuff.block.TextureUV;
import kpan.ig_custom_stuff.resource.JsonUtil;
import kpan.ig_custom_stuff.util.MyJsonUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector3f;
import java.util.Map;
import java.util.Map.Entry;

public class BlockModelEntryStair extends BlockModelEntryBase {

	protected static BlockModelEntryStair deserialize(JsonObject jsonObject, StairModelType stairModelType) {
		BlockModelEntryStair blockModelEntryStair = new BlockModelEntryStair(stairModelType);
		String parent = jsonObject.getAsJsonPrimitive("parent").getAsString();
		if (!parent.equals("block/block"))
			throw new JsonParseException("invalid parent:" + parent);
		JsonObject textures = JsonUtils.getJsonObject(jsonObject, "textures");
		blockModelEntryStair.setTexture("particle", new BlockModelTextureEntry(new ResourceLocation(JsonUtils.getString(textures, "particle")), TextureUV.FULL, 0));

		JsonArray elements = JsonUtils.getJsonArray(jsonObject, "elements");
		switch (stairModelType) {
			case STRAIGHT -> {
				if (elements.size() != 2)
					throw new JsonParseException("elements size is not 2, found:" + elements.size());
				for (int i = 0; i < elements.size(); i++) {
					JsonObject element = elements.get(i).getAsJsonObject();
					StairPart stairPart = MyJsonUtil.deserializeEnum(element, "stair_part", StairPart.class);
					if (stairPart == StairPart.BOTTOM) {
						Vector3f from = JsonUtil.parsePosition(element, "from");
						if (from.x != 0 || from.y != 0 || from.z != 0)
							throw new JsonParseException("from is not [0, 0, 0], found:" + from);
						Vector3f to = JsonUtil.parsePosition(element, "to");
						if (to.x != 16 || to.y != 8 || to.z != 16)
							throw new JsonParseException("to is not [16, 8, 16], found:" + to);
						JsonObject faces = JsonUtils.getJsonObject(element, "faces");
						for (int j = 0; j < EnumFacing.VALUES.length; j++) {
							EnumFacing face = EnumFacing.VALUES[j];
							BlockModelFaceJson faceJson = BlockModelFaceJson.deserialize(JsonUtils.getJsonObject(faces, face.getName2()), from, to, face);
							if (face.getAxis() != Axis.Y) {
								faceJson = faceJson.subFace(0, -1, 1, 1);//上方向に拡張
							}
							blockModelEntryStair.setTexture(faceJson.textureTag, new BlockModelTextureEntry(new ResourceLocation(JsonUtils.getString(textures, faceJson.textureTag)), faceJson.uv, faceJson.rotation));
						}
					} else {
						//無視
					}
				}
			}
			case INNER -> {
				if (elements.size() != 3)
					throw new JsonParseException("elements size is not 3, found:" + elements.size());
				for (int i = 0; i < elements.size(); i++) {
					JsonObject element = elements.get(i).getAsJsonObject();
					StairPart stairPart = MyJsonUtil.deserializeEnum(element, "stair_part", StairPart.class);
					switch (stairPart) {
						case BOTTOM -> {
							Vector3f from = JsonUtil.parsePosition(element, "from");
							if (from.x != 0 || from.y != 0 || from.z != 0)
								throw new JsonParseException("from is not [0, 0, 0], found:" + from);
							Vector3f to = JsonUtil.parsePosition(element, "to");
							if (to.x != 16 || to.y != 8 || to.z != 16)
								throw new JsonParseException("to is not [16, 8, 16], found:" + to);
							JsonObject faces = JsonUtils.getJsonObject(element, "faces");
							//front以外全部のテクスチャ
							for (EnumFacing face : new EnumFacing[]{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST}) {
								BlockModelFaceJson faceJson = BlockModelFaceJson.deserialize(JsonUtils.getJsonObject(faces, face.getName2()), from, to, face);
								if (face.getAxis() != Axis.Y) {
									faceJson = faceJson.subFace(0, -1, 1, 1);//上方向に拡張
								}
								blockModelEntryStair.setTexture(faceJson.textureTag, new BlockModelTextureEntry(new ResourceLocation(JsonUtils.getString(textures, faceJson.textureTag)), faceJson.uv, faceJson.rotation));
							}
						}
						case TOP_STRAIGHT -> {
							JsonObject faces = JsonUtils.getJsonObject(element, "faces");
							Vector3f from = JsonUtil.parsePosition(element, "from");
							if (from.x != 0 || from.y != 8 || from.z != 0)
								throw new JsonParseException("from is not [0, 8, 0], found:" + from);
							Vector3f to = JsonUtil.parsePosition(element, "to");
							if (to.x != 16 || to.y != 16 || to.z != 8)
								throw new JsonParseException("to is not [16, 16, 8], found:" + to);
							//front以外全部のテクスチャ
							EnumFacing face = EnumFacing.SOUTH;
							BlockModelFaceJson faceJson = BlockModelFaceJson.deserialize(JsonUtils.getJsonObject(faces, face.getName2()), from, to, face);
							faceJson = faceJson.subFace(0, 0, 1, 2);//下方向に拡張
							blockModelEntryStair.setTexture(faceJson.textureTag, new BlockModelTextureEntry(new ResourceLocation(JsonUtils.getString(textures, faceJson.textureTag)), faceJson.uv, faceJson.rotation));
						}
						default -> {
							//無視
						}
					}
				}
			}
			case OUTER -> {
				if (elements.size() != 2)
					throw new JsonParseException("elements size is not 2, found:" + elements.size());
				for (int i = 0; i < elements.size(); i++) {
					JsonObject element = elements.get(i).getAsJsonObject();
					StairPart stairPart = MyJsonUtil.deserializeEnum(element, "stair_part", StairPart.class);
					if (stairPart == StairPart.BOTTOM) {
						Vector3f from = JsonUtil.parsePosition(element, "from");
						if (from.x != 0 || from.y != 0 || from.z != 0)
							throw new JsonParseException("from is not [0, 0, 0], found:" + from);
						Vector3f to = JsonUtil.parsePosition(element, "to");
						if (to.x != 16 || to.y != 8 || to.z != 16)
							throw new JsonParseException("to is not [16, 8, 16], found:" + to);
						JsonObject faces = JsonUtils.getJsonObject(element, "faces");
						//back以外全部のテクスチャ
						//backは存在しない
						for (EnumFacing face : new EnumFacing[]{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST}) {
							BlockModelFaceJson faceJson = BlockModelFaceJson.deserialize(JsonUtils.getJsonObject(faces, face.getName2()), from, to, face);
							if (face.getAxis() != Axis.Y) {
								faceJson = faceJson.subFace(0, -1, 1, 1);//上方向に拡張
							}
							blockModelEntryStair.setTexture(faceJson.textureTag, new BlockModelTextureEntry(new ResourceLocation(JsonUtils.getString(textures, faceJson.textureTag)), faceJson.uv, faceJson.rotation));
						}
					} else {
						//無視
					}
				}
			}
		}
		for (int i = 0; i < elements.size(); i++) {
			JsonObject element = elements.get(i).getAsJsonObject();
			StairPart stairPart = MyJsonUtil.deserializeEnum(element, "stair_part", StairPart.class);
			if (stairPart == StairPart.BOTTOM) {
				Vector3f from = JsonUtil.parsePosition(element, "from");
				if (from.x != 0 || from.y != 0 || from.z != 0)
					throw new JsonParseException("from is not [0, 0, 0], found:" + from);
				Vector3f to = JsonUtil.parsePosition(element, "to");
				if (to.x != 16 || to.y != 8 || to.z != 16)
					throw new JsonParseException("to is not [16, 8, 16], found:" + to);
				JsonObject faces = JsonUtils.getJsonObject(element, "faces");
				for (int j = 0; j < EnumFacing.VALUES.length; j++) {
					EnumFacing face = EnumFacing.VALUES[j];
					BlockModelFaceJson faceJson = BlockModelFaceJson.deserialize(JsonUtils.getJsonObject(faces, face.getName2()), from, to, face);
					if (face.getAxis() != Axis.Y) {
						faceJson = faceJson.subFace(0, -1, 1, 1);//上方向に拡張
					}
					blockModelEntryStair.setTexture(face.getName2(), new BlockModelTextureEntry(new ResourceLocation(JsonUtils.getString(textures, faceJson.textureTag)), faceJson.uv, faceJson.rotation));
				}
			} else {
				//無視
			}
		}
		return blockModelEntryStair;
	}

	protected static BlockModelEntryStair fromBuf(ByteBuf buf, StairModelType stairModelType) {
		return new BlockModelEntryStair(stairModelType);
	}

	private final StairModelType stairModelType;

	private BlockModelEntryStair(StairModelType stairModelType) {
		super(toModelType(stairModelType));
		this.stairModelType = stairModelType;
	}
	public BlockModelEntryStair(StairModelType stairModelType, Map<String, BlockModelTextureEntry> textures) {
		this(stairModelType);
		this.textures.putAll(textures);
	}

	@Override
	protected void writeToInternal(ByteBuf buf) {
		//何もしない
	}
	@Override
	protected void serializeInternal(JsonObject jsonObject) {
		jsonObject.addProperty("parent", "block/block");
		{
			JsonObject display = new JsonObject();
			display.add("gui", createDisplay(30, 45, 0, 0, 0, 0, 0.625));
			jsonObject.add("display", display);
		}
		{
			JsonObject textures = new JsonObject();
			for (Entry<String, BlockModelTextureEntry> entry : this.textures.entrySet()) {
				textures.addProperty(entry.getKey(), entry.getValue().textureId.toString());
			}
			jsonObject.add("textures", textures);
		}
		{
			JsonArray elements = new JsonArray();
			{
				switch (stairModelType) {
					case STRAIGHT -> {
						{
							JsonObject bottom = new JsonObject();
							bottom.add("stair_part", MyJsonUtil.serializeEnum(StairPart.BOTTOM));
							{
								JsonArray from = new JsonArray();
								from.add(0);
								from.add(0);
								from.add(0);
								bottom.add("from", from);
								JsonArray to = new JsonArray();
								to.add(16);
								to.add(8);
								to.add(16);
								bottom.add("to", to);
							}
							JsonObject faces = new JsonObject();
							faces.add(EnumFacing.DOWN.getName2(), textures.get("bottom").serialize("bottom", EnumFacing.DOWN));
							faces.add(EnumFacing.UP.getName2(), textures.get("top").serialize("top", null));
							faces.add(EnumFacing.NORTH.getName2(), textures.get("back").subTexture(0, 0.5f, 1, 1).serialize("back", EnumFacing.NORTH));
							faces.add(EnumFacing.SOUTH.getName2(), textures.get("front").subTexture(0, 0.5f, 1, 1).serialize("front", EnumFacing.SOUTH));
							faces.add(EnumFacing.WEST.getName2(), textures.get("side-left").subTexture(0, 0.5f, 1, 1).serialize("side-left", EnumFacing.WEST));
							faces.add(EnumFacing.EAST.getName2(), textures.get("side-right").subTexture(0, 0.5f, 1, 1).serialize("side-right", EnumFacing.EAST));
							bottom.add("faces", faces);
							elements.add(bottom);
						}
						//構造は
						//北
						//**
						//..
						//南
						{
							JsonObject top = new JsonObject();
							top.add("stair_part", MyJsonUtil.serializeEnum(StairPart.TOP_STRAIGHT));
							{
								JsonArray from = new JsonArray();
								from.add(0);
								from.add(8);
								from.add(0);
								top.add("from", from);
								JsonArray to = new JsonArray();
								to.add(16);
								to.add(16);
								to.add(8);
								top.add("to", to);
							}
							{
								JsonObject faces = new JsonObject();
								//DOWNは省略
								faces.add(EnumFacing.UP.getName2(), textures.get("top").subTexture(0, 0, 1, 0.5f).serialize("top", EnumFacing.UP));
								faces.add(EnumFacing.NORTH.getName2(), textures.get("back").subTexture(0, 0, 1, 0.5f).serialize("back", EnumFacing.NORTH));
								faces.add(EnumFacing.SOUTH.getName2(), textures.get("front").subTexture(0, 0, 1, 0.5f).serialize("front", null));
								faces.add(EnumFacing.WEST.getName2(), textures.get("side-left").subTexture(0, 0, 0.5f, 0.5f).serialize("side-left", EnumFacing.WEST));
								faces.add(EnumFacing.EAST.getName2(), textures.get("side-right").subTexture(0.5f, 0, 1, 0.5f).serialize("side-right", EnumFacing.EAST));
								top.add("faces", faces);
							}
							elements.add(top);
						}
					}
					case INNER -> {
						{
							JsonObject bottom = new JsonObject();
							bottom.add("stair_part", MyJsonUtil.serializeEnum(StairPart.BOTTOM));
							{
								JsonArray from = new JsonArray();
								from.add(0);
								from.add(0);
								from.add(0);
								bottom.add("from", from);
								JsonArray to = new JsonArray();
								to.add(16);
								to.add(8);
								to.add(16);
								bottom.add("to", to);
							}
							JsonObject faces = new JsonObject();
							faces.add(EnumFacing.DOWN.getName2(), textures.get("bottom").serialize("bottom", EnumFacing.DOWN));
							faces.add(EnumFacing.UP.getName2(), textures.get("top").serialize("top", null));
							faces.add(EnumFacing.NORTH.getName2(), textures.get("back").subTexture(0, 0.5f, 1, 1).serialize("back", EnumFacing.NORTH));
							faces.add(EnumFacing.SOUTH.getName2(), textures.get("side-right").subTexture(0, 0.5f, 1, 1).serialize("side-right", EnumFacing.SOUTH));
							faces.add(EnumFacing.WEST.getName2(), textures.get("side-left").subTexture(0, 0.5f, 1, 1).serialize("side-left", EnumFacing.WEST));
							faces.add(EnumFacing.EAST.getName2(), textures.get("back").subTexture(0, 0.5f, 1, 1).serialize("back", EnumFacing.EAST));
							bottom.add("faces", faces);
							elements.add(bottom);
						}
						//構造は
						//北
						//**
						//.+
						//南
						{
							JsonObject topStraight = new JsonObject();
							topStraight.add("stair_part", MyJsonUtil.serializeEnum(StairPart.TOP_STRAIGHT));
							{
								JsonArray from = new JsonArray();
								from.add(0);
								from.add(8);
								from.add(0);
								topStraight.add("from", from);
								JsonArray to = new JsonArray();
								to.add(16);
								to.add(16);
								to.add(8);
								topStraight.add("to", to);
							}
							{
								JsonObject faces = new JsonObject();
								//DOWNは省略
								faces.add(EnumFacing.UP.getName2(), textures.get("top").subTexture(0, 0, 1, 0.5f).serialize("top", EnumFacing.UP));
								faces.add(EnumFacing.NORTH.getName2(), textures.get("back").subTexture(0, 0, 1, 0.5f).serialize("back", EnumFacing.NORTH));
								faces.add(EnumFacing.SOUTH.getName2(), textures.get("front").subTexture(0, 0, 1, 0.5f).serialize("front", null));
								faces.add(EnumFacing.WEST.getName2(), textures.get("side-left").subTexture(0, 0, 0.5f, 0.5f).serialize("side-left", EnumFacing.WEST));
								faces.add(EnumFacing.EAST.getName2(), textures.get("back").subTexture(0.5f, 0, 1, 0.5f).serialize("back", EnumFacing.EAST));
								topStraight.add("faces", faces);
							}
							elements.add(topStraight);
						}
						{
							JsonObject topCube = new JsonObject();
							topCube.add("stair_part", MyJsonUtil.serializeEnum(StairPart.TOP_INNER_CUBE));
							{
								JsonArray from = new JsonArray();
								from.add(8);
								from.add(8);
								from.add(8);
								topCube.add("from", from);
								JsonArray to = new JsonArray();
								to.add(16);
								to.add(16);
								to.add(16);
								topCube.add("to", to);
							}
							{
								JsonObject faces = new JsonObject();
								//DOWNは省略
								faces.add(EnumFacing.UP.getName2(), textures.get("top").subTexture(0.5f, 0.5f, 1, 1).serialize("top", EnumFacing.UP));
								//NORTHは省略
								faces.add(EnumFacing.SOUTH.getName2(), textures.get("side-right").subTexture(0.5f, 0, 1, 0.5f).serialize("side-right", EnumFacing.SOUTH));
								faces.add(EnumFacing.WEST.getName2(), textures.get("front").subTexture(0.5f, 0, 1, 0.5f).serialize("front", EnumFacing.WEST));
								faces.add(EnumFacing.EAST.getName2(), textures.get("back").subTexture(0, 0, 0.5f, 0.5f).serialize("back", null));
								topCube.add("faces", faces);
							}
							elements.add(topCube);
						}
					}
					case OUTER -> {
						{
							JsonObject bottom = new JsonObject();
							bottom.add("stair_part", MyJsonUtil.serializeEnum(StairPart.BOTTOM));
							{
								JsonArray from = new JsonArray();
								from.add(0);
								from.add(0);
								from.add(0);
								bottom.add("from", from);
								JsonArray to = new JsonArray();
								to.add(16);
								to.add(8);
								to.add(16);
								bottom.add("to", to);
							}
							JsonObject faces = new JsonObject();
							faces.add(EnumFacing.DOWN.getName2(), textures.get("bottom").serialize("bottom", EnumFacing.DOWN));
							faces.add(EnumFacing.UP.getName2(), textures.get("top").serialize("top", null));
							faces.add(EnumFacing.NORTH.getName2(), textures.get("side-left").subTexture(0, 0.5f, 1, 1).serialize("side-left", EnumFacing.NORTH));
							faces.add(EnumFacing.SOUTH.getName2(), textures.get("front").subTexture(0, 0.5f, 1, 1).serialize("front", EnumFacing.SOUTH));
							faces.add(EnumFacing.WEST.getName2(), textures.get("front").subTexture(0, 0.5f, 1, 1).serialize("front", EnumFacing.WEST));
							faces.add(EnumFacing.EAST.getName2(), textures.get("side-right").subTexture(0, 0.5f, 1, 1).serialize("side-right", EnumFacing.EAST));
							bottom.add("faces", faces);
							elements.add(bottom);
						}
						//構造は
						//北
						//..
						//.+
						//南
						{
							JsonObject top = new JsonObject();
							top.add("stair_part", MyJsonUtil.serializeEnum(StairPart.TOP_OUTER));
							{
								JsonArray from = new JsonArray();
								from.add(8);
								from.add(8);
								from.add(0);
								top.add("from", from);
								JsonArray to = new JsonArray();
								to.add(16);
								to.add(16);
								to.add(8);
								top.add("to", to);
							}
							{
								JsonObject faces = new JsonObject();
								//DOWNは省略
								faces.add(EnumFacing.UP.getName2(), textures.get("top").subTexture(0.5f, 0, 1, 0.5f).serialize("top", EnumFacing.UP));
								faces.add(EnumFacing.NORTH.getName2(), textures.get("side-left").subTexture(0, 0, 0.5f, 0.5f).serialize("side-left", null));
								faces.add(EnumFacing.SOUTH.getName2(), textures.get("front").subTexture(0.5f, 0, 1, 0.5f).serialize("front", EnumFacing.SOUTH));
								faces.add(EnumFacing.WEST.getName2(), textures.get("front").subTexture(0, 0, 0.5f, 0.5f).serialize("front", null));
								faces.add(EnumFacing.EAST.getName2(), textures.get("side-right").subTexture(0.5f, 0, 1, 0.5f).serialize("side-right", EnumFacing.EAST));
								top.add("faces", faces);
							}
							elements.add(top);
						}
					}
				}
			}
			jsonObject.add("elements", elements);
		}

	}

	private static ModelType toModelType(StairModelType stairModelType) {
		switch (stairModelType) {
			case STRAIGHT -> {
				return ModelType.STAIR_STRAIGHT;
			}
			case INNER -> {
				return ModelType.STAIR_INNER;
			}
			case OUTER -> {
				return ModelType.STAIR_OUTER;
			}
			default -> throw new AssertionError();
		}
	}

	public enum StairModelType {
		STRAIGHT,
		INNER,
		OUTER,
	}

	private enum StairPart {
		BOTTOM,
		TOP_STRAIGHT,
		TOP_INNER_CUBE,
		TOP_OUTER,
	}
}
