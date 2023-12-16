package kpan.ig_custom_stuff.resource.ids;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;

public class ItemModelId implements Comparable<ItemModelId> {

	public static ItemModelId formByteBuf(ByteBuf buf) {
		return new ItemModelId(new ResourceLocation(MyByteBufUtil.readString(buf)));
	}
	public static boolean isItemModelId(ResourceLocation modelId) {
		return modelId.getPath().startsWith("item/");
	}

	public final String namespace;
	public final String name;
	public ItemModelId(ResourceLocation itemModelId) {
		this(itemModelId.getNamespace(), itemModelId.getPath().substring("item/".length()));
	}
	public ItemModelId(String namespace, String name) {
		this.namespace = namespace.toLowerCase(Locale.ROOT);
		this.name = name.toLowerCase(Locale.ROOT);
	}

	public ItemId toItemId() {
		return new ItemId(namespace, name);
	}
	public ResourceLocation toItemModelName() {
		return new ResourceLocation(namespace, name);
	}

	public ResourceLocation toResourceLocation() {
		return new ResourceLocation(namespace, "item/" + name);
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
		} else if (!(other instanceof ItemModelId o)) {
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
	public int compareTo(ItemModelId other) {
		int i = namespace.compareTo(other.namespace);

		if (i == 0) {
			i = name.compareTo(other.name);
		}

		return i;
	}

}
