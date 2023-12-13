package kpan.ig_custom_stuff.resource.ids;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

public class BlockModelId implements Comparable<BlockModelId> {

	public static BlockModelId formByteBuf(ByteBuf buf) {
		return new BlockModelId(new ResourceLocation(MyByteBufUtil.readString(buf)));
	}

	public final String namespace;
	public final String path;
	public BlockModelId(ResourceLocation blockModelId) {
		this(blockModelId.getNamespace(), blockModelId.getPath().substring("block/".length()));
	}
	public BlockModelId(String namespace, String path) {
		this.namespace = namespace.toLowerCase(Locale.ROOT);
		this.path = path.toLowerCase(Locale.ROOT);
	}
	public static boolean isBlockModelId(ResourceLocation modelId) {
		return modelId.getPath().startsWith("block/");
	}

	public ResourceLocation toResourceLocation() {
		return new ResourceLocation(namespace, "block/" + path);
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
		} else if (!(other instanceof BlockModelId o)) {
			return false;
		} else {
			return namespace.equals(o.namespace) && path.equals(o.path);
		}
	}

	@Override
	public int hashCode() {
		return 31 * namespace.hashCode() + path.hashCode();
	}

	@Override
	public int compareTo(BlockModelId other) {
		int i = namespace.compareTo(other.namespace);

		if (i == 0) {
			i = path.compareTo(other.path);
		}

		return i;
	}

}
