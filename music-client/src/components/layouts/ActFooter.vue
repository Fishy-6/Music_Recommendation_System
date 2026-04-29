<template>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <div class="footer">
    <div class="footer-links">
      <a :href="config.footer_about_link" target="_blank">
        <i class="fas fa-info-circle"></i>关于本站
      </a>
      <a :href="config.footer_resource_link" target="_blank">
        <i class="fas fa-question-circle"></i>资源来源
      </a>
      <a :href="config.footer_terms_link" target="_blank">
        <i class="fas fa-file-contract"></i>使用条款
      </a>
      <a :href="config.footer_feedback_link" target="_blank">
        <i class="fas fa-comment-dots"></i>反馈意见
      </a>
    </div>
    
    <p>欢迎访问<span class="baidu-link"><a href="https://www.baidu.com" target="_blank" style="color: inherit;">百度搜索</a></span>，获取更多信息</p>
    
    <div class="social-icons">
      <a :href="config.social_qq_link" target="_blank" class="qq">
        <i class="fab fa-qq"></i>
        <span class="social-label">QQ</span>
      </a>
      <a :href="config.social_wechat_link" target="_blank" class="wechat">
        <i class="fab fa-weixin"></i>
        <span class="social-label">微信</span>
      </a>
      </div>
    
    <p class="copyright">Modification © 2025 Actording</p>
    <p class="copyright">Copyright © 2019 Yin-Hongwei</p>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue';
// 假设你有一个封装好的 http 请求工具，如 axios
import axios from 'axios'; 

export default defineComponent({
  name: 'ActFooter',
  setup() {
    // 1. 定义响应式数据，设置默认值以防接口请求失败
    const config = ref({
      footer_about_link: 'https://www.baidu.com',
      footer_resource_link: 'https://www.gequbao.com/',
      footer_terms_link: 'https://www.baidu.com',
      footer_feedback_link: 'https://www.baidu.com',
      social_qq_link: 'https://im.qq.com',
      social_wechat_link: 'https://wx.qq.com'
    });

    // 2. 获取数据的函数
    const fetchConfig = async () => {
      try {
        // 这里替换为你实际的后端 API 地址
        const response = await axios.get('/api/config');
        if (response.data && response.data.code === 200) {
          // 假设后端返回的数据格式是 { code: 200, data: { key: value, ... } }
          // 将后端返回的配置覆盖默认配置
          config.value = { ...config.value, ...response.data.data };
        }
      } catch (error) {
        console.error('获取配置失败:', error);
      }
    };

    // 3. 在组件挂载时调用
    onMounted(() => {
      fetchConfig();
    });

    return {
      config
    };
  }
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";
@import "@/assets/css/global.scss";

* {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
      font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
    }
    
    body {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    }
    
    .content {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      padding: 2rem;
      text-align: center;
    }
    
    .content h1 {
      font-size: 2.5rem;
      color: #2c3e50;
      margin-bottom: 1rem;
    }
    
    .content p {
      font-size: 1.2rem;
      color: #34495e;
      max-width: 800px;
      line-height: 1.6;
    }
    
    .footer {
      // background: linear-gradient(to bottom, #ffffff, #50a6ff);
      background: linear-gradient(to bottom, var(--footer-bg-start, #ffffff), #50a6ff);
      color: #ecf0f1;
      padding: 2rem 1rem;
      text-align: center;
      box-shadow: 0 0px 0px rgba(0, 0, 0, 0.15);
    }
    
    .footer p {
      margin: 0.8rem 0;
      font-size: 1rem;
    }
    
    .footer-links {
      display: flex;
      justify-content: center;
      flex-wrap: wrap;
      gap: 1.5rem;
      margin-bottom: 1rem;
    }
    
    .footer-links a {
      color: #3c92eb;
      text-decoration: none;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      transition: all 0.3s ease;
      padding: 0.5rem 1rem;
      border-radius: 4px;
    }
    
    .footer-links a:hover {
      background-color: rgba(255, 255, 255, 0.1);
      transform: translateY(-2px);
    }
    
    .copyright {
      font-size: 0.9rem;
      opacity: 0.8;
      margin-top: 1.5rem;
    }
    
    .baidu-link {
      background-color: rgba(255, 255, 255, 0.15);
      border-radius: 20px;
      padding: 0.5rem 1.2rem;
      margin: 0 0.3rem;
      font-weight: 600;
      transition: all 0.3s ease;
    }
    
    .baidu-link:hover {
      background-color: #3949ab;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    }
    
    .social-icons {
      display: flex;
      justify-content: center;
      gap: 1.2rem;
      margin: 1.5rem 0;
    }
    
    .social-icons a {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 50px;
      height: 50px;
      background-color: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
      color: #ecf0f1;
      transition: all 0.3s ease;
      position: relative;
      font-size: 1.4rem;
    }
    
    .social-icons a:hover {
      transform: translateY(-5px);
      box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
    }
    
    /* 微信图标样式 */
    .social-icons a.wechat:hover {
      background-color: #2aae67;
    }
    
    /* 抖音图标样式 */
    .social-icons a.douyin:hover {
      background-color: #000000;
    }
    
    /* 小红书图标样式 */
    .social-icons a.xiaohongshu:hover {
      background-color: #ff2741;
    }
    
    /* QQ图标样式 */
    .social-icons a.qq:hover {
      background-color: #12b7f5;
    }
    
    .social-label {
      position: absolute;
      bottom: -25px;
      left: 50%;
      transform: translateX(-50%);
      font-size: 0.75rem;
      white-space: nowrap;
      opacity: 0;
      transition: opacity 0.3s ease;
    }
    
    .social-icons a:hover .social-label {
      opacity: 1;
    }
    
    @media (max-width: 768px) {
      .footer-links {
        flex-direction: column;
        gap: 0.8rem;
      }
      
      .content h1 {
        font-size: 2rem;
      }
      
      .content p {
        font-size: 1rem;
      }
      
      .social-icons {
        gap: 0.8rem;
      }
      
      .social-icons a {
        width: 45px;
        height: 45px;
        font-size: 1.2rem;
      }
    }


</style>

