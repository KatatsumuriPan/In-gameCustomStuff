package kpan.ig_custom_stuff.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Objects;

public class BlockCollisionAABB {

	public static final BlockCollisionAABB FULL_BLOCK = new BlockCollisionAABB(0, 0, 0, 16, 16, 16);

	public static BlockCollisionAABB deserialize(JsonArray jsonArray) throws JsonParseException {
		if (jsonArray.size() != 6)
			throw new JsonParseException("Expected 6 values, found: " + jsonArray.size());
		float minX = jsonArray.get(0).getAsFloat();
		float minY = jsonArray.get(1).getAsFloat();
		float minZ = jsonArray.get(2).getAsFloat();
		float maxX = jsonArray.get(3).getAsFloat();
		float maxY = jsonArray.get(4).getAsFloat();
		float maxZ = jsonArray.get(5).getAsFloat();
		if (minX == 0 && minY == 0 && minZ == 0 && maxX == 16 && maxY == 16 && maxZ == 16)
			return FULL_BLOCK;
		return new BlockCollisionAABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public static BlockCollisionAABB fromByteBuf(ByteBuf buf) {
		float minX = buf.readFloat();
		float minY = buf.readFloat();
		float minZ = buf.readFloat();
		float maxX = buf.readFloat();
		float maxY = buf.readFloat();
		float maxZ = buf.readFloat();
		if (minX == 0 && minY == 0 && minZ == 0 && maxX == 16 && maxY == 16 && maxZ == 16)
			return FULL_BLOCK;
		return new BlockCollisionAABB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public final float minX;
	public final float minY;
	public final float minZ;
	public final float maxX;
	public final float maxY;
	public final float maxZ;

	public BlockCollisionAABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		if (maxX < minX)
			throw new IllegalArgumentException(String.format("maxX < minX(%s,%s)", maxX, minX));
	}

	public void writeTo(ByteBuf buf) {
		buf.writeFloat(minX);
		buf.writeFloat(minY);
		buf.writeFloat(minZ);
		buf.writeFloat(maxX);
		buf.writeFloat(maxY);
		buf.writeFloat(maxZ);
	}

	public JsonElement serialize() {
		JsonArray aabb = new JsonArray();
		aabb.add(minX);
		aabb.add(minY);
		aabb.add(minZ);
		aabb.add(maxX);
		aabb.add(maxY);
		aabb.add(maxZ);
		return aabb;
	}

	public AxisAlignedBB toAABB() {
		return new AxisAlignedBB(minX / 16, minY / 16, minZ / 16, maxX / 16, maxY / 16, maxZ / 16);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		BlockCollisionAABB that = (BlockCollisionAABB) o;
		return Float.compare(minX, that.minX) == 0 && Float.compare(minY, that.minY) == 0 && Float.compare(minZ, that.minZ) == 0 && Float.compare(maxX, that.maxX) == 0 && Float.compare(maxY, that.maxY) == 0 && Float.compare(maxZ, that.maxZ) == 0;
	}
	@Override
	public int hashCode() {
		return Objects.hash(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public String toString() {
		return "[" + minX +
				", " + minY +
				", " + minZ +
				"] ->[" + maxX +
				", " + maxY +
				", " + maxZ +
				']';
	}
}
