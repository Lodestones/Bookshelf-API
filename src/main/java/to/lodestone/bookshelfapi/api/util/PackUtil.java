package to.lodestone.bookshelfapi.api.util;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public class PackUtil {

    public static byte[] convertHexToByteArray(String hexString) {
        int length = hexString.length();
        byte[] byteArray = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return byteArray;
    }

    @SuppressWarnings("deprecation")
    public static String retrieveSHA1FromURL(String fileUrl) throws Exception {
        URL url = new URL(fileUrl);

        // Download the file
        Path tempFile = Files.createTempFile("downloaded", ".zip");
        try (InputStream in = url.openStream()) {
            Files.copy(in, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        // Calculate SHA-1 hash
        return calculateSHA1(tempFile);
    }

    private static String calculateSHA1(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream fis = Files.newInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
