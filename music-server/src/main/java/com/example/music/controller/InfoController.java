package com.example.music.controller;

import com.example.music.common.R;
import com.example.music.mapper.ConsumerMapper;
import com.example.music.mapper.PlayHistoryMapper;
import com.example.music.mapper.SongMapper;
import com.example.music.service.ConsumerService;
import com.example.music.service.SingerService;
import com.example.music.service.SongService;
import com.example.music.service.SongListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@RestController
@RequestMapping("/info")
public class InfoController {

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private SongService songService;

    @Autowired
    private SingerService singerService;

    @Autowired
    private SongListService songListService;
    
    @Autowired
    private PlayHistoryMapper playHistoryMapper;
    
    @Autowired
    private ConsumerMapper consumerMapper;
    
    @Autowired
    private SongMapper songMapper;

    /**
     * 获取主页统计信息
     *
     * @return 包含用户数、歌曲数、歌手数、歌单数的统计信息
     */
    @GetMapping("/home/count")
    public R getHomeCount() {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("userCount", consumerService.count());
        resultMap.put("songCount", songService.count());
        resultMap.put("singerCount", singerService.count());
        resultMap.put("songListCount", songListService.count());
        return R.success(null, resultMap);
    }

    /**
     * 获取最近30天用户活跃趋势数据
     *
     * @return 最近30天每天的活跃用户数
     */
    @GetMapping("/user/activity")
    public R getRecentUserActivity() {
        List<Map<String, Object>> activityData = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDateTime endDate = now.atStartOfDay().plusDays(1); // 今天结束时间
        LocalDateTime startDate = now.minusDays(29).atStartOfDay(); // 30天前开始时间
        // 查询最近30天每天的活跃用户数
        List<Map<String, Object>> dbResults = playHistoryMapper.getDailyActiveUsers(startDate, endDate);
        // 创建一个map方便查找
        Map<String, Integer> resultMap = new HashMap<>();
        for (Map<String, Object> row : dbResults) {
            String dateStr = row.get("date").toString();
            Integer count = ((Long) row.get("count")).intValue();
            resultMap.put(dateStr, count);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        // 填充每天的数据
        for (int i = 29; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ISO_DATE); // yyyy-MM-dd格式
            String displayDate = date.format(formatter); // MM-dd格式
            
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", displayDate);
            dataPoint.put("count", resultMap.getOrDefault(dateStr, 0));
            
            activityData.add(dataPoint);
        }
        return R.success(null, activityData);
    }

    /**
     * 获取播放量前10的歌曲
     *
     * @return 播放量前10的歌曲及其播放次数
     */
    @GetMapping("/song/top10")
    public R getTop10Songs() {
        // 从数据库查询播放量前10的歌曲
        List<Map<String, Object>> topSongs = playHistoryMapper.getTopSongsByPlayCount(10);
        return R.success(null, topSongs);
    }

    /**
     * 获取新增内容趋势数据
     *
     * @return 最近一周每日新增的用户、歌曲数量
     */
    @GetMapping("/content/trend")
    public R getNewContentTrend() {
        Map<String, Object> trendData = new HashMap<>();
        
        // 计算本周第一天(周一)和最后一天(周日)
        LocalDate today = LocalDate.now();
        LocalDateTime weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                                          .atStartOfDay();
        LocalDateTime weekEndDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                                        .atStartOfDay()
                                        .plusDays(1);
        
        // 获取一周的日期列表 (周一到周日)
        List<String> dates = new ArrayList<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.CHINESE);
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStartDate.toLocalDate().plusDays(i);
            dates.add(dayFormatter.format(date));
        }
        trendData.put("dates", dates);
        
        // 查询每日新增用户数
        List<Map<String, Object>> weeklyNewUsers = consumerMapper.getWeeklyNewUsers(weekStartDate, weekEndDate);
        Map<String, Integer> userResultMap = new HashMap<>();
        for (Map<String, Object> row : weeklyNewUsers) {
            String dateStr = row.get("date").toString();
            Integer count = ((Long) row.get("count")).intValue();
            userResultMap.put(dateStr, count);
        }
        
        // 查询每日新增歌曲数
        List<Map<String, Object>> weeklyNewSongs = songMapper.getWeeklyNewSongs(weekStartDate, weekEndDate);
        Map<String, Integer> songResultMap = new HashMap<>();
        for (Map<String, Object> row : weeklyNewSongs) {
            String dateStr = row.get("date").toString();
            Integer count = ((Long) row.get("count")).intValue();
            songResultMap.put(dateStr, count);
        }
        
        // 填充每日新增数据
        List<Integer> newUser = new ArrayList<>();
        List<Integer> newSong = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStartDate.toLocalDate().plusDays(i);
            String dateStr = date.format(DateTimeFormatter.ISO_DATE);
            
            newUser.add(userResultMap.getOrDefault(dateStr, 0));
            newSong.add(songResultMap.getOrDefault(dateStr, 0));
        }
        
        trendData.put("newUser", newUser);
        trendData.put("newSong", newSong);
        
        return R.success(null, trendData);
    }

    /**
     * 获取用户年龄分布数据
     *
     * @return 各年龄段用户数量分布
     */
    @GetMapping("/user/age/distribution")
    public R getUserAgeDistribution() {
        // 从数据库查询用户年龄分布
        List<Map<String, Object>> dbResults = consumerMapper.getUserAgeDistribution();
        
        List<Map<String, Object>> ageData = new ArrayList<>();
        for (Map<String, Object> row : dbResults) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("name", row.get("ageRange"));
            dataPoint.put("value", ((Long) row.get("count")).intValue());
            ageData.add(dataPoint);
        }
        
        return R.success(null, ageData);
    }
}