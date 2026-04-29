package com.example.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.music.common.R;
import com.example.music.model.domain.Admin;
import com.example.music.model.request.AdminRequest;

import javax.servlet.http.HttpSession;

public interface AdminService extends IService<Admin> {
    /**
     * 验证管理员密码
     * @param adminRequest 管理员请求对象，包含用户名和密码
     * @param session HTTP会话，用于存储登录状态
     * @return 返回验证结果，包括成功或失败信息
     */
    R verityPasswd(AdminRequest adminRequest, HttpSession session);


    R addAdmin(AdminRequest adminRequest, HttpSession session);

    R getAdministrator(HttpSession session);
    
    /**
     * 删除管理员账户
     * @param id 要删除的管理员ID
     * @param session HTTP会话，用于权限验证
     * @return 删除结果
     */
    R deleteAdmin(Integer id, HttpSession session);
}