package com.github.freegeese.easymybatis.test;

import com.aspose.words.Document;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;

public class AsposeTests {
    static {
        try {
            Class<?> cls = Class.forName("com.aspose.words.zzXyu");
            Field encoded = cls.getDeclaredField("zzZXG");
            Field decoded = cls.getDeclaredField("zz1Y");
            encoded.setAccessible(true);
            decoded.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(encoded, encoded.getModifiers() & ~Modifier.FINAL);
            encoded.set(null,decoded.get(null));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void test() throws Exception {
        String dir = System.getProperty("user.dir");
//        File word = Paths.get(dir, "src/test/resources", "test4.doc").toFile();
        File docx = Paths.get(dir, "src/test/resources", "test4x.docx").toFile();
        File pdf = Paths.get(dir, "src/test/resources", "test4x.pdf").toFile();

        Document doc = new Document(docx.getAbsolutePath());
        doc.save(pdf.getAbsolutePath());
    }
}
