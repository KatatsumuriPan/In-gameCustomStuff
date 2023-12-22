package kpan.ig_custom_stuff.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.block.TextureUV;
import kpan.ig_custom_stuff.resource.JsonUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector3f;
import java.util.Map;
import java.util.Map.Entry;

public class BlockModelEntryNormal extends BlockModelEntryBase {

	protected static BlockModelEntryNormal deserialize(JsonObject jsonObject) {
		BlockModelEntryNormal blockModelEntryNormal = new BlockModelEntryNormal();
		String parent = jsonObject.getAsJsonPrimitive("parent").getAsString();
		switch (parent) {
			case "block/cube" -> {
				if (jsonObject.has("elements")) {
					throw new JsonParseException("block/cube with elements is not supported yet.");
				} else {
					//texturesのみ
					JsonObject textures = JsonUtils.getJsonObject(jsonObject, "textures");
					for (EnumFacing facing : EnumFacing.values()) {
						blockModelEntryNormal.setTexture(facing.getName2(), new BlockModelTextureEntry(new ResourceLocation(JsonUtils.getString(textures, facing.getName2())), TextureUV.FULL, 0));
					}
					blockModelEntryNormal.setTexture("particle", new BlockModelTextureEntry(new ResourceLocation(JsonUtils.getString(textures, "particle")), TextureUV.FULL, 0));
				}
			}
			case "block/block" -> {
				JsonObject textures = JsonUtils.getJsonObject(jsonObject, "textures");

				JsonArray elements = JsonUtils.getJsonArray(jsonObject, "elements");
				if (elements.size() != 1)
					throw new JsonParseException("elements size is not 1, found:" + elements.size());
				JsonObject element = elements.get(0).getAsJsonObject();
				Vector3f from = JsonUtil.parsePosition(element, "from");
//					if (vector3f.x >= -16.0F && vector3f.y >= -16.0F && vector3f.z >= -16.0F && vector3f.x <= 32.0F && vector3f.y <= 32.0F && vector3f.z <= 32.0F)
				if (from.x != 0 || from.y != 0 || from.z != 0)
					throw new JsonParseException("from is not [0, 0, 0], found:" + from);
				Vector3f to = JsonUtil.parsePosition(element, "to");
				if (to.x != 16 || to.y != 16 || to.z != 16)
					throw new JsonParseException("to is not [16, 16, 16], found:" + to);
				deserializeFaces(element, from, to, blockModelEntryNormal, textures);
			}
			default -> throw new JsonParseException("invalid parent:" + parent);
		}
		return blockModelEntryNormal;
	}

	protected static BlockModelEntryNormal fromBuf(ByteBuf buf) {
		return new BlockModelEntryNormal();
	}


	private BlockModelEntryNormal() {
		super(ModelType.NORMAL);
	}
	public BlockModelEntryNormal(BlockModelTextureEntry[] faces, BlockModelTextureEntry particle) {
		this();
		for (EnumFacing facing : EnumFacing.values()) {
			setTexture(facing.getName2(), faces[facing.getIndex()]);
		}
		setTexture("particle", particle);
	}
	public BlockModelEntryNormal(Map<String, BlockModelTextureEntry> textures) {
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
				JsonObject element = new JsonObject();
				{
					JsonArray from = new JsonArray();
					from.add(0);
					from.add(0);
					from.add(0);
					element.add("from", from);
					JsonArray to = new JsonArray();
					to.add(16);
					to.add(16);
					to.add(16);
					element.add("to", to);
				}
				serializeFaces(element);
				elements.add(element);
			}
			jsonObject.add("elements", elements);
		}
	}
}
