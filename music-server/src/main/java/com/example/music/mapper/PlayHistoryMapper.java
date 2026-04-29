package com.example.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.music.model.domain.PlayHistory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface PlayHistoryMapper extends BaseMapper<PlayHistory> {
    
    /**
     * 查询指定日期范围内的每日活跃用户数
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日活跃用户数统计
     */
    @Select("SELECT DATE(play_time) as date, COUNT(DISTINCT user_id) as count " +
           "FROM play_history " +
           "WHERE play_time >= #{startDate} AND play_time < #{endDate} " +
           "GROUP BY DATE(play_time) " +
           "ORDER BY date")
    List<Map<String, Object>> getDailyActiveUsers(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
                                                  
    /**
     * 查询播放量前N的歌曲
     * @param limit 限制数量
     * @return 歌曲播放量统计
     */
    @Select("SELECT s.name as songName, COUNT(ph.song_id) as playCount " +
           "FROM play_history ph " +
           "JOIN song s ON ph.song_id = s.id " +
           "GROUP BY ph.song_id, s.name " +
           "ORDER BY playCount DESC " +
           "LIMIT #{limit}")
    List<Map<String, Object>> getTopSongsByPlayCount(@Param("limit") int limit);
}