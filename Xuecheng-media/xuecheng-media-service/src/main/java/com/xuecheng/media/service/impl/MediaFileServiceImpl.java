package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.execption.XuechengException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Value("${minio.bucket.files}")
    String bucket_file;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto dto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(dto.getFilename() != null,MediaFiles::getFilename,dto.getFilename())
                .eq(dto.getFileType() != null,MediaFiles::getFileType,dto.getFileType())
                .eq(MediaFiles::getCompanyId,companyId);
        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        page = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = page.getRecords();
        // 获取数据总数
        long total = page.getTotal();
        if (total >= 1L) {
            // 构建结果集
            return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        }
        return null;
    }

    @Override
    public MediaFiles upload(Long companyId, byte[] bytes, UploadFileParamsDto dto, String folder, String objectName) {

        // 处理folder
        if (StringUtils.isEmpty(folder)) {
            // 规定：如果用户没有传文件夹名称，自动以日期生成目录
            folder = getFileFolder(new Date(), true, true, true);
        } else {
            // 如果用户输入了，需要给文件夹名称拼接上 '/'
            folder = folder + '/';
        }
        // 处理objectName
        // 获取文件名
        String filename = dto.getFilename();
        String file_md5 = DigestUtils.md5Hex(bytes);
        if (StringUtils.isEmpty(objectName)) {
            //规定：如果用户没有传对象名称，objectName = 文件md5 + 文件后缀
            objectName = file_md5 + filename.substring(filename.lastIndexOf('.'));
        }
        // mp4/jy.mp4
        objectName = folder + objectName;
        try {
            // ==================上传到minio========================
            uploadToMinio(bytes, objectName,bucket_file,dto.getContentType());
            // ==================保存到数据库========================
            // 事务生效需要由代理完成
            MediaFileService proxy = (MediaFileService) AopContext.currentProxy();
            return proxy.SaveMediaFileToDb(companyId, dto, objectName,bucket_file, file_md5);
            //return SaveMediaFileToDb(companyId,dto,objectName,bucket_file,file_md5);
        } catch (Exception e) {
            log.info("保存文件异常： {}" ,e.getMessage());
        }
        return null;
    }

    /**
     * 保存文件到数据库
     * @param companyId
     * @param dto
     * @param objectName
     * @param bucket
     * @param mediaFileId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @NotNull
    public MediaFiles SaveMediaFileToDb(Long companyId, UploadFileParamsDto dto, String objectName,String bucket, String mediaFileId) {
        // 先从数据库查询是否存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaFileId);
        if (mediaFiles == null) {
            // 新建
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(dto, mediaFiles);
            mediaFiles.setId(mediaFileId);
            mediaFiles.setFileId(mediaFileId);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");

            // 插入文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                XuechengException.cast("保存文件信息失败");
            }
        }
        return mediaFiles;
    }

    /**
     * 上传图片到minio
     * @param bytes
     * @param objectName
     * @param bucket
     * @param contentType
     * @throws ErrorResponseException
     * @throws InsufficientDataException
     * @throws InternalException
     * @throws InvalidKeyException
     * @throws InvalidResponseException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws ServerException
     * @throws XmlParserException
     */
    private void uploadToMinio(byte[] bytes, String objectName,String bucket,String contentType) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        // 转化为字节输入流
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        // minio 上传对象
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                // InputStream stream, long objectSize 对象大小 long partSize 分片大小 -1:5M 最大5T 最多10000
                .stream(inputStream, inputStream.available(), -1)
                .bucket(bucket)
                .contentType(contentType)
                .object(objectName)
                .build();
        minioClient.putObject(putObjectArgs);
    }

    /**
     * 根据日期拼接目录
     */
    private String getFileFolder(Date date, boolean year, boolean month, boolean day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //获取当前日期字符串
        String dateString = sdf.format(new Date());
        //取出年、月、日
        String[] dateStringArray = dateString.split("-");
        StringBuffer folderString = new StringBuffer();
        if (year) {
            folderString.append(dateStringArray[0]);
            folderString.append("/");
        }
        if (month) {
            folderString.append(dateStringArray[1]);
            folderString.append("/");
        }
        if (day) {
            folderString.append(dateStringArray[2]);
            folderString.append("/");
        }
        return folderString.toString();
    }

}
