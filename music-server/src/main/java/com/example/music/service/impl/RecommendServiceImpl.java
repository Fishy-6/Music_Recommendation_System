package com.example.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.music.common.R;
import com.example.music.mapper.ConsumerMapper;
import com.example.music.mapper.SongMapper;
import com.example.music.model.domain.Consumer;
import com.example.music.model.domain.Song;
import com.example.music.service.RecommendService;
import com.example.music.utils.RecommendUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.music.service.ConsumerService;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.*;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Resource
    private ConsumerMapper consumerMapper;
    @Resource
    private SongMapper songMapper;
    // 协同过滤推荐算法
    @Override
    public List<Song> selectListByRecommend(Integer userId, Integer limit) {
        // 参数验证
        if (userId == null || limit == null || limit <= 0) {
            throw new IllegalArgumentException("参数错误");
        }
        // 当前用户下标
        int userIndex = -1;
        // 用户数据集
        List<Consumer> userList = consumerMapper.selectList(new QueryWrapper<>());
        // 物品数据集（歌曲）
        List<Song> songList = songMapper.selectList(new QueryWrapper<>());

        if (userList.isEmpty() || songList.isEmpty()) {
            return  getFallbackRecommendations(limit);
        }
        // 用户、物品矩阵，行代表用户，列代表物品，值代表是否喜爱
        double[][] userRatings = new double[userList.size()][songList.size()];
        // 构建用户-物品矩阵
        for (int k = 0; k < userList.size(); k++) {
            Consumer user = userList.get(k);
            // 根据用户ID匹配，记录用户下标
            if (user.getId().equals(userId)) {
                userIndex = k;
            }
            // 获取用户的真实喜好列表
            List<Song> favoriteSongs = getRealFavoriteSongsByUserId(user.getId());
            // 用户物品数据集
            double[] userItem = new double[songList.size()];
            for (int i = 0; i < songList.size(); i++) {
                // 默认不喜爱标记为0
                userItem[i] = 0;
                for (Song favoriteSong : favoriteSongs) {
                    // 判断是否为喜爱物品
                    if (songList.get(i).getId().equals(favoriteSong.getId())) {
                        // 喜爱物品标记为1
                        userItem[i] = 1;
                        break;
                    }
                }
            }
            // 用户物品数据集赋值到矩阵
            userRatings[k] = userItem;
        }

        // 如果未找到目标用户
        if (userIndex == -1) {
            return getFallbackRecommendations(limit);
        }

        // 使用带异常热门惩罚因子的协同过滤算法计算推荐
        Set<Song> recommendSet = new LinkedHashSet<>();
        try {
            List<Integer> recommendedIndices = RecommendUtils.recommendItemsWithPopularityPenalty(userIndex, userRatings, limit);

            // 遍历物品下标数据集
            for (Integer index : recommendedIndices) {
                if (index >= 0 && index < songList.size()) {
                    recommendSet.add(songList.get(index));
                }
            }
        } catch (Exception e) {
            // 出现异常时，记录日志并返回备用推荐
            System.err.println("协同过滤推荐异常: " + e.getMessage());
            return getFallbackRecommendations(limit);
        }

        // 如果推荐数量不足，则补充默认推荐
        if (recommendSet.size() < limit) {
            List<Song> additionalSongs = getFallbackRecommendations(limit - recommendSet.size());
            recommendSet.addAll(additionalSongs);
        }

        System.out.println("为用户 " + userId + " 推荐结果，数量: " + recommendSet.size());
        return new ArrayList<>(recommendSet);
    }

    /**
     * 获取用户真实喜爱的歌曲列表（从数据库查询）
     */
    @Autowired
    private ConsumerService consumerService;
    private List<Song> getRealFavoriteSongsByUserId(Integer userId) {
        // TODO: 根据实际业务需求，从数据库查询用户收藏/喜欢的歌曲
        // 这里需要根据您的数据库结构来实现
        // 临时实现：查询用户收藏的歌曲
        List<Song> favoriteSongs = (List<Song>) consumerService.getRecentPlayHistory(userId).getData();
        return favoriteSongs;
        // 临时返回空列表，实际应该查询数据库
       // return Collections.emptyList();
    }

    /**
     * 获取备用推荐（最新歌曲）
     */
    private List<Song> getFallbackRecommendations(Integer limit) {
        System.out.println("使用备用推荐，数量: " + limit);
        return songMapper.selectList(
                new QueryWrapper<Song>()
                        .orderByDesc("create_time")
                        .last("LIMIT " + limit)
        );
    }

}