<template>
  <div class="container">
    <div class="handle-box">
      <div class="left-panel">
        <el-button type="danger" :icon="Delete" @click="handleBatchDelete" :disabled="multipleSelection.length === 0">批量删除</el-button>
        <el-input v-model="searchWord" :prefix-icon="Search" placeholder="筛选歌手" class="handle-input"></el-input>
      </div>
      <div class="right-panel">
        <el-button type="primary" :icon="Plus" @click="centerDialogVisible = true">添加歌手</el-button>
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
      
      <el-table-column label="歌手图片" width="110" align="center">
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

      <el-table-column label="歌手" prop="name" width="120" align="center"></el-table-column>

      <el-table-column label="性别" prop="sex" width="80" align="center">
        <template #default="scope">
          <el-tag :type="getSexTagType(scope.row.sex)">{{ changeSex(scope.row.sex) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="出生" prop="birth" width="120" align="center">
        <template #default="scope">
          <div>{{ getBirth(scope.row.birth) }}</div>
        </template>
      </el-table-column>

      <el-table-column label="地区" prop="location" width="100" align="center"></el-table-column>

      <el-table-column label="简介" min-width="200">
        <template #default="scope">
          <el-tooltip effect="dark" :content="scope.row.introduction" placement="top">
            <p class="introduction-text">{{ scope.row.introduction || '暂无简介' }}</p>
          </el-tooltip>
        </template>
      </el-table-column>

      <el-table-column label="内容管理" width="120" align="center">
        <template #default="scope">
          <el-button link type="primary" :icon="Headset" @click="goSongPage(scope.row)">歌曲管理</el-button>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button size="small" :icon="Edit" @click="editRow(scope.row)">编辑</el-button>
          <!-- <el-button size="small" type="danger" :icon="Delete" @click="handleSingleDelete(scope.row.id)">删除</el-button> -->
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

    <el-dialog title="添加歌手" v-model="centerDialogVisible" width="500px" destroy-on-close>
      <el-form label-width="80px" :model="registerForm" :rules="singerRule" ref="addFormRef">
        <el-form-item label="歌手名" prop="name">
          <el-input v-model="registerForm.name" placeholder="请输入歌手姓名"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="registerForm.sex">
            <el-radio :label="0">女</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">组合</el-radio>
            <el-radio :label="3">不明</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="故乡" prop="location">
          <el-input v-model="registerForm.location" placeholder="请输入国家或地区"></el-input>
        </el-form-item>
        <el-form-item label="出生日期" prop="birth">
          <el-date-picker type="date" v-model="registerForm.birth" placeholder="选择日期" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="歌手介绍" prop="introduction">
          <el-input type="textarea" :rows="4" v-model="registerForm.introduction" placeholder="请输入歌手简介"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="centerDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="addSinger">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="编辑歌手" v-model="editVisible" width="500px">
      <el-form label-width="80px" :model="editForm" :rules="singerRule">
        <el-form-item label="歌手名" prop="name">
          <el-input v-model="editForm.name"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="editForm.sex">
            <el-radio :label="0">女</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">组合</el-radio>
            <el-radio :label="3">不明</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birth">
          <el-date-picker type="date" v-model="editForm.birth" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="地区" prop="location">
          <el-input v-model="editForm.location"></el-input>
        </el-form-item>
        <el-form-item label="简介" prop="introduction">
          <el-input type="textarea" :rows="4" v-model="editForm.introduction"></el-input>
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
import { ref, reactive, computed, watch, getCurrentInstance } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus';
import { Delete, Search, Plus, Edit, Picture, Upload, Headset } from '@element-plus/icons-vue';
import mixin from "@/mixins/mixin";
import { HttpManager } from "@/api/index";
import { RouterName } from "@/enums";
import { getBirth } from "@/utils";

// Mixins & Global
const { changeSex, routerManager, beforeImgUpload } = mixin();
const { proxy } = getCurrentInstance(); // 仅用于 store 面包屑操作

// Data
const tableData = ref([]); // 原始数据
const filteredData = ref([]); // 搜索后数据
const loading = ref(false);
const pageSize = ref(7);
const currentPage = ref(1);
const searchWord = ref("");
const multipleSelection = ref([]);

// Dialog Control
const centerDialogVisible = ref(false);
const editVisible = ref(false);

// Forms
const addFormRef = ref(null);
const registerForm = reactive({
  name: "",
  sex: 1, // 默认为男
  birth: new Date(),
  location: "",
  introduction: "",
});

const editForm = reactive({
  id: "",
  name: "",
  sex: "",
  pic: "",
  birth: new Date(),
  location: "",
  introduction: "",
});

const singerRule = reactive({
  name: [{ required: true, message: "请输入歌手名字", trigger: "blur" }],
  sex: [{ required: true, message: "请选择性别", trigger: "change" }],
});

// Computed: Pagination
const paginatedData = computed(() => {
  return filteredData.value.slice(
    (currentPage.value - 1) * pageSize.value,
    currentPage.value * pageSize.value
  );
});

// Watch: Search
watch(searchWord, (newVal) => {
  currentPage.value = 1;
  if (!newVal) {
    filteredData.value = tableData.value;
  } else {
    filteredData.value = tableData.value.filter(item =>
      item.name.toLowerCase().includes(newVal.toLowerCase())
    );
  }
});

// Lifecycle
getData();

// Actions
async function getData() {
  loading.value = true;
  tableData.value = [];
  filteredData.value = [];
  try {
    const result = (await HttpManager.getAllSinger()) as any;
    tableData.value = result.data;
    filteredData.value = result.data;
  } catch (error) {
    ElMessage.error("获取歌手列表失败");
  } finally {
    loading.value = false;
  }
}

function handleCurrentChange(val) {
  currentPage.value = val;
}

// Image & Helpers
function uploadUrl(id) {
  return HttpManager.attachImageUrl(`/singer/avatar/update?id=${id}`);
}

function attachImageUrl(url) {
  return HttpManager.attachImageUrl(url);
}

function handleImgSuccess(response, file) {
  ElMessage({
    message: response.message,
    type: response.type,
  });
  if (response.success) getData();
}

// 获取性别标签颜色
function getSexTagType(sex) {
  if (sex === 0) return 'danger'; // 女 - 红
  if (sex === 1) return '';       // 男 - 蓝(默认)
  if (sex === 2) return 'warning'; // 组合 - 黄
  return 'info';                  // 不明 - 灰
}

// Routing
function goSongPage(row) {
  const breadcrumbList = [
    { path: RouterName.Singer, name: "歌手管理" },
    { path: "", name: "歌曲信息" },
  ];
  proxy.$store.commit("setBreadcrumbList", breadcrumbList);
  routerManager(RouterName.Song, {
    path: RouterName.Song,
    query: { id: row.id, name: row.name },
  });
}

// CRUD: Add
async function addSinger() {
  // 简单的表单预校验
  if(!registerForm.name) return ElMessage.warning("请输入歌手姓名");

  let datetime = getBirth(registerForm.birth);
  const params = {
    name: registerForm.name,
    sex: registerForm.sex,
    birth: datetime,
    location: registerForm.location,
    introduction: registerForm.introduction
  };

  const result = (await HttpManager.setSinger(params)) as any;
  ElMessage({
    message: result.message,
    type: result.type,
  });

  if (result.success) {
    getData();
    // Reset form
    registerForm.name = "";
    registerForm.location = "";
    registerForm.introduction = "";
    registerForm.birth = new Date();
  }
  centerDialogVisible.value = false;
}

// CRUD: Edit
function editRow(row) {
  Object.assign(editForm, row);
  // 确保日期格式正确，Element日期选择器通常需要 Date 对象或标准字符串
  editForm.birth = new Date(row.birth); 
  editVisible.value = true;
}

async function saveEdit() {
  try {
    let datetime = getBirth(new Date(editForm.birth));
    const params = {
      id: editForm.id,
      name: editForm.name,
      sex: editForm.sex,
      birth: datetime,
      location: editForm.location,
      introduction: editForm.introduction
    }

    const result = (await HttpManager.updateSingerMsg(params)) as any;
    ElMessage({
      message: result.message,
      type: result.type,
    });

    if (result.success) getData();
    editVisible.value = false;
  } catch (error) {
    console.error(error);
    ElMessage.error("更新失败");
  }
}

// CRUD: Delete
function handleSelectionChange(val) {
  multipleSelection.value = val;
}

// 单个删除
function handleSingleDelete(id) {
   ElMessageBox.confirm('确定要删除该歌手吗？同时会删除其所有歌曲。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await executeDelete(id);
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
}

// 批量删除
function handleBatchDelete() {
  ElMessageBox.confirm(`确定要删除选中的 ${multipleSelection.value.length} 位歌手吗？`, '批量删除', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const promises = multipleSelection.value.map(item => HttpManager.deleteSinger(item.id));
    try {
      await Promise.all(promises);
      ElMessage.success("批量删除成功");
      getData();
      multipleSelection.value = [];
    } catch (e) {
      ElMessage.error("删除过程中出现错误");
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
}

async function executeDelete(id) {
  const result = await HttpManager.deleteSinger(id) as any;
  if (result.success) {
    ElMessage.success(result.message);
    getData();
  } else {
    ElMessage.error(result.message);
  }
}
</script>

<style scoped>
.container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

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

/* 图片样式 */
.table-img {
  width: 80px;
  height: 80px;
  border-radius: 50%; /* 歌手头像一般用圆形比较好看，如果想用方形改成 4px */
  object-fit: cover;
}

.img-upload-box {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto;
  border-radius: 50%; /* 配合上面的圆角 */
  overflow: hidden;
  cursor: pointer;
}

/* 遮罩层 */
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

.introduction-text {
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
  cursor: help;
}

.pagination-box {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>