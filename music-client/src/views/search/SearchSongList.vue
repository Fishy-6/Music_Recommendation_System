<template>
  <div class="search-song-list">
    <play-list :playList="playList" path="song-sheet-detail"></play-list>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, watch, onMounted } from "vue";
import { useStore } from "vuex";
import PlayList from "@/components/PlayList.vue";
import { HttpManager } from "@/api";
import { useRoute } from "vue-router"; 

export default defineComponent({
  components: {
    PlayList,
  },
  setup() {
    const store = useStore();
    const route = useRoute();  // 使用 useRoute 获取路由信息
    
    const playList = ref([]);
    const searchWord = computed(() => store.getters.searchWord);
    
    watch(searchWord, (value) => {
      getSearchList(value);
    });

    async function getSearchList(value) {
      if (!value) return;
      const result = (await HttpManager.getSongListOfLikeTitle(value)) as ResponseBody;
      if (!result.data.length) {
        // 这里可以替换成你喜欢的提示方式
        console.warn("暂无该歌曲内容");
      } else {
        playList.value = result.data;
      }
    }

    onMounted(() => {
      // 直接从 route.query 获取参数
      getSearchList(route.query.keywords as string);
    });

    return {
      playList,
    };
  },
});
</script>