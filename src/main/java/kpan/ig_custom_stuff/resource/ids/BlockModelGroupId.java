package kpan.ig_custom_stuff.resource.ids;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.block.model.BlockModelEntryBase;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class BlockModelGroupId implements Comparable<BlockModelGroupId> {

	public static BlockModelGroupId formByteBuf(ByteBuf buf) {
		BlockModelGroupType blockModelGroupType = MyByteBufUtil.readEnum(buf, BlockModelGroupType.class);
		String namespace = MyByteBufUtil.readString(buf);
		String path = MyByteBufUtil.readString(buf);
		return new BlockModelGroupId(blockModelGroupType, namespace, path);
	}

	public final BlockModelGroupType blockModelGroupType;
	public final String namespace;
	public final String path;
	public BlockModelGroupId(BlockModelGroupType blockModelGroupType, BlockModelId blockModelId) {
		this(blockModelGroupType, blockModelId.namespace, blockModelId.path.contains(BlockModelEntryBase.VARIANT_MARKER) ? blockModelId.path.substring(0, blockModelId.path.indexOf(BlockModelEntryBase.VARIANT_MARKER)) : blockModelId.path);
	}
	public BlockModelGroupId(BlockModelGroupType blockModelGroupType, String namespace, String path) {
		this.blockModelGroupType = blockModelGroupType;
		this.namespace = namespace.toLowerCase(Locale.ROOT);
		this.path = path.toLowerCase(Locale.ROOT);
	}
	public void writeTo(ByteBuf buf) {
		MyByteBufUtil.writeEnum(buf, blockModelGroupType);
		MyByteBufUtil.writeString(buf, namespace);
		MyByteBufUtil.writeString(buf, path);
	}

	public ResourceLocation toResourceLocation() {
		return new ResourceLocation(namespace, path);
	}

	public BlockModelId getVariantId(String variant) {
		return new BlockModelId(namespace, path + BlockModelEntryBase.VARIANT_MARKER + variant);
	}

	@Override
	public String toString() {
		return blockModelGroupType + "(" + namespace + ":" + path + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		BlockModelGroupId that = (BlockModelGroupId) o;
		return blockModelGroupType == that.blockModelGroupType && Objects.equals(namespace, that.namespace) && Objects.equals(path, that.path);
	}
	@Override
	public int hashCode() {
		return Objects.hash(blockModelGroupType, namespace, path);
	}

	@Override
	public int compareTo(BlockModelGroupId that) {
		int blockStateTypeComparison = Integer.compare(blockModelGroupType.ordinal(), that.blockModelGroupType.ordinal());
		if (blockStateTypeComparison != 0)
			return blockStateTypeComparison;
		int namespaceComparison = namespace.compareTo(that.namespace);
		if (namespaceComparison != 0)
			return namespaceComparison;

		return path.compareTo(that.path);
	}

	public BlockModelId getRenderModelId() {
		switch (blockModelGroupType) {
			case NORMAL -> {
				return new BlockModelId(namespace, path);
			}
			case SLAB -> {
				return new BlockModelId(namespace, path + BlockModelEntryBase.VARIANT_MARKER + "bottom");
			}
			case STAIR -> {
				return new BlockModelId(namespace, path + BlockModelEntryBase.VARIANT_MARKER + "straight");
			}
			default -> throw new AssertionError();
		}
	}
	public Map<String, BlockModelId> getBlockModelIds() {
		switch (blockModelGroupType) {
			case NORMAL -> {
				return Collections.singletonMap("", new BlockModelId(namespace, path));
			}
			case SLAB -> {
				Map<String, BlockModelId> map = new HashMap<>();
				addVariantBlockModelId(map, "top");
				addVariantBlockModelId(map, "bottom");
				addVariantBlockModelId(map, "double");
				return map;
			}
			case STAIR -> {
				Map<String, BlockModelId> map = new HashMap<>();
				addVariantBlockModelId(map, "straight");
				addVariantBlockModelId(map, "inner");
				addVariantBlockModelId(map, "outer");
				return map;
			}
			default -> throw new AssertionError();
		}
	}

	private void addVariantBlockModelId(Map<String, BlockModelId> map, String variant) {
		map.put(variant, getVariantId(variant));
	}

	public enum BlockModelGroupType {
		NORMAL,
		SLAB,
		STAIR,
	}
}
