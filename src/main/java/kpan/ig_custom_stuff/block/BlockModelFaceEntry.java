package kpan.ig_custom_stuff.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Vector3f;

public class BlockModelFaceEntry {

	public static BlockModelFaceEntry deserialize(JsonObject jsonObject, Vector3f from, Vector3f to, EnumFacing face) {
		String textureTag = JsonUtils.getString(jsonObject, "texture");
		TextureUV uv;
		if (jsonObject.has("uv")) {
			JsonArray jsonarray = JsonUtils.getJsonArray(jsonObject, "uv");
			if (jsonarray.size() != 4)
				throw new JsonParseException("Expected 4 uv values, found: " + jsonarray.size());
			float minU = JsonUtils.getFloat(jsonarray.get(0), "uv[" + 0 + "]");
			float minV = JsonUtils.getFloat(jsonarray.get(1), "uv[" + 1 + "]");
			float maxU = JsonUtils.getFloat(jsonarray.get(2), "uv[" + 2 + "]");
			float maxV = JsonUtils.getFloat(jsonarray.get(3), "uv[" + 3 + "]");
			uv = new TextureUV(minU, minV, maxU, maxV);
		} else {
			float minU, minV, maxU, maxV;
			switch (face) {
				case DOWN -> {
					minU = from.x;
					minV = 16 - to.z;
					maxU = to.x;
					maxV = 16 - from.z;
				}
				case UP -> {
					minU = from.x;
					minV = from.z;
					maxU = to.x;
					maxV = to.z;
				}
				case NORTH -> {
					minU = 16 - to.x;
					minV = 16 - to.y;
					maxU = 16 - from.x;
					maxV = 16 - from.y;
				}
				case SOUTH -> {
					minU = from.x;
					minV = 16 - to.y;
					maxU = to.x;
					maxV = 16 - from.y;
				}
				case WEST -> {
					minU = from.z;
					minV = 16 - to.y;
					maxU = to.z;
					maxV = 16 - from.y;
				}
				case EAST -> {
					minU = 16 - to.z;
					minV = 16 - to.y;
					maxU = 16 - from.z;
					maxV = 16 - from.y;
				}
				default -> {
					//NORTH
					minU = 16 - to.x;
					minV = 16 - to.y;
					maxU = 16 - from.x;
					maxV = 16 - from.y;
				}
			}
			uv = new TextureUV(minU, minV, maxU, maxV);
		}
		int rotation = JsonUtils.getInt(jsonObject, "rotation", 0);
		if (rotation < 0 || rotation % 90 != 0 || rotation / 90 > 3)
			throw new JsonParseException("Invalid rotation " + rotation + " found, only 0/90/180/270 allowed");
		EnumFacing cullface = EnumFacing.byName(JsonUtils.getString(jsonObject, "cullface", ""));

		return new BlockModelFaceEntry(textureTag, uv, rotation, cullface);
	}

	public static BlockModelFaceEntry fromByteBuf(ByteBuf buf) {
		String textureTag = MyByteBufUtil.readString(buf);
		TextureUV uv = TextureUV.fromByteBuf(buf);
		int rotation = buf.readByte() * 90;
		@Nullable EnumFacing cullface = EnumFacing.byName(MyByteBufUtil.readString(buf));
		return new BlockModelFaceEntry(textureTag, uv, rotation, cullface);
	}

	public final String textureTag;
	public final TextureUV uv;
	public final int rotation;
	public final @Nullable EnumFacing cullface;

	public BlockModelFaceEntry(String textureTag, TextureUV uv, int rotation, @Nullable EnumFacing cullface) {
		this.textureTag = textureTag.startsWith("#") ? textureTag.substring(1) : textureTag;
		this.uv = uv;
		this.rotation = rotation;
		this.cullface = cullface;
	}

	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeString(buf, textureTag);
		uv.writeTo(buf);
		buf.writeByte(rotation / 90);
		MyByteBufUtil.writeString(buf, toName(cullface));
	}

	public JsonElement serialize() {
		JsonObject jsonobject = new JsonObject();
		{
			JsonArray uv = new JsonArray();
			uv.add(this.uv.minU);
			uv.add(this.uv.minV);
			uv.add(this.uv.maxU);
			uv.add(this.uv.maxV);
			jsonobject.add("uv", uv);
		}
		jsonobject.addProperty("texture", "#" + textureTag);
		if (cullface != null)
			jsonobject.addProperty("cullface", toName(cullface));
		if (rotation != 0)
			jsonobject.addProperty("rotation", rotation);
		return jsonobject;
	}

	private static String toName(@Nullable EnumFacing cullface) {
		if (cullface == null)
			return "";
		return cullface.getName2();
	}

}
