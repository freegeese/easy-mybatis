package com.github.freegeese.easymybatis.generator.util;

import com.google.common.base.Preconditions;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 文件内容替换器
 */
public class FileReplacer {
    /**
     * 保留内容开始标记
     */
    private String keepMarkStart;

    /**
     * 保留内容结束标记
     */
    private String keepMarkEnd;

    private FileReplacer(String keepMarkStart) {
        this.keepMarkStart = Preconditions.checkNotNull(keepMarkStart);
    }

    private FileReplacer(String keepMarkStart, String keepMarkEnd) {
        this.keepMarkStart = Preconditions.checkNotNull(keepMarkStart);
        this.keepMarkEnd = Preconditions.checkNotNull(keepMarkEnd);
    }

    public static FileReplacer create(String keepMarkStart) {
        return new FileReplacer(keepMarkStart);
    }

    public FileReplacer keepMarkEnd(String keepMarkEnd) {
        this.keepMarkEnd = keepMarkEnd;
        return this;
    }

    /**
     * 替换文件内容
     *
     * @param file                                    目标文件
     * @param newContentLines                         新的内容
     * @param keepMarkedContentInsertPositionInLineNo 保留的标记内容插入到哪里一行
     * @return
     */
    public List<String> replace(File file, List<String> newContentLines, Integer keepMarkedContentInsertPositionInLineNo) {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(newContentLines);
        Preconditions.checkNotNull(keepMarkedContentInsertPositionInLineNo);
        try {
            // 拿到文件内容
            final List<String> lines = Files.readAllLines(Paths.get(file.toURI()), StandardCharsets.UTF_8);
            // 获取保留的内容
            final List<String> markedLines = new ArrayList<>();
            boolean marked = false;
            for (String line : lines) {
                if (Objects.nonNull(this.keepMarkEnd) && line.contains(this.keepMarkEnd)) {
                    markedLines.add(line);
                    break;
                }

                if (marked) {
                    markedLines.add(line);
                    continue;
                }

                if (line.contains(this.keepMarkStart)) {
                    marked = true;
                    markedLines.add(line);
                }
            }
            final List<String> merged = new ArrayList<>(newContentLines);
            // 合并新的内容和保留内容
            if (keepMarkedContentInsertPositionInLineNo >= 0) {
                merged.addAll(keepMarkedContentInsertPositionInLineNo, markedLines);
                return merged;
            }
            merged.addAll(newContentLines.size() + 1 + keepMarkedContentInsertPositionInLineNo, markedLines);
            return merged;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
