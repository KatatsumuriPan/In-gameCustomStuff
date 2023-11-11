package kpan.ig_custom_stuff.util;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

import java.util.Map.Entry;

public abstract class PropertyUtil {

	public static String getPropertyString(IBlockState state) {
		StringBuilder stringbuilder = new StringBuilder();
		for (Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
			if (stringbuilder.length() != 0) {
				stringbuilder.append(",");
			}

			IProperty<?> iproperty = entry.getKey();
			stringbuilder.append(iproperty.getName());
			stringbuilder.append("=");
			stringbuilder.append(getPropertyName(iproperty, entry.getValue()));
		}
		if (stringbuilder.length() == 0)
			stringbuilder.append("normal");

		return stringbuilder.toString();
	}
	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value) { return property.getName((T) value); }

}
