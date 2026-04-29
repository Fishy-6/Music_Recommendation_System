package com.example.music.controller;

import com.example.music.common.R;
import com.example.music.model.domain.Song;
import com.example.music.service.RecommendService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/recommend")
public class RecommendController {

    @Resource
    private RecommendService recommendService;

    /**
     * 协同过滤推荐
     */
    @GetMapping("/data")
    public R getRecommendations(HttpSession session) {
        Integer currentUserId = (Integer) session.getAttribute("userId");
        int limit =10;
        if (currentUserId == null || currentUserId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空") ;
        }
        return R.success("推荐成功", recommendService.selectListByRecommend(currentUserId, limit));
    }

    /**
     * 为当前登录用户推荐
     * @param limit 推荐数量，默认为10
     * @return 推荐歌曲列表
     */
    @GetMapping("/current")
    public List<Song> getCurrentUserRecommendations(
            @RequestParam(defaultValue = "10") Integer limit, HttpSession session) {
        // 这里应该从安全上下文中获取当前登录用户的ID
        // 暂时用固定值演示，实际项目中需要替换为真实的用户ID获取逻辑
        Integer currentUserId = (Integer) session.getAttribute("userId");

        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }

        return recommendService.selectListByRecommend(currentUserId, limit);
    }


}