<template>
  
  <d-s-header></d-s-header>
  <!-- <yin-aside></yin-aside> -->
  <d-s-aside></d-s-aside>
  <div class="content-box" :class="{ 'content-collapse': collapse }">
    <router-view></router-view>

    <el-affix position="bottom" style="height: 59px; width: 300px; bottom: 0px; z-index: 100;">
    <!-- <yin-audio></yin-audio> -->
    <d-s-audio></d-s-audio>
    </el-affix>
    
  </div>
  


</template>

<script lang="ts" setup>
import { ref } from "vue";
import DSHeader from "@/components/layouts/DSHeader.vue";
import DSAside from "@/components/layouts/DSAside.vue";
import DSAudio from "@/components/layouts/DSAudio.vue";

import emitter from "@/utils/emitter";

const collapse = ref(false);
emitter.on("collapse", (msg) => {
  collapse.value = msg as boolean;
});
</script>

<style scoped>
.content-box {
  position: absolute;
  left: 150px;
  right: 0;
  top: 60px;
  bottom: 0;
  overflow-y: scroll;
  transition: left 0.3s ease-in-out;
  padding: 20px;
}

.content-collapse {
  left: 65px;
}
</style>
