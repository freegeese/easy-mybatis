package com.github.freegeese.easymybatis.generator.util;

import com.google.common.collect.Sets;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * 文件过滤器
 */
public class FileRenamer {
    // 文件路径替换
    private Map<String, String> filePathReplaceMap = new LinkedHashMap<>();
    // 文件名称替换
    private Map<String, String> fileNameReplaceMap = new LinkedHashMap<>();
    // 文件内容替换
    private Map<FilenameFilter, Map<String, String>> fileContentReplaceMap = new LinkedHashMap<>();
    // 默认的文件名称过滤器
    private final FilenameFilter DEFAULT_FILENAME_FILTER = (dir, name) -> true;
    // 文件路径过滤
    private List<FileFilter> filePathFilters = new ArrayList<>();
    // 文件名称过滤
    private List<FilenameFilter> fileNameFilters = new ArrayList<>();

    private FileRenamer() {
    }

    public static FileRenamer create() {
        return new FileRenamer();
    }

    public FileRenamer addFilePathReplace(String source, String target) {
        filePathReplaceMap.put(source, target);
        return this;
    }

    public FileRenamer addFileNameReplace(String source, String target) {
        fileNameReplaceMap.put(source, target);
        return this;
    }

    public FileRenamer addFileContentReplace(String source, String target) {
        return addFileContentReplace(DEFAULT_FILENAME_FILTER, source, target);
    }

    public FileRenamer addFileContentReplace(FilenameFilter filter, String source, String target) {
        if (!fileContentReplaceMap.containsKey(filter)) {
            fileContentReplaceMap.put(filter, new LinkedHashMap<>());
        }
        fileContentReplaceMap.get(filter).put(source, target);
        return this;
    }

    public FileRenamer addFilePathFilter(FileFilter filter) {
        filePathFilters.add(filter);
        return this;
    }

    public FileRenamer addFilePathFilter(Collection<FileFilter> filters) {
        filePathFilters.addAll(filters);
        return this;
    }

    public FileRenamer addFileNameFilter(FilenameFilter filter) {
        fileNameFilters.add(filter);
        return this;
    }

    public FileRenamer addFileNameFilter(Collection<FilenameFilter> filters) {
        fileNameFilters.addAll(filters);
        return this;
    }

    private File sourceDirectory;
    private File targetDirectory;

    public void execute(File sourceDirectory, File targetDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.targetDirectory = targetDirectory;
        filterFiles(sourceDirectory).stream().forEach(this::rename);
    }

    /**
     * 文件重命名
     *
     * @param source
     */
    private void rename(File source) {
        if (source.isDirectory()) {
            filterFiles(source).stream().forEach(this::rename);
        } else {
            // 源文件名称
            String fileName = source.getName();
            // 源文件相对路径
            int sourcePathLength = source.getAbsolutePath().length();
            String relativePath = source.getAbsolutePath().substring(sourceDirectory.getAbsolutePath().length(), sourcePathLength - fileName.length());
            // 替换相对路径
            if (!filePathReplaceMap.isEmpty()) {
                for (Map.Entry<String, String> entry : filePathReplaceMap.entrySet()) {
                    relativePath = relativePath.replace(entry.getKey(), entry.getValue());
                }
            }
            // 替换名称
            if (!fileNameReplaceMap.isEmpty()) {
                for (Map.Entry<String, String> entry : fileNameReplaceMap.entrySet()) {
                    fileName = fileName.replace(entry.getKey(), entry.getValue());
                }
            }
            // 组装成目标路径
            File target = Paths.get(targetDirectory.getAbsolutePath(), relativePath, fileName).toFile();
            // 复制内容
            copy(source, target);
        }
    }

    /**
     * 文件内容复制
     *
     * @param source
     * @param target
     */
    private void copy(File source, File target) {
        // 文件内容替换
        try {
            String content = new String(Files.readAllBytes(source.toPath()));
            if (!fileContentReplaceMap.isEmpty()) {
                for (Map.Entry<FilenameFilter, Map<String, String>> entry : fileContentReplaceMap.entrySet()) {
                    if (entry.getKey().accept(source.getParentFile(), source.getName())) {
                        Map<String, String> replaceContentMap = entry.getValue();
                        for (Map.Entry<String, String> replaceContentEntry : replaceContentMap.entrySet()) {
                            final String key = replaceContentEntry.getKey();
                            if (key.matches("@\\[.*]")) {
                                content = content.replaceAll(key.substring(2, key.length() - 1), replaceContentEntry.getValue());
                                continue;
                            }
                            content = content.replace(key, replaceContentEntry.getValue());
                        }
                    }
                }
            }
            // 判断目标路径是否存在，不存在则创建
            if (!target.getParentFile().exists()) {
                boolean created = target.getParentFile().mkdirs();
                if (!created) {
                    throw new RuntimeException("创建目标路径失败 -> " + target.getParentFile());
                }
                // 创建目标文件
                boolean newFile = target.createNewFile();
                if (!newFile) {
                    throw new RuntimeException("创建目标文件失败 -> " + target);
                }
            }
            // 复制内容
            Files.write(target.toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 过滤文件，对所有过滤器过滤后的文件集合做交集得到最终过滤后的文件集合
     *
     * @param directory
     * @return
     */
    private List<File> filterFiles(File directory) {
        Set<File> filtered = new HashSet<>(Arrays.asList(Objects.requireNonNull(directory.listFiles())));
        // 文件路径过滤
        if (!filePathFilters.isEmpty()) {
            for (FileFilter filter : filePathFilters) {
                filtered = Sets.intersection(filtered, new HashSet<>(Arrays.asList(Objects.requireNonNull(directory.listFiles(filter)))));
            }
        }
        // 文件名称过滤
        if (!fileNameFilters.isEmpty()) {
            for (FilenameFilter filter : fileNameFilters) {
                filtered = Sets.intersection(filtered, new HashSet<>(Arrays.asList(Objects.requireNonNull(directory.listFiles(filter)))));
            }
        }
        return new ArrayList<>(filtered);
    }

}
