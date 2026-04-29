package com.example.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.music.common.R;
//import com.example.yin.controller.MinioUploadController;
import com.example.music.controller.FileUploadController;
import com.example.music.mapper.AdminMapper;
import com.example.music.mapper.ConsumerMapper;
import com.example.music.mapper.SongMapper;
import com.example.music.model.domain.Song;
import com.example.music.model.request.SongRequest;
import com.example.music.service.SongService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService {

    @Autowired
    private SongMapper songMapper;

//    @Value("${minio.bucket-name}")
//    private String bucketName;
//
//    @Autowired
//    MinioClient minioClient;

    @Value("${local.file.storage-path}")
    private String storagePath;
    @Override
    public R allSong() {

        return R.success(null, songMapper.selectList(null));
    }
    @Autowired  // 注入 FileUploadController
    private FileUploadController fileUploadController;


    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private AdminMapper adminMapper;

    //上传歌曲
    @Override
    public R addSong(SongRequest addSongRequest, MultipartFile lrcfile,MultipartFile mpfile,HttpSession session){
        Song song = new Song();
        BeanUtils.copyProperties(addSongRequest, song);

//        if (session.getAttribute("adminId")!=null){
//            return R.error("请先登录");
//        }
        //String pic = "/img/songPic/tubiao.jpg";

        //安全检查
        String fileName = mpfile.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            return R.error("文件名不能为空");
        }
        if (mpfile.getSize() > 30 * 1024 * 1024) { // 30MB限制
            return R.error("文件大小不能超过30MB");
        }
        // 检查是否包含扩展名
        if (!fileName.contains(".")) {
            return R.error("文件必须包含扩展名");
        }
        // 提取并检查扩展名
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        String[] allowedExtensions = {"mp3", "wav", "flac", "aac", "ogg", "m4a"};
        if (!Arrays.asList(allowedExtensions).contains(fileExtension)) {
            return R.error("不支持的文件格式");
        }

        String s = fileUploadController.uploadFile(mpfile);
        //System.out.println("上传结果歌曲文件名 = " + s );
        String storeUrlPath = "/song/"+ s;
        song.setCreateTime(new Date());
        song.setUpdateTime(new Date());
        //song.setPic(pic);
        song.setUrl(storeUrlPath);
        song.setType(3);//歌曲状态 启用1 隐藏2 审核3 禁用4

        //System.out.println("上传结果歌曲文件的路径 = " + storeUrlPath);

        if (lrcfile!=null&&(song.getLyric().equals("[00:00:00]暂无歌词"))){
            byte[] fileContent = new byte[0];
            try {
                fileContent = lrcfile.getBytes();
                String content = new String(fileContent, "UTF-8");
                song.setLyric(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (songMapper.insert(song) > 0) {
            return R.success("上传成功");
        } else {
            return R.error("上传失败");
        }
    }


    @Override
    public R updateSongMsg(SongRequest updateSongRequest, HttpSession session) {
        // 验证用户权限
        //if (updateSongRequest.getSingerId() == consumerMapper.selectById((int)session.getAttribute("userId")).getSingerId() );
        Song song = new Song();
        BeanUtils.copyProperties(updateSongRequest, song);
        song.setType(3);
        // 歌曲状态: 启用1 隐藏2 审核3 禁用4
        if (songMapper.updateById(song) > 0) {
            return R.success("修改成功");
        } else {
            return R.error("修改失败");
        }
    }

    //跟新歌曲保存的路径
    @Override
    public R updateSongUrl(MultipartFile urlFile, int id) {
        Song song = songMapper.selectById(id);
        String path = song.getUrl();
        String[] parts = path.split("/");
        String fileName = parts[parts.length - 1];

        String filePath = storagePath + File.separator + fileName;

// 创建文件对象
        File fileToDelete = new File(filePath);

// 判断文件是否存在并尝试删除
//        if (fileToDelete.exists()) {
//            if (fileToDelete.delete()) {
//                // 删除成功
//                System.out.println("文件删除成功: " + filePath);
//            } else {
//                // 删除失败
//                throw new RuntimeException("无法删除文件，请检查文件权限或文件是否被占用");
//            }
//        } else {
//            throw new RuntimeException("文件不存在，无法删除");
//        }
        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
        String s = fileUploadController.uploadFile(urlFile);

        //String urlflie = urlFile.getOriginalFilename();
        String storeUrlPath = "/song/" + s ;
        song.setId(id);
        song.setUrl(storeUrlPath);
        song.setType(3);
        //歌曲状态 启用1 隐藏2 审核3 禁用4
        if (songMapper.updateById(song) > 0) {
            return R.success("更新成功", storeUrlPath);
        } else {
            return R.error("更新失败");
        }
    }

    //更新歌曲图片
    @Override
    public R updateSongPic(MultipartFile urlFile, int id) {


        //保存图片到本地
        String s = fileUploadController.uploadSongImgFile(urlFile);
        //保存图片到数据库
        String storeUrlPath = "/img/songPic/" + s;
        Song song = new Song();
        song.setId(id);
        song.setPic(storeUrlPath);
        song.setType(3);
        // 歌曲状态: 启用1 隐藏2 审核3 禁用4
        System.out.println("SongServiceImpl上传结果 s = " + storeUrlPath);
        if (songMapper.updateById(song) > 0) {
            System.out.println("数据库更新行数" + songMapper.updateById(song) );
            return R.success("上传成功", storeUrlPath);
        } else {
            return R.error("上传失败");
        }
    }

    @Override
    public R deleteSong(Integer id) {
        // 歌曲状态: 启用1 隐藏2 审核3 禁用4
        if (songMapper.deleteById(id) > 0) {
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }

    @Override
    public R songOfSingerId(Integer singerId, HttpSession session) {

        if (session.getAttribute("name") == null) {
            QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("singer_id", singerId);
            queryWrapper.eq("type", 1);
            // 歌曲状态: 启用1 隐藏2 审核3 禁用4
            return R.success(null, songMapper.selectList(queryWrapper));
        }

        QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("singer_id", singerId);
        return R.success(null, songMapper.selectList(queryWrapper));
    }

    @Override //获取用户歌曲
    public R songOfuserSingerId(HttpSession session) {
        try {
            if (session.getAttribute("userId") == null) {
                return R.error("请重新登录");
            }
            int userId = (int) session.getAttribute("userId");
            int singerId = consumerMapper.selectById(userId).getSingerId();
            QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("singer_id",singerId);
            return R.success(null, songMapper.selectList(queryWrapper));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public R songOfId(Integer id) {
        QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        return R.success(null, songMapper.selectList(queryWrapper));
    }

    @Override
    public R songOfSingerName(String name) {
        QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name",name);
        List<Song> songs = songMapper.selectList(queryWrapper);
        if (songs.isEmpty()){
            return R.error("添加失败，没有找到该歌,无法加入该歌单");
        }

        return R.success(null, songMapper.selectList(queryWrapper));
    }

    @Override
    public R updateSongLrc(MultipartFile lrcFile, int id) {
        Song song = songMapper.selectById(id);
        if (lrcFile!=null&&!(song.getLyric().equals("[00:00:00]暂无歌词"))){
            byte[] fileContent = new byte[0];
            try {
                fileContent = lrcFile.getBytes();
                String content = new String(fileContent, "UTF-8");
                song.setLyric(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // 歌曲状态: 启用1 隐藏2 审核3 禁用4
        song.setType(3);
        if (songMapper.updateById(song) > 0) {
            return R.success("更新成功");
        } else {
            return R.error("更新失败");
        }
    }


    //更新歌曲状态
    @Override
    public R updateSongStatus(int songId, int singerId,HttpSession session) {

        int userId = (int) session.getAttribute("userId");
        if (singerId != consumerMapper.selectById(userId).getSingerId())
            return R.error("请勿越权操作");

        // 查询歌曲是否存在
        Song song = songMapper.selectById(songId);
        if (song == null) {
            return R.error("歌曲不存在");
        }
        // 验证歌手ID是否匹配
        if (song.getSingerId() != singerId) {
            return R.error("歌手信息不匹配");
        }
        // 新建歌曲对象并设置状态为1（启用）
        song.setType(3); // 歌曲状态: 启用1 隐藏2 审核3 禁用4

        // 更新数据
        if (songMapper.updateById(song) > 0) {
            return R.success("歌曲状态更新成功");
        } else {
            return R.error("歌曲状态更新失败");
        }

    }

    //管理员更新歌曲状态
    @Override
    public R adminUpdateSongStatus(int songId, int type) {

        // 查询歌曲是否存在
        Song song = songMapper.selectById(songId);
        if (song == null) {
            return R.error("歌曲不存在");
        }

        // 验证状态值是否合法(1:启用, 2:隐藏, 3:审核, 4:禁用)
        if (type < 1 || type > 4) {
            return R.error("无效的歌曲状态值");
        }

        // 更新歌曲状态
        song.setType(type);
        song.setUpdateTime(new Date());

        if (songMapper.updateById(song) > 0) {
            return R.success("歌曲状态更新成功");
        } else {
            return R.error("歌曲状态更新失败");
        }
    }


    /**
     * 分页获取歌曲列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param name 歌曲名搜索关键字
     * @return 分页结果
     */
    @Override
    public R getPageSongs(Integer pageNum, Integer pageSize, String name) {
        Page<Song> page = new Page<>(pageNum, pageSize);// 创建分页对象
        QueryWrapper<Song> queryWrapper = new QueryWrapper<>();// 构造查询条件
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        // 只查询启用状态的歌曲(状态为1)或者根据业务需求调整
        // queryWrapper.eq("type", 1);
        queryWrapper.orderByDesc("update_time");// 按照更新时间倒序排列
        // 执行分页查询
        Page<Song> songPage = songMapper.selectPage(page, queryWrapper);
        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", songPage.getRecords());
        result.put("total", songPage.getTotal());
        result.put("size", songPage.getSize());
        result.put("current", songPage.getCurrent());
        return R.success(null, result);
    }



}
