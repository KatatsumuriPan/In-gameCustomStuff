package kpan.ig_custom_stuff.resource.ids;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

public class ItemId implements Comparable<ItemId> {

	public static ItemId formByteBuf(ByteBuf buf) {
		return new ItemId(new ResourceLocation(MyByteBufUtil.readString(buf)));
	}

	public final String namespace;
	public final String name;
	public ItemId(ResourceLocation blockId) {
		this(blockId.getNamespace(), blockId.getPath());
	}
	public ItemId(String namespace, String name) {
		this.namespace = namespace.toLowerCase(Locale.ROOT);
		this.name = name.toLowerCase(Locale.ROOT);
	}

	public BlockId toBlockId() {
		return new BlockId(namespace, name);
	}

	public ItemModelId toModelId() {
		return new ItemModelId(namespace, name);
	}
	public ResourceLocation toItemModelName() {
		return new ResourceLocation(namespace, name);
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
		} else if (!(other instanceof ItemId o)) {
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
	public int compareTo(ItemId other) {
		int i = namespace.compareTo(other.namespace);

		if (i == 0) {
			i = name.compareTo(other.name);
		}

		return i;
	}

}
