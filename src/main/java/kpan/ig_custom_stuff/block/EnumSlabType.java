package kpan.ig_custom_stuff.block;

import net.minecraft.util.IStringSerializable;

public enum EnumSlabType implements IStringSerializable {
	TOP("top"),
	BOTTOM("bottom"),
	DOUBLE("double"),
	;

	private final String name;

	EnumSlabType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public String getName() {
		return name;
	}
}
