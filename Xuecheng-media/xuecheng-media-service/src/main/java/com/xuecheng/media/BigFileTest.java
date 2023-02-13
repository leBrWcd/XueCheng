package com.xuecheng.media;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
*Description 上传大文件视频测试 - 视频分块/合并
*@author lebrwcd
*@date 2023/2/13
*@version 1.0
 *
 * 1、获取源文件长度
 * 2、根据设定的分块文件的大小计算出块数
 * 3、从源文件读数据依次向每一个块文件写数据
*/
@Slf4j
public class BigFileTest {

    public static void main(String[] args) throws IOException {
        //block();
        //merge();
        noticeTime();
    }
    public static void noticeTime() {

        List<Integer> list = Arrays.asList(5,3,1);
        list.add(6);
        list.add(0,4);
        list.remove(1);
        list.stream().forEach( e-> {
            System.out.println(e);
        });
    }


    /**
     * 视频文件分块
     * @throws IOException
     */
    public static void block() throws IOException {
        // 1.获取源文件长度  源文件
        File sourceFile = new File("D:\\video\\Hero.mp4");
        // 分块文件存储路径
        String chunkPath = "D:\\video\\chunk\\";
        File chunkFilePath = new File(chunkPath);
        if (!chunkFilePath.exists()) {
            chunkFilePath.mkdirs();
        }
        // 分块大小 1M
        int chunkSize = 1024 * 1024 * 1;
        // 2.根据设定的分块文件的大小计算出块数
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        
        // 3.从源文件读数据依次向每一个分块文件写数据
        // 使用流对象读取源文件，向分块文件写数据
        RandomAccessFile read_random = new RandomAccessFile(sourceFile,"r");

        // 缓冲区
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            // 分块文件名称
            File file = new File(chunkPath + i);
            if (file.exists()) {
                // 清除之前的分块
                file.delete();
            }
            boolean newFile = file.createNewFile();
            if (newFile) {
                // 开始写
                RandomAccessFile write_random = new RandomAccessFile(file,"rw");
                int len = -1;
                while ((len = read_random.read(bytes)) != -1) {
                    write_random.write(bytes,0,len);
                    log.info("完成分块 {} ",i);
                    // 达到分块大小不再写
                    if (file.length() >= chunkSize) {
                        break;
                    }
                }
                write_random.close();
            }
        }
        read_random.close();
    }


    /**
     * 视频分块文件合并
     * @throws IOException
     */
    public static void merge() throws IOException {

        // 源文件
        File sourceFile = new File("D:\\video\\Hero.mp4");

        // 获取分块文件
        String chunkPath = "D:\\video\\chunk\\";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        // 新建合并文件
        File mergeFile = new File("D:\\video\\Hero01.mp4");
        boolean newFile = mergeFile.createNewFile();

        // 分块文件规律：分块后的文件是按升序排列的，往合并文件写也要按序写
        File[] chunkFiles = chunkFolder.listFiles();
        List<File> chunkFilesList = Arrays.asList(chunkFiles);
        Collections.sort(chunkFilesList, Comparator.comparingInt(e -> Integer.parseInt(e.getName())));
        // 创建合并文件的流对象
        RandomAccessFile write_random = new RandomAccessFile(mergeFile,"rw");
        // 依次读取分块文件，向合并文件写数据
        for (File chunkFile : chunkFiles) {
            // 读取每一个分块文件
            RandomAccessFile read_random = new RandomAccessFile(chunkFile,"r");
            byte[] bytes = new byte[1024];
            int len = -1;
            while ((len = read_random.read(bytes)) != -1) {
                write_random.write(bytes,0,len);
            }
        }
        // 校验 合并前的和合并后的md5是否相同
        FileInputStream sourceFileStream = new FileInputStream(sourceFile);
        FileInputStream mergeFileStream = new FileInputStream(mergeFile);
        if (DigestUtils.md5Hex(sourceFileStream).equals(DigestUtils.md5Hex(mergeFileStream))) {
            System.out.println("合并成功!");
        }
    }


}
