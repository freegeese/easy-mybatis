package com.github.freegeese.easymybatis.generator.util;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.StringWriter;
import java.util.UUID;

/**
 * Freemarker 模板解析工具
 */
public class FreemarkerParser {
    private static Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);

    static {
        cfg.setTemplateLoader(new StringTemplateLoader());
        cfg.setDefaultEncoding("UTF-8");
        cfg.setWrapUncheckedExceptions(true);
    }

    public static FreemarkerParser create() {
        return new FreemarkerParser();
    }

    public String parse(Object data, String template) {
        String name = UUID.randomUUID().toString() + System.currentTimeMillis();
        StringTemplateLoader loader = (StringTemplateLoader) cfg.getTemplateLoader();
        try {
            loader.putTemplate(name, template);
            Template ftl = cfg.getTemplate(name);
            StringWriter writer = new StringWriter();
            ftl.process(data, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            loader.removeTemplate(name);
        }
    }
}
