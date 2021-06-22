package com.github.freegeese.easymybatis.generator.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public abstract class IoUtils {
    public static byte[] readBytes(InputStream source) {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        try {
            while ((n = source.read(buf)) > 0) {
                sink.write(buf, 0, n);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sink.toByteArray();
    }

    public static String readString(InputStream source) {
        return new String(readBytes(source));
    }
}
