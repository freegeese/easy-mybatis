package com.github.freegeese.easymybatis.generator;

import com.github.freegeese.easymybatis.generator.util.FileReplacer;
import com.github.freegeese.easymybatis.generator.util.FreemarkerUtils;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

/**
 * 代码生成器
 *
 * @author zhangguangyong
 * @since 1.0
 */
@Slf4j
public class Generator {
    /**
     * 是否覆盖已存在的文件
     */
    private boolean override;

    /**
     * 当选择覆盖已存在文件的时候，可选择被覆盖文件保留的范围（开始标记）
     */
    private String keepMarkStart;

    /**
     * 当选择覆盖已存在文件的时候，可选择被覆盖文件保留的范围（结束标记）
     */
    private String keepMarkEnd;

    public static Generator create() {
        return new Generator();
    }

    public Generator override(boolean override) {
        this.override = override;
        return this;
    }

    public Generator keepMarkStart(String keepMarkStart) {
        this.keepMarkStart = keepMarkStart;
        return this;
    }

    public Generator keepMarkEnd(String keepMarkEnd) {
        this.keepMarkEnd = keepMarkEnd;
        return this;
    }

    /**
     * 生成代码
     *
     * @param dataModel
     * @param template
     * @param output
     */
    public void generate(Object dataModel, String template, File output) {
        // 输出文件已存在，并且设置为不覆盖
        if (output.exists() && !this.override) {
            log.info("输出文件已存在，并且配置属性[override]为false -> {}", output);
            return;
        }

        // 数据 + 模板 = 内容
        String content = FreemarkerUtils.parse(dataModel, template);

        // 输出文件不存在的情况
        if (!output.exists()) {
            try {
                if (!output.getParentFile().exists()) {
                    output.getParentFile().mkdirs();
                }
                log.info("输出文件不存在，创建输出文件 -> {}", output);
                output.createNewFile();
                Files.write(output.toPath(), content.getBytes(StandardCharsets.UTF_8));
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            // 输出文件已存在的内容
            final String existsContent = new String(Files.readAllBytes(output.toPath()), StandardCharsets.UTF_8);

            // 输出文件已存在的内容没有包含需要保留的内容，则直接覆盖
            if (Objects.isNull(this.keepMarkStart) || !existsContent.contains(this.keepMarkStart)) {
                Files.write(output.toPath(), content.getBytes(StandardCharsets.UTF_8));
                return;
            }

            // 输出文件已存在的内容包含需要保留的内容，进行内容合并
            final List<String> newLines = Splitter.onPattern(System.lineSeparator()).splitToList(content);
            final List<String> mergedLines = FileReplacer
                    .create(this.keepMarkStart)
                    .keepMarkEnd(this.keepMarkEnd)
                    .replace(output, newLines, -2);
            Files.write(output.toPath(), mergedLines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
