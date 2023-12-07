package kpan.ig_custom_stuff.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import org.jetbrains.annotations.Nullable;

public class BlockPropertyEntryBuilder {

	public float hardness;
	public float resistance;
	public SoundType soundType;
	public @Nullable CreativeTabs creativeTab;
	public Material material;
	public boolean isFullOpaqueCube;
	public FaceCullingType faceCullingType;


	public BlockPropertyEntryBuilder(BlockPropertyEntry propertyEntry) {
		hardness = propertyEntry.hardness;
		resistance = propertyEntry.resistance;
		soundType = propertyEntry.soundType;
		creativeTab = propertyEntry.creativeTab;
		material = propertyEntry.material;
		isFullOpaqueCube = propertyEntry.isFullOpaqueCube;
		faceCullingType = propertyEntry.faceCullingType;
	}
	public BlockPropertyEntry build() {
		return new BlockPropertyEntry(hardness, resistance, soundType, creativeTab, material, isFullOpaqueCube, faceCullingType);
	}

	public float getHardness() {
		return hardness;
	}
	public void setHardness(float hardness) {
		this.hardness = hardness;
	}
	public float getResistance() {
		return resistance;
	}
	public void setResistance(float resistance) {
		this.resistance = resistance;
	}
	public SoundType getSoundType() {
		return soundType;
	}
	public void setSoundType(SoundType soundType) {
		this.soundType = soundType;
	}
	public @Nullable CreativeTabs getCreativeTab() {
		return creativeTab;
	}
	public void setCreativeTab(@Nullable CreativeTabs creativeTab) {
		this.creativeTab = creativeTab;
	}
	public Material getMaterial() {
		return material;
	}
	public void setMaterial(Material material) {
		this.material = material;
	}
	public boolean isFullOpaqueCube() {
		return isFullOpaqueCube;
	}
	public void setIsFullOpaqueCube(boolean fullOpaqueCube) {
		isFullOpaqueCube = fullOpaqueCube;
	}
	public FaceCullingType getFaceCullingType() {
		return faceCullingType;
	}
	public void setFaceCullingType(FaceCullingType faceCullingType) {
		this.faceCullingType = faceCullingType;
	}
}
