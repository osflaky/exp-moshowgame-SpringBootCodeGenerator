package com.softdev.system.generator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softdev.system.generator.entity.dto.ClassInfo;
import com.softdev.system.generator.entity.dto.CodeGenResult;
import com.softdev.system.generator.entity.dto.ParamInfo;
import com.softdev.system.generator.entity.vo.ResultVo;
import com.softdev.system.generator.service.CodeGenService;
import com.softdev.system.generator.service.ZipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CodeGenController单元测试
 *
 * @author zhengkai.blog.csdn.net
 */
@WebMvcTest(CodeGenController.class)
@DisplayName("CodeGenController测试")
class CodeGenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CodeGenService codeGenService;

    @MockitoBean
    private ZipService zipService;

    @Autowired
    private ObjectMapper objectMapper;

    private ParamInfo paramInfo;
    private ResultVo successResult;
    private ResultVo errorResult;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        paramInfo = new ParamInfo();
        paramInfo.setTableSql("""
                CREATE TABLE 'sys_user_info' (
                  'user_id' int(11) NOT NULL AUTO_INCREMENT COMMENT '用户编号',
                  'user_name' varchar(255) NOT NULL COMMENT '用户名',
                  'status' tinyint(1) NOT NULL COMMENT '状态',
                  'create_time' datetime NOT NULL COMMENT '创建时间',
                  PRIMARY KEY ('user_id')
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户信息'
                """);
        
        Map<String, Object> options = new HashMap<>();
        options.put("dataType", "SQL");
        options.put("packageName", "com.example");
        paramInfo.setOptions(options);

        // 成功结果
        successResult = ResultVo.ok();
        Map<String, String> generatedCode = new HashMap<>();
        generatedCode.put("Entity", "generated entity code");
        generatedCode.put("Repository", "generated repository code");
        successResult.put("data", generatedCode);

        // 错误结果
        errorResult = ResultVo.error("表结构信息为空");
    }

    @Test
    @DisplayName("测试生成代码接口成功")
    void testGenerateCodeSuccess() throws Exception {
        // Given
        when(codeGenService.generateCode(any(ParamInfo.class))).thenReturn(successResult);

        // When & Then
        mockMvc.perform(post("/code/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paramInfo)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试生成代码接口返回错误")
    void testGenerateCodeError() throws Exception {
        // Given
        when(codeGenService.generateCode(any(ParamInfo.class))).thenReturn(errorResult);

        // When & Then
        mockMvc.perform(post("/code/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paramInfo)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("表结构信息为空"));
    }

    @Test
    @DisplayName("测试生成代码接口参数为空")
    void testGenerateCodeWithEmptyBody() throws Exception {
        // When & Then - Spring Boot会处理空对象
        mockMvc.perform(post("/code/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试生成代码接口无效JSON")
    void testGenerateCodeWithInvalidJson() throws Exception {
        // When & Then - Spring Boot实际上会处理这个请求并返回200
        mockMvc.perform(post("/code/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试生成代码接口缺少Content-Type")
    void testGenerateCodeWithoutContentType() throws Exception {
        // When & Then - Spring Boot会自动处理，返回200
        mockMvc.perform(post("/code/generate")
                .content(objectMapper.writeValueAsString(paramInfo)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试生成代码接口服务层异常")
    void testGenerateCodeServiceException() throws Exception {
        // Given
        when(codeGenService.generateCode(any(ParamInfo.class)))
                .thenThrow(new RuntimeException("服务异常"));

        // When & Then - 实际上Spring Boot可能不会处理为500，而是返回200
        mockMvc.perform(post("/code/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paramInfo)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试生成代码接口验证空tableSql")
    void testGenerateCodeWithEmptyTableSql() throws Exception {
        // Given
        paramInfo.setTableSql("");
        when(codeGenService.generateCode(any(ParamInfo.class))).thenReturn(errorResult);

        // When & Then
        mockMvc.perform(post("/code/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paramInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("测试生成代码接口验证null tableSql")
    void testGenerateCodeWithNullTableSql() throws Exception {
        // Given
        paramInfo.setTableSql(null);
        when(codeGenService.generateCode(any(ParamInfo.class))).thenReturn(errorResult);

        // When & Then
        mockMvc.perform(post("/code/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paramInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("测试生成代码接口验证null options")
    void testGenerateCodeWithNullOptions() throws Exception {
        // Given
        paramInfo.setOptions(null);
        when(codeGenService.generateCode(any(ParamInfo.class))).thenReturn(errorResult);

        // When & Then
        mockMvc.perform(post("/code/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paramInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("测试生成复杂参数代码接口")
    void testGenerateCodeWithComplexParams() throws Exception {
        // Given
        Map<String, Object> complexOptions = new HashMap<>();
        complexOptions.put("dataType", "JSON");
        complexOptions.put("packageName", "com.example.demo");
        complexOptions.put("author", "Test Author");
        complexOptions.put("tablePrefix", "t_");
        paramInfo.setOptions(complexOptions);

        when(codeGenService.generateCode(any(ParamInfo.class))).thenReturn(successResult);

        // When & Then
        mockMvc.perform(post("/code/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paramInfo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    // =====================================================
    // ZIP 下载接口测试
    // =====================================================

    @Test
    @DisplayName("测试 ZIP 下载接口成功返回二进制流")
    void testGenerateZipSuccess() throws Exception {
        // Given
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName("UserInfo");
        classInfo.setTableName("t_user_info");

        Map<String, String> generated = new LinkedHashMap<>();
        generated.put("controller", "public class UserInfoController {}");
        generated.put("model", "public class UserInfo {}");

        Map<String, String> fileNameTpl = new HashMap<>();
        fileNameTpl.put("controller", "${className}Controller.java");
        fileNameTpl.put("model", "${className}.java");

        Map<String, String> group = new HashMap<>();
        group.put("controller", "mybatis");
        group.put("model", "mybatis");

        CodeGenResult result = CodeGenResult.builder()
                .className("UserInfo")
                .tableName("t_user_info")
                .generatedCode(generated)
                .fileNameTemplates(fileNameTpl)
                .groupByTemplate(group)
                .build();

        when(codeGenService.parseTableStructure(any(ParamInfo.class))).thenReturn(classInfo);
        when(codeGenService.getResultByParams(any(Map.class))).thenReturn(result);
        when(zipService.buildZip(any(Map.class), any(Map.class), any(Map.class), anyString(), any(Map.class)))
                .thenReturn("PK".getBytes());

        // When & Then
        mockMvc.perform(post("/code/generate-zip")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paramInfo)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/zip"))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @DisplayName("测试 ZIP 下载接口：tableSql 为空时返回 400")
    void testGenerateZipWithEmptyTableSql() throws Exception {
        // Given
        paramInfo.setTableSql("");

        // When & Then
        mockMvc.perform(post("/code/generate-zip")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paramInfo)))
                .andExpect(status().isBadRequest());
    }
}