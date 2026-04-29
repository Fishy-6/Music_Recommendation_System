<template>
  <teleport to="body">
    <transition name="slide-fade">
      <div class="playlist-overlay" v-if="showAside" @click.self="closePlaylist">
        <div class="playlist-container" @click.stop>
          <div class="playlist-header">
            <h2 class="playlist-title">
              <el-icon class="title-icon"><List /></el-icon>
              当前播放
            </h2>
            <div class="header-actions">
              <div class="playlist-info">
                <span class="song-count">共 {{ currentPlayList.length || 0 }} 首</span>
                <el-button 
                  type="text" 
                  class="clear-btn"
                  @click="clearPlaylist"
                  :disabled="!currentPlayList.length"
                >
                  <el-icon><Delete /></el-icon>
                  清空列表
                </el-button>
              </div>
              <el-icon class="close-btn" @click="closePlaylist"><Close /></el-icon>
            </div>
          </div>
          
          <el-scrollbar class="playlist-scrollbar" height="calc(100% - 80px)">
            <ul class="song-list">
              <li
                v-for="(item, index) in currentPlayList"
                :key="item.id"
                :class="{ 'active': songId === item.id, 'playing': songId === item.id && isPlaying }"
                @click="handleSongClick(item, index)"
              >
                <div class="song-info">
                  <span class="song-index">{{ index + 1 }}</span>
                  <div class="song-details">
                    <span class="song-name">{{ getSongTitle(item.name) }}</span>
                    <span class="song-artist">{{ item.artist || '未知歌手' }}</span>
                  </div>
                </div>
                <div class="song-actions">
                  <el-icon class="action-icon" @click.stop="removeFromPlaylist(index)">
                    <Close />
                  </el-icon>
                </div>
              </li>
            </ul>
          </el-scrollbar>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, computed } from "vue";
import { useStore } from "vuex";
import mixin from "@/mixins/mixin";
import { List, Delete, Close } from "@element-plus/icons-vue";

export default defineComponent({
  components: {
    List,
    Delete,
    Close
  },
  setup() {
    const { proxy } = getCurrentInstance();
    const store = useStore();
    const { getSongTitle, playMusic } = mixin();

    const songId = computed(() => store.getters.songId);
    const currentPlayList = computed(() => store.getters.currentPlayList);
    const showAside = computed(() => store.getters.showAside);
    const isPlaying = computed(() => store.getters.isPlaying);

    const handleSongClick = (item: any, index: number) => {
      playMusic({
        id: item.id,
        url: item.url,
        pic: item.pic,
        index: index,
        name: item.name,
        lyric: item.lyric,
        currentSongList: currentPlayList.value,
      });
    };

    const clearPlaylist = () => {
      store.commit("clearCurrentPlayList");
    };

    const removeFromPlaylist = (index: number) => {
      store.commit("removeFromCurrentPlayList", index);
    };

    // 关闭播放列表
    const closePlaylist = () => {
      store.commit("setShowAside", false);
    };

    // 监听 ESC 键关闭
    const handleEscKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && showAside.value) {
        closePlaylist();
      }
    };

    // 添加键盘事件监听
    const addKeydownListener = () => {
      document.addEventListener('keydown', handleEscKey);
    };

    // 移除键盘事件监听
    const removeKeydownListener = () => {
      document.removeEventListener('keydown', handleEscKey);
    };

    // 组件挂载时添加事件监听
    addKeydownListener();

    return {
      songId,
      currentPlayList,
      showAside,
      isPlaying,
      getSongTitle,
      handleSongClick,
      clearPlaylist,
      removeFromPlaylist,
      closePlaylist,
    };
  },
});
</script>

<style lang="scss" scoped>
.playlist-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.3);
  z-index: 1000;
  display: flex;
  justify-content: flex-end;
}

.playlist-container {
  position: relative;
  width: 320px;
  height: 100%;
  background-color: #fff;
  box-shadow: -5px 0 15px rgba(0, 0, 0, 0.1);
  z-index: 1001;
  display: flex;
  flex-direction: column;
  transform: translateX(0);
  
  .dark & {
    background-color: #1e1e1e;
    box-shadow: -5px 0 15px rgba(0, 0, 0, 0.3);
  }
}

.playlist-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  
  .dark & {
    border-bottom-color: #333;
  }
}

.playlist-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  
  .title-icon {
    margin-right: 8px;
    font-size: 20px;
    color: var(--el-color-primary);
  }
  
  .dark & {
    color: #fff;
  }
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.playlist-info {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #888;
  
  .song-count {
    margin-right: 15px;
  }
  
  .dark & {
    color: #aaa;
  }
}

.clear-btn {
  padding: 0;
  color: #888;
  
  &:hover {
    color: var(--el-color-primary);
  }
  
  .dark & {
    color: #aaa;
    
    &:hover {
      color: var(--el-color-primary);
    }
  }
}

.close-btn {
  font-size: 20px;
  color: #999;
  cursor: pointer;
  padding: 5px;
  border-radius: 50%;
  transition: all 0.3s;
  
  &:hover {
    color: #333;
    background-color: #f0f0f0;
  }
  
  .dark & {
    color: #aaa;
    
    &:hover {
      color: #fff;
      background-color: #333;
    }
  }
}

.playlist-scrollbar {
  flex: 1;
}

.song-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.song-list li {
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid #f5f5f5;
  
  &:hover {
    background-color: #f9f9f9;
    
    .song-actions {
      opacity: 1;
    }
  }
  
  &.active {
    background-color: #f0f7ff;
    color: var(--el-color-primary);
    
    .song-name {
      font-weight: 600;
    }
  }
  
  &.playing {
    position: relative;
    
    &::before {
      content: "";
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 3px;
      background-color: var(--el-color-primary);
    }
  }
  
  .dark & {
    border-bottom-color: #333;
    
    &:hover {
      background-color: #282828;
    }
    
    &.active {
      background-color: #1a2a3a;
    }
  }
}

.song-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.song-index {
  width: 24px;
  color: #999;
  font-size: 14px;
  text-align: center;
  margin-right: 10px;
  
  .active & {
    color: var(--el-color-primary);
  }
  
  .dark & {
    color: #666;
  }
}

.song-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.song-name {
  font-size: 14px;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.song-artist {
  font-size: 12px;
  color: #888;
  
  .active & {
    color: var(--el-color-primary-light-3);
  }
  
  .dark & {
    color: #777;
  }
}

.song-actions {
  opacity: 0;
  transition: opacity 0.3s;
  
  .action-icon {
    color: #999;
    padding: 5px;
    border-radius: 50%;
    
    &:hover {
      color: var(--el-color-danger);
      background-color: rgba(0, 0, 0, 0.05);
    }
  }
  
  .dark & {
    .action-icon:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }
  }
}

/* 过渡动画 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  .playlist-container {
    transform: translateX(100%);
  }
  .playlist-overlay {
    opacity: 0;
  }
}

.slide-fade-enter-to,
.slide-fade-leave-from {
  .playlist-container {
    transform: translateX(0);
  }
  .playlist-overlay {
    opacity: 1;
  }
}

@media (max-width: 480px) {
  .playlist-container {
    width: 100%;
  }
}
</style>