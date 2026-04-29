package com.example.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.music.model.domain.Consumer;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface ConsumerMapper extends BaseMapper<Consumer> {
    String selectConsumerNameById(Integer id);

    Integer selectIdByconsumerName(String username);
    
    /**
     * 查询指定日期范围内每天新增用户数
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日新增用户数统计
     */
    @Select("SELECT DATE(create_time) as date, COUNT(*) as count " +
           "FROM consumer " +
           "WHERE create_time >= #{startDate} AND create_time < #{endDate} " +
           "GROUP BY DATE(create_time) " +
           "ORDER BY date")
    List<Map<String, Object>> getWeeklyNewUsers(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
                                               
    /**
     * 按年龄段统计用户分布
     * @return 各年龄段用户数量
     */
    @Select("SELECT " +
           "CASE " +
           "  WHEN TIMESTAMPDIFF(YEAR, birth, CURDATE()) < 18 THEN '18岁以下' " +
           "  WHEN TIMESTAMPDIFF(YEAR, birth, CURDATE()) BETWEEN 18 AND 24 THEN '18-24岁' " +
           "  WHEN TIMESTAMPDIFF(YEAR, birth, CURDATE()) BETWEEN 25 AND 30 THEN '25-30岁' " +
           "  WHEN TIMESTAMPDIFF(YEAR, birth, CURDATE()) BETWEEN 31 AND 40 THEN '31-40岁' " +
           "  WHEN TIMESTAMPDIFF(YEAR, birth, CURDATE()) BETWEEN 41 AND 50 THEN '41-50岁' " +
           "  WHEN TIMESTAMPDIFF(YEAR, birth, CURDATE()) > 50 THEN '50岁以上' " +
           "  ELSE '未知' " +
           "END as ageRange, " +
           "COUNT(*) as count " +
           "FROM consumer " +
           "WHERE birth IS NOT NULL " +
           "GROUP BY ageRange")
    List<Map<String, Object>> getUserAgeDistribution();
}