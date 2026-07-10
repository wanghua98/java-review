package com.uniplore.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.uniplore.pojo.FileChunk;
import com.uniplore.result.Result;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 分片service接口
 *
 * @author yf
 */
public interface FileChunkService extends IService<FileChunk> {


    /**
     * 保存分片
     *
     * @param fileChunk 分片文件信息
     * @param file      分片文件
     * @return 保存结果
     */
    Result<String> saveChunk(FileChunk fileChunk, MultipartFile file) throws IOException;
}
