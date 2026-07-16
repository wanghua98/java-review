package com.uniplore.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 目录文件列表响应VO
 * <p>
 * 返回当前目录信息、子目录列表和分页后的文件列表。
 * 文件列表支持分页，子目录数量少所以一次返回全部。
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
     * 子目录列表（已按 sort 正序排列，全部返回不分页）
     */
    private List<FileDirectory> subDirs;
    /**
     * 文件列表（已按 create_time 倒序排列，当前页数据）
     */
    private List<FileInfo> files;

    // ====== 分页信息 ======

    /**
     * 当前页码（从 1 开始）
     */
    private Integer currentPage;
    /**
     * 每页条数
     */
    private Integer pageSize;
    /**
     * 总记录数
     */
    private Long totalCount;
    /**
     * 总页数
     */
    private Integer totalPages;
}
