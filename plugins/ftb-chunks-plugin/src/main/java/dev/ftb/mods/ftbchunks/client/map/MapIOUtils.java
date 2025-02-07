package dev.ftb.mods.ftbchunks.client.map;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class MapIOUtils {
	public interface IOCallback<T> {
		void callback(T stream) throws IOException;
	}

	public static void write(OutputStream stream, IOCallback<DataOutputStream> callback) {
		try (DataOutputStream s = new DataOutputStream(new BufferedOutputStream(new DeflaterOutputStream(stream)))) {
			callback.callback(s);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void write(Path path, IOCallback<DataOutputStream> callback) {
		try (OutputStream fos = Files.newOutputStream(path)) {
			write(fos, callback);
		} catch (Exception ignored) {
		}
	}

	public static boolean read(Path path, IOCallback<DataInputStream> callback) {
		if (Files.notExists(path) || !Files.isReadable(path)) {
			return false;
		}

		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new InflaterInputStream(Files.newInputStream(path))))) {
			callback.callback(in);
			return true;
		} catch (Exception ignored) {
		}

		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(path))))) {
			callback.callback(in);
			return false;
		} catch (Exception ignored) {
		}

		return false;
	}
}