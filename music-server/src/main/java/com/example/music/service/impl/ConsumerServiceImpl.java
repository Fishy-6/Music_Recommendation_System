package com.example.music.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.music.common.R;
import com.example.music.controller.FileUploadController;
import com.example.music.controller.UserSingerController;
import com.example.music.mapper.*;
import com.example.music.model.domain.*;
import com.example.music.model.request.ConsumerRequest;
import com.example.music.model.request.SingerRequest;
import com.example.music.service.ConsumerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.music.constant.Constants.SALT;

@Service
public class ConsumerServiceImpl extends ServiceImpl<ConsumerMapper, Consumer>
        implements ConsumerService {

    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private SingerMapper singerMapper;

    @Autowired
    private AdminMapper adminMapper;


    @Override
    public R logout(HttpSession session){
        session.removeAttribute("username");
        session.removeAttribute("userId");
        return R.success("注销成功");
    }

    /**
     * 新增用户
     */
    //实现 addUser 方法，调用 ConsumerMapper 进行数据库操作
    @Override
    public R addUser(ConsumerRequest registryRequest) {
        if (this.existUser(registryRequest.getUsername())) {
            return R.warning("用户名已注册");
        }
        Consumer consumer = new Consumer();
        BeanUtils.copyProperties(registryRequest, consumer);
        //MD5加密
        String password = DigestUtils.md5DigestAsHex((SALT + registryRequest.getPassword()).getBytes(StandardCharsets.UTF_8));
        consumer.setPassword(password);
        //都用用
        if (StringUtils.isBlank(consumer.getPhoneNum())) {
            consumer.setPhoneNum(null);
        }
        if ("".equals(consumer.getEmail())) {
            consumer.setEmail(null);
        }
        consumer.setAvator("img/avatorImages/user.jpg");
        try {
            QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email",consumer.getEmail());
            Consumer one = consumerMapper.selectOne(queryWrapper);
            if (one!=null){
                return R.fatal("邮箱不允许重复");
            }
            if (consumerMapper.insert(consumer) > 0) {
                return R.success("注册成功");
            } else {
                return R.error("注册失败");
            }
        } catch (DuplicateKeyException e) {
            return R.fatal(e.getMessage());
        }
    }

    //修改用户信息
    @Override
    public R updateUserMsg(ConsumerRequest updateRequest) {
        Consumer consumer = new Consumer();
        BeanUtils.copyProperties(updateRequest, consumer);
            // 开始事务处理
            try {
                // 更新用户头像
                if (consumerMapper.updateById(consumer) > 0) {
                    // 查询该用户关联的歌手ID
                    Consumer user = consumerMapper.selectById(consumer.getId());
                    // 如果用户是歌手，同时更新歌手的图片
                    if (user != null && user.getSingerId() != null) {

                        Singer singer = new Singer();
                        singer.setId(user.getSingerId());
                        singer.setName(user.getUsername());
                        singer.setBirth(user.getBirth());
                        singer.setSex(user.getSex());
                        singer.setLocation(user.getLocation());
                        singer.setIntroduction(user.getIntroduction());
                        singerMapper.updateById(singer);
                    }
                    return R.success("修改成功");
                } else {
                    return R.error("修改失败");
                }
            } catch (Exception e) {
                return R.error("更新失败: " + e.getMessage());
            }
    }

    //修改用户密码
    @Override
    public R updatePassword(ConsumerRequest updatePasswordRequest) {

       if (!this.verityPasswd(updatePasswordRequest.getUsername(),updatePasswordRequest.getOldPassword())) {
            return R.error("密码输入错误");
        }

        Consumer consumer = new Consumer();
        consumer.setId(updatePasswordRequest.getId());
        String secretPassword = DigestUtils.md5DigestAsHex((SALT + updatePasswordRequest.getPassword()).getBytes(StandardCharsets.UTF_8));
        consumer.setPassword(secretPassword);

        if (consumerMapper.updateById(consumer) > 0) {
            return R.success("密码修改成功");
        } else {
            return R.error("密码修改失败");
        }
    }

    /**
     * 缩减验证
     * @param updatePasswordRequest
     * @return
     */
    @Override
    public R updatePassword01(ConsumerRequest updatePasswordRequest) {
        Consumer consumer = new Consumer();
        consumer.setId(updatePasswordRequest.getId());
        String secretPassword = DigestUtils.md5DigestAsHex((SALT + updatePasswordRequest.getPassword()).getBytes(StandardCharsets.UTF_8));
        consumer.setPassword(secretPassword);

        if (consumerMapper.updateById(consumer) > 0) {
            return R.success("密码修改成功");
        } else {
            return R.error("密码修改失败");
        }
    }

    @Autowired  // 注入 FileUploadController
    private FileUploadController fileUploadController;

    //更新用户头像
    @Override
    public R updateUserAvator(MultipartFile avatorFile,int id,HttpSession session) {
        //文件上传当使用 el-upload 组件上传文件时，默认情况下不会自动携带 cookie 或 token 等认证信息。

        String s = fileUploadController.uploadAtorImgFile(avatorFile);
        String imgPath = "/img/avatorImages/" + s;
        Consumer consumer = new Consumer();
        consumer.setId(id);
        consumer.setAvator(imgPath);
        // 开始事务处理
        try {
            // 更新用户头像
            if (consumerMapper.updateById(consumer) > 0) {
                // 查询该用户关联的歌手ID
                Consumer user = consumerMapper.selectById(id);
                if (user != null && user.getSingerId() != null) {
                    // 如果用户是歌手，同时更新歌手的图片
                    Singer singer = new Singer();
                    singer.setId(user.getSingerId());
                    singer.setPic(imgPath);
                    singerMapper.updateById(singer);
                }
                return R.success("上传成功", imgPath);
            } else {
                return R.error("上传失败");
            }
        } catch (Exception e) {
            return R.error("更新失败: " + e.getMessage());
        }
    }

    @Override
    public boolean existUser(String username) {
        QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        return consumerMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean verityPasswd(String username, String password) {
        QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        String secretPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes(StandardCharsets.UTF_8));
        queryWrapper.eq("password",secretPassword);
        return consumerMapper.selectCount(queryWrapper) > 0;
    }

    // 删除用户，注销用户
    @Override
    public R deleteUser(Integer id) {
        if (consumerMapper.deleteById(id) > 0) {
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }


    //管理员的session正常使用
    @Override
    public R allUser(HttpSession session) {
        String username = (String) session.getAttribute("name");
        // 查询admin表确认用户是否存在
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", username);
        if (adminMapper.selectCount(queryWrapper) > 0) {
            // 是管理员，返回所有用户
            return R.success(null, consumerMapper.selectList(null));
        } else {
            // 不是管理员，拒绝访问
            return R.error("权限不足");
        }
    }

    @Override
    public R userOfId(Integer id) {
        QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        return R.success(null, consumerMapper.selectList(queryWrapper));
    }


    //用户登录
    @Override
    public R loginStatus(ConsumerRequest loginRequest, HttpSession session) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        if (this.verityPasswd(username, password)) {
            session.setAttribute("username", username);
            session.setAttribute("userId", consumerMapper.selectIdByconsumerName(username));
            //System.out.println("登录用户名ID:"+ session.getAttribute("userId"));
            //System.out.println("登录用户名: " + username);
            Consumer consumer = new Consumer();
            consumer.setUsername(username);
            return R.success("登录成功", consumerMapper.selectList(new QueryWrapper<>(consumer)));
        } else {
            return R.error("用户名或密码错误");
        }
    }

    @Override
    public R loginEmailStatus(ConsumerRequest loginRequest, HttpSession session) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        Consumer consumer1 = findByEmail(email);
        if (this.verityPasswd(consumer1.getUsername(), password)) {
            session.setAttribute("username", consumer1.getUsername());
            Consumer consumer = new Consumer();
            consumer.setUsername(consumer1.getUsername());
            return R.success("登录成功", consumerMapper.selectList(new QueryWrapper<>(consumer)));
        } else {
            return R.error("用户名或密码错误");
        }
    }

    @Override
    public Consumer findByEmail(String email) {
        QueryWrapper<Consumer> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("email",email);
        Consumer consumer = consumerMapper.selectOne(queryWrapper);
        return consumer;
    }

    // 获取用户创建的歌手的id
    @Override
    public R getUserSinger(Integer id) {
        // 首先从consumer表中获取用户的singer_id
        Consumer consumer = consumerMapper.selectById(id);
        //System.out.println("获取用户创建的歌手的id:  "+consumer.getSingerId());
        if (consumer == null || consumer.getSingerId() == null) {
            return R.error("用户不存在");
        }
        // 返回歌手信息
        // 直接返回 singerId（如果没有，返回 null）
        Integer singerId = consumer.getSingerId();
        return R.success("查询成功", consumer.getSingerId());
    }

    //用户成为歌手，用户注册为歌手，用户歌手
    @Override
    public R becomeSinger(UserSingerController.SingerRequests singerRequests, HttpSession session) {
        Singer singer = new Singer();
        BeanUtils.copyProperties(singerRequests, singer);
        String pic = "/img/avatorImages/user.jpg";
        singer.setPic(pic);
        if (singerMapper.insert(singer) > 0) {
            //在数据库创建歌手后，获取歌手的id，并更新用户的singer_id字段
            String username = (String) session.getAttribute("username");
            System.out.println("用户名: " + username);

            Consumer consumer = new Consumer();
            consumer.setId(singerRequests.getUserId());  // 设置用户ID
            consumer.setSingerId(singer.getId());         // 设置歌手ID
            //System.out.println("成为歌手更新数据库:  "+consumer);
            // 正确调用updateById方法
            if (consumerMapper.updateById(consumer) > 0) {
                //System.out.println(singerRequests.getUserId());
                return R.success("添加成功");
            } else {
                return R.error("用户信息更新失败");
            }
        } else {
            return R.error("添加失败");
        }
    }

    @Override
    public R becomeSinger1(SingerRequest singerRequests, HttpSession session){
        return null;
    }

    @Autowired
    private PlayHistoryMapper playHistoryMapper;
    // ConsumerServiceImpl.java


    // 添加歌曲播放记录
    @Override
    public R addPlayHistory(int songId, int userId, HttpSession session) {
        // 验证用户身份：检查session中的用户名是否对应传入的userId
        String sessionUsername = (String) session.getAttribute("username");
        if (sessionUsername == null) {
            return R.error("用户未登录");
        }
        // 根据用户名查找用户信息
        QueryWrapper<Consumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", sessionUsername);
        Consumer sessionConsumer = consumerMapper.selectOne(queryWrapper);

        if (sessionConsumer == null) {
            return R.error("用户不存在");
        }
        // 验证传入的userId是否与session中的用户ID一致
        if (sessionConsumer.getId() != userId) {
            return R.error("权限不足，无法为其他用户添加播放记录");
        }
        try {
            PlayHistory playHistory = new PlayHistory();
            playHistory.setUserId(userId);
            playHistory.setSongId(songId);
            playHistory.setPlayTime(new Date());
            // 你可以根据需要设置 duration，默认为0
            playHistory.setDuration(30);

            // 插入播放记录到数据库
            playHistoryMapper.insert(playHistory);

            return R.success("播放记录添加成功");
        } catch (Exception e) {
            return R.error("播放记录添加失败: " + e.getMessage());
        }
    }

    @Autowired
    private SongMapper songMapper;

    // 获取最近20个播放记录
    @Override
    public R getRecentPlayHistory(int userId) {
        if (userId <= 0) {
            return R.error("用户ID无效");
        }

        try {
            // --- 1. 获取原始播放记录（查询足够多的记录进行去重）---
            // 为了确保能找到 20 个不重复的歌曲ID，我们先查询最近的 100 条记录
            QueryWrapper<PlayHistory> historyQueryWrapper = new QueryWrapper<>();
            historyQueryWrapper.eq("user_id", userId);
            historyQueryWrapper.orderByDesc("play_time");
            historyQueryWrapper.last("LIMIT 50");

            List<PlayHistory> rawHistoryList = playHistoryMapper.selectList(historyQueryWrapper);

            // --- 2. 内存中进行去重和排序，获取最近20个不重复的歌曲ID ---
            // 使用 LinkedHashMap：
            // 1. Map 的 Key 自动去重 (songId)
            // 2. LinkedHashMap 保持插入顺序
            // 由于 rawHistoryList 是按时间倒序的，先遇到的记录就是该 songId 的最新播放记录
            Map<Integer, PlayHistory> uniqueRecentPlays = new LinkedHashMap<>();

            for (PlayHistory history : rawHistoryList) {
                // putIfAbsent 保证只会存储该 songId 第一次出现（即最晚）的记录
                uniqueRecentPlays.putIfAbsent(history.getSongId(), history);
            }

            // 提取最终的歌曲ID列表（最多20个），顺序已经是按最近播放时间倒序
            List<Integer> finalSongIds = uniqueRecentPlays.keySet().stream()
                    .limit(20)
                    .collect(Collectors.toList());
            if (finalSongIds.isEmpty()) {
                return R.success("查询成功", new ArrayList<>());
            }
            // --- 3. 批量查询歌曲详细信息 ---
            QueryWrapper<Song> songQueryWrapper = new QueryWrapper<>();
            songQueryWrapper.in("id", finalSongIds);

            // 批量查询出来的歌曲，顺序是乱的
            List<Song> songs = songMapper.selectList(songQueryWrapper);


            return R.success("查询成功", songs);
        } catch (Exception e) {
            return R.error("查询失败: 系统错误");
        }
    }
}

