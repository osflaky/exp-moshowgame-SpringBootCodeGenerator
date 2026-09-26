package com.softdev.system.generator.service;

import java.util.Map;

/**
 * ZIP 打包服务接口
 *
 * @author zhengkai.blog.csdn.net
 */
public interface ZipService {

    /**
     * 将已生成的代码结果（key=模板名, value=渲染后内容）打包为 ZIP
     *
     * @param generatedCode      模板渲染结果（key=template.name, value=内容）
     * @param fileNameTemplates  模板元数据（key=template.name, value=fileName 模板，可含 ${className} 占位符，可为 null）
     * @param groupByTemplate    模板元数据（key=template.name, value=group 名称）
     * @param zipFileName        最终 zip 文件名（不含后缀）
     * @param context            解析占位符的上下文（至少含 className、tableName）
     * @return zip 字节数组
     */
    byte[] buildZip(Map<String, String> generatedCode,
                    Map<String, String> fileNameTemplates,
                    Map<String, String> groupByTemplate,
                    String zipFileName,
                    Map<String, Object> context);
}
