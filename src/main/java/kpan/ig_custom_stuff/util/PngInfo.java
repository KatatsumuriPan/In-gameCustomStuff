package kpan.ig_custom_stuff.util;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PngInfo {

	public static PngInfo from(byte[] data) throws IOException {
		return new PngInfo(new ByteArrayInputStream(data));
	}

	public final int pngWidth;
	public final int pngHeight;

	private PngInfo(InputStream stream) throws IOException {
		DataInputStream datainputstream = new DataInputStream(stream);

		if (datainputstream.readLong() != -8552249625308161526L) {
			throw new IOException("Bad PNG Signature");
		} else if (datainputstream.readInt() != 13) {
			throw new IOException("Bad length for IHDR chunk!");
		} else if (datainputstream.readInt() != 1229472850) {
			throw new IOException("Bad type for IHDR chunk!");
		} else {
			pngWidth = datainputstream.readInt();
			pngHeight = datainputstream.readInt();
			IOUtils.closeQuietly(datainputstream);
		}
	}

}
