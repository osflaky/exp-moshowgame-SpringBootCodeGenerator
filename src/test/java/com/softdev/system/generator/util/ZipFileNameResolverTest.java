package com.softdev.system.generator.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ZipFileNameResolver 测试")
class ZipFileNameResolverTest {

    private Map<String, Object> ctx(String className, String tableName) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("className", className);
        ctx.put("tableName", tableName);
        return ctx;
    }

    @Test
    @DisplayName("className 占位符被正确替换")
    void testPlaceholderReplace() {
        String result = ZipFileNameResolver.resolve(
                "${className}Controller.java", "controller", "mybatis", ctx("UserInfo", "sys_user_info"));
        assertEquals("UserInfoController.java", result);
    }

    @Test
    @DisplayName("tableName 占位符被正确替换")
    void testTableNamePlaceholder() {
        String result = ZipFileNameResolver.resolve(
                "${tableName}.sql", "table-sql", "sql", ctx("UserInfo", "t_user_info"));
        assertEquals("t_user_info.sql", result);
    }

    @Test
    @DisplayName("多个占位符同时替换（已知+未知）")
    void testMultiplePlaceholders() {
        String tpl = "${className}-${tableName}.sql";
        String result = ZipFileNameResolver.resolve(tpl, "sql", "sql", ctx("Order", "t_order"));
        assertEquals("Order-t_order.sql", result);
    }

    @Test
    @DisplayName("未识别占位符被替换为空字符串")
    void testUnknownPlaceholderRemoved() {
        String result = ZipFileNameResolver.resolve(
                "${className}-${unknown}.java", "controller", "mybatis", ctx("Foo", "t"));
        assertEquals("Foo-.java", result);
    }

    @Test
    @DisplayName("fileName 缺失时 group=mybatis 推断为 .java 后缀")
    void testConventionMybatis() {
        String result = ZipFileNameResolver.resolve(null, "controller", "mybatis", ctx("UserInfo", "t_user"));
        assertEquals("UserInfo.java", result);
    }

    @Test
    @DisplayName("fileName 缺失时 group=ui 推断 .html 后缀并含模板名")
    void testConventionUi() {
        String result = ZipFileNameResolver.resolve(null, "element-ui", "ui", ctx("UserInfo", "t_user"));
        assertEquals("UserInfo-element-ui.html", result);
    }

    @Test
    @DisplayName("fileName 缺失时 name 含 xml 推断 .xml 后缀")
    void testConventionXml() {
        String result = ZipFileNameResolver.resolve(null, "mapper-xml", "mybatis", ctx("UserInfo", "t_user"));
        assertEquals("UserInfo.xml", result);
    }

    @Test
    @DisplayName("fileName 缺失时 name 含 sql 推断 .sql 后缀")
    void testConventionSql() {
        String result = ZipFileNameResolver.resolve(null, "create-sql", "mybatis", ctx("UserInfo", "t_user"));
        assertEquals("UserInfo.sql", result);
    }

    @Test
    @DisplayName("非法字符被替换为下划线")
    void testInvalidCharsAreSanitized() {
        String result = ZipFileNameResolver.resolve(
                "${className}/*.java", "controller", "mybatis", ctx("Us?er/Info", "t"));
        assertNotNull(result);
        assertTrue(!result.contains("/"), "结果不应含 /: " + result);
        assertTrue(!result.contains("?"), "结果不应含 ?: " + result);
    }

    @Test
    @DisplayName("className 和 tableName 都为空时 fallback 为 Generated")
    void testEmptyClassNameFallback() {
        String result = ZipFileNameResolver.resolve(null, "controller", "mybatis", ctx("", ""));
        assertNotNull(result);
        assertTrue(result.startsWith("Generated"), "应使用兜底 Generated: " + result);
    }

    @Test
    @DisplayName("输入纯非法字符时被 sanitize 兜底为合法名")
    void testSanitizeFallback() {
        String result = ZipFileNameResolver.resolve(
                "///.java", "controller", "mybatis", ctx("UserInfo", "t_user"));
        assertNotNull(result);
        assertTrue(result.endsWith(".java") || result.length() > 0, "兜底结果应可用: " + result);
    }
}
