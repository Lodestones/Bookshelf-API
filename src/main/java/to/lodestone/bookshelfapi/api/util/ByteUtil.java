package to.lodestone.bookshelfapi.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ByteUtil {

    private static final HashMap<String, String> gzipCache = new HashMap<>();

    public static String compressJSON(String json) throws IOException {
        if (gzipCache.containsKey(json)) return gzipCache.get(json);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
        gzipOut.write(json.getBytes(StandardCharsets.UTF_8));
        gzipOut.close();
        byte[] compressedBytes = baos.toByteArray();
        String gzipCompressed = Base64.getEncoder().encodeToString(compressedBytes);
        gzipCache.put(json, gzipCompressed);
        return gzipCompressed;
    }

    public static String decompressJSON(String compressedJson) throws IOException {
        if (gzipCache.containsKey(compressedJson)) return gzipCache.get(compressedJson);
        byte[] compressedBytes = Base64.getDecoder().decode(compressedJson);
        ByteArrayInputStream bais = new ByteArrayInputStream(compressedBytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPInputStream gzipIn = new GZIPInputStream(bais);

        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzipIn.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }

        gzipIn.close();
        String decompressed = baos.toString(StandardCharsets.UTF_8);
        gzipCache.put(compressedJson, decompressed);
        return decompressed;
    }

}