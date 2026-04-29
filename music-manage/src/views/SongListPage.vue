<template>
  <div class="container">
    <div class="handle-box">
      <div class="left-panel">
        <el-button type="danger" :icon="Delete" @click="handleBatchDelete" :disabled="multipleSelection.length === 0">批量删除</el-button>
        <el-input v-model="searchWord" :prefix-icon="Search" placeholder="筛选关键词" class="handle-input"></el-input>
      </div>
      <div class="right-panel">
        <el-button type="primary" :icon="Plus" @click="centerDialogVisible = true">添加歌单</el-button>
        <el-button type="success" :icon="Download" @click="exportPlaylist">导出歌单</el-button>
      </div>
    </div>

    <el-table
      height="550px"
      border
      size="small"
      stripe
      v-loading="loading"
      :data="paginatedData"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="40" align="center"></el-table-column>
      <el-table-column label="ID" prop="id" width="60" align="center"></el-table-column>
      <el-table-column label="歌单图片" width="120" align="center">
        <template #default="scope">
          <div class="img-upload-box">
            <el-image :src="attachImageUrl(scope.row.pic)" class="table-img" fit="cover">
              <template #error>
                <div class="image-slot">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <el-upload
              class="upload-mask"
              :action="uploadUrl(scope.row.id)"
              :show-file-list="false"
              :on-success="handleImgSuccess"
              :before-upload="beforeImgUpload"
            >
              <el-icon class="upload-icon"><Upload /></el-icon>
              <span>更新</span>
            </el-upload>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="title" label="标题" width="180"></el-table-column>
      
      <el-table-column label="简介" min-width="200">
        <template #default="scope">
          <el-tooltip effect="dark" :content="scope.row.introduction" placement="top">
            <p class="introduction-text">{{ scope.row.introduction }}</p>
          </el-tooltip>
        </template>
      </el-table-column>
      
      <el-table-column label="风格" prop="style" width="100" align="center">
        <template #default="scope">
           <el-tag effect="plain">{{ scope.row.style || '无' }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="状态" prop="type" width="100" align="center">
        <template #default="scope">
           <el-tag :type="getStatusType(scope.row.type)">
             {{ getStatusLabel(scope.row.type) }}
           </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="详情" width="160" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="goContentPage(scope.row.id)">内容管理</el-button>
          <el-button link type="success" @click="goCommentPage(scope.row.id)">评论管理</el-button>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button size="small" :icon="Edit" @click="editRow(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" :icon="Delete" @click="handleSingleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-box">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="filteredData.length"
        @current-change="handleCurrentChange"
      >
      </el-pagination>
    </div>

    <el-dialog title="添加歌单" v-model="centerDialogVisible" width="400px" destroy-on-close>
      <el-form label-width="80px" :model="registerForm" ref="addFormRef">
        <el-form-item label="歌单名" prop="title">
          <el-input v-model="registerForm.title"></el-input>
        </el-form-item>
        <el-form-item label="歌单介绍" prop="introduction">
          <el-input type="textarea" v-model="registerForm.introduction"></el-input>
        </el-form-item>
        <el-form-item label="风格" prop="style">
          <el-input v-model="registerForm.style"></el-input>
        </el-form-item>
        </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="centerDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="addSongList">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="编辑歌单" v-model="editVisible" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title"></el-input>
        </el-form-item>
        <el-form-item label="简介">
          <el-input type="textarea" :rows="3" v-model="editForm.introduction"></el-input>
        </el-form-item>
        <el-form-item label="风格">
          <el-input v-model="editForm.style"></el-input>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.type">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="2">审核中</el-radio>
            <el-radio :label="3">禁用</el-radio>
          </el-radio-group>
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
import { computed, reactive, ref, watch, getCurrentInstance } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus';
import { Delete, Search, Plus, Download, Edit, Picture, Upload } from '@element-plus/icons-vue';
import mixin from "@/mixins/mixin";
import { HttpManager } from "@/api/index";
import { RouterName } from "@/enums";
import axios from 'axios';
import {getBaseURL} from '@/api/request'

// 引入 mixin
const { routerManager, beforeImgUpload } = mixin();
const { proxy } = getCurrentInstance(); // 仅保留用于 store 操作

// 数据定义
const tableData = ref([]); // 原始数据
const filteredData = ref([]); // 搜索过滤后的数据
const loading = ref(false);
const pageSize = ref(7); // 每页显示数量
const currentPage = ref(1);
const searchWord = ref("");
const multipleSelection = ref([]);

// 弹窗控制
const centerDialogVisible = ref(false);
const editVisible = ref(false);

// 表单对象
const registerForm = reactive({
  title: "",
  introduction: "",
  style: "",
  type: 1 // 默认添加为启用状态
});

// ✨ 编辑表单增加 type 字段
const editForm = reactive({
  id: "",
  title: "",
  pic: "",
  introduction: "",
  style: "",
  type: 1,
  consumer: "" 
});

// 计算当前页显示的数据
const paginatedData = computed(() => {
  return filteredData.value.slice(
    (currentPage.value - 1) * pageSize.value,
    currentPage.value * pageSize.value
  );
});

// 监听搜索
watch(searchWord, (newVal) => {
  currentPage.value = 1;
  if (!newVal) {
    filteredData.value = tableData.value;
  } else {
    filteredData.value = tableData.value.filter(item => 
      item.title.includes(newVal)
    );
  }
});

// ✨ 辅助函数：获取状态对应的 Tag 类型颜色
const getStatusType = (type) => {
  if (type === 1) return 'success'; // 启用 - 绿色
  if (type === 2) return 'warning'; // 审核中 - 橙色
  if (type === 3) return 'danger';  // 禁用 - 红色
  return 'info';
};

// ✨ 辅助函数：获取状态对应的文字
const getStatusLabel = (type) => {
  if (type === 1) return '启用';
  if (type === 2) return '审核中';
  if (type === 3) return '禁用';
  return '未知';
};

// 初始化获取数据
getData();

async function getData() {
  loading.value = true;
  tableData.value = [];
  filteredData.value = [];
  try {
    const result = (await HttpManager.getSongList()) as any;
    tableData.value = result.data;
    filteredData.value = result.data;
  } catch (error) {
    ElMessage.error("获取歌单数据失败");
  } finally {
    loading.value = false;
  }
}

// 翻页
function handleCurrentChange(val) {
  currentPage.value = val;
}

// 图片相关
function attachImageUrl(url) {
  return HttpManager.attachImageUrl(url);
}

function uploadUrl(id) {
  return HttpManager.attachImageUrl(`/songList/img/update?id=${id}`);
}

function handleImgSuccess(response, file) {
  ElMessage({
    message: response.message,
    type: response.type,
  });
  if (response.success) getData();
}

// 导出
function exportPlaylist() {
  axios({
    method: 'get',
    url: {getBaseURL}+'excle',
    responseType: 'blob',
  })
    .then((response) => {
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'SongList.xlsx');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    })
    .catch((error) => {
      console.error('导出失败：', error);
      ElMessage.error("导出失败，请检查服务器连接");
    });
}

// 路由跳转
function goContentPage(id) {
  updateBreadcrumb("歌单内容");
  routerManager(RouterName.ListSong, { path: RouterName.ListSong, query: { id } });
}

function goCommentPage(id) {
  updateBreadcrumb("评论详情");
  routerManager(RouterName.Comment, { path: RouterName.Comment, query: { id, type: 1 } });
}

function updateBreadcrumb(name) {
  const breadcrumbList = [
    { path: RouterName.SongList, name: "歌单管理" },
    { path: "", name: name },
  ];
  proxy.$store.commit("setBreadcrumbList", breadcrumbList);
}

// --- CRUD 操作 ---

// 新增
async function addSongList() {
  // 确保包含默认 type
  const params = {
      ...registerForm,
      type: 1 // 默认启用
  };
  const result = (await HttpManager.setSongList(params)) as any;
  ElMessage({
    message: result.message,
    type: result.type,
  });

  if (result.success) {
    getData();
    // 重置表单
    registerForm.title = "";
    registerForm.introduction = "";
    registerForm.style = "";
  }
  centerDialogVisible.value = false;
}

// 编辑
function editRow(row) {
  // ✨ 这里会把 row 里的 type 属性也赋值给 editForm
  Object.assign(editForm, row); 
  editVisible.value = true;
}

async function saveEdit() {
  // ✨ 提交时会带上修改后的 type
  const result = (await HttpManager.updateSongListMsg(editForm)) as any;
  ElMessage({
    message: result.message,
    type: result.type,
  });
  if (result.success) getData();
  editVisible.value = false;
}

// 删除逻辑 (单条 + 批量)
function handleSelectionChange(val) {
  multipleSelection.value = val;
}

// 单条删除
function handleSingleDelete(id) {
  ElMessageBox.confirm('确定要删除该歌单吗？删除后无法恢复。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await deleteOperation(id);
  }).catch(() => {
    console.log('取消删除');
  });
}

// 批量删除
function handleBatchDelete() {
  ElMessageBox.confirm(`确定要删除选中的 ${multipleSelection.value.length} 个歌单吗？`, '批量删除', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const promises = multipleSelection.value.map(item => HttpManager.deleteSongList(item.id));
    try {
      await Promise.all(promises);
      ElMessage.success("批量删除成功");
      getData();
      multipleSelection.value = []; 
    } catch (e) {
      ElMessage.error("部分删除失败，请刷新重试");
    }
  }).catch(() => {
    console.log('取消批量删除');
  });
}

async function deleteOperation(id) {
  const result = await HttpManager.deleteSongList(id) as any;
  if (result.success) {
    ElMessage.success(result.message);
    getData();
  } else {
    ElMessage.error(result.message);
  }
}
</script>

<style scoped>
/* 容器样式 */
.container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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

/* 表格样式优化 */
.table-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

/* 图片上传覆盖层特效 */
.img-upload-box {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto;
  overflow: hidden;
  border-radius: 4px;
  cursor: pointer;
}

.upload-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.img-upload-box:hover .upload-mask {
  opacity: 1;
}

.upload-icon {
  font-size: 20px;
  margin-bottom: 2px;
}

/* 简介文本截断 */
.introduction-text {
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
  cursor: pointer;
}

.pagination-box {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>