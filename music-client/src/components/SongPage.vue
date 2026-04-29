<template>
  <div class="play-list">
    <div class="play-title" v-if="title">{{ title }}</div>
    <ul class="play-body">
      <li class="card-frame" v-for="(item, index) in playList" :key="index">
        <div class="card" @click="handleCardClick(item, $event)">
          <el-image class="card-img" fit="contain" :src="attachImageUrl(item.pic)" />
          <div class="mask" @click="handleCardClick(item, $event)">
            <yin-icon class="mask-icon" :icon="BOFANG"></yin-icon>
          </div>
          <transition name="note-float" @after-leave="removeNote(index)">
            <div 
              v-if="showNotes.includes(index)"
              class="floating-note"
              :style="getNoteStyle(index)">
              ♫
            </div>
          </transition>
        </div>
        <p class="card-name">{{ item.name || item.title }}</p>
      </li>
    </ul>
  </div>
</template>

<script lang="ts">
import { defineComponent, getCurrentInstance, toRefs, ref } from "vue";

import YinIcon from "@/components/layouts/YinIcon.vue";
import mixin from "@/mixins/mixin";
import { Icon } from "@/enums";
import { HttpManager } from "@/api";

export default defineComponent({
  components: {
    YinIcon,
  },
  props: {
    title: String,
    playList: Array,
    path: String,
  },
  setup(props) {
    const { proxy } = getCurrentInstance();
    const { path } = toRefs(props);
    const { getSongTitle, getSingerName, playMusic, checkStatus, downloadMusic } = mixin();
    
    // 音符动画相关数据
    const showNotes = ref<number[]>([]);
    const notePositions = ref<{[key: number]: {x: number, y: number}}>({});

    function handleClick(row) {
      
      
      playMusic({
        id: row.id,
        url: row.url,
        pic: row.pic,
        index: row.index,
        name: row.name,
        lyric: row.lyric,
        currentSongList: null,
      });
      
    }
    
    function handleCardClick(row: any, event: MouseEvent) {
      handleClick(row);
    }
    function createNoteAnimation(event: MouseEvent, index: number) {
      // 记录点击位置
      notePositions.value[index] = {
        x: event.clientX,
        y: event.clientY
      };
      showNotes.value.push(index);
      
      // 3秒后移除音符
      setTimeout(() => {
        showNotes.value = showNotes.value.filter(i => i !== index);
      }, 3000);
    }
    
    function removeNote(index: number) {
      delete notePositions.value[index];
    }
    
    function getNoteStyle(index: number) {
      const pos = notePositions.value[index];
      if (!pos) return {};
      return {
        position: 'fixed',
        left: `${pos.x}px`,
        top: `${pos.y}px`
      };
    }

    return {
      BOFANG: Icon.BOFANG,
      attachImageUrl: HttpManager.attachImageUrl,
      handleClick,
      showNotes,
      getNoteStyle,
      removeNote,
      handleCardClick
    };
  },
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";
@import "@/assets/css/global.scss";

.play-list {
  padding: 0 1rem;

  .play-title {
    height: 60px;
    line-height: 60px;
    font-size: 28px;
    font-weight: 500;
    text-align: center;
    color: $color-black;
    box-sizing: border-box;
  }

  .play-body {
    @include layout(flex-start, stretch, row, wrap);
  }
}

.card-frame {
  .card {
    position: relative;
    height: 0;
    padding-bottom: 100%;
    overflow: hidden;
    border-radius: 5px;

    .card-img {
      width: 100%;
      transition: all 0.4s ease;
    }
  }

  .card-name {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    margin: 0.5rem 0;
  }

  &:hover .card-img {
    transform: scale(1.2);
  }
}

.mask {
  position: absolute;
  top: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 5px;
  background-color: rgba(52, 47, 41, 0.4);
  @include layout(center, center);
  transition: all 0.3s ease-in-out;
  opacity: 0;

  .mask-icon {
    @include icon(2em, rgba(240, 240, 240, 1));
  }

  &:hover {
    opacity: 1;
    cursor: pointer;
  }
}

.floating-note {
  position: fixed;
  font-size: 24px;
  color: #ff6b6b;
  z-index: 9999;
  pointer-events: none;
  animation: floatDown 3s ease-out forwards;
}

@keyframes floatDown {
  0% {
    transform: translate(0, 0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translate(0, 100vh) rotate(360deg);
    opacity: 0;
  }
}

.note-float-enter-active {
  animation: floatDown 3s ease-out forwards;
}

.note-float-leave-active {
  animation: floatDown 3s ease-out forwards;
}

@media screen and (min-width: $sm) {
  .card-frame {
    width: 18%;
    margin: 0.5rem 1%;
  }
}

@media screen and (max-width: $sm) {
  .card-frame {
    width: 46%;
    margin: 0.5rem 2%;
  }
}
</style>