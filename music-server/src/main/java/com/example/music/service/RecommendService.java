package com.example.music.service;

import com.example.music.model.domain.Song;

import javax.servlet.http.HttpSession;
import java.util.List;

public interface RecommendService {
    // 协同过滤推荐算法
    List<Song> selectListByRecommend(Integer userId, Integer limit);
}
