package com.softdev.system.generator.service;

import com.softdev.system.generator.entity.dto.ClassInfo;
import com.softdev.system.generator.entity.dto.CodeGenResult;
import com.softdev.system.generator.entity.dto.ParamInfo;
import com.softdev.system.generator.entity.vo.ResultVo;

import java.util.Map;

/**
 * 代码生成服务接口
 *
 * @author zhengkai.blog.csdn.net
 */
public interface CodeGenService {

    /**
     * 生成代码（前端在线预览版本）
     *
     * @param paramInfo 参数信息
     * @return 生成的代码映射（key=模板名, value=渲染内容）
     * @throws Exception 生成过程中的异常
     */
    ResultVo generateCode(ParamInfo paramInfo) throws Exception;

    /**
     * 解析表结构（仅解析，不生成）
     *
     * @param paramInfo 参数信息
     * @return 类信息
     * @throws Exception 解析异常
     */
    ClassInfo parseTableStructure(ParamInfo paramInfo) throws Exception;

    /**
     * 根据参数获取结果（富信息版本，包含模板分组与 fileName 配置）
     *
     * @param params 参数映射（含 classInfo、tableName 等）
     * @return CodeGenResult
     * @throws Exception 处理过程中的异常
     */
    CodeGenResult getResultByParams(Map<String, Object> params) throws Exception;
}
