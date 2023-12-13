package kpan.ig_custom_stuff.resource.ids;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

public class BlockStateId implements Comparable<BlockStateId> {

	public static BlockStateId formByteBuf(ByteBuf buf) {
		return new BlockStateId(new ResourceLocation(MyByteBufUtil.readString(buf)));
	}

	public final String namespace;
	public final String name;
	public BlockStateId(ResourceLocation blockStateId) {
		this(blockStateId.getNamespace(), blockStateId.getPath());
	}
	public BlockStateId(String namespace, String name) {
		this.namespace = namespace.toLowerCase(Locale.ROOT);
		this.name = name.toLowerCase(Locale.ROOT);
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
		} else if (!(other instanceof BlockStateId o)) {
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
	public int compareTo(BlockStateId other) {
		int i = namespace.compareTo(other.namespace);

		if (i == 0) {
			i = name.compareTo(other.name);
		}

		return i;
	}

}
