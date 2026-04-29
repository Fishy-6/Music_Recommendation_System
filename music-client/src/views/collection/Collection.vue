<!-- <template>
    <el-container>
      <p>没做</p>
      //src\views\singer\SingerDetail.vue
    </el-container>
  </template>
   -->

<template>
  <div v-if="token">
    <!-- 正常显示收藏页面内容 -->
    <el-container>

      <el-aside class="album-slide">
        <el-image class="singer-img" fit="contain" :src="attachImageUrl(userPic)" />
        <div class="album-info">
          <h2>我的收藏</h2>
          <ul>
            <li v-if="personalInfo.userSex">
              性别：
              <span v-if="personalInfo.userSex == '1'">男</span>
              <span v-else-if="personalInfo.userSex == '2'">女</span>
              <span v-else-if="personalInfo.userSex == '3'">其他</span>
              <span v-else>未知</span>
            </li>
            <li>生日：{{ personalInfo.birth }}</li>
            <li>故乡：{{ personalInfo.location }}</li>
          </ul>
        </div>
      </el-aside>
      <el-main class="album-main">
        <h1>{{ personalInfo.username }}</h1>
        <p>{{ personalInfo.introduction }}</p>
        <song-list :songList="collectSongList" :show="true" @changeData="changeData"></song-list>
      </el-main>
    </el-container>
  </div>

  <div v-else>
    <p>请先 <a @click="goLogin">登录</a> 查看收藏内容</p>
  </div>
</template>

<script lang="ts">
import { defineComponent, nextTick,computed, onBeforeMount, ref, watch, reactive } from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import { RouterName } from "@/enums/router-name";



import { Edit } from "@element-plus/icons-vue";
import SongList from "@/components/SongList.vue";
import Upload from "../setting/Upload.vue";
import mixin from "@/mixins/mixin";
import { HttpManager } from "@/api";

import { onMounted } from "vue";



export default defineComponent({
  components: {
    SongList,
    
    //Upload,
  },
  setup() {
    const store = useStore();
    const router = useRouter();
    const token = computed(() => store.getters.token);

    const dialogTableVisible = ref(false);
    const collectSongList = ref([]); // 收藏的歌曲
    const personalInfo = reactive({
      username: "",
      userSex: "",
      birth: "",
      location: "",
      introduction: "",
    });
    const userId = computed(() => store.getters.userId);
    const userPic = computed(() => store.getters.userPic);
    watch(userPic, () => {
      dialogTableVisible.value = false;
    });



    onMounted(async () => {
      if (token.value && userId.value) {
        await getUserInfo(userId.value);
      } else {
        goLogin();
      }
    });

    const goLogin = () => {
      router.push({
        path: RouterName.SignIn,
        query: { redirect: RouterName.Collection },
      });
    };

    onBeforeMount(() => {
      if (!token.value) {
        goLogin();
      }
    });


    async function getUserInfo(id) {
      const result = (await HttpManager.getUserOfId(id)) as ResponseBody;
      personalInfo.username = result.data[0].username;
      personalInfo.userSex = result.data[0].sex;
      personalInfo.birth = result.data[0].birth;
      personalInfo.introduction = result.data[0].introduction;
      personalInfo.location = result.data[0].location;
  
    }
    // 获取收藏的歌曲
    async function getCollection(userId) {
      collectSongList.value = []
      const result = (await HttpManager.getCollectionOfUser(userId)) as ResponseBody;
      const collectIDList = result.data || []; // 存放收藏的歌曲ID
      // 通过歌曲ID获取歌曲信息
      for (const item of collectIDList) {
        if (!item.songId) {
          console.error(`歌曲${item}异常`);
          continue;
        }
        const result = (await HttpManager.getSongOfId(item.songId)) as ResponseBody;
        collectSongList.value.push(result.data[0]);
      }
    }

    function changeData() {
      getCollection(userId.value);
    }

    nextTick(() => {
      getUserInfo(userId.value);
      getCollection(userId.value);
    });

    return {
      token,
      goLogin,
      userPic,
      personalInfo,
      changeData,
      collectSongList,
      attachImageUrl: HttpManager.attachImageUrl,
    };
  },


});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";

.album-slide {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 20px;

  .singer-img {
    height: 250px;
    width: 250px;
    border-radius: 50%;
  }

  .album-info {
    width: 60%;
    padding-top: 2rem;
    li {
      width: 100%;
      height: 30px;
      line-height: 30px;
    }
  }
}

.album-main {
  p {
    color: rgba(0, 0, 0, 0.5);
    margin: 10px 0 20px 0px;
  }
}

@media screen and (min-width: $sm) {
  .album-slide {
    position: fixed;
    width: 400px;
  }
  .album-main {
    min-width: 600px;
    padding-right: 10vw;
    margin-left: 400px;
  }
}

@media screen and (max-width: $sm) {
  .album-slide {
    display: none;
  }
}

//图片旋转----------------------------------------------
@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.singer-img {
  height: 250px;
  width: 250px;
  border-radius: 50%;
  animation: rotate 5s linear infinite; // 每5秒旋转一圈，线性匀速，无限循环
}
//图片旋转=--------------------------------------------

</style>
