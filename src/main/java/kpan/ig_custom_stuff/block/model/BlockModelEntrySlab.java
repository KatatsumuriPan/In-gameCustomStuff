package kpan.ig_custom_stuff.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.block.EnumSlabType;
import kpan.ig_custom_stuff.resource.JsonUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;

import javax.vecmath.Vector3f;
import java.util.Map.Entry;

public class BlockModelEntrySlab extends BlockModelEntryBase {

	protected static BlockModelEntrySlab deserialize(JsonObject jsonObject, EnumSlabType slabType) {
		BlockModelEntrySlab blockModelEntryNormal = new BlockModelEntrySlab(slabType);
		String parent = jsonObject.getAsJsonPrimitive("parent").getAsString();
		if (!parent.equals("block/block"))
			throw new JsonParseException("invalid parent:" + parent);
		JsonObject textures = JsonUtils.getJsonObject(jsonObject, "textures");

		JsonArray elements = JsonUtils.getJsonArray(jsonObject, "elements");
		if (elements.size() != 1)
			throw new JsonParseException("elements size is not 1, found:" + elements.size());
		JsonObject element = elements.get(0).getAsJsonObject();
		switch (slabType) {
			case TOP -> {
				Vector3f from = JsonUtil.parsePosition(element, "from");
				if (from.x != 0 || from.y != 8 || from.z != 0)
					throw new JsonParseException("from is not [0, 8, 0], found:" + from);
				Vector3f to = JsonUtil.parsePosition(element, "to");
				if (to.x != 16 || to.y != 16 || to.z != 16)
					throw new JsonParseException("to is not [16, 16, 16], found:" + to);
				deserializeFaces(element, from, to, blockModelEntryNormal, textures);
			}
			case BOTTOM -> {
				Vector3f from = JsonUtil.parsePosition(element, "from");
				if (from.x != 0 || from.y != 0 || from.z != 0)
					throw new JsonParseException("from is not [0, 0, 0], found:" + from);
				Vector3f to = JsonUtil.parsePosition(element, "to");
				if (to.x != 16 || to.y != 8 || to.z != 16)
					throw new JsonParseException("to is not [16, 8, 16], found:" + to);
				deserializeFaces(element, from, to, blockModelEntryNormal, textures);
			}
			case DOUBLE -> {
				Vector3f from = JsonUtil.parsePosition(element, "from");
				if (from.x != 0 || from.y != 0 || from.z != 0)
					throw new JsonParseException("from is not [0, 0, 0], found:" + from);
				Vector3f to = JsonUtil.parsePosition(element, "to");
				if (to.x != 16 || to.y != 16 || to.z != 16)
					throw new JsonParseException("to is not [16, 16, 16], found:" + to);
				deserializeFaces(element, from, to, blockModelEntryNormal, textures);
			}
		}
		return blockModelEntryNormal;
	}

	protected static BlockModelEntrySlab fromBuf(ByteBuf buf, EnumSlabType slabType) {
		return new BlockModelEntrySlab(slabType);
	}

	private final EnumSlabType slabType;

	private BlockModelEntrySlab(EnumSlabType slabType) {
		super(toModelType(slabType));
		this.slabType = slabType;
	}
	public BlockModelEntrySlab(EnumSlabType slabType, BlockModelTextureEntry[] faces, BlockModelTextureEntry particle) {
		this(slabType);
		for (EnumFacing facing : EnumFacing.values()) {
			setTexture(facing.getName2(), faces[facing.getIndex()]);
		}
		setTexture("particle", particle);
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
				switch (slabType) {
					case TOP -> {
						{
							JsonArray from = new JsonArray();
							from.add(0);
							from.add(8);
							from.add(0);
							element.add("from", from);
							JsonArray to = new JsonArray();
							to.add(16);
							to.add(16);
							to.add(16);
							element.add("to", to);
						}
						serializeFaces(element);
					}
					case BOTTOM -> {
						{
							JsonArray from = new JsonArray();
							from.add(0);
							from.add(0);
							from.add(0);
							element.add("from", from);
							JsonArray to = new JsonArray();
							to.add(16);
							to.add(8);
							to.add(16);
							element.add("to", to);
						}
						serializeFaces(element);
					}
					case DOUBLE -> {
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
					}
				}
				elements.add(element);
			}
			jsonObject.add("elements", elements);
		}
	}

	private static ModelType toModelType(EnumSlabType slabType) {
		switch (slabType) {
			case TOP -> {
				return ModelType.SLAB_TOP;
			}
			case BOTTOM -> {
				return ModelType.SLAB_BOTTOM;
			}
			case DOUBLE -> {
				return ModelType.SLAB_DOUBLE;
			}
			default -> throw new AssertionError();
		}
	}
}
