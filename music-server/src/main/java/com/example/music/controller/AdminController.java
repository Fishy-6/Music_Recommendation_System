package com.example.music.controller;

import com.example.music.common.R;
import com.example.music.model.request.AdminRequest;
import com.example.music.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * 后台管理的相关事宜
 */
@RestController
public class AdminController {
    @Autowired
    private AdminService adminService;

    // 判断是否登录成功
    @PostMapping("/admin/login/status")
    public R loginStatus(@RequestBody AdminRequest adminRequest, HttpSession session) {
        return adminService.verityPasswd(adminRequest, session);
    }

    @PostMapping("/admin/add")
    public R addAdmin(@RequestBody AdminRequest adminRequest, HttpSession session) {
        return adminService.addAdmin(adminRequest,session);
    }

    // 获取管理员信息
    @GetMapping("/admin/administrator")
    public R getAdministrator(HttpSession session) {
        return adminService.getAdministrator(session);
    }


    @PostMapping("/admin/logout")
    public R logout(HttpSession session) {
        // 清除session中的管理员信息
        session.removeAttribute("adminId");
        session.removeAttribute("adminName");
        // 使session失效
        session.invalidate();
        return R.success("退出登录成功");
    }


    @DeleteMapping("/admin/delete")
    public R deleteAdmin(@RequestParam Integer id, HttpSession session) {
        return adminService.deleteAdmin(id, session);
    }

}
