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

public class BlockModelEntryCage extends BlockModelEntryBase {

	protected static BlockModelEntryCage deserialize(JsonObject jsonObject) {
		BlockModelEntryCage blockModelEntryCage = new BlockModelEntryCage();
		String parent = jsonObject.getAsJsonPrimitive("parent").getAsString();
		if (!parent.equals("block/block"))
			throw new JsonParseException("invalid parent:" + parent);
		JsonObject textures = JsonUtils.getJsonObject(jsonObject, "textures");
		blockModelEntryCage.setTexture("particle", new BlockModelTextureEntry(new ResourceLocation(JsonUtils.getString(textures, "particle")), TextureUV.FULL, 0));

		JsonArray elements = JsonUtils.getJsonArray(jsonObject, "elements");
		if (elements.size() != 6)
			throw new JsonParseException("elements size is not 6, found:" + elements.size());
		for (int i = 0; i < elements.size(); i++) {
			JsonObject element = elements.get(i).getAsJsonObject();
			EnumFacing facing = MyJsonUtil.deserializeEnum(element, "cage_face", EnumFacing.class);
			Vector3f from = JsonUtil.parsePosition(element, "from");
			Vector3f to = JsonUtil.parsePosition(element, "to");
//						if (from.x != 0 || from.y != 0 || from.z != 0)
//							throw new JsonParseException("from is not [0, 0, 0], found:" + from);
//						if (to.x != 16 || to.y != 16 || to.z != 16)
//							throw new JsonParseException("to is not [16, 16, 16], found:" + to);
			JsonObject faces = JsonUtils.getJsonObject(element, "faces");
			BlockModelFaceJson faceJson = BlockModelFaceJson.deserialize(JsonUtils.getJsonObject(faces, facing.getName2()), from, to, facing);
			blockModelEntryCage.setTexture(facing.getName2(), new BlockModelTextureEntry(new ResourceLocation(JsonUtils.getString(textures, faceJson.textureTag)), faceJson.uv, faceJson.rotation));
		}
		return blockModelEntryCage;
	}

	protected static BlockModelEntryCage fromBuf(ByteBuf buf) {
		return new BlockModelEntryCage();
	}


	private BlockModelEntryCage() {
		super(ModelType.CAGE);
	}
	public BlockModelEntryCage(BlockModelTextureEntry[] faces, BlockModelTextureEntry particle) {
		this();
		for (EnumFacing facing : EnumFacing.values()) {
			setTexture(facing.getName2(), faces[facing.getIndex()]);
		}
		setTexture("particle", particle);
	}
	public BlockModelEntryCage(Map<String, BlockModelTextureEntry> textures) {
		this();
		this.textures.putAll(textures);
	}

	@Override
	protected void writeToInternal(ByteBuf buf) {
		//何もしない
	}
	@Override
	protected void serializeInternal(JsonObject jsonObject) {
		jsonObject.addProperty("parent", "block/block");
//		{
//			JsonObject display = new JsonObject();
//			display.add("gui", createDisplay(30, 45, 0, 0, 0, 0, 0.625));
//			jsonObject.add("display", display);
//		}
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
				for (EnumFacing facing : EnumFacing.values()) {
					BlockModelTextureEntry face = textures.get(facing.getName2());
					JsonObject element = new JsonObject();
					element.add("cage_face", MyJsonUtil.serializeEnum(facing));
					{
						JsonArray from = new JsonArray();
						switch (facing) {
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
						switch (facing) {
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
						{
							faces.add(facing.getName2(), face.serialize(facing.getName2(), facing));
						}
						{
							TextureUV uv = face.uv;
							TextureUV opposite_uv;
							if ((face.rotation / 90 % 2 == 0) == (facing.getAxis() != Axis.Y))
								opposite_uv = new TextureUV(uv.maxU, uv.minV, uv.minU, uv.maxV);
							else
								opposite_uv = new TextureUV(uv.minU, uv.maxV, uv.maxU, uv.minV);
							BlockModelTextureEntry opposite = new BlockModelTextureEntry(face.textureId, opposite_uv, face.rotation);
							faces.add(facing.getOpposite().getName2(), opposite.serialize(facing.getName2(), null));
						}
						element.add("faces", faces);
					}
					elements.add(element);
				}
			}
			jsonObject.add("elements", elements);
		}
	}
}
