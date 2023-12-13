package kpan.ig_custom_stuff.resource.ids;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

public class BlockTextureId implements Comparable<BlockTextureId>, ITextureId {

	public static BlockTextureId formByteBuf(ByteBuf buf) {
		return new BlockTextureId(new ResourceLocation(MyByteBufUtil.readString(buf)));
	}

	public final String namespace;
	public final String path;
	public BlockTextureId(ResourceLocation blockTextureId) {
		this(blockTextureId.getNamespace(), blockTextureId.getPath().substring("blocks/".length()));
	}
	public BlockTextureId(String namespace, String path) {
		this.namespace = namespace.toLowerCase(Locale.ROOT);
		this.path = path.toLowerCase(Locale.ROOT);
	}

	@Override
	public ResourceLocation toResourceLocation() {
		return new ResourceLocation(namespace, "blocks/" + path);
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
		} else if (!(other instanceof BlockTextureId o)) {
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
	public int compareTo(BlockTextureId other) {
		int i = namespace.compareTo(other.namespace);

		if (i == 0) {
			i = path.compareTo(other.path);
		}

		return i;
	}

}
