package com.softdev.system.generator.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件名解析器
 * <p>
 * 解析模板配置中的 fileName 字段，支持 ${className}、${tableName}、${group}、${name} 占位符。
 * <p>
 * 解析规则：
 * <ul>
 *   <li>若 fileName 中含 ${xxx} 占位符，则按占位符从 context 解析</li>
 *   <li>若 fileName 为空，则根据 template.name + group 自动推断（兜底策略）</li>
 *   <li>自动清理非法字符，确保可作为文件名</li>
 * </ul>
 *
 * @author zhengkai.blog.csdn.net
 */
public final class ZipFileNameResolver {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)\\}");

    private static final Pattern INVALID_FILE_NAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|\\r\\n\\t]");

    private ZipFileNameResolver() {
    }

    /**
     * 解析文件名
     *
     * @param fileNameTemplate 模板配置中的 fileName（可为 null/空）
     * @param templateName     模板 name（如 controller / model / mapper）
     * @param group            模板 group（如 mybatis / jpa / ui）
     * @param context          占位符上下文（至少含 className、tableName）
     * @return 最终文件名（不含路径）
     */
    public static String resolve(String fileNameTemplate,
                                 String templateName,
                                 String group,
                                 Map<String, Object> context) {
        String raw = (fileNameTemplate == null || fileNameTemplate.trim().isEmpty())
                ? guessByConvention(templateName, group, context)
                : fileNameTemplate;

        String resolved = replacePlaceholders(raw, context);
        return sanitize(resolved, templateName, group, context);
    }

    /**
     * 兜底策略：根据模板名 + 分组推断文件名
     */
    private static String guessByConvention(String templateName, String group, Map<String, Object> context) {
        String className = stringOf(context, "className");
        if (className == null || className.isEmpty()) {
            className = stringOf(context, "tableName");
        }
        if (className == null || className.isEmpty()) {
            className = "Generated";
        }
        String ext = guessExtension(templateName, group);
        if (ext == null || ext.isEmpty()) {
            return className + "-" + templateName;
        }
        return className + ext;
    }

    /**
     * 推断文件后缀
     */
    private static String guessExtension(String templateName, String group) {
        if (group != null) {
            switch (group.toLowerCase()) {
                case "ui":
                case "renren-fast":
                    return "-" + templateName + ".html";
                case "bi":
                case "cloud":
                    return "-" + templateName + ".txt";
                default:
                    break;
            }
        }
        if (templateName == null) {
            return ".txt";
        }
        if (templateName.contains("xml") || templateName.endsWith("-xml")) {
            return ".xml";
        }
        if (templateName.contains("yml") || templateName.contains("yaml")) {
            return ".yml";
        }
        if (templateName.contains("sql")) {
            return ".sql";
        }
        if (templateName.contains("vue")) {
            return ".vue";
        }
        if (templateName.contains("json")) {
            return ".json";
        }
        if (templateName.contains("md")) {
            return ".md";
        }
        return ".java";
    }

    private static String replacePlaceholders(String input, Map<String, Object> context) {
        if (input == null || !input.contains("${")) {
            return input;
        }
        Matcher matcher = PLACEHOLDER.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = stringOf(context, key);
            if (value == null) {
                value = "";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 清理非法字符；如果清理后为空则使用兜底
     */
    private static String sanitize(String fileName, String templateName, String group, Map<String, Object> context) {
        if (fileName == null) {
            return guessByConvention(templateName, group, context);
        }
        String cleaned = INVALID_FILE_NAME_CHARS.matcher(fileName).replaceAll("_").trim();
        if (cleaned.isEmpty() || ".".equals(cleaned) || "..".equals(cleaned)) {
            return guessByConvention(templateName, group, context);
        }
        return cleaned;
    }

    private static String stringOf(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }
}
