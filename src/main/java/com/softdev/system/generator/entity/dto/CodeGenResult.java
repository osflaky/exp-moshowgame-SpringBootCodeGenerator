package com.softdev.system.generator.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 代码生成结果封装
 * <p>
 * 携带：渲染后的代码、模板 fileName 模板、模板分组，供 ZIP 打包 / 单文件下载等场景使用。
 *
 * @author zhengkai.blog.csdn.net
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeGenResult {

    /** 表名（原始或处理后） */
    private String tableName;

    /** 类名 */
    private String className;

    /** 渲染结果：key=template.name, value=渲染后的内容 */
    private Map<String, String> generatedCode;

    /** 模板 fileName 配置：key=template.name, value=fileName 模板（可含 ${className} 占位符） */
    private Map<String, String> fileNameTemplates;

    /** 模板分组：key=template.name, value=group */
    private Map<String, String> groupByTemplate;
}
