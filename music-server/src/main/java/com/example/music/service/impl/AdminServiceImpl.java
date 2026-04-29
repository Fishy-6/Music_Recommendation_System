package com.example.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.music.common.R;
import com.example.music.mapper.AdminMapper;
import com.example.music.model.domain.Admin;
import com.example.music.model.request.AdminRequest;
import com.example.music.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;

import static com.example.music.constant.Constants.SALT;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public R verityPasswd(AdminRequest adminRequest, HttpSession session) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name",adminRequest.getUsername());
        queryWrapper.eq("password", DigestUtils.md5DigestAsHex((SALT + adminRequest.getPassword()).getBytes(StandardCharsets.UTF_8)));
        //queryWrapper.eq("password",adminRequest.getPassword());
        if (adminMapper.selectCount(queryWrapper) > 0) {
            session.setAttribute("name", adminRequest.getUsername());

            return R.success("登录成功");
        } else {
            return R.error("用户名或密码错误");
        }
    }

    @Override
    public R addAdmin(AdminRequest adminRequest, HttpSession session) {
        // 获取session中的管理员用户名
        String sessionAdminName = (String) session.getAttribute("name");
        // 检查session中是否存在管理员用户名
        if (sessionAdminName == null) {
            return R.error("请先登录管理员账户");
        }
        // 查询session中的管理员是否存在
        QueryWrapper<Admin> sessionAdminQuery = new QueryWrapper<>();
        sessionAdminQuery.eq("name", sessionAdminName);
        if (adminMapper.selectCount(sessionAdminQuery) <= 0) {
            return R.error("当前登录的管理员账户不存在");
        }
        // 检查要添加的管理员用户名是否与session中的用户名重合
        if (sessionAdminName.equals(adminRequest.getUsername())) {
            return R.error("不能添加与当前登录管理员同名的账户");
        }
        // 检查要添加的管理员用户名是否已存在
        QueryWrapper<Admin> newAdminQuery = new QueryWrapper<>();
        newAdminQuery.eq("name", adminRequest.getUsername());
        if (adminMapper.selectCount(newAdminQuery) > 0) {
            return R.error("该管理员用户名已存在");
        }
        Admin admin = new Admin();
        admin.setName(adminRequest.getUsername());
        String password = DigestUtils.md5DigestAsHex((SALT + adminRequest.getPassword()).getBytes(StandardCharsets.UTF_8));
        admin.setPassword(password);
        if (adminMapper.insert(admin) > 0) {
            return R.success("添加成功");
        } else {
            return R.error("添加失败");
        }
    }

    @Override
    public R getAdministrator(HttpSession session) {
        // 获取session中的用户名
        String username = (String) session.getAttribute("name");
        // 检查session中是否存在用户名
        if (username == null) {
            return R.error("未登录");
        }
        // 查询admin表确认用户是否存在
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", username);
        if (adminMapper.selectCount(queryWrapper) > 0) {
            // 用户存在，执行相应动作
            return R.success("用户验证成功，可以执行动作",adminMapper.selectList(null));
        } else {
            // 用户不存在
            return R.error("用户不存在");
        }
    }
    
    @Override
    public R deleteAdmin(Integer id, HttpSession session) {
        // 获取session中的管理员用户名
        String sessionAdminName = (String) session.getAttribute("name");
        // 检查session中是否存在管理员用户名
        if (sessionAdminName == null) {
            return R.error("请先登录管理员账户");
        }
        // 查询session中的管理员是否存在
        QueryWrapper<Admin> sessionAdminQuery = new QueryWrapper<>();
        sessionAdminQuery.eq("name", sessionAdminName);
        if (adminMapper.selectCount(sessionAdminQuery) <= 0) {
            return R.error("当前登录的管理员账户不存在");
        }
        // 检查要删除的管理员是否存在
        Admin adminToDelete = adminMapper.selectById(id);
        if (adminToDelete == null) {
            return R.error("要删除的管理员账户不存在");
        }
        // 检查是否试图删除自己
        if (adminToDelete.getName().equals(sessionAdminName)) {
            return R.error("不能删除当前登录的管理员账户");
        }
        // 执行删除操作
        if (adminMapper.deleteById(id) > 0) {
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }
}