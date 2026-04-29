<template>
  <div class="song-page">
    <div class="song-background" :style="{ backgroundImage: `url(${attachImageUrl(songPic)})` }"></div>

    <div class="song-content">
      <div class="song-info-container">
        <!-- 移动端优化：歌手信息放在封面顶部 -->
        <div class="mobile-song-meta">
          <h1 class="song-title">{{ songTitle }}</h1>
          <p class="song-artist">{{ singerName }}</p>
        </div>

        <div class="cover-rating-wrapper">
          <div class="song-cover-wrapper">
            <el-image class="song-cover" :src="attachImageUrl(songPic)" fit="cover"
              :preview-src-list="[attachImageUrl(songPic)]">
              <template #error>
                <div class="cover-error">
                  <el-icon>
                    <Picture />
                  </el-icon>
                </div>
              </template>
            </el-image>

            <!-- 桌面端歌手信息 -->
            <div class="desktop-song-meta">
              <h1 class="song-title">{{ songTitle }}</h1>
              <p class="song-artist">{{ singerName }}</p>
            </div>
          </div>

          <div class="song-rating-card">
            <div class="rating-item">
              <div class="rating-label">综合评分</div>
              <div class="rating-value">
                <span class="score-num">{{ (averageScore * 2).toFixed(1) }}</span>
                <el-rate v-model="averageScore" disabled show-score text-color="#ff9900" score-template="{value}"
                  class="readonly-rate" />
              </div>
            </div>

            <div class="rating-divider"></div>
            <div class="rating-item">
              <div class="rating-label">我要评分</div>
              <div class="rating-action">
                <el-rate v-model="userScore" :disabled="isRated" allow-half @change="handlePushScore"
                  :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
                <div class="rate-controls">
                  <span class="rate-text" v-if="isRated">已评价 ({{ userScore * 2 }}分)</span>
                  <span class="rate-text" v-else>点击星星评分</span>
                  <el-button v-if="isRated" size="small" type="danger" @click="handleDeleteScore" class="delete-rating-btn" plain>
                    删除评分
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="lyric-container">
        <div class="lyric-header">
          <span>歌词</span>
          <!-- 移动端滚动提示 -->
          <div class="lyric-hint" v-if="isMobile">
            <el-icon><ArrowDownBold /></el-icon>
            <span>向上滑动查看歌词</span>
          </div>
        </div>
        <div class="lyric-viewport" ref="lyricViewport">
          <div class="lyric-content">
            <template v-if="lyricArr.length > 0">
              <div v-for="(item, index) in visibleLyrics" :key="index" :class="{ 
                    'active': isActive(index),
                    'passed': isPassed(index),
                    'future': isFuture(index)
                  }" class="lyric-line" ref="lyricLines">
                {{ item && item.length > 1 ? item[1] : '' }}
              </div>
            </template>

            <div v-else class="no-lyric">
              <el-empty description="暂无歌词或歌词格式不支持" :image-size="isMobile ? 80 : 100">
                <template #description>
                  <p style="color: rgba(255,255,255,0.6)">暂无歌词或歌词格式不支持</p>
                </template>
              </el-empty>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="comment-section">
      <h2 class="section-title">听友评论</h2>
      <comment :playId="songId" :type="0"></comment>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, ref, watch, onMounted, nextTick, getCurrentInstance, onUnmounted } from "vue";
import { useStore } from "vuex";
import { VideoPlay, VideoPause, Picture, ArrowDownBold } from "@element-plus/icons-vue";
import Comment from "@/components/Comment.vue";
import { parseLyric } from "@/utils";
import { HttpManager } from "@/api";
import { ElMessage } from "element-plus";

export default defineComponent({
  components: {
    Comment,
    Picture,
    ArrowDownBold
  },
  setup() {
    const { proxy } = getCurrentInstance();
    const store = useStore();
    const lyricViewport = ref<HTMLElement | null>(null);
    
    // --- 响应式判断 ---
    const isMobile = ref(false);
    const checkMobile = () => {
      isMobile.value = window.innerWidth <= 768;
    };
    
    // --- 评分相关数据 ---
    const averageScore = ref(0); // 平均分 (0-5)
    const userScore = ref(0);    // 用户评分 (0-5)
    const isRated = ref(false);  // 是否已评分

    // 基础数据
    const songTags = ref(['流行', '华语']); 
    const songDescription = ref('');
    
    // 歌词相关
    const activeLyricIndex = ref(-1);
    const visibleRange = ref({ start: 0, end: 0 });
    const viewportHeight = ref(0);
    const lineHeight = ref(50);

    // Vuex Getters
    const songId = computed(() => store.getters.songId);
    const lyric = computed(() => store.getters.lyric);
    const curTime = computed(() => store.getters.curTime);
    const songTitle = computed(() => store.getters.songTitle);
    const singerName = computed(() => store.getters.singerName);
    const songPic = computed(() => store.getters.songPic);
    const isPlay = computed(() => store.getters.isPlay);
    const userId = computed(() => store.getters.userId);

    const handleDeleteScore = async () => {
      if (!userId.value || !songId.value || !userScore.value) {
        ElMessage.warning("缺少必要信息");
        return;
      }

      try {
        const result = (await HttpManager.deleteSongRank({
          songId: songId.value,
          consumerId: userId.value
        })) as any;

        ElMessage.success("删除成功");
        userScore.value = 0;
        isRated.value = false;
        getRank();
      } catch (error) {
        ElMessage.error("删除评分时发生错误");
        console.error(error);
      }
    };

    // --- 歌词处理 ---
    const lyricArr = computed(() => {
      try {
        const lrc = lyric.value;
        if (!lrc || typeof lrc !== 'string') return [];
        const parsed = parseLyric(lrc);
        return Array.isArray(parsed) ? parsed : [];
      } catch (e) {
        console.warn("歌词解析失败:", e);
        return [];
      }
    });

    const visibleLyrics = computed(() => {
      if (!lyricArr.value.length) return [];
      return lyricArr.value.slice(visibleRange.value.start, visibleRange.value.end + 1);
    });

    // --- 评分逻辑 ---
    async function getRank() {
      try {
        const result = (await HttpManager.getRankOfSongId(songId.value)) as any;
        const score = typeof result === 'number' ? result : (result.data || 0);
        averageScore.value = score / 2;
      } catch (error) {
        averageScore.value = 0;
      }
    }

    async function getUserRank() {
      if (!songId.value || !userId.value) {
        isRated.value = false;
        userScore.value = 0;
        return;
      }

      try {
        const result = (await HttpManager.getUserRankSong({
          songId: songId.value,
          consumerId: userId.value
        })) as any; 
        
        const score = (result && result.data !== undefined) ? result.data : result;

        if (score > 0) {
          userScore.value = parseFloat((score / 2).toFixed(1));
          isRated.value = true;
        } else {
          userScore.value = 0;
          isRated.value = false;
        }
      } catch (error) {
        console.error("获取用户评分失败", error);
        userScore.value = 0;
        isRated.value = false;
      }
    }

    async function handlePushScore() {
      if (!userId.value) {
        ElMessage.warning("请先登录");
        userScore.value = 0;
        return;
      }
      
      if (isRated.value) return;

      try {
        const params = {
          songId: songId.value,
          consumerId: userId.value,
          score: userScore.value * 2
        };
        
        const result = (await HttpManager.setSongRank(params)) as any;
        
        if (result.code === 1 || result.success) { 
          ElMessage.success("评分成功");
          isRated.value = true;
          getRank(); 
        } else {
          ElMessage.error(result.message || "评分失败");
          userScore.value = 0;
        }
      } catch (error) {
        ElMessage.error("评分发生错误");
        userScore.value = 0;
      }
    }

    // --- 歌词滚动逻辑 ---
    const updateActiveLyric = () => {
      if (!lyricArr.value.length) return;
      for (let i = 0; i < lyricArr.value.length; i++) {
        const current = lyricArr.value[i];
        const next = lyricArr.value[i + 1];
        if (current && curTime.value >= current[0] && (!next || curTime.value < next[0])) {
          if (activeLyricIndex.value !== i) {
            activeLyricIndex.value = i;
            updateVisibleRange(i);
          }
          break;
        }
      }
    };

    const updateVisibleRange = (activeIndex: number) => {
      if (!lyricViewport.value) return;
      const start = Math.max(0, activeIndex - 4);
      const end = Math.min(lyricArr.value.length - 1, activeIndex + 5);
      visibleRange.value = { start, end };
      
      const target = (activeIndex - start) * lineHeight.value;
      lyricViewport.value.scrollTo({ top: target, behavior: 'smooth' });
    };

    const isActive = (i) => visibleRange.value.start + i === activeLyricIndex.value;
    const isPassed = (i) => visibleRange.value.start + i < activeLyricIndex.value;
    const isFuture = (i) => visibleRange.value.start + i > activeLyricIndex.value;

    watch(curTime, updateActiveLyric);
    
    watch(songId, async (val) => {
      if (val) {
        activeLyricIndex.value = 0;
        userScore.value = 0; 
        isRated.value = false;
        await Promise.all([getRank(), getUserRank()]);
      }
    });

    onMounted(() => {
      checkMobile();
      window.addEventListener('resize', checkMobile);
      
      if (songId.value) {
        getRank();
        getUserRank();
      }
    });

    onUnmounted(() => {
      window.removeEventListener('resize', checkMobile);
    });

    return {
      lyricViewport,
      handleDeleteScore,
      attachImageUrl: HttpManager.attachImageUrl,
      // 基础数据
      songTitle, singerName, songPic, songTags, songDescription, isPlay, songId,
      // 歌词
      lyricArr, visibleLyrics, isActive, isPassed, isFuture,
      // 评分
      averageScore, userScore, isRated, handlePushScore,
      // 响应式
      isMobile,
      // 图标
      Picture, ArrowDownBold
    };
  }
});
</script>

<style lang="scss" scoped>
/* 基础样式 */
.song-page {
  position: relative;
  min-height: 100vh;
  padding-bottom: 60px;
  color: #333;
}

.song-background {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-size: cover;
  background-position: center;
  filter: blur(30px) brightness(0.5);
  z-index: -1;
  opacity: 0.8;
}

.song-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 30px;
  
  @media (min-width: 1024px) {
    padding: 40px;
    flex-direction: row;
    gap: 40px;
  }
}

/* 歌曲信息容器 */
.song-info-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  
  @media (min-width: 1024px) {
    flex: 0 0 350px;
  }
}

/* 移动端歌曲信息（只在移动端显示） */
.mobile-song-meta {
  text-align: center;
  margin-bottom: 15px;
  
  @media (min-width: 768px) {
    display: none;
  }
  
  .song-title {
    font-size: 20px;
    color: #fff;
    margin-bottom: 5px;
    line-height: 1.3;
  }
  
  .song-artist {
    color: rgba(255, 255, 255, 0.7);
    font-size: 14px;
  }
}

/* 封面和评分包装器 */
.cover-rating-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
  
  @media (min-width: 768px) {
    gap: 30px;
  }
  
  @media (min-width: 1024px) {
    flex-direction: row;
    flex-wrap: wrap;
  }
}

.song-cover-wrapper {
  @media (min-width: 1024px) {
    flex: 0 0 100%;
  }
}

/* 封面样式 */
.song-cover {
  width: 100%;
  max-width: 300px;
  margin: 0 auto;
  border-radius: 12px;
  aspect-ratio: 1/1;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  display: block;
  
  @media (min-width: 768px) {
    max-width: 350px;
  }
  
  @media (min-width: 1024px) {
    max-width: 100%;
  }
}

/* 桌面端歌曲信息（只在桌面端显示） */
.desktop-song-meta {
  display: none;
  text-align: center;
  margin-top: 15px;
  
  @media (min-width: 768px) {
    display: block;
  }
  
  .song-title {
    font-size: 24px;
    color: #fff;
    margin-bottom: 5px;
  }
  
  .song-artist {
    color: rgba(255, 255, 255, 0.7);
  }
}

/* 评分卡片 */
.song-rating-card {
  background-color: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px;
  color: #fff;
  display: flex;
  flex-direction: column;
  gap: 15px;
  
  @media (min-width: 1024px) {
    flex: 1;
    margin-top: 0;
  }
}

.rating-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.rating-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 5px;
  
  @media (min-width: 768px) {
    margin-bottom: 0;
  }
}

.rating-value {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.score-num {
  font-size: 20px;
  font-weight: bold;
  color: #ff9900;
  
  @media (min-width: 768px) {
    font-size: 24px;
  }
}

.rating-divider {
  height: 1px;
  background-color: rgba(255, 255, 255, 0.1);
  margin: 5px 0;
}

.rating-action {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  width: 100%;
  
  @media (min-width: 768px) {
    width: auto;
    align-items: flex-end;
  }
}

.rate-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  margin-top: 10px;
  
  @media (min-width: 768px) {
    justify-content: flex-end;
    gap: 15px;
  }
}

.rate-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.delete-rating-btn {
  margin-left: auto;
  
  @media (min-width: 768px) {
    margin-left: 0;
  }
}

/* 歌词容器 */
.lyric-container {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 12px;
  padding: 20px;
  height: 50vh;
  display: flex;
  flex-direction: column;
  flex: 1;
  
  @media (min-width: 768px) {
    padding: 30px;
    height: 60vh;
  }
  
  @media (min-width: 1024px) {
    height: calc(100vh - 200px);
  }
}

.lyric-header {
  font-size: 16px;
  font-weight: bold;
  color: #fff;
  margin-bottom: 15px;
  text-align: center;
  opacity: 0.8;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  
  @media (min-width: 768px) {
    font-size: 18px;
    flex-direction: row;
    justify-content: center;
  }
}

.lyric-hint {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  
  @media (min-width: 768px) {
    display: none;
  }
}

.lyric-viewport {
  flex: 1;
  overflow-y: auto;
  position: relative;
  -webkit-overflow-scrolling: touch; /* 移动端平滑滚动 */

    /* 隐藏滚动条 - Webkit浏览器 */
    &::-webkit-scrollbar {
    display: none;
  }
  
  /* 隐藏滚动条 - Firefox */
  scrollbar-width: none;
  
  /* 隐藏滚动条 - IE/Edge */
  -ms-overflow-style: none;
  


}

.lyric-content {
  padding: 10px 0;
}

.lyric-line {
  text-align: center;
  padding: 10px 0;
  color: rgba(255, 255, 255, 0.6);
  transition: all 0.3s;
  font-size: 14px;
  line-height: 1.5;
  min-height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  @media (min-width: 768px) {
    font-size: 16px;
    padding: 12px 0;
    min-height: 50px;
  }
}

.lyric-line.active {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  
  @media (min-width: 768px) {
    font-size: 22px;
  }
}

.lyric-line.passed {
  opacity: 0.8;
}

.lyric-line.future {
  opacity: 0.4;
}

/* 评论区域 */
.comment-section {
  padding: 0 20px;
  margin-top: 30px;
  
  @media (min-width: 768px) {
    padding: 0 40px;
    margin-top: 40px;
  }
}

.section-title {
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding-bottom: 10px;
  margin-bottom: 20px;
  font-size: 18px;
  
  @media (min-width: 768px) {
    font-size: 20px;
  }
}

/* 全局样式覆盖 */
:deep(.el-rate__icon) {
  font-size: 18px;
  
  @media (min-width: 768px) {
    font-size: 20px;
  }
}

:deep(.readonly-rate .el-rate__icon) {
  font-size: 14px;
  
  @media (min-width: 768px) {
    font-size: 16px;
  }
}

:deep(.el-rate__text) {
  color: #ff9900 !important;
  font-size: 12px;
  
  @media (min-width: 768px) {
    font-size: 14px;
  }
}

/* 无歌词状态 */
.no-lyric {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  
  :deep(.el-empty__description p) {
    font-size: 14px;
    
    @media (min-width: 768px) {
      font-size: 16px;
    }
  }
}
</style>