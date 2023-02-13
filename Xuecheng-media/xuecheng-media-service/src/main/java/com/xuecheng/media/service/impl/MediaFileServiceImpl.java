package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.execption.XuechengException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author lebrwcd
 * @version 1.0
 * @description 媒资管理服务
 * @date 2023/2/10 8:58
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

    @Value("${minio.bucket.videofiles}")
    String bucket_video_file;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto dto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        if (!StringUtils.isEmpty(dto.getFilename())) {
            queryWrapper.like(MediaFiles::getFilename, dto.getFilename());
        }
        if (!StringUtils.isEmpty(dto.getFileType())) {
            queryWrapper.eq(MediaFiles::getFileType, dto.getFileType());
        }
        queryWrapper.eq(MediaFiles::getCompanyId, companyId);
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
            uploadToMinio(bytes, objectName, bucket_file, dto.getContentType());
            // ==================保存到数据库========================
            // 事务生效需要由代理完成
            MediaFileService proxy = (MediaFileService) AopContext.currentProxy();
            return proxy.SaveMediaFileToDb(companyId, dto, objectName, bucket_file, file_md5);
            //return SaveMediaFileToDb(companyId,dto,objectName,bucket_file,file_md5);
        } catch (Exception e) {
            log.info("保存文件异常： {}", e.getMessage());
        }
        return null;
    }

    /**
     * 保存文件到数据库
     *
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
    public MediaFiles SaveMediaFileToDb(Long companyId, UploadFileParamsDto dto, String objectName, String bucket, String mediaFileId) {
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
            mediaFiles.setFilePath(objectName);
            mediaFiles.setAuditStatus("002003");

            // 插入文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                XuechengException.cast("保存文件信息失败");
            }
        }
        return mediaFiles;
    }

    @Override
    public RestResponse<Boolean> checkfile(String fileMd5) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            String bucket = mediaFiles.getBucket();
            String objectName = mediaFiles.getFilePath();
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            if (stream != null) {
                // 文件已存在
                return RestResponse.success(true);
            }
        }
        // 文件不存在
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkchunk(String fileMd5, int chunk) {
        // 获得分块文件的目录 /1/f/chunk
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 获得分块文件的路径 /1/f/chunk/0
        String chunkFile = chunkFileFolderPath + chunk;

        InputStream inputStream = null;
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket_video_file)
                    .object(chunkFile)
                    .build();
            inputStream = minioClient.getObject(getObjectArgs);
            if (inputStream != null) {
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            log.error("异常信息： {}", e.getMessage());
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadchunk(byte[] bytes, String fileMd5, int chunk) {
        // 获得分块文件的目录 /1/f/chunk
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 获得分块文件的路径 /1/f/chunk/0
        String chunkFile = chunkFileFolderPath + chunk;
        // 将文件上传到minio
        try {
            uploadToMinio(bytes, chunkFile, bucket_video_file, "application/octet-stream");
        } catch (Exception e) {
            log.error("分片上传异常: {}", e.getMessage());
            XuechengException.cast("视频分片上传异常");
        }
        return RestResponse.success();
    }

    /**
     * 合并分块前要检查分块文件是否全部上传完成，如果完成则将已经上传的分块文件下载下来，然后再进行合并
     *
     * @param
     * @param fileMd5
     * @param chunkTotal
     * @return
     */
    @Override
    public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto) {

        // 下载所有分块
        File[] chunkFiles = downloadChunk(fileMd5, chunkTotal);
        // 合并分块
        // 新建临时合并分块文件
        String filename = uploadFileParamsDto.getFilename();
        String extensionName = filename.substring(filename.lastIndexOf("."));
        File mergeTempFile = null;
        RandomAccessFile read_random = null;
        try {
            mergeTempFile = File.createTempFile("merge", extensionName);
            // 创建合并文件的流对象
            RandomAccessFile writeRandom = new RandomAccessFile(mergeTempFile,"rw");
            // 依次读取分块文件，向合并文件写数据
            for (File chunkFile : chunkFiles) {
                // 读取每一个分块文件
                read_random = new RandomAccessFile(chunkFile,"r");
                byte[] bytes = new byte[1024];
                int len = -1;
                while ((len = read_random.read(bytes)) != -1) {
                    writeRandom.write(bytes,0,len);
                }
            }
        } catch (IOException e) {
            XuechengException.cast("合并文件失败");
            e.printStackTrace();
        } finally {
            if (read_random != null) {
                try {
                    read_random.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        log.debug("合并文件完成{}",mergeTempFile.getAbsolutePath());
        uploadFileParamsDto.setFileSize(mergeTempFile.length());

        // 校验 合并前的和合并后的md5是否相同
        FileInputStream mergeFileStream = null;
        try {
            mergeFileStream = new FileInputStream(mergeTempFile);
            try {
                if (DigestUtils.md5Hex(fileMd5).equals(DigestUtils.md5Hex(mergeFileStream))) {
                    System.out.println("合并成功!");
                }
            } catch (IOException e) {
                XuechengException.cast("合并文件校验不通过!");
            }
        } catch (FileNotFoundException e) {
            XuechengException.cast("创建合并文件吧");
            e.printStackTrace();
        }
        String mergeTempPath = getFilePathByMd5(fileMd5,extensionName);
        try {
            // 将文件上传到Minio
            uploadToMinio(mergeTempFile.getAbsolutePath(),mergeTempPath,bucket_video_file);
            // 存入数据库
            MediaFiles mediaFiles = SaveMediaFileToDb(companyId, uploadFileParamsDto, mergeTempPath, bucket_video_file, fileMd5);
            if (mediaFiles == null) {
                XuechengException.cast("媒资文件入库出错");
            }
            return RestResponse.success();
        }catch (Exception e) {
            XuechengException.cast("文件上传到Minio出错");
            e.printStackTrace();
        } finally {
            //删除临时文件
            for (File file : chunkFiles) {
                try {
                    file.delete();
                } catch (Exception e) {

                }
            }
            try {
                mergeTempFile.delete();
            } catch (Exception e) {

            }
        }
        return null;
    }

    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }


    /**
     * 下载某文件的所有分块文件
     * @param fileMd5 视频md5
     * @param chunkTotal 分块总数
     * @return 分块文件列表
     */
    private File[] downloadChunk(String fileMd5, int chunkTotal) {

        // 获得分块文件的目录 /1/f/chunk
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 返回结果
        File[] chunkFiles = new File[chunkTotal];
        for (int i = 0; i < chunkTotal; i++) {
            // 获得每个分块
            String chunkPath = chunkFileFolderPath + i;
            // 下载每个分块，需要在本地上创建临时文件，存放分块
            File chunkTempFile = null;
            try {
                chunkTempFile = File.createTempFile("chunk" + i, null);
            } catch (IOException e) {
                e.printStackTrace();
                XuechengException.cast("下载分块时创建临时文件出错");
            }
            // 开始下载
            chunkFiles[i] = downloadChunkFromMinio(chunkPath, bucket_video_file, chunkTempFile);
        }
        return chunkFiles;
    }

    /**
     * 从Minio下载分块文件
     * @param chunkPath 分块文件所在路径
     * @param bucket 桶名称
     * @param chunkTempFile 本地临时分块文件位置
     * @return 分块文件
     */
    private File downloadChunkFromMinio(String chunkPath, String bucket,File chunkTempFile) {
        GetObjectArgs objectArgs = GetObjectArgs.builder().object(chunkPath).bucket(bucket).build();
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = minioClient.getObject(objectArgs);
            outputStream = new FileOutputStream(chunkTempFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            XuechengException.cast("查询分块文件出错!");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return chunkTempFile;
    }

    /**
     * 上传图片到minio
     *
     * @param bytes
     * @param objectName
     * @param bucket
     * @param contentType
     * @throws ErrorResponseException
     */
    private void uploadToMinio(byte[] bytes, String objectName, String bucket, String contentType) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
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

    private void uploadToMinio(String filePath, String objectName, String bucket) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        // minio 上传对象
        try {
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .object(objectName)
                    .bucket(bucket)
                    .filename(filePath)
                    .build());
        } catch (Exception e) {
            log.error("文件上传到minio失败，异常信息：{}",e.getMessage());
            XuechengException.cast("文件上传到minio失败!");
        }
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

    /**
     * 得到分块文件的目录
     */
    private String getChunkFileFolderPath(String fileMd5) {
        // 分块文件规律 /1/f/1fasdsadasdasdasdas.mp4
        // 分块文件规律 /1/f/chunk
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

}
