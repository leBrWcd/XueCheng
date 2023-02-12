package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.po.MediaFiles;

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


    MediaFiles SaveMediaFileToDb(Long companyId, UploadFileParamsDto dto, String objectName, String bucket, String mediaFileId);

}
