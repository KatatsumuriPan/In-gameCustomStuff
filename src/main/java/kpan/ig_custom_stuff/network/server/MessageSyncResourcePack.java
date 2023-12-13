package kpan.ig_custom_stuff.network.server;

import io.netty.buffer.ByteBuf;
import kpan.ig_custom_stuff.network.MessageBase;
import kpan.ig_custom_stuff.registry.MCRegistryUtil;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceLoader.SingleBlockModelLoader;
import kpan.ig_custom_stuff.resource.DynamicResourceManager;
import kpan.ig_custom_stuff.resource.DynamicResourceManager.ClientCache;
import kpan.ig_custom_stuff.resource.ids.BlockId;
import kpan.ig_custom_stuff.resource.ids.ITextureId;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MessageSyncResourcePack extends MessageBase {


	//デフォルトコンストラクタは必須
	public MessageSyncResourcePack() { }

	public FileDatas fileDatas = new FileDatas();

	public MessageSyncResourcePack(Path directory) {
		fileDatas.fromFileSystem(directory);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		fileDatas.fromBytes(buf);
	}
	@Override
	public void toBytes(ByteBuf buf) {
		fileDatas.toBytes(buf);
	}
	@Override
	public void doAction(MessageContext ctx) {
		Client.saveAndLoad(fileDatas);
	}


	public static class FileDatas {
		private final Map<String, byte[]> map = new HashMap<>();

		public void fromFileSystem(Path directory) {
			try (Stream<Path> stream = Files.find(directory, Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile())) {
				stream.forEach(p -> {
					String path = directory.relativize(p).toString().replace('\\', '/');
					byte[] bytes;
					try {
						bytes = Files.readAllBytes(p);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					map.put(path, bytes);
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		public void toFileSystem(Path directory) {
			try {
				FileUtils.deleteDirectory(directory.toFile());
				for (Entry<String, byte[]> entry : map.entrySet()) {
					Path path = directory.resolve(entry.getKey());
					Files.createDirectories(path.getParent());
					Files.write(path, entry.getValue());
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public void fromBytes(ByteBuf buf) {
			int length = buf.readInt();
			byte[] compressed = new byte[length];
			buf.readBytes(compressed);
			decompress(map, compressed);
		}
		public void toBytes(ByteBuf buf) {
			byte[] compressed = compress(map);
			buf.writeInt(compressed.length);
			buf.writeBytes(compressed);
		}

		private static byte[] compress(Map<String, byte[]> map) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			try (DataOutputStream gzip = new DataOutputStream(new GZIPOutputStream(stream))) {
				gzip.writeInt(map.size());
				for (Entry<String, byte[]> entry : map.entrySet()) {
					gzip.writeUTF(entry.getKey());
					gzip.writeInt(entry.getValue().length);
					gzip.write(entry.getValue());
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return stream.toByteArray();
		}
		private static void decompress(Map<String, byte[]> map, byte[] compressed) {
			byte[] decompressed;//GZIPInputStreamとDataInputStreamは組み合わせるとバグる
			try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
				decompressed = IOUtils.toByteArray(gzipInputStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			try (DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(decompressed))) {
				int size = inputStream.readInt();
				for (int i = 0; i < size; i++) {
					String path = inputStream.readUTF();
					int len = inputStream.readInt();
					byte[] data = new byte[len];
					if (inputStream.read(data) != len)
						throw new RuntimeException();
					map.put(path, data);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static class Client {
		public static void saveAndLoad(FileDatas fileDatas) {
			fileDatas.toFileSystem(ClientCache.resourcePackPath);
			DynamicResourceManager.ClientCache.readFromFolder();
			List<ITextureId> textureIds = new ArrayList<>();
			textureIds.addAll(ClientCache.INSTANCE.blockTextureIds.keySet());
			textureIds.addAll(ClientCache.INSTANCE.itemTextureIds.keySet());
			DynamicResourceLoader.loadTexturesDynamic(textureIds);
			DynamicResourceLoader.loadItemModels(ClientCache.INSTANCE);
			for (BlockId blockId : MCRegistryUtil.getBlockIds()) {
				DynamicResourceLoader.loadBlockModel(blockId);
			}
			DynamicResourceLoader.loadItemModels(MCRegistryUtil.getBlockIds().stream().map(BlockId::toItemId).collect(Collectors.toList()));
			MCRegistryUtil.reloadItemModelMeshes();
			SingleBlockModelLoader.loadBlockModels(ClientCache.INSTANCE.blockModelIds.keySet());
			for (Entry<String, Map<String, Map<String, String>>> namespacedLang : ClientCache.INSTANCE.lang.entrySet()) {
				Map<String, String> translationMap = namespacedLang.getValue().get("en_us");
				for (Entry<String, String> entry : translationMap.entrySet()) {
					DynamicResourceLoader.putLang(entry.getKey(), entry.getValue());
				}
				translationMap = namespacedLang.getValue().get(Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode());
				if (translationMap != null) {
					for (Entry<String, String> entry : translationMap.entrySet()) {
						DynamicResourceLoader.putLang(entry.getKey(), entry.getValue());
					}
				}
			}
		}
	}
}
