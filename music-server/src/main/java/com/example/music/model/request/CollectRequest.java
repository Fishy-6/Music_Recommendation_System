package com.example.music.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class CollectRequest {
    private Integer id;

    private Integer userId;

    //0:歌曲 1:歌单
    private Byte type;

    private Integer songId;

    private Integer songListId;

    private Date createTime;
}
