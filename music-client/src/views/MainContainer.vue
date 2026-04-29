<template >
  <!-- 主界面 -->
  <el-container>
    <el-header>
      <!-- 导航栏 -->
      <d-s-header></d-s-header>
    </el-header>
    <el-main>
      <router-view />

      <!-- 播放列表 -->
      <d-s-current-play></d-s-current-play>
      
      <!-- 播放栏 控制音乐播放 -->
      <mod-play-bar></mod-play-bar>

      <!-- 置顶按钮 -->
      <el-backtop :right="50" :bottom="100" />
      
     <!-- 音频播放器，隐藏原生控件使用自定义UI控制  -->
     <yin-audio></yin-audio> 

    </el-main>
    <el-footer>

      <el-affix position="bottom" >
       
      </el-affix>

      <act-footer></act-footer>
      
    </el-footer>
  </el-container>
</template>

<script lang="ts" setup>
import { getCurrentInstance } from "vue";
import YinAudio from "@/components/layouts/YinAudio.vue";
import ActFooter from "@/components/layouts/ActFooter.vue";
import DSHeader from "@/components/layouts/DSHeader.vue";
import DSCurrentPlay from "@/components/layouts/DSCurrentPlay.vue";
import ModPlayBar from "@/components/layouts/ModPlayBar.vue";


const { proxy } = getCurrentInstance();

if (sessionStorage.getItem("dataStore")) {
  proxy.$store.replaceState(Object.assign({}, proxy.$store.state, JSON.parse(sessionStorage.getItem("dataStore"))));
}

window.addEventListener("beforeunload", () => {
  sessionStorage.setItem("dataStore", JSON.stringify(proxy.$store.state));
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";
@import "@/assets/css/global.scss";

.el-footer {
  --el-footer-padding: -2 20px;
}

.el-container {
  min-height: calc(100% - 60px);
}
.el-header {
  padding: 0;
}
.el-main {
  padding-left: 1px;
  padding-right: 0;
  --el-main-padding: 9px;
  // 暗黑模式样式
  .dark & {
    //背景略微灰色
    background-color: #303133;
    
    color: #ffffff;
  }
}

// 暗黑模式下的容器背景
.dark .el-container {
  background-color: #303133;
}

.dark .el-header {
  background-color: #121212;
}

.dark .el-footer {
  background-color: #121212;
}
</style>