package com.uniplore.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 目录文件列表响应VO
 * <p>
 * 返回当前目录信息、子目录列表和文件列表，供前端目录树展示。
 * </p>
 *
 * @author yf
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectoryVO {
    /**
     * 当前目录信息
     */
    private FileDirectory currentDir;
    /**
     * 子目录列表（已按 sort 正序排列）
     */
    private List<FileDirectory> subDirs;
    /**
     * 文件列表（已按 create_time 倒序排列）
     */
    private List<FileInfo> files;
}
