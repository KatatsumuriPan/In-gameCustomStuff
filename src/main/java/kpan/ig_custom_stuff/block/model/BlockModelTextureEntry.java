package kpan.ig_custom_stuff.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.block.TextureUV;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class BlockModelTextureEntry {

	public static BlockModelTextureEntry fromByteBuf(ByteBuf buf) {
		ResourceLocation textureId = new ResourceLocation(MyByteBufUtil.readString(buf));
		TextureUV uv = TextureUV.fromByteBuf(buf);
		int rotation = buf.readByte() * 90;
		return new BlockModelTextureEntry(textureId, uv, rotation);
	}

	public final ResourceLocation textureId;
	public final TextureUV uv;
	public final int rotation;

	public BlockModelTextureEntry(ResourceLocation textureId, TextureUV uv, int rotation) {
		this.textureId = textureId;
		this.uv = uv;
		this.rotation = rotation;
	}

	//0から1の間
	public BlockModelTextureEntry subTexture(float minU, float minV, float maxU, float maxV) {
		if (rotation == 0)
			return new BlockModelTextureEntry(textureId, uv.subUV(minU, minV, maxU, maxV), rotation);
		else if (rotation == 90)
			return new BlockModelTextureEntry(textureId, uv.subUV(minV, 1 - maxU, maxV, 1 - minU), rotation);
		else if (rotation == 180)
			return new BlockModelTextureEntry(textureId, uv.subUV(1 - maxU, 1 - maxV, 1 - minU, 1 - minV), rotation);
		else
			return new BlockModelTextureEntry(textureId, uv.subUV(1 - maxV, minU, 1 - minV, maxU), rotation);
	}

	public BlockModelTextureEntry with(ResourceLocation textureId) {
		return new BlockModelTextureEntry(textureId, uv, rotation);
	}

	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeString(buf, textureId.toString());
		uv.writeTo(buf);
		buf.writeByte(rotation / 90);
	}

	public JsonElement serialize(String textureTag, @Nullable EnumFacing cullface) {
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
			jsonobject.addProperty("cullface", cullface.getName2());
		if (rotation != 0)
			jsonobject.addProperty("rotation", rotation);
		return jsonobject;
	}

}
