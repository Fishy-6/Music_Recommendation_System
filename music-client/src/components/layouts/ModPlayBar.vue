
<template>
  <!--播放进度栏-->

  <div class="play-bar" :class="{ show: !toggle }" style="color: var(--footer-bg-start, #ffffff)">

    <!-- 动画进度条 -->
    <div class="animated-progress">
      <el-progress :percentage="nowTime" :indeterminate="true" :color="progressColor" :stroke-width="4"
        :show-text="false" @click="changeTime" />
    </div>

    <div class="fold" :class="{ turn: toggle }">
      <yin-icon :icon="iconList.ZHEDIE" @click="toggle = !toggle"></yin-icon>
      
    </div>

    <!--原始播放进度-->
    <el-slider class="progress" v-model="nowTime" @change="changeTime" size="small"></el-slider>

    <div class="control-box"  >
      <div class="info-box" >
        <!--歌曲图片-->
        <div @click="goPlayerPage">
          <el-image class="song-bar-img" fit="contain" :src="songPic ? attachImageUrl(songPic) : ''" />
        </div>
        <!--播放开始结束时间-->
        <div v-if="songId">
          <div class="song-info">{{ this.songTitle }} - {{ this.singerName }}</div>
          <div class="time-info">{{ startTime }} / {{ endTime }}</div>
        </div>
      </div>
      <div class="song-ctr" >

        <yin-icon class="yin-play-show" :icon="playStateList[playStateIndex]" @click="changePlayState"></yin-icon>
        


        <!--上一首-->
        <yin-icon class="yin-play-show" :icon="iconList.SHANGYISHOU" @click="prev"></yin-icon>
        

        <!--播放，播放按钮蓝色-->
        <yin-icon :icon="playBtnIcon" style="color: #409eff"  @click="togglePlay"></yin-icon>


        <!--下一首-->
        <yin-icon class="yin-play-show" :icon="iconList.XIAYISHOU" @click="next"></yin-icon>
        

        <!--音量-->
        <el-dropdown class="yin-play-show" trigger="click">

          <yin-icon v-if="volume !== 0" :icon="iconList.YINLIANG"></yin-icon>
          <yin-icon v-else :icon="iconList.JINGYIN"></yin-icon>
          

          <template #dropdown>
            <el-dropdown-menu>
              <el-slider class="yin-slider" style="height: 150px; margin: 10px 0" v-model="volume"
                :vertical="true"></el-slider>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <div class="song-ctr song-edit">
        <!--收藏-->
        <yin-icon class="yin-play-show" :class="{ active: isCollection }"
          :icon="isCollection ? iconList.like : iconList.dislike" @click="changeCollection"></yin-icon>

       
        <!--下载-->
        <yin-icon class="yin-play-show" :icon="iconList.download" @click="
            downloadMusic({
              songUrl,
              songName: singerName + '-' + songTitle,
            })
          "></yin-icon>



        <!--歌曲列表-->
        <yin-icon :icon="iconList.LIEBIAO" @click="changeAside"></yin-icon>
        

      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {computed, defineComponent, getCurrentInstance, onMounted, ref, watch} from "vue";
import {mapGetters, useStore} from "vuex";
import mixin from "@/mixins/mixin";
import YinIcon from "./YinIcon.vue";
import {HttpManager} from "@/api";
import {formatSeconds} from "@/utils";
import {Icon, RouterName} from "@/enums";


export default defineComponent({
  components: {
    YinIcon,
    
  },
  setup() {
    const {proxy} = getCurrentInstance();
    const store = useStore();
    const {routerManager, playMusic, checkStatus, downloadMusic} = mixin();

    const isCollection = ref(false); // 是否收藏

    const userIdVO = computed(() => store.getters.userId);
    const songIdVO = computed(() => store.getters.songId);
    const token = computed(() => store.getters.token);

    // --- 新增：播放记录相关状态 ---
    const hasRecorded = ref(false); // 标记当前歌曲是否已记录
    const RECORD_THRESHOLD = 30;    // 记录阈值：30秒
    const curTime = computed(() => store.getters.curTime); // 获取当前播放时间

    watch(songIdVO, () => {
      hasRecorded.value = false;
      initCollection();
    });
    watch(token, (value) => {
      if (!value) isCollection.value = false;
    });


    watch(curTime, (newTime) => {
      // 1. 如果已经记录过，直接跳过
      if (hasRecorded.value) return;
      
      // 2. 检查是否满足记录条件：时间 > 15秒 且 用户已登录 且 有歌曲ID
      if (newTime > RECORD_THRESHOLD && userIdVO.value && songIdVO.value) {
        recordPlayHistory();
      }
    });


// --- 新增：记录播放历史的方法 ---
async function recordPlayHistory() {
    if (!checkStatus(false)) return;
      try {
        hasRecorded.value = true; // 立即标记为已记录，防止重复请求
      
        // 构建请求参数，对应你的 Java 实体类
        const params = {
          userId: userIdVO.value,
          songId: songIdVO.value,
          // playTime: 后端会自动生成 new Date()
          // duration: Math.floor(curTime.value) // 可选：记录触发时的时长
        };

        // 发送静默请求（不需要 await 阻塞后续逻辑，也不需要弹窗提示）
        await HttpManager.addPlayHistory(params); 
        console.log(`[PlayHistory] 已记录歌曲 ID: ${params.songId}`);
        
      } catch (error) {
        console.error("[PlayHistory] 记录失败:", error);
        // 如果失败，可以选择是否重置 hasRecorded 允许重试，
        // 但为了避免网络问题导致频繁请求，建议这里不重置，这首歌这次就不记了。
      }
    }

    async function initCollection() {
      if (!checkStatus(false)) return;

      const userId = userIdVO.value;
      const type = '0';
      const songId = songIdVO.value;
      isCollection.value = ((await HttpManager.isCollection({userId, type, songId})) as ResponseBody).data;
    }

    async function changeCollection() {
      //检查登陆状态
      if (!checkStatus()) return;

      const userId = userIdVO.value;
      const type = '0'; //这里要看看 不能直接写死
      const songId = songIdVO.value;

      const result = isCollection.value
          ? ((await HttpManager.deleteCollection(userIdVO.value, songIdVO.value)) as ResponseBody)
          : ((await HttpManager.setCollection({userId, type, songId})) as ResponseBody);
      (proxy as any).$message({
        message: result.message,
        type: result.type,
      });

      if (result.data == true || result.data == false) isCollection.value = result.data;
    }

    onMounted(() => {
    if (songIdVO.value) initCollection();
    window.addEventListener("keydown", handleKeyDown);
  });

  //空格键播放暂停音乐
  function handleKeyDown(event) {
 // 检查当前焦点是否在输入相关元素上
 const activeElement = document.activeElement;
  const isInputFocused = activeElement instanceof HTMLInputElement || 
                         activeElement instanceof HTMLTextAreaElement ||
                         activeElement?.hasAttribute('contenteditable');
  
  // 排除特定的CSS类名或元素
  const isExcludedElement = activeElement?.classList.contains('comment-input') ||
                            activeElement?.closest('.no-space-toggle');
  
  // 只有当焦点不在输入元素上且不在排除元素上时才触发播放/暂停
  if (event.code === "Space" && !isInputFocused && !isExcludedElement) {
    event.preventDefault(); // 阻止默认行为
    store.commit("setIsPlay", !store.getters.isPlay); // 切换播放状态
  }
  }



    return {
      isCollection,
      playMusic,
      routerManager,
      checkStatus,
      attachImageUrl: HttpManager.attachImageUrl,
      changeCollection,
      downloadMusic,
      // 新增进度条颜色计算
      progressColor: [
        { color: '#f56c6c', percentage: 20 },
        { color: '#e6a23c', percentage: 40 },
        { color: '#5cb87a', percentage: 60 },
        { color: '#1989fa', percentage: 80 },
        { color: '#6f7ad3', percentage: 100 }
      ]
    };
  },
  data() {
    return {
      startTime: "00:00",
      endTime: "00:00",
      nowTime: 0, // 进度条的位置
      toggle: true,
      volume: 50,
      playState: Icon.XUNHUAN,
      playStateList: [Icon.XUNHUAN, Icon.LUANXU],
      playStateIndex: 0,
      iconList: {
        download: Icon.XIAZAI,
        ZHEDIE: Icon.ZHEDIE,
        SHANGYISHOU: Icon.SHANGYISHOU,
        XIAYISHOU: Icon.XIAYISHOU,
        YINLIANG: Icon.YINLIANG1,
        JINGYIN: Icon.JINGYIN,
        LIEBIAO: Icon.LIEBIAO,
        dislike: Icon.Dislike,
        like: Icon.Like,
      },
    };
  },
  computed: {
    ...mapGetters([
      "userId",
      "isPlay", // 播放状态
      "playBtnIcon", // 播放状态的图标
      "songId", // 音乐id
      "songUrl", // 音乐地址
      "songTitle", // 歌名
      "singerName", // 歌手名
      "songPic", // 歌曲图片
      "curTime", // 当前音乐的播放位置
      "duration", // 音乐时长
      "currentPlayList",
      "currentPlayIndex", // 当前歌曲在歌曲列表的位置
      "showAside", // 是否显示侧边栏
      "autoNext", // 用于触发自动播放下一首
    ]),
  },
  watch: {
    // 切换播放状态的图标
    isPlay(value) {
      this.$store.commit("setPlayBtnIcon", value ? Icon.ZANTING : Icon.BOFANG);
    },
    volume() {
      this.$store.commit("setVolume", this.volume / 100);
    },
    // 播放时间的开始和结束
    curTime() {
      this.startTime = formatSeconds(this.curTime);
      this.endTime = formatSeconds(this.duration);
      // 移动进度条
      this.nowTime = (this.curTime / this.duration) * 100;
    },
    // 自动播放下一首
    autoNext() {
      this.next();
    },
  },
  methods: {
    changeAside() {
      this.$store.commit("setShowAside", !this.showAside);
    },
    // 控制音乐播放 / 暂停
    togglePlay() {
      this.$store.commit("setIsPlay", this.isPlay ? false : true);
    },
    changeTime() {
      this.$store.commit("setChangeTime", this.duration * (this.nowTime * 0.01));
    },
    changePlayState() {
      this.playStateIndex = this.playStateIndex >= this.playStateList.length - 1 ? 0 : ++this.playStateIndex;
      this.playState = this.playStateList[this.playStateIndex];
    },
    // 上一首
    prev() {
      if (this.playState === Icon.LUANXU) {
        let playIndex = Math.floor(Math.random() * this.currentPlayList.length);
        playIndex = playIndex === this.currentPlayIndex ? playIndex + 1 : playIndex;
        this.$store.commit("setCurrentPlayIndex", playIndex);
        this.toPlay(this.currentPlayList[playIndex].url);
      } else if (this.currentPlayIndex !== -1 && this.currentPlayList.length > 1) {
        if (this.currentPlayIndex > 0) {
          this.$store.commit("setCurrentPlayIndex", this.currentPlayIndex - 1);
          this.toPlay(this.currentPlayList[this.currentPlayIndex].url);
        } else {
          this.$store.commit("setCurrentPlayIndex", this.currentPlayList.length - 1);
          this.toPlay(this.currentPlayList[this.currentPlayIndex].url);
        }
      }
    },
    // 下一首
    next() {
      if (this.playState === Icon.LUANXU) {
        let playIndex = Math.floor(Math.random() * this.currentPlayList.length);
        playIndex = playIndex === this.currentPlayIndex ? playIndex + 1 : playIndex;
        this.$store.commit("setCurrentPlayIndex", playIndex);
        this.toPlay(this.currentPlayList[playIndex].url);
      } else if (this.currentPlayIndex !== -1 && this.currentPlayList.length > 1) {
        if (this.currentPlayIndex < this.currentPlayList.length - 1) {
          this.$store.commit("setCurrentPlayIndex", this.currentPlayIndex + 1);
          this.toPlay(this.currentPlayList[this.currentPlayIndex].url);
        } else {
          this.$store.commit("setCurrentPlayIndex", 0);
          this.toPlay(this.currentPlayList[0].url);
        }
      }
    },
    // 选中播放
    toPlay(url) {
      if (url && url !== this.songUrl) {
        const song = this.currentPlayList[this.currentPlayIndex];
        this.playMusic({
          id: song.id,
          url,
          pic: song.pic,
          index: this.currentPlayIndex,
          name: song.name,
          lyric: song.lyric,
          currentSongList: this.currentPlayList,
        });
      }
    },
    goPlayerPage() {
      this.routerManager(RouterName.Lyric, {path: `${RouterName.Lyric}/${this.songId}`});
    },



  },
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/yin-play-bar.scss";

/* 进度条样式优化 */
.animated-progress {
  width: 100%;
  padding: 0 20px;
  position: relative;
  top: -8px; /* 调整位置 */
}

/* 动画进度条样式 */
:deep(.el-progress-bar__outer) {
  border-radius: 0;
  background-color: rgba(255, 255, 255, 0);
}

:deep(.el-progress-bar__inner) {
  transition: all 0.4s cubic-bezier(0.08, 0.82, 0.17, 1);
}

/* 播放条整体样式调整 */
// .play-bar {
//   transition: all 0.3s ease;
//   background: linear-gradient(to right, #1a1a2e, #16213e);
//   box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.2);
// }

// .control-box {
//   padding: 10px 20px;
// }

.song-info {
  color: #76838b;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 300px;
}

.time-info {
  color: rgba(255, 255, 255, 0);
  font-size: 12px;
}
</style>
