package net.verytools.unipay.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;

public class IOUtils {

    private static final int BUFFER_SIZE = 1024;

    public static String copyToString(InputStream in, Charset charset) throws IOException {
        StringBuilder out = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(in, charset);
        char[] buffer = new char[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = reader.read(buffer)) != -1) {
            out.append(buffer, 0, bytesRead);
        }
        return out.toString();
    }

    /**
     * read key as input stream.
     *
     * @param keyPath the key path, should be absolute path or path relative to classpath.
     */
    public static InputStream readKey(String keyPath) {
        if (StringUtils.isBlank(keyPath)) {
            throw new IllegalArgumentException("key is required for refunding");
        }
        if (keyPath.startsWith("/")) {
            try {
                return new FileInputStream(keyPath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("key specified may not exists: " + keyPath);
            }
        }
        if (keyPath.startsWith("classpath:")) {
            keyPath = keyPath.replace("classpath:", "");
            if (keyPath.startsWith("/")) {
                keyPath = keyPath.replace("/", "");
            }
        }
        return IOUtils.class.getClassLoader().getResourceAsStream(keyPath);
    }
}
