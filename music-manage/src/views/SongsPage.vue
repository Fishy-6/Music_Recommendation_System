<template>
  <div class="main-wrapper">
    <div class="breadcrumb-bar">
      <el-breadcrumb separator-icon="ArrowRight">
        <el-breadcrumb-item v-for="item in breadcrumbList" :key="item.path"
          :to="{ path: item.path, query: item.query }">
          {{ item.name }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="container">
      <div class="handle-box">
        <div class="left-panel">
          <el-button type="danger" :icon="Delete" @click="handleBatchDelete"
            :disabled="multipleSelection.length === 0">批量删除</el-button>
          <el-input v-model="searchWord" :prefix-icon="Search" placeholder="筛选歌曲" class="handle-input"></el-input>
        </div>
        <div class="right-panel">
          <el-button type="primary" :icon="Plus" @click="isAddVisible = true">添加歌曲</el-button>
        </div>
      </div>

      <el-table border size="small" stripe v-loading="loading" :data="paginatedData"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="40" align="center"></el-table-column>
        <el-table-column label="ID" prop="id" width="60" align="center"></el-table-column>

        <el-table-column label="封面/播放" width="100" align="center">
          <template #default="scope">
            <div class="song-cover-box" @click="togglePlay(scope.row)">
              <el-image :src="attachImageUrl(scope.row.pic)" class="cover-img" fit="cover">
                <template #error>
                  <div class="image-slot"><el-icon>
                      <Picture />
                    </el-icon></div>
                </template>
              </el-image>
              <div class="play-mask" :class="{ 'is-playing': isCurrentSong(scope.row.id) && isPlay }">
                <el-icon class="play-icon">
                  <component :is="isCurrentSong(scope.row.id) && isPlay ? VideoPause : VideoPlay" />
                </el-icon>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="歌名" prop="name" min-width="150"></el-table-column>
        <el-table-column label="歌手" prop="singerName" min-width="100"></el-table-column>
        <el-table-column label="专辑/简介" prop="introduction" min-width="150" show-overflow-tooltip></el-table-column>

        <el-table-column label="状态" width="120" align="center">
          <template #default="scope">
            <el-dropdown trigger="click" @command="(command) => changeSongStatus(scope.row, command)">
              <el-tag :type="getSongStatusType(scope.row.type)" effect="light" style="cursor: pointer;">
                {{ getSongStatusLabel(scope.row.type) }}
                <el-icon class="el-icon--right">
                  <ArrowDown />
                </el-icon>
              </el-tag>

              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :disabled="scope.row.type === 1" :command="1">启用</el-dropdown-item>
                  <el-dropdown-item :disabled="scope.row.type === 2" :command="2">隐藏</el-dropdown-item>
                  <el-dropdown-item :disabled="scope.row.type === 3" :command="3">审核中</el-dropdown-item>
                  <el-dropdown-item :disabled="scope.row.type === 4" :command="4">禁用</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>

        <el-table-column label="歌词" align="center" width="100">
          <template #default="scope">
            <el-popover placement="left" title="歌词预览" :width="300" trigger="hover">
              <template #reference>
                <el-tag>查看歌词</el-tag>
              </template>
              <div class="lyrics-preview">
                {{ scope.row.lyric || '暂无歌词' }}
              </div>
            </el-popover>
          </template>
        </el-table-column>

        <el-table-column label="资源更新" width="180" align="center">
          <template #default="scope">
            <div class="upload-group">
              <el-tooltip content="更新封面" placement="top">
                <el-upload class="upload-item" :action="updateSongImg(scope.row.id)" :show-file-list="false"
                  :on-success="handleImgSuccess" :before-upload="beforeImgUpload">
                  <el-button circle size="small" type="success" plain :icon="Picture" />
                </el-upload>
              </el-tooltip>

              <el-tooltip content="更新音频文件" placement="top">
                <el-upload class="upload-item" :action="updateSongUrl(scope.row.id)" :show-file-list="false"
                  :on-success="handleSongSuccess" :before-upload="beforeSongUpload">
                  <el-button circle size="small" type="primary" plain :icon="Headset" />
                </el-upload>
              </el-tooltip>

              <el-tooltip content="更新LRC歌词" placement="top">
                <el-upload class="upload-item" :action="updateSongLrc(scope.row.id)" :show-file-list="false"
                  :on-success="handleSongSuccess" :before-upload="beforeSongUpload">
                  <el-button circle size="small" type="warning" plain :icon="Document" />
                </el-upload>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评论" width="80" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="goCommentPage(scope.row.id)">查看</el-button>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="140" align="center">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="editRow(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" :icon="Delete"
              @click="handleSingleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-box">
        <el-pagination background layout="total, prev, pager, next" :current-page="currentPage" :page-size="pageSize"
          :total="total" @current-change="handleCurrentChange">
        </el-pagination>
      </div>
    </div>

    <el-dialog title="添加歌曲" v-model="isAddVisible" width="500px" destroy-on-close>
      <el-form label-width="100px" :model="registerForm" ref="addFormRef">
        <el-form-item label="选择歌手" prop="singerId">
          <el-select v-model="registerForm.singerId" placeholder="请选择歌手" style="width: 100%">
            <el-option v-for="singer in singers" :key="singer.id" :label="singer.name" :value="singer.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="歌曲名" prop="name">
          <el-input v-model="registerForm.name" placeholder="请输入歌曲名"></el-input>
        </el-form-item>
        <el-form-item label="专辑/简介" prop="introduction">
          <el-input v-model="registerForm.introduction" placeholder="专辑名称"></el-input>
        </el-form-item>
        <el-form-item label="歌词文本" prop="lyric">
          <el-input type="textarea" :rows="3" v-model="registerForm.lyric" placeholder="可选：直接粘贴歌词文本"></el-input>
        </el-form-item>

        <el-form-item label="歌词文件(.lrc)">
          <el-upload class="upload-demo" action="" :auto-upload="false" :limit="1" accept=".lrc"
            :on-change="(file) => handleFileChange(file, 'lrc')" :on-remove="() => handleFileRemove('lrc')">
            <el-button type="primary" plain>选择LRC文件</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="歌曲文件(.mp3)">
          <el-upload class="upload-demo" action="" :auto-upload="false" :limit="1" accept=".mp3,.flac,.wav"
            :on-change="(file) => handleFileChange(file, 'song')" :on-remove="() => handleFileRemove('song')">
            <el-button type="primary">选择音频文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="isAddVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitAddSong">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="编辑歌曲" v-model="editVisible" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="歌曲名">
          <el-input v-model="editForm.name"></el-input>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="editForm.introduction"></el-input>
        </el-form-item>
        <el-form-item label="歌词">
          <el-input type="textarea" :rows="6" v-model="editForm.lyric"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editVisible = false">取 消</el-button>
          <el-button type="primary" @click="saveEdit">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, watch, onMounted } from "vue";
import { useStore } from "vuex";
import { ElMessage, ElMessageBox } from "element-plus";
import { 
  Delete, Search, Plus, Edit, Picture, Headset, Document, 
  VideoPlay, VideoPause, ArrowRight, ArrowDown
} from '@element-plus/icons-vue';
import mixin from "@/mixins/mixin";
import { HttpManager } from "@/api";
import { RouterName } from "@/enums";
import axios from 'axios';

// --- Setup & Hooks ---
const store = useStore();
const { routerManager, beforeImgUpload, beforeSongUpload } = mixin();

// --- State ---
const loading = ref(false);
const tableData = ref([]);
const filteredData = ref([]);
const searchWord = ref("");
const pageSize = ref(7);
const currentPage = ref(1);
const total = ref(0); // 添加总数据量
const multipleSelection = ref([]);
const singers = ref([]);

// 弹窗控制
const isAddVisible = ref(false);
const editVisible = ref(false);

// 表单数据
const registerForm = reactive({
  singerId: "",
  name: "",
  introduction: "",
  lyric: "",
});

// 暂存上传的文件
const tempFiles = reactive({
  lrc: null,
  song: null
});

const editForm = reactive({
  id: "",
  singerId: "",
  name: "",
  introduction: "",
  lyric: "",
  type: "",
  singerName: "",
});

// --- Computed ---
const breadcrumbList = computed(() => {
  return [
    { path: "/info", name: "系统首页" },
    { path: "", name: "所有歌曲" }
  ];
});

const isPlay = computed(() => store.getters.isPlay);

const paginatedData = computed(() => {
  // 直接返回过滤后的数据，因为我们现在是从后端获取的分页数据
  return filteredData.value;
});

// --- Methods ---
const isCurrentSong = (songId) => {
  return store.getters.id === songId;
};

// 歌曲状态: 启用1 隐藏2 审核3 禁用4
function getSongStatusType(type: number) {
  switch (type) {
    case 1: return 'success'; // 启用
    case 2: return 'info';    // 隐藏
    case 3: return 'warning'; // 审核中
    case 4: return 'danger';  // 禁用
    default: return 'info';
  }
}

function getSongStatusLabel(type: number) {
  switch (type) {
    case 1: return '启用';
    case 2: return '隐藏';
    case 3: return '审核中';
    case 4: return '禁用';
    default: return '未知';
  }
}

// 1. 获取数据 (修改为分页获取)
async function getData() {
  loading.value = true;
  try {
    // 分页获取歌曲
    const songResult = (await HttpManager.getSongPages({
      currentPage: currentPage.value,
      pageSize: pageSize.value,
      name: searchWord.value
    })) as any;

    // 获取所有歌手
    const singerResult = (await HttpManager.getAllSinger()) as any;
    
    // 建立歌手ID到名称的映射
    const singerMap = {};
    singerResult.data.forEach(singer => {
      singerMap[singer.id] = singer.name;
    });
    
    // 为每首歌曲添加歌手名称
    const songsWithSingerName = songResult.data.records.map(song => ({
      ...song,
      singerName: singerMap[song.singerId] || '未知歌手'
    }));
    
    tableData.value = songsWithSingerName;
    filteredData.value = songsWithSingerName;
    total.value = songResult.data.total; // 设置总数据量
    
    // 更新歌手列表用于添加歌曲
    singers.value = singerResult.data;
  } catch (e) {
    console.error(e);
    ElMessage.error("获取数据失败");
  } finally {
    loading.value = false;
  }
}

// 初始化
store.commit("setIsPlay", false);
onMounted(() => {
  getData();
});

// 监听搜索 (修改搜索逻辑)
watch(searchWord, (newVal) => {
  currentPage.value = 1;
  getData(); // 直接调用分页接口重新获取数据
});

// 2. 播放控制
function togglePlay(row) {
  const url = row.url;
  
  if (store.getters.url === url && isPlay.value) {
     store.commit("setIsPlay", false);
  } else {
     store.commit("setUrl", url);
     store.commit("setId", row.id);
     store.commit("setIsPlay", true);
  }
}

// 3. 新增歌曲
function handleFileChange(file, type) {
  tempFiles[type] = file.raw;
}

function handleFileRemove(type) {
  tempFiles[type] = null;
}

async function submitAddSong() {
  if(!registerForm.singerId || !registerForm.name || !tempFiles.song) {
    ElMessage.error("歌手、歌曲名和音频文件为必填项");
    return;
  }

  const formData = new FormData();
  formData.append("singerId", registerForm.singerId);
  formData.append("name", registerForm.name);
  formData.append("introduction", registerForm.introduction);
  formData.append("lyric", registerForm.lyric || "[00:00:00]暂无歌词");
  
  if (tempFiles.song) formData.append("file", tempFiles.song);
  if (tempFiles.lrc) formData.append("lrcfile", tempFiles.lrc);

  try {
    const res = await axios.post(HttpManager.attachImageUrl(`/song/add`), formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    
    const result = res.data;
    ElMessage({
      message: result.message,
      type: result.type,
    });

    if (result.success) {
      getData();
      isAddVisible.value = false;
    }
  } catch (error) {
    console.error(error);
    ElMessage.error("上传失败");
  }
}

// 4. 编辑歌曲
function editRow(row) {
  Object.assign(editForm, row);
  // 如果歌曲名包含歌手名前缀，则只显示不包含前缀的部分
  if (editForm.name.startsWith(editForm.singerName + '-')) {
    editForm.name = editForm.name.substring(editForm.singerName.length + 1);
  }
  editVisible.value = true;
}

async function saveEdit() {
  // 名字格式：歌手名-歌名
  editForm.name = `${editForm.singerName}-${editForm.name}`;
  const result = (await HttpManager.updateSongMsg(editForm)) as any;
  ElMessage({
     message: result.message,
     type: result.type,
  });
  if (result.success) getData();
  editVisible.value = false;
}

// 5. 删除 (单条 + 批量)
function handleSelectionChange(val) {
  multipleSelection.value = val;
}

function handleSingleDelete(id) {
  ElMessageBox.confirm('确定删除这首歌吗？', '警告', {
    type: 'warning'
  }).then(async () => {
    const result = await HttpManager.deleteSong(id) as any;
    if (result.success) {
      ElMessage.success("删除成功");
      getData();
    } else {
      ElMessage.error("删除失败");
    }
  }).catch(() => {
    ElMessage.error("删除失败");
  });
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确定删除选中的 ${multipleSelection.value.length} 首歌吗？`, '批量删除', {
    type: 'warning'
  }).then(async () => {
    const promises = multipleSelection.value.map(item => HttpManager.deleteSong(item.id));
    await Promise.all(promises);
    ElMessage.success("批量删除执行完毕");
    getData();
    multipleSelection.value = [];
  }).catch(() => {
    ElMessage.error("批量删除失败");
  });
}

// 6. 辅助函数
function attachImageUrl(url) {
  return HttpManager.attachImageUrl(url);
}

function updateSongImg(id) {
  return HttpManager.attachImageUrl(`/song/img/update?id=${id}`);
}

function updateSongUrl(id) {
  return HttpManager.attachImageUrl(`/song/url/update?id=${id}`);
}

function updateSongLrc(id) {
  return HttpManager.attachImageUrl(`/song/lrc/update?id=${id}`);
}

function handleImgSuccess(res) {
  ElMessage({ message: res.message, type: res.type });
  if (res.success) getData();
}

function handleSongSuccess(res) {
  ElMessage({ message: res.message, type: res.type });
  if (res.success) getData();
}

// 翻页处理
function handleCurrentChange(val) {
  currentPage.value = val;
  getData();
}

async function changeSongStatus(row, newType: number) {
  if (row.type === newType) return;

  ElMessageBox.confirm(`确定将歌曲《${row.name}》的状态修改为【${getSongStatusLabel(newType)}】吗？`, '状态变更', {
    type: 'warning'
  }).then(async () => {
    const result = (await HttpManager.updateSongStatus(row.id, newType)) as any;
    
    ElMessage({
      message: result.message,
      type: result.type,
    });
    
    if (result.success) {
      getData();
    }
  }).catch(() => {
    // 用户取消操作
  });
}

// 路由跳转
function goCommentPage(id) {
  routerManager(RouterName.Comment, { path: RouterName.Comment, query: { id, type: 0 } });
}
</script>

<style scoped>
/* 全局布局 */
.main-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding-bottom: 80px; /* 为底部固定音频播放器预留空间 */
}

.breadcrumb-bar {
  margin-bottom: 15px;
  padding-left: 5px;
}

.container {
  flex: 1;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

/* 顶部操作栏 */
.handle-box {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-panel, .right-panel {
  display: flex;
  gap: 10px;
  align-items: center;
}

.handle-input {
  width: 200px;
}

/* 封面与播放按钮样式 */
.song-cover-box {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
}

.cover-img {
  width: 100%;
  height: 100%;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  color: #909399;
}

.play-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.song-cover-box:hover .play-mask,
.play-mask.is-playing {
  opacity: 1;
}

.play-icon {
  font-size: 24px;
  color: white;
}

/* 上传按钮组 */
.upload-group {
  display: flex;
  justify-content: center;
  gap: 5px;
}

.upload-item {
  display: inline-block;
}

/* 分页 */
.pagination-box {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 歌词预览 */
.lyrics-preview {
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
}
</style>