<template>
  <div class="nav-container">
    <!-- 左侧Logo和导航 -->
    <div class="nav-left">
      <div class="logo" @click="goPage()">
        
        <yin-icon :icon="iconList.ERJI" class="logo-icon"></yin-icon>
        
        <span class="logo-text" style="color:rgb(64 158 255);">{{ musicName }}</span>
      </div>

      <!-- 主导航：PC显示 -->
      <yin-header-nav
        class="main-nav"
        :styleList="headerNavList"
        :activeName="activeNavName"
        @click="goPage"
      ></yin-header-nav>
    </div>

    <!-- 右侧功能区域 -->
    <div class="nav-right">
      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          placeholder="搜索音乐、歌手、专辑..."
          :prefix-icon="Search"
          v-model="keywords"
          @keyup.enter="goSearch()"
          clearable
        />
      </div>

      <!-- 暗黑模式切换 -->
      <div class="dark-mode-toggle" @click="toggleDarkMode">
        <el-tooltip :content="isDarkMode ? '切换到明亮模式' : '切换到暗黑模式'" placement="bottom">
          <el-icon :size="22" :color="isDarkMode ? '#FFD700' : '#606266'">
            <component :is="isDarkMode ? Sunny : Moon" />
          </el-icon>
        </el-tooltip>
      </div>

      <!-- 用户操作区域 -->
      <div class="user-actions">
        <yin-header-nav v-if="!token" :styleList="signList" :activeName="activeNavName" @click="goPage" class="auth-nav"></yin-header-nav>

        <el-dropdown v-if="token" trigger="click" class="user-dropdown">
          <div class="user-avatar">
            <el-avatar :size="36" :src="attachImageUrl(userPic)" />
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="(item, index) in menuList"
                :key="index"
                @click.stop="goMenuList(item.path)"
              >
                <el-icon v-if="item.icon" class="menu-item-icon">
                  <component :is="item.icon" />
                </el-icon>
                {{ item.name }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- 汉堡菜单按钮：移动端显示 -->
      <div class="hamburger-menu" @click="toggleMobileNav">
        <span :class="{ active: showMobileNav }"></span>
        <span :class="{ active: showMobileNav }"></span>
        <span :class="{ active: showMobileNav }"></span>
      </div>
    </div>

    <!-- 移动端导航弹出层 -->
    <transition name="slide-down">
      <div v-if="showMobileNav" class="mobile-nav">
        <div
          v-for="item in headerNavList"
          :key="item.name"
          class="mobile-nav-item"
          @click="goPage(item.path, item.name); toggleMobileNav()"
        >
          {{ item.name }}
        </div>
      </div>
    </transition>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, computed, onMounted, getCurrentInstance } from "vue";
import { Search, Sunny, Moon, User, Setting, SwitchButton } from "@element-plus/icons-vue";
import { useStore } from "vuex";
import YinIcon from "./YinIcon.vue";
import YinHeaderNav from "./YinHeaderNav.vue";
import mixin from "@/mixins/mixin";
import { SIGNLIST, MENULIST, Icon, MUSICNAME, RouterName, NavName, HEADERNAVLIST } from "@/enums";
import { HttpManager } from "@/api";

export default defineComponent({
  components: { YinIcon, YinHeaderNav, Sunny, Moon },
  setup() {
    const { proxy } = getCurrentInstance();
    const store = useStore();
    const { changeIndex, routerManager } = mixin();

    const musicName = ref(MUSICNAME);
    const headerNavList = ref(HEADERNAVLIST);
    const signList = ref(SIGNLIST);
    const menuList = ref(MENULIST.map(item => ({
      ...item,
      icon: item.name === '个人主页' ? User :
            item.name === '设置' ? Setting :
            item.name === '退出' ? SwitchButton : null
    })));
    const iconList = reactive({ ERJI: Icon.ERJI });
    const keywords = ref("");
    const activeNavName = computed(() => store.getters.activeNavName);
    const userPic = computed(() => store.getters.userPic);
    const token = computed(() => store.getters.token);
    const isDarkMode = ref(localStorage.getItem("darkMode") === "true");
    const showMobileNav = ref(false);

    function goPage(path?, name?) {
      if (!path && !name) {
        changeIndex(NavName.Home);
        routerManager(RouterName.Home, { path: RouterName.Home });
      } else {
        changeIndex(name);
        routerManager(path, { path });
      }
    }


    // 菜单列表退出登录
    function goMenuList(path) {
      if (path === RouterName.SignOut) {
        proxy.$store.commit("setToken", false);
        HttpManager.logout();
          (proxy as any).$message({ message: "退出成功", type: "success" });
          proxy.$store.commit("clearUserInfo");
              // 重置所有用户相关状态
        proxy.$store.commit("setToken", false);
        proxy.$store.commit("setUserId", null);
        proxy.$store.commit("setUsername", null);
        proxy.$store.commit("setUserPic", null);
        changeIndex(NavName.Home);
        routerManager(RouterName.Home, { path: RouterName.Home });
      
      } else {
        routerManager(path, { path });
      }
    }

    function goSearch() {
      if (keywords.value.trim() !== "") {
        proxy.$store.commit("setSearchWord", keywords.value);
        routerManager(RouterName.Search, { path: RouterName.Search, query: { keywords: keywords.value } });
      } else {
        (proxy as any).$message({ message: "搜索内容不能为空", type: "error" });
      }
    }

    const toggleDarkMode = () => {
      isDarkMode.value = !isDarkMode.value;
      localStorage.setItem("darkMode", isDarkMode.value.toString());
      document.documentElement.classList.toggle("dark", isDarkMode.value);
      window.dispatchEvent(new CustomEvent('darkModeToggle', { detail: isDarkMode.value }));
    };

    const toggleMobileNav = () => { showMobileNav.value = !showMobileNav.value; };

    onMounted(() => { document.documentElement.classList.toggle("dark", isDarkMode.value); });

    return {
      musicName,
      headerNavList,
      signList,
      menuList,
      iconList,
      keywords,
      activeNavName,
      userPic,
      token,
      isDarkMode,
      Search,
      Sunny,
      Moon,
      User,
      Setting,
      SwitchButton,
      showMobileNav,
      goPage,
      goMenuList,
      goSearch,
      toggleDarkMode,
      toggleMobileNav,
      attachImageUrl: HttpManager.attachImageUrl,
    };
  },
});
</script>

<style lang="scss" scoped>
@import "@/assets/css/var.scss";
@import "@/assets/css/global.scss";

.nav-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  z-index: 1000;
  transition: all 0.3s ease;

  .dark & { background-color: #121212; box-shadow: 0 2px 10px rgba(0,0,0,0.5); }
}

.nav-left { display: flex; align-items: center; gap: 40px; }
.logo { display:flex; align-items:center; cursor:pointer; transition: transform 0.2s; &:hover { transform: scale(1.05); } }
.logo-icon { @include icon(40px, $color-blue); margin-right:10px; }

.main-nav { flex:1; }
.nav-right { display:flex; align-items:center; gap:20px; }
.search-box { width:240px; transition: width 0.3s; &:focus-within { width:280px; } }
.search-box :deep(.el-input__wrapper) { border-radius:20px; box-shadow:0 1px 4px rgba(0,0,0,0.1); transition: all 0.3s; }
.search-box :deep(.el-input__wrapper:hover) { box-shadow:0 2px 8px rgba(0,0,0,0.15); }
.dark .search-box :deep(.el-input__wrapper) { background-color:#282828; box-shadow:0 1px 4px rgba(0,0,0,0.3); .el-input__inner{color:#fff;} .el-icon{color:#a0a0a0;} }

.dark-mode-toggle { width:36px;height:36px;display:flex;align-items:center;justify-content:center;border-radius:50%;cursor:pointer;transition:0.3s; &:hover{background-color: rgba(0,0,0,0.05);} .dark &:hover{background-color: rgba(255,255,255,0.1);} }

.user-actions { display:flex; align-items:center; gap:15px; }
.auth-nav { display:flex; gap:15px; }
.user-dropdown { cursor:pointer; }
.user-avatar { transition:transform 0.2s; &:hover { transform:scale(1.1); } }
.menu-item-icon { margin-right:8px; font-size:16px; }

.hamburger-menu { display:none; flex-direction:column; justify-content:space-between; width:22px; height:18px; cursor:pointer; margin-left:10px; 
  span { display:block; height:3px; width:100%; background:$color-black; border-radius:2px; transition: all 0.3s; }
  span.active:nth-child(1) { transform: rotate(45deg) translate(3px,3px); }
  span.active:nth-child(2) { opacity:0; }
  span.active:nth-child(3) { transform: rotate(-45deg) translate(3px,-3px); }
  .dark & span { background:#fff; }
}

.mobile-nav { position:absolute; top:64px; left:0; right:0; background:white; z-index:999; box-shadow:0 2px 10px rgba(0,0,0,0.15); padding:10px 0; }
.mobile-nav-item { padding:10px 20px; cursor:pointer; &:hover { background: rgba(0,0,0,0.05); } }

.slide-down-enter-active, .slide-down-leave-active { transition: all 0.3s ease; }
.slide-down-enter-from, .slide-down-leave-to { transform: translateY(-100%); opacity:0; }
.slide-down-enter-to, .slide-down-leave-from { transform: translateY(0); opacity:1; }

@media (max-width:992px) { .nav-container { padding:0 16px; } .nav-left { gap:20px; } .search-box{width:180px;&:focus-within{width:220px;}} }
@media (max-width:768px) { .logo-text{display:none;} .search-box{width:150px;&:focus-within{width:180px;}} .main-nav{display:none;} .hamburger-menu{display:flex;} }
@media (max-width:576px) { .search-box{display:none;} .nav-right{gap:12px;} }
</style>
