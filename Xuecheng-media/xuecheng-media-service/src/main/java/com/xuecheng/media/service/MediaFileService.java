package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import io.minio.errors.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams 分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


    /**
     * 文件上传 上传到minio  保存到数据库
     *
     * @param companyId 机构id
     * @param bytes 文件byte数组
     * @param dto dto
     * @param folder 文件夹名
     * @param objectName 文件名
     * @return 文件对象
     */
    MediaFiles upload(Long companyId, byte[] bytes, UploadFileParamsDto dto, String folder, String objectName);

    /**
     * 保存到数据库
     * @param companyId
     * @param dto
     * @param objectName
     * @param bucket
     * @param mediaFileId
     * @return
     */
    MediaFiles SaveMediaFileToDb(Long companyId, UploadFileParamsDto dto, String objectName, String bucket, String mediaFileId);

    /**
     * 检查文件是否存在于数据库和minio
     * @param fileMd5
     * @return
     */
    RestResponse<Boolean> checkfile(String fileMd5) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    /**
     * 检查分片文件是否存在
     * @param fileMd5
     * @param chunk
     * @return
     */
    RestResponse<Boolean> checkchunk(String fileMd5, int chunk);

    /**
     * 上传文件分块
     * @param bytes
     * @param fileMd5
     * @param chunk
     * @return
     */
    RestResponse uploadchunk(byte[] bytes, String fileMd5, int chunk);

    /**
     * 合并文件分块
     * @param
     * @param fileMd5
     * @param chunkTotal
     * @return
     */
    RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);
}
