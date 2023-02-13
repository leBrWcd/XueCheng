package com.xuecheng.media.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
@Slf4j
public class MediaFilesController {


    @Autowired
    MediaFileService mediaFileService;



    @ApiOperation("上传文件")
    @RequestMapping(value = "/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaFiles upload(@RequestPart("filedata") MultipartFile upload,
                            @RequestParam(value = "folder",required=false) String folder,
                            @RequestParam(value = "objectName",required=false) String objectName) throws IOException {
        log.info("multipart: {}",upload.toString());
        Long companyId = 123123L;
        UploadFileParamsDto paramsDto = new UploadFileParamsDto();
        paramsDto.setContentType(upload.getContentType());
        paramsDto.setFileSize(upload.getSize());
        paramsDto.setFilename(upload.getOriginalFilename());
        paramsDto.setRemark("");
        // Content-Type: image/png
        if (upload.getContentType().indexOf("image") >= 0) {
            //图片
            paramsDto.setFileType("001001");
        }else{
            //其它
            paramsDto.setFileType("001003");
        }
        return mediaFileService.upload(companyId,upload.getBytes(),paramsDto,folder,objectName);
    }


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams,
                                       @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 123123L;
        return mediaFileService.queryMediaFiels(companyId, pageParams, queryMediaParamsDto);

    }

}
