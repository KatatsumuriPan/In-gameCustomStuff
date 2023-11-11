package kpan.ig_custom_stuff.block;

import io.netty.buffer.ByteBuf;

public class TextureUV {

	public static final TextureUV DEFAULT = new TextureUV(0, 0, 16, 16);

	public static TextureUV fromByteBuf(ByteBuf buf) {
		float minU = buf.readFloat();
		float minV = buf.readFloat();
		float maxU = buf.readFloat();
		float maxV = buf.readFloat();
		return new TextureUV(minU, minV, maxU, maxV);
	}

	public final float minU;
	public final float minV;
	public final float maxU;
	public final float maxV;

	public TextureUV(float minU, float minV, float maxU, float maxV) {
		this.minU = minU;
		this.minV = minV;
		this.maxU = maxU;
		this.maxV = maxV;
	}

	public TextureUV withMinU(float minU) {
		return new TextureUV(minU, minV, maxU, maxV);
	}

	public TextureUV withMinV(float minV) {
		return new TextureUV(minU, minV, maxU, maxV);
	}

	public TextureUV withMaxU(float maxU) {
		return new TextureUV(minU, minV, maxU, maxV);
	}

	public TextureUV withMaxV(float maxV) {
		return new TextureUV(minU, minV, maxU, maxV);
	}

	public void writeTo(ByteBuf buf) {
		buf.writeFloat(minU);
		buf.writeFloat(minV);
		buf.writeFloat(maxU);
		buf.writeFloat(maxV);
	}
}
