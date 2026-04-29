package com.example.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.music.model.domain.SongList;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository

public interface SongListMapper extends BaseMapper<SongList> {

    String selectSongListNameById(Long songListId);

     List<SongList> getSongListByConsumerId(int ConsumerId);

     int getSongListByConsumer(int ConsumerId);

    @Select("SELECT " +
            "    rl.song_list_id, " +
            "    AVG(rl.score) as avg_score, " +
            "    COUNT(rl.id) as rating_count " +
            "FROM " +
            "    rank_list rl " +
            "INNER JOIN " +
            "    song_list sl ON rl.song_list_id = sl.id " +
            "WHERE " +
            "    sl.type = 1 " +  // 只查询启用的歌单
            "GROUP BY " +
            "    rl.song_list_id " +
            "HAVING " +
            "    COUNT(rl.id) >= 1 " +  // 至少有1个评分
            "ORDER BY " +
            "    avg_score DESC, rating_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectHotSongListsByRating(@Param("limit") Integer limit);

    // 获取指定歌单的评分统计
    @Select("SELECT " +
            "    AVG(score) as avg_score, " +
            "    COUNT(id) as rating_count, " +
            "    MAX(score) as max_score, " +
            "    MIN(score) as min_score " +
            "FROM " +
            "    rank_list " +
            "WHERE " +
            "    song_list_id = #{songListId}")
    Map<String, Object> selectRatingStatsBySongListId(@Param("songListId") Integer songListId);
}
