package kpan.ig_custom_stuff.resource.ids;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

public class BlockId implements Comparable<BlockId> {

	public static BlockId formByteBuf(ByteBuf buf) {
		return new BlockId(new ResourceLocation(MyByteBufUtil.readString(buf)));
	}

	public final String namespace;
	public final String name;
	public BlockId(ResourceLocation blockId) {
		this(blockId.getNamespace(), blockId.getPath());
	}
	public BlockId(String namespace, String name) {
		this.namespace = namespace.toLowerCase(Locale.ROOT);
		this.name = name.toLowerCase(Locale.ROOT);
	}

	public ItemId toItemId() {
		return new ItemId(namespace, name);
	}

	public ResourceLocation toResourceLocation() {
		return new ResourceLocation(namespace, name);
	}

	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeString(buf, toResourceLocation().toString());
	}

	@Override
	public String toString() {
		return toResourceLocation().toString();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else if (!(other instanceof BlockId o)) {
			return false;
		} else {
			return namespace.equals(o.namespace) && name.equals(o.name);
		}
	}

	@Override
	public int hashCode() {
		return 31 * namespace.hashCode() + name.hashCode();
	}

	@Override
	public int compareTo(BlockId other) {
		int i = namespace.compareTo(other.namespace);

		if (i == 0) {
			i = name.compareTo(other.name);
		}

		return i;
	}

	public BlockStateId toBlockStateId() {
		return new BlockStateId(namespace, name);
	}
	public ResourceLocation toBlockModelName() {
		return new ResourceLocation(namespace, name);
	}
}
