package com.example.music.controller;

import com.example.music.common.R;
import com.example.music.model.request.SongListRequest;
import com.example.music.service.SongListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;

@RestController
public class SongListController {

    @Autowired
    private SongListService songListService;


    // 添加歌单
    @PostMapping("/songList/add")
    public R addSongList(@RequestBody SongListRequest addSongListRequest) {
        return songListService.addSongList(addSongListRequest);
    }

    // 添加用户歌单
    @PostMapping("/songList/user/add")
    public R addUserSongList(@RequestBody SongListRequest addSongListRequest, HttpSession session) {
        return songListService.addUserSongList(addSongListRequest,session);
    }


    // 删除歌单
    @GetMapping("/songList/delete")
    public R deleteSongList(@RequestParam int id) {
        return songListService.deleteSongList(id);
    }

    //TODO 这块就是前端显现相应的歌单list
    // 返回所有歌单
    @GetMapping("/songList")
    public R allSongList() {
        return songListService.allSongList();
    }

    // 返回标题包含文字的歌单
    @GetMapping("/songList/likeTitle/detail")
    public R songListOfLikeTitle(@RequestParam String title) {
        return songListService.likeTitle('%' + title + '%');
    }

    // 返回指定类型的歌单
    @GetMapping("/songList/style/detail")
    public R songListOfStyle(@RequestParam String style) {
        return songListService.likeStyle('%' + style + '%');
    }

    // 更新歌单信息
    @PostMapping("/songList/update")
    public R updateSongListMsg(@RequestBody SongListRequest updateSongListRequest) {
        return songListService.updateSongListMsg(updateSongListRequest);

    }
    // 更新用户歌单信息
    @PostMapping("/songList/user/update")
    public R updateUserSongListMsg(@RequestBody SongListRequest updateSongListRequest, HttpSession  session) {
        return songListService.updateUserSongListMsg(updateSongListRequest,session);

    }

    // 更新歌单图片
    @PostMapping("/songList/img/update")
    public R updateSongListPic(@RequestParam("file") MultipartFile avatorFile, @RequestParam("id") int id) {
        return songListService.updateSongListImg(avatorFile,id);
    }

    // 分页查询歌单
    @GetMapping("/songList/page")
    public R songListByPage(@RequestParam(defaultValue = "1") Integer currentPage,
                            @RequestParam(defaultValue = "15") Integer pageSize) {
        return songListService.songListByPage(currentPage, pageSize);
    }

    // 根据用户者ID获取歌单
    @GetMapping("/songList/user/detail")
    public R getSongListByConsumerId(@RequestParam int id, HttpSession session) {
        return songListService.getSongListByConsumerId(session);
    }


    @GetMapping("/songList/user/all")
    public R getHotSongListsByRating() {
        return songListService.getHotSongListsByRating(3);
    }

    // 热门歌单
    @GetMapping("/songList/hot")
    public R getHotSongLists(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return songListService.getHotSongListsByRating(limit);
    }


}
