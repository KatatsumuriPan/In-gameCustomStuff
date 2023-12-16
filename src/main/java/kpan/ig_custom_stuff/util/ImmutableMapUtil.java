package kpan.ig_custom_stuff.util;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;

public class ImmutableMapUtil {

	public static <K, V> ImmutableMap<K, V> with(ImmutableMap<K, V> map, K key, V value) {
		HashMap<K, V> newMap = new HashMap<>();
		newMap.putAll(map);
		newMap.put(key, value);
		return ImmutableMap.copyOf(newMap);
	}
}
