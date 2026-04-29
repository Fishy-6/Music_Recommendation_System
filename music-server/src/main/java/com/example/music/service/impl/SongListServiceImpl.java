package com.example.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.music.common.R;
//import com.example.yin.controller.MinioUploadController;
import com.example.music.controller.FileUploadController;
import com.example.music.mapper.ConsumerMapper;
import com.example.music.mapper.SongListMapper;
import com.example.music.model.domain.SongList;
import com.example.music.model.request.ListSongRequest;
import com.example.music.model.request.SongListRequest;
import com.example.music.service.SongListService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SongListServiceImpl extends ServiceImpl<SongListMapper, SongList> implements SongListService {

    @Autowired
    private SongListMapper songListMapper;
//    @Value("${minio.bucket-name}")
//    String bucketName;
    @Value("${local.file.storage-path}")
    private String storagePath;
    @Autowired
    private ConsumerMapper consumerMapper;

    @Override
    public R updateSongListMsg(SongListRequest updateSongListRequest) {

        SongList songList = new SongList();
        BeanUtils.copyProperties(updateSongListRequest, songList);
        if (songListMapper.updateById(songList) > 0) {
            return R.success("修改成功");
        } else {
            return R.error("修改失败");
        }
    }


    @Override
    public R updateUserSongListMsg(SongListRequest updateSongListRequest, HttpSession session) {

        String consumerName = (String) session.getAttribute("username");
        SongList songList = new SongList();
        BeanUtils.copyProperties(updateSongListRequest, songList);
        songList.setConsumer(consumerMapper.selectIdByconsumerName(consumerName));
        songList.setType(2);
        //启用1、审核中2, 禁用3
        if (songListMapper.updateById(songList) > 0) {
            return R.success("修改成功");
        } else {
            return R.error("修改失败");
        }
    }



    @Override
    public R deleteSongList(Integer id) {
        if (songListMapper.deleteById(id) > 0) {
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }

    @Override
    public R allSongList() {
        return R.success(null, songListMapper.selectList(null));
    }

    @Override
    public List<SongList> findAllSong() {
        List<SongList> songLists = songListMapper.selectList(null);
        return songLists;
    }


    @Override
    public R likeTitle(String title) {
        QueryWrapper<SongList> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("title",title);
        queryWrapper.eq("type", 1);
        return R.success(null, songListMapper.selectList(queryWrapper));
    }

    @Override
    public R likeStyle(String style) {
        QueryWrapper<SongList> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("style",style);
        queryWrapper.eq("type", 1);
        return R.success(null, songListMapper.selectList(queryWrapper));
    }

    // 在 SongListServiceImpl.java 中添加分页查询实现
    @Override
    public R songListByPage(Integer currentPage, Integer pageSize) {
        try {
            // 构建分页对象
            Page<SongList> page = new Page<>(currentPage, pageSize);
            // 构建查询条件
            LambdaQueryWrapper<SongList> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SongList::getType, 1);
            //启用1、审核中2, 禁用3
            // 按ID倒序排列
            queryWrapper.orderByDesc(SongList::getId);
            // 执行分页查询
            IPage<SongList> songListPage = songListMapper.selectPage(page, queryWrapper);
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("records", songListPage.getRecords());  // 当前页数据
            result.put("total", songListPage.getTotal());      // 总记录数
            result.put("size", songListPage.getSize());        // 每页大小
            result.put("current", songListPage.getCurrent());  // 当前页码
            result.put("pages", songListPage.getPages());      // 总页数

            return R.success("查询成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("查询失败: " + e.getMessage());
        }
    }


    // 获取用户歌单
    @Override
    public R getSongListByConsumerId(HttpSession session) {
        try {
            int id = (int)session.getAttribute("userId");
            // 使用QueryWrapper明确指定查询字段，避免重复列
            QueryWrapper<SongList> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("consumer", id);
            List<SongList> songList = songListMapper.selectList(queryWrapper);
            return R.success("查询成功", songList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }



    @Override
    public R addSongList(SongListRequest addSongListRequest) {
        SongList songList = new SongList();
        BeanUtils.copyProperties(addSongListRequest, songList);
        String pic = "/img/songListPic/123.jpg";
        songList.setPic(pic);
        songList.setType(3);//启用1、审核中2, 禁用3
        songList.setConsumer(1);//管理员添加
        if (songListMapper.insert(songList) > 0) {
            return R.success("添加成功");
        } else {
            return R.error("添加失败");
        }
    }
    @Override
    public R addUserSongList(SongListRequest addSongListRequest,HttpSession session) {
        String consumerName = (String) session.getAttribute("username");
        if (consumerName == null)
            return R.error("请先登录");
        SongList songList = new SongList();
        BeanUtils.copyProperties(addSongListRequest, songList);
        String pic = "/img/songlist/douyin.png";
        songList.setPic(pic);
        songList.setConsumer(consumerMapper.selectIdByconsumerName(consumerName));
        songList.setType(2);
        if (songListMapper.insert(songList) > 0) {
            return R.success("添加成功");
        } else {
            return R.error("添加失败");
        }
    }



    @Autowired  // 注入 FileUploadController
    private FileUploadController fileUploadController;

    //更新歌单图片
    @Override
    public R updateSongListImg(MultipartFile avatorFile, @RequestParam("id") int id) {

        String s = fileUploadController.uploadSonglistImgFile(avatorFile);
        String fileName =s;
        String imgPath = "/img/songlist/" + fileName;
        SongList songList = new SongList();
        songList.setId(id);
        songList.setPic(imgPath);
        // 查询该歌单对应的 consumer
        SongList existingSongList = songListMapper.selectById(id);
        if (existingSongList != null) {
            songList.setConsumer(existingSongList.getConsumer());
        } else {
            return R.error("歌单不存在");
        }
        if (songListMapper.updateById(songList) > 0) {
            return R.success("上传成功", imgPath);
        } else {
            return R.error("上传失败");
        }
    }


    //获取热门歌单（评分）
    @Override
    public R getHotSongListsByRating(Integer limit) {
        try {
            // 如果 limit 为 null，设置默认值
            if (limit == null || limit <= 0) {
                limit = 10; // 默认返回前10个热门歌单
            }
            // 使用自定义SQL查询获取平均评分最高的歌单
            List<Map<String, Object>> hotSongLists = songListMapper.selectHotSongListsByRating(limit);
            // 将结果转换为 SongList 对象列表
            List<SongList> result = new ArrayList<>();
            for (Map<String, Object> map : hotSongLists) {
                // 修复类型转换问题
                Object songListIdObj = map.get("song_list_id");
                Integer songListId = null;
                // 处理不同类型的 song_list_id
                if (songListIdObj instanceof Long) {
                    songListId = ((Long) songListIdObj).intValue();
                } else if (songListIdObj instanceof Integer) {
                    songListId = (Integer) songListIdObj;
                } else if (songListIdObj instanceof String) {
                    songListId = Integer.parseInt((String) songListIdObj);
                }
                if (songListId == null) {
                    continue; // 跳过无效的记录
                }
                // 获取完整的歌单信息
                SongList songList = songListMapper.selectById(songListId);
                if (songList != null) {
                    // 设置评分信息到歌单对象中
                    Object avgScoreObj = map.get("avg_score");
                    Object ratingCountObj = map.get("rating_count");
                    // 可以在这里添加评分信息到歌单的扩展属性中
                    // 由于SongList实体类可能没有这些字段，我们可以创建一个DTO或者使用Map返回
                    result.add(songList);
                }
            }
            return R.success("获取热门歌单成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("获取热门歌单失败: " + e.getMessage());
        }
    }


}
