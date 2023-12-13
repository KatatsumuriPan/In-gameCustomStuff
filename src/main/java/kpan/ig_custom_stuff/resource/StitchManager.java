package kpan.ig_custom_stuff.resource;

import com.google.common.collect.Lists;
import kpan.ig_custom_stuff.ModMain;
import kpan.ig_custom_stuff.asm.acc.ACC_Stitcher;
import kpan.ig_custom_stuff.asm.acc.ACC_TextureMap;
import kpan.ig_custom_stuff.resource.ids.ITextureId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.Stitcher.Holder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

public class StitchManager {


	private static final Set<Holder> setStitchHolders = new HashSet<>();//自動でclear
	//TextureManagerから正規にロードされたものを除く
	private static final Map<ResourceLocation, Integer> loadedTextureHash = new HashMap<>();

	public static void onLoadSprites() {
		loadedTextureHash.clear();
	}

	public static void addLoadedTextureHash(ResourceLocation textureId) {
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		try (IResource resource = resourceManager.getResource(getActualLocation(textureId))) {
			if (!resource.getResourcePackName().equals(ModResourcePack.CLIENT_CACHE.getPackName()))
				return;
			loadedTextureHash.put(textureId, calcCRC32(resource));
		} catch (IOException ignored) {
			//見つからない場合は大抵別mod
		}
	}

	public static <T extends ITextureId> void loadTexturesDynamic(Iterable<T> textureIds) throws StitchFullSpaceException {
		TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		Stitcher stitcher = ((ACC_TextureMap) textureMap).get_stitcher();

		for (T textureId : textureIds) {
			ResourceLocation textureIdResourceLocation = textureId.toResourceLocation();
			String textureIdString = textureIdResourceLocation.toString();
			try (IResource resource = resourceManager.getResource(getActualLocation(textureIdResourceLocation))) {
				int value = calcCRC32(resource);
				if (loadedTextureHash.containsKey(textureIdResourceLocation) && loadedTextureHash.get(textureIdResourceLocation) == value) {
					if (DynamicResourceLoader.unregisteredTextureCache.containsKey(textureId)) {
						textureMap.mapRegisteredSprites.put(textureIdString, DynamicResourceLoader.unregisteredTextureCache.get(textureId));
						textureMap.mapUploadedSprites.put(textureIdString, textureMap.mapRegisteredSprites.get(textureIdString));
						updateAnimation(textureIdResourceLocation);
						DynamicResourceLoader.reloadTextureDependantsModel(textureId.toResourceLocation());
					} else {
						updateAnimation(textureIdResourceLocation);
					}
					continue;
				}
				DynamicResourceLoader.unregisteredTextureCache.remove(textureId);
				loadedTextureHash.put(textureIdResourceLocation, value);
				DynamicResourceLoader.reloadTextureDependantsModel(textureId.toResourceLocation());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			TextureAtlasSprite textureAtlasSprite = textureMap.registerSprite(textureIdResourceLocation);
			textureMap.mapRegisteredSprites.put(textureIdString, textureAtlasSprite);
			loadTexture(stitcher, resourceManager, textureAtlasSprite, 1 << textureMap.getMipmapLevels());
		}

		finishLoading(stitcher, textureMap);
		setStitchHolders.clear();
	}

	public static void updateAnimation(ResourceLocation textureId) throws IOException {
		TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		TextureAtlasSprite textureAtlasSprite = textureMap.mapRegisteredSprites.get(textureId.toString());
		textureMap.generateMipmaps(resourceManager, textureAtlasSprite);
		if (textureAtlasSprite.hasAnimationMetadata()) {
			textureMap.listAnimatedSprites.add(textureAtlasSprite);
		} else {
			textureMap.listAnimatedSprites.remove(textureAtlasSprite);
		}
	}


	private static void loadTexture(Stitcher stitcher, IResourceManager resourceManager, TextureAtlasSprite textureatlassprite, int sizeMin) {
		TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
		ResourceLocation resourcelocation = textureMap.getResourceLocation(textureatlassprite);
		IResource iresource = null;

		for (ResourceLocation dependency : textureatlassprite.getDependencies()) {
			if (!textureMap.mapRegisteredSprites.containsKey(dependency.toString())) {
				textureMap.registerSprite(dependency);
			}
			TextureAtlasSprite depSprite = textureMap.mapRegisteredSprites.get(dependency.toString());
			loadTexture(stitcher, resourceManager, depSprite, sizeMin);
		}
		try {
			if (textureatlassprite.hasCustomLoader(resourceManager, resourcelocation)) {
				if (textureatlassprite.load(resourceManager, resourcelocation, l -> textureMap.mapRegisteredSprites.get(l.toString()))) {
					return;
				}
			} else {
				PngSizeInfo pngsizeinfo = PngSizeInfo.makeFromResource(resourceManager.getResource(resourcelocation));
				iresource = resourceManager.getResource(resourcelocation);
				boolean flag = iresource.getMetadata("animation") != null;
				textureatlassprite.loadSprite(pngsizeinfo, flag);
			}
		} catch (RuntimeException runtimeexception) {
			net.minecraftforge.fml.client.FMLClientHandler.instance().trackBrokenTexture(resourcelocation, runtimeexception.getMessage());
			return;
		} catch (IOException ioexception) {
			net.minecraftforge.fml.client.FMLClientHandler.instance().trackMissingTexture(resourcelocation);
			return;
		} finally {
			IOUtils.closeQuietly(iresource);
		}

		int j = Math.min(textureatlassprite.getIconWidth(), textureatlassprite.getIconHeight());
		int j1 = Math.min(Integer.lowestOneBit(textureatlassprite.getIconWidth()), Integer.lowestOneBit(textureatlassprite.getIconHeight()));

		if (j1 < sizeMin) {
			// FORGE: do not lower the mipmap level, just log the problematic textures
			ModMain.LOGGER.warn("Texture {} with size {}x{} will have visual artifacts at mip level {}, it can only support level {}. Please report to the mod author that the texture should be some multiple of 16x16.", resourcelocation, Integer.valueOf(textureatlassprite.getIconWidth()), Integer.valueOf(textureatlassprite.getIconHeight()), Integer.valueOf(MathHelper.log2(sizeMin)), Integer.valueOf(MathHelper.log2(j1)));
		}

		if (textureMap.generateMipmaps(resourceManager, textureatlassprite))
			addSprite(stitcher, textureatlassprite, textureMap);
	}
	private static void addSprite(Stitcher stitcher, TextureAtlasSprite textureAtlas, TextureMap textureMap) {
		Stitcher.Holder holder = new Stitcher.Holder(textureAtlas, textureMap.getMipmapLevels());

		if (stitcher.maxTileDimension > 0) {
			holder.setNewDimension(stitcher.maxTileDimension);
		}

		setStitchHolders.add(holder);
	}


	private static void finishLoading(Stitcher stitcher, TextureMap textureMap) throws StitchFullSpaceException {
		doStitch(stitcher);

		ModMain.LOGGER.info("Using: {}x{} {}-atlas", stitcher.currentWidth, stitcher.currentHeight, textureMap.getBasePath());
		GlStateManager.bindTexture(textureMap.getGlTextureId());

		for (TextureAtlasSprite sprite : getStitchSlots(stitcher)) {
			{
				String s = sprite.getIconName();
				textureMap.mapUploadedSprites.put(s, sprite);

				try {
					TextureUtil.uploadTextureMipmap(sprite.getFrameTextureData(0), sprite.getIconWidth(), sprite.getIconHeight(), sprite.getOriginX(), sprite.getOriginY(), false, false);
				} catch (Throwable throwable) {
					CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Stitching texture atlas");
					CrashReportCategory crashreportcategory = crashreport.makeCategory("Texture being stitched together");
					crashreportcategory.addCrashSection("Atlas path", textureMap.getBasePath());
					crashreportcategory.addCrashSection("Sprite", sprite);
					throw new ReportedException(crashreport);
				}

				if (sprite.hasAnimationMetadata()) {
					textureMap.listAnimatedSprites.add(sprite);
				} else {
					textureMap.listAnimatedSprites.remove(sprite);
				}
			}
		}

	}

	private static void doStitch(Stitcher stitcher) throws StitchFullSpaceException {
		Stitcher.Holder[] holders = setStitchHolders.toArray(new Holder[0]);
		Arrays.sort(holders);
		for (Stitcher.Holder holder : holders) {
			allocateSlot(stitcher, holder);
		}
	}

	private static void allocateSlot(Stitcher stitcher, Stitcher.Holder holder) throws StitchFullSpaceException {
		TextureAtlasSprite textureatlassprite = holder.getAtlasSprite();
		boolean flag = textureatlassprite.getIconWidth() != textureatlassprite.getIconHeight();

		for (int i = 0; i < stitcher.stitchSlots.size(); ++i) {
			if (stitcher.stitchSlots.get(i).addSlot(holder)) {
				return;
			}

			if (flag) {
				holder.rotate();

				if (stitcher.stitchSlots.get(i).addSlot(holder)) {
					return;
				}

				holder.rotate();
			}
		}
		expandAndAllocateSlot(stitcher, holder);
	}


	private static void expandAndAllocateSlot(Stitcher stitcher, Stitcher.Holder holder) throws StitchFullSpaceException {
		int usedWidth = ((ACC_Stitcher) stitcher).get_usedWidth();
		int usedHeight = ((ACC_Stitcher) stitcher).get_usedHeight();
		int minSize = Math.min(holder.getWidth(), holder.getHeight());
		int w2 = MathHelper.smallestEncompassingPowerOfTwo(usedWidth);
		int h2 = MathHelper.smallestEncompassingPowerOfTwo(usedHeight);
		int nextw2 = MathHelper.smallestEncompassingPowerOfTwo(usedWidth + minSize);
		int nexth2 = MathHelper.smallestEncompassingPowerOfTwo(usedHeight + minSize);
		boolean widthMatched = nextw2 <= stitcher.currentWidth;
		boolean heightMatched = nexth2 <= stitcher.currentHeight;

		if (!widthMatched && !heightMatched) {
			throw new StitchFullSpaceException();
		} else {
			boolean extendWidth = widthMatched && w2 != nextw2;
			boolean extendHeight = heightMatched && h2 != nexth2;
			boolean flag;

			if (extendWidth ^ extendHeight) {
				flag = !extendWidth && widthMatched; //Forge: Fix stitcher not expanding entire height before growing width, and {potentially} growing larger then the max size.
			} else {
				flag = widthMatched && w2 <= h2;
			}

			Stitcher.Slot slot;

			if (flag) {
				if (holder.getWidth() > holder.getHeight()) {
					holder.rotate();
				}

				if (stitcher.currentHeight == 0) {
					stitcher.currentHeight = holder.getHeight();
				}

				slot = new Stitcher.Slot(usedWidth, 0, holder.getWidth(), stitcher.currentHeight);
				usedWidth += holder.getWidth();
				if (usedWidth > stitcher.currentWidth)
					throw new StitchFullSpaceException();
				((ACC_Stitcher) stitcher).set_usedWidth(usedWidth);
			} else {
				slot = new Stitcher.Slot(0, usedHeight, stitcher.currentWidth, holder.getHeight());
				usedHeight += holder.getHeight();
				if (usedHeight > stitcher.currentHeight)
					throw new StitchFullSpaceException();
				((ACC_Stitcher) stitcher).set_usedHeight(usedHeight);
			}

			slot.addSlot(holder);
			stitcher.stitchSlots.add(slot);
		}
	}

	private static List<TextureAtlasSprite> getStitchSlots(Stitcher stitcher) {
		List<Stitcher.Slot> list = Lists.newArrayList();

		for (Stitcher.Slot stitcher$slot : stitcher.stitchSlots) {
			stitcher$slot.getAllStitchSlots(list);
		}

		List<TextureAtlasSprite> list1 = Lists.newArrayList();

		for (Stitcher.Slot slot : list) {
			Stitcher.Holder holder = slot.getStitchHolder();
			if (!setStitchHolders.contains(holder))
				continue;
			TextureAtlasSprite textureatlassprite = holder.getAtlasSprite();
			textureatlassprite.initSprite(stitcher.currentWidth, stitcher.currentHeight, slot.getOriginX(), slot.getOriginY(), holder.isRotated());
			list1.add(textureatlassprite);
		}
		return list1;
	}

	private static int calcCRC32(IResource resource) {
		try (InputStream stream = resource.getInputStream()) {
			CRC32 crc32 = new CRC32();
			crc32.update(IOUtils.toByteArray(stream));
			return (int) crc32.getValue();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static ResourceLocation getActualLocation(ResourceLocation textureId) {
		return new ResourceLocation(textureId.getNamespace(), String.format("%s/%s%s", "textures", textureId.getPath(), ".png"));
	}

	public static class StitchFullSpaceException extends Exception {

		private static final long serialVersionUID = 5067924877088717476L;
	}
}
