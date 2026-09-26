package com.softdev.system.generator.service;

import com.softdev.system.generator.service.impl.ZipServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ZipServiceImpl 测试")
class ZipServiceImplTest {

    private final ZipService zipService = new ZipServiceImpl();

    @Test
    @DisplayName("基本打包：按 group/fileName 输出 ZIP")
    void testBasicBuild() throws Exception {
        Map<String, String> generated = new LinkedHashMap<>();
        generated.put("controller", "public class UserInfoController {}");
        generated.put("model", "public class UserInfo {}");

        Map<String, String> fileNameTpl = new HashMap<>();
        fileNameTpl.put("controller", "${className}Controller.java");
        fileNameTpl.put("model", "${className}.java");

        Map<String, String> group = new HashMap<>();
        group.put("controller", "mybatis");
        group.put("model", "mybatis");

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("className", "UserInfo");
        ctx.put("tableName", "user_info");

        byte[] zip = zipService.buildZip(generated, fileNameTpl, group, "UserInfo", ctx);
        assertNotNull(zip);
        assertTrue(zip.length > 0);

        Map<String, String> entries = unzip(zip);
        assertEquals(2, entries.size());
        assertEquals("public class UserInfoController {}", entries.get("mybatis/UserInfoController.java"));
        assertEquals("public class UserInfo {}", entries.get("mybatis/UserInfo.java"));
    }

    @Test
    @DisplayName("重名文件自动加序号")
    void testDuplicateFileNames() throws Exception {
        Map<String, String> generated = new LinkedHashMap<>();
        generated.put("model", "class A {}");
        generated.put("entity", "class A {}");

        Map<String, String> fileNameTpl = new HashMap<>();
        fileNameTpl.put("model", "${className}.java");
        fileNameTpl.put("entity", "${className}.java");

        Map<String, String> group = new HashMap<>();
        group.put("model", "jpa");
        group.put("entity", "mybatis-plus");

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("className", "UserInfo");
        ctx.put("tableName", "user_info");

        byte[] zip = zipService.buildZip(generated, fileNameTpl, group, "UserInfo", ctx);
        Map<String, String> entries = unzip(zip);
        assertEquals(2, entries.size(), "应该有两个 entry");
        long javaCount = entries.keySet().stream().filter(k -> k.endsWith(".java")).count();
        assertEquals(2, javaCount, "应该有两个 .java 文件: " + entries.keySet());
        // 一个走 jpa 一个走 mybatis-plus 目录，路径不同
        assertTrue(entries.containsKey("jpa/UserInfo.java"));
        assertTrue(entries.containsKey("mybatis-plus/UserInfo.java"));
    }

    @Test
    @DisplayName("fileName 配置为空时由约定推断")
    void testConventionFallback() throws Exception {
        Map<String, String> generated = new LinkedHashMap<>();
        generated.put("element-ui", "<form>...</form>");
        generated.put("json", "{}");

        Map<String, String> fileNameTpl = new HashMap<>();
        fileNameTpl.put("element-ui", null);
        fileNameTpl.put("json", null);

        Map<String, String> group = new HashMap<>();
        group.put("element-ui", "ui");
        group.put("json", "util");

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("className", "UserInfo");

        byte[] zip = zipService.buildZip(generated, fileNameTpl, group, "UserInfo", ctx);
        Map<String, String> entries = unzip(zip);
        assertEquals(2, entries.size());
        assertTrue(entries.containsKey("ui/UserInfo-element-ui.html"));
        assertTrue(entries.containsKey("util/UserInfo.json"));
    }

    @Test
    @DisplayName("空内容 map 抛 IllegalArgumentException")
    void testEmptyGeneratedCode() {
        Map<String, String> empty = new HashMap<>();
        assertThrows(IllegalArgumentException.class,
                () -> zipService.buildZip(empty, null, null, "x", new HashMap<>()));
    }

    @Test
    @DisplayName("null 内容 entry 被跳过，不写入 ZIP")
    void testNullContentSkipped() throws Exception {
        Map<String, String> generated = new LinkedHashMap<>();
        generated.put("a", "real");
        generated.put("b", null);

        Map<String, String> fileNameTpl = new HashMap<>();
        fileNameTpl.put("a", "a.txt");
        fileNameTpl.put("b", "b.txt");

        Map<String, String> group = new HashMap<>();
        group.put("a", "g");
        group.put("b", "g");

        byte[] zip = zipService.buildZip(generated, fileNameTpl, group, "x", new HashMap<>());
        Map<String, String> entries = unzip(zip);
        assertEquals(1, entries.size());
        assertTrue(entries.containsKey("g/a.txt"));
    }

    private Map<String, String> unzip(byte[] zip) throws Exception {
        Map<String, String> entries = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n;
                while ((n = zis.read(buf)) > 0) {
                    baos.write(buf, 0, n);
                }
                entries.put(entry.getName(), baos.toString(StandardCharsets.UTF_8));
            }
        }
        return entries;
    }
}
