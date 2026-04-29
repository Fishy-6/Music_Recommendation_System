<template>
  <div class="home-container">
    <!-- <video-player videoSrc="http://localhost:8888/song/1.mp4"  poster="attachImageUrl(userPic)" :autoplay="false" :loop="false" :muted="false"></video-player> -->
    <!-- 轮播图 -->
    <div class="swiper-section">
      <el-carousel v-if="swiperList.length" class="swiper-container" height="400px" :interval="4000" arrow="always">
        <el-carousel-item v-for="(item, index) in swiperList" :key="index">
          <div class="swiper-item">
            <img :src="HttpManager.attachImageUrl(item.pic)" class="swiper-image" />
            <div class="swiper-overlay"></div>
            <div class="swiper-content">
              <h2 v-if="item.title">{{ item.title }}</h2>
              <p v-if="item.description">{{ item.description }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 内容区域 -->
    <div class="content-wrapper">

      <!-- 热门歌单 -->
      <section class="section-container">
        <div class="section-header">
          <h2 class="section-title">热门歌单</h2>
          <!-- 全部页面暂时用歌单页面代替 song-sheet-detail-->
          <router-link to="/song-sheet" class="view-all">查看全部</router-link>
        </div>
        <!-- ---------------------------------------------------------- 在这里请求 歌单列表songList ----------------------->
        <play-list class="play-list-container" path="song-sheet-detail" :playList="songList"
          card-type="cover"></play-list>
      </section>

         <!-- 推荐歌曲，登录状态时显示 -->
         <section v-if="isLogin" class="section-container">
        <div class="section-header">
          <h2 class="section-title">推荐歌曲</h2>
          <!-- 全部页面暂时用歌曲页面代替 singer-detail-->
          <router-link to="/singer" class="view-all">查看全部</router-link>
        </div>
        <!-- ------------------------------------------------------- 在这里请求 歌手列表singerList ----------------------->
        <SongPage class="play-list-container" path="singer-detail" :playList="RecommendsongList"
          card-type="artist"></SongPage>
      </section>

      <!-- 热门歌手 -->
      <section class="section-container">
        <div class="section-header">
          <h2 class="section-title">热门歌手</h2>
          <!-- 全部页面暂时用歌手页面代替 singer-detail-->
          <router-link to="/singer" class="view-all">查看全部</router-link>
        </div>
        <!-- ------------------------------------------------------- 在这里请求 歌手列表singerList ----------------------->
        <play-list class="play-list-container" path="singer-detail" :playList="singerList"
          card-type="artist"></play-list>
      </section>

    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from "vue";
import PlayList from "@/components/PlayList.vue";
import { NavName } from "@/enums";
import { HttpManager } from "@/api";
import mixin from "@/mixins/mixin";

import VideoPlayer from "@/components/layouts/VideoPlayer.vue";
import SongPage from "@/components/SongPage.vue";

const songList = ref([]); // 歌单列表
const singerList = ref([]); // 歌手列表
const swiperList = ref([]); // 轮播图列表
const RecommendsongList = ref([]); // 歌单列表

const { changeIndex, checkStatus } = mixin();

// 格式化歌曲数据，确保有 title 字段（PlayList 组件需要）
// const formattedSongList = computed(() => {
//   return RecommendsongList.value.map(song => ({
//     ...song,
//     title: song.name, // 确保有 title 字段
//     pic: song.pic || '/img/songPic/tubiao.jpg' // 确保图片不为空

//   }));
// });

try {
  HttpManager.getBannerList().then((res) => {
    swiperList.value = (res as ResponseBody).data.sort();
  });

  // 歌单列表
  HttpManager. getHotSongLists().then((res) => {
    songList.value = (res as ResponseBody).data.sort().slice(0, 10);
  });

  // HttpManager.getAllSinger().then((res) => {
  //   singerList.value = (res as ResponseBody).data.sort().slice(0, 10);
  //   console.log("singerList", singerList.value);
  // });

  // 歌手列表
  HttpManager.getHotSingers().then((res) => {
    singerList.value = (res as ResponseBody).data.sort().slice(0, 10);
  });

  // 推荐歌曲
  HttpManager.getRecommendSongList().then((res) => {
    RecommendsongList.value = (res as ResponseBody).data.sort().slice(0, 10); 
    
  });
  

  onMounted(() => {
    changeIndex(NavName.Home);
  });
} catch (error) {
  console.error(error);
}

// 检查登录状态
const isLogin = computed(() => {
  return checkStatus(false) !== false; // 传递 false 以阻止显示消息
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";

.home-container {
  max-width: 1400px;
  margin: 0 auto;
  padding-bottom: 40px;
}

/* 轮播图样式 */
.swiper-section {
  margin-bottom: 40px;
}

.swiper-container {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  
  &:deep(.el-carousel__arrow) {
    background-color: rgba(255, 255, 255, 0.3);
    color: white;
    font-size: 20px;
    width: 50px;
    height: 50px;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.5);
    }
  }
  
  &:deep(.el-carousel__indicators) {
    bottom: 30px;
    
    .el-carousel__button {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      background-color: rgba(255, 255, 255, 0.5);
      
      &.is-active {
        background-color: $color-blue;
      }
    }
  }
}

.swiper-item {
  position: relative;
  height: 100%;
  width: 100%;
}

.swiper-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.swiper-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.1), rgba(0, 0, 0, 0.7));
}

.swiper-content {
  position: absolute;
  bottom: 80px;
  left: 60px;
  color: white;
  max-width: 60%;
  
  h2 {
    font-size: 36px;
    font-weight: 700;
    margin-bottom: 15px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  }
  
  p {
    font-size: 16px;
    line-height: 1.6;
    opacity: 0.9;
  }
}

/* 内容区域样式 */
.content-wrapper {
  padding: 0 40px;
}

.section-container {
  margin-bottom: 50px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: $color-black;
  position: relative;
  padding-left: 15px;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 20px;
    background-color: $color-blue;
    border-radius: 2px;
  }
}

.view-all {
  color: $color-grey;
  font-size: 14px;
  transition: color 0.3s;
  
  &:hover {
    color: $color-blue;
  }
}

.play-list-container {
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .swiper-container {
    height: 350px;
  }
  
  .swiper-content {
    bottom: 60px;
    left: 40px;
    
    h2 {
      font-size: 28px;
    }
  }
}

@media (max-width: 768px) {
  .content-wrapper {
    padding: 0 20px;
  }
  
  .swiper-container {
    height: 300px;
    
    &:deep(.el-carousel__arrow) {
      width: 40px;
      height: 40px;
      font-size: 16px;
    }
  }
  
  .swiper-content {
    bottom: 40px;
    left: 20px;
    max-width: 80%;
    
    h2 {
      font-size: 24px;
    }
    
    p {
      font-size: 14px;
    }
  }
  
  .section-title {
    font-size: 20px;
  }
}

@media (max-width: 576px) {
  .swiper-container {
    height: 250px;
  }
  
  .swiper-content {
    h2 {
      font-size: 20px;
      margin-bottom: 10px;
    }
    
    p {
      display: none;
    }
  }
}
</style>