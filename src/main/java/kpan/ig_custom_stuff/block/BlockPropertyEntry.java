package kpan.ig_custom_stuff.block;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.util.MyByteBufUtil;
import kpan.ig_custom_stuff.util.MyJsonUtil;
import kpan.ig_custom_stuff.util.MyReflectionHelper;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class BlockPropertyEntry {
	public static final float DEFAULT_HARDNESS = 0.5f;
	public static final float DEFAULT_RESISTANCE = DEFAULT_HARDNESS * 5;
	public static final SoundType DEFAULT_SOUND = SoundType.GROUND;
	public static final CreativeTabs DEFAULT_CREATIVE_TAB = CreativeTabs.BUILDING_BLOCKS;
	public static final String NONE_CREATIVE_TAB = "NONE";
	public static final Material DEFAULT_MATERIAL = Material.GROUND;
	public static final boolean DEFAULT_IS_FULL_OPAQUE_CUBE = true;
	public static final FaceCullingType DEFAULT_GLASS_LIKE_CULLING = FaceCullingType.NORMAL;

	public static final BiMap<String, SoundType> VANILLA_SOUND_TYPES;
	public static final List<SoundType> VANILLA_SOUND_TYPE_LIST = new ArrayList<>();
	public static final BiMap<String, CreativeTabs> ALL_CREATIVE_TABS;
	public static final List<CreativeTabs> ALL_CREATIVE_TAB_LIST = new ArrayList<>();
	public static final BiMap<String, Material> VANILLA_MATERIALS;
	public static final List<Material> VANILLA_MATERIAL_LIST = new ArrayList<>();

	static {
		VANILLA_SOUND_TYPES = HashBiMap.create();
		VANILLA_SOUND_TYPES.put("anvil", SoundType.ANVIL);
		VANILLA_SOUND_TYPES.put("cloth", SoundType.CLOTH);
		VANILLA_SOUND_TYPES.put("glass", SoundType.GLASS);
		VANILLA_SOUND_TYPES.put("ground", SoundType.GROUND);
		VANILLA_SOUND_TYPES.put("ladder", SoundType.LADDER);
		VANILLA_SOUND_TYPES.put("metal", SoundType.METAL);
		VANILLA_SOUND_TYPES.put("plant", SoundType.PLANT);
		VANILLA_SOUND_TYPES.put("sand", SoundType.SAND);
		VANILLA_SOUND_TYPES.put("slime", SoundType.SLIME);
		VANILLA_SOUND_TYPES.put("snow", SoundType.SNOW);
		VANILLA_SOUND_TYPES.put("stone", SoundType.STONE);
		VANILLA_SOUND_TYPES.put("wood", SoundType.WOOD);
		VANILLA_SOUND_TYPE_LIST.addAll(VANILLA_SOUND_TYPES.entrySet().stream().sorted(Entry.comparingByKey()).map(Entry::getValue).collect(Collectors.toList()));
		ALL_CREATIVE_TABS = HashBiMap.create();
		ALL_CREATIVE_TAB_LIST.add(null);
		for (CreativeTabs creativeTab : CreativeTabs.CREATIVE_TAB_ARRAY) {
			if (creativeTab == CreativeTabs.SEARCH || creativeTab == CreativeTabs.INVENTORY || creativeTab == CreativeTabs.HOTBAR)
				continue;
			ALL_CREATIVE_TABS.put(MyReflectionHelper.getObfPrivateValue(CreativeTabs.class, creativeTab, "tabLabel", "field_78034_o"), creativeTab);
			ALL_CREATIVE_TAB_LIST.add(creativeTab);
		}
		VANILLA_MATERIALS = HashBiMap.create();
//		VANILLA_MATERIALS.put("air", Material.AIR);
		VANILLA_MATERIALS.put("grass", Material.GRASS);
		VANILLA_MATERIALS.put("ground", Material.GROUND);
		VANILLA_MATERIALS.put("wood", Material.WOOD);
		VANILLA_MATERIALS.put("rock", Material.ROCK);
		VANILLA_MATERIALS.put("iron", Material.IRON);
//		MATERIALS.put("ANVIL", Material.ANVIL);
//		VANILLA_MATERIALS.put("WATER", Material.WATER);
//		VANILLA_MATERIALS.put("LAVA", Material.LAVA);
		VANILLA_MATERIALS.put("leaves", Material.LEAVES);
		VANILLA_MATERIALS.put("plants", Material.PLANTS);
//		VANILLA_MATERIALS.put("VINE", Material.VINE);
//		VANILLA_MATERIALS.put("SPONGE", Material.SPONGE);
		VANILLA_MATERIALS.put("cloth", Material.CLOTH);
//		VANILLA_MATERIALS.put("FIRE", Material.FIRE);
		VANILLA_MATERIALS.put("sand", Material.SAND);
		VANILLA_MATERIALS.put("circuits", Material.CIRCUITS);
		VANILLA_MATERIALS.put("carpet", Material.CARPET);
		VANILLA_MATERIALS.put("glass", Material.GLASS);
//		VANILLA_MATERIALS.put("REDSTONE_LIGHT", Material.REDSTONE_LIGHT);
		VANILLA_MATERIALS.put("tnt", Material.TNT);
		VANILLA_MATERIALS.put("coral", Material.CORAL);
		VANILLA_MATERIALS.put("ice", Material.ICE);
		VANILLA_MATERIALS.put("packed_ice", Material.PACKED_ICE);
		VANILLA_MATERIALS.put("snow", Material.SNOW);
		VANILLA_MATERIALS.put("crafted_snow", Material.CRAFTED_SNOW);
		VANILLA_MATERIALS.put("cactus", Material.CACTUS);
		VANILLA_MATERIALS.put("clay", Material.CLAY);
//		VANILLA_MATERIALS.put("GOURD", Material.GOURD);
//		VANILLA_MATERIALS.put("DRAGON_EGG", Material.DRAGON_EGG);
//		VANILLA_MATERIALS.put("PORTAL", Material.PORTAL);
//		VANILLA_MATERIALS.put("CAKE", Material.CAKE);
//		VANILLA_MATERIALS.put("WEB", Material.WEB);
//		VANILLA_MATERIALS.put("PISTON", Material.PISTON);
//		VANILLA_MATERIALS.put("BARRIER", Material.BARRIER);
//		VANILLA_MATERIALS.put("STRUCTURE_VOID", Material.STRUCTURE_VOID);
		VANILLA_MATERIAL_LIST.addAll(VANILLA_MATERIALS.entrySet().stream().sorted(Entry.comparingByKey()).map(Entry::getValue).collect(Collectors.toList()));

	}


	public static BlockPropertyEntry fromByteBuf(ByteBuf buf) {
		float hardness = buf.readFloat();
		float resistance = buf.readFloat();
		SoundType soundType = VANILLA_SOUND_TYPES.get(MyByteBufUtil.readString(buf));
		CreativeTabs creativeTab = toCreativeTab(MyByteBufUtil.readString(buf));
		Material material = VANILLA_MATERIALS.get(MyByteBufUtil.readString(buf));
		boolean isFullOpaqueCube = buf.readBoolean();
		FaceCullingType faceCullingType = MyByteBufUtil.readEnum(buf, FaceCullingType.class);
		return new BlockPropertyEntry(hardness, resistance, soundType, creativeTab, material, isFullOpaqueCube, faceCullingType);
	}

	public static BlockPropertyEntry defaultOption() { return new BlockPropertyEntry(); }
	public static BlockPropertyEntry forRemovedBlock() { return new BlockPropertyEntry(0, 0, SoundType.GLASS, null, DEFAULT_MATERIAL, false, DEFAULT_GLASS_LIKE_CULLING); }

	public final float hardness;
	public final float resistance;
	public final SoundType soundType;
	public final @Nullable CreativeTabs creativeTab;
	public final Material material;
	public final boolean isFullOpaqueCube;
	public final FaceCullingType faceCullingType;

	private BlockPropertyEntry() {
		this(DEFAULT_HARDNESS, DEFAULT_RESISTANCE, DEFAULT_SOUND, DEFAULT_CREATIVE_TAB, DEFAULT_MATERIAL, DEFAULT_IS_FULL_OPAQUE_CUBE, DEFAULT_GLASS_LIKE_CULLING);
	}

	public BlockPropertyEntry(float hardness, float resistance, SoundType soundType, @Nullable CreativeTabs creativeTab, Material material, boolean isFullOpaqueCube, FaceCullingType faceCullingType) {
		this.hardness = hardness;
		this.resistance = resistance;
		this.soundType = soundType;
		this.creativeTab = creativeTab;
		this.material = material;
		this.isFullOpaqueCube = isFullOpaqueCube;
		this.faceCullingType = faceCullingType;
	}

	public void writeTo(ByteBuf buf) {
		buf.writeFloat(hardness);
		buf.writeFloat(resistance);
		MyByteBufUtil.writeString(buf, VANILLA_SOUND_TYPES.inverse().get(soundType));
		MyByteBufUtil.writeString(buf, toCreativeTabLabel(creativeTab));
		MyByteBufUtil.writeString(buf, VANILLA_MATERIALS.inverse().get(material));
		buf.writeBoolean(isFullOpaqueCube);
		MyByteBufUtil.writeEnum(buf, faceCullingType);
	}

	public static BlockPropertyEntry deserialize(@Nullable JsonObject jsonObject, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (jsonObject == null)
			return defaultOption();
		float hardness = JsonUtils.getFloat(jsonObject, "hardness", DEFAULT_HARDNESS);
		float resistance = JsonUtils.getFloat(jsonObject, "resistance", DEFAULT_RESISTANCE);
		SoundType soundType = VANILLA_SOUND_TYPES.getOrDefault(JsonUtils.getString(jsonObject, "soundType", ""), DEFAULT_SOUND);
		CreativeTabs creativeTab = toCreativeTab(JsonUtils.getString(jsonObject, "creativeTab", ""));
		Material material = VANILLA_MATERIALS.getOrDefault(JsonUtils.getString(jsonObject, "material", ""), DEFAULT_MATERIAL);
		boolean isFullOpaqueCube = JsonUtils.getBoolean(jsonObject, "isFullOpaqueCube", DEFAULT_IS_FULL_OPAQUE_CUBE);
		FaceCullingType faceCullingType = MyJsonUtil.deserializeEnum(jsonObject, "faceCullingType", context, FaceCullingType.NORMAL, FaceCullingType.class);

		return new BlockPropertyEntry(hardness, resistance, soundType, creativeTab, material, isFullOpaqueCube, faceCullingType);
	}

	public JsonElement serialize(Type type, JsonSerializationContext context) {
		JsonObject basic_property = new JsonObject();
		basic_property.addProperty("hardness", hardness);
		basic_property.addProperty("resistance", resistance);
		basic_property.addProperty("soundType", VANILLA_SOUND_TYPES.inverse().get(soundType));
		basic_property.addProperty("creativeTab", toCreativeTabLabel(creativeTab));
		basic_property.addProperty("material", VANILLA_MATERIALS.inverse().get(material));
		basic_property.addProperty("isFullOpaqueCube", isFullOpaqueCube);
		basic_property.add("faceCullingType", context.serialize(faceCullingType));
		return basic_property;
	}


	public static @Nullable CreativeTabs toCreativeTab(String tabLabel) {
		if (tabLabel.equals(NONE_CREATIVE_TAB))
			return null;
		if (!ALL_CREATIVE_TABS.containsKey(tabLabel))
			return DEFAULT_CREATIVE_TAB;
		return ALL_CREATIVE_TABS.get(tabLabel);
	}
	public static String toCreativeTabLabel(@Nullable CreativeTabs creativeTab) {
		if (creativeTab == null)
			return "NONE";
		if (!ALL_CREATIVE_TABS.inverse().containsKey(creativeTab))
			throw new IllegalArgumentException("Unknown CreativeTabs:" + MyReflectionHelper.getObfPrivateValue(CreativeTabs.class, creativeTab, "tabLabel", "field_78034_o"));
		return ALL_CREATIVE_TABS.inverse().get(creativeTab);
	}

	public static String getTranslationKey(@Nullable CreativeTabs creativeTab) {
		if (creativeTab == null)
			return "registry_message.block.property.creative_tab.none.name";
		return creativeTab.getTranslationKey();//client only
	}

	public static String getTranslationKey(SoundType soundType) {
		return "registry_message.block.property.sound." + VANILLA_SOUND_TYPES.inverse().get(soundType) + ".name";
	}
	public static String getTranslationKey(Material material) {
		return "registry_message.block.property.material." + VANILLA_MATERIALS.inverse().get(material) + ".name";
	}
}
