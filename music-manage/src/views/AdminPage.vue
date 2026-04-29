<template>
  <div class="admin-manager">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>👥 管理员用户管理</span>
          <el-button type="primary" @click="openDialog('add')">
            <el-icon><i-ep-plus /></el-icon>
            新增管理员
          </el-button>
        </div>
      </template>

      <el-table :data="adminList" style="width: 100%" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="用户名" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button size="small" @click="openDialog('view', scope.row)">
              查看信息
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="formData"
        :rules="formRules"
        ref="formRef"
        label-width="80px"
        :disabled="isView"
      >
        <el-form-item v-if="formData.id" label="ID">
          <el-input v-model="formData.id" disabled />
        </el-form-item>
        <el-form-item label="用户名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="formData.password"
            :type="passwordVisible ? 'text' : 'password'"
            placeholder="请输入密码"
            abled
          >
            <template #append>
              <el-button @click="togglePasswordVisibility">
                {{ passwordVisible ? '隐藏' : '显示' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ isView ? '关闭' : '取消' }}</el-button>
          <el-button v-if="!isView" type="primary" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '新增' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { HttpManager } from "@/api/index";
import { h, onMounted } from 'vue';

// --- 模拟数据和状态 ---
// 在 setup 中添加
onMounted(() => {
  getAdmin();
});

// 模拟的管理员列表 (实际项目中应从 API 获取)
const adminList = ref([]);

async function getAdmin() {
  const result= await HttpManager.getAdmin();
  adminList.value = result.data;
}

// 对话框状态
const dialogVisible = ref(false);
const isEdit = ref(false); // 标记当前是新增(false)还是编辑(true)
const isView = ref(false); // 标记当前是否为查看模式
const passwordVisible = ref(false); // 密码可见性控制

const dialogTitle = computed(() => {
  if (isView.value) return '查看管理员信息';
  return isEdit.value ? '编辑管理员' : '新增管理员';
});

// 表单数据
const initialFormData = {
  id: null,
  name: '',
  password: '',
};
const formData = reactive({ ...initialFormData });

// 表单引用和校验规则
const formRef = ref(null);
const formRules = {
  name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, message: '密码至少 3 个字符', trigger: 'blur' },
  ],
};

// 切换密码可见性
function togglePasswordVisibility() {
  passwordVisible.value = !passwordVisible.value;
}

// --- 操作函数 ---

/**
 * 打开新增、编辑或查看对话框
 * @param {string} type - 'add'、'edit' 或 'view'
 * @param {object} row - 编辑时传入的行数据
 */
function openDialog(type, row = null) {
  isEdit.value = type === 'edit';
  isView.value = type === 'view';
  passwordVisible.value = false; // 默认隐藏密码
  // 重置表单
  Object.assign(formData, initialFormData);

  if ((type === 'edit' || type === 'view') && row) {
    // 填充编辑数据，使用 Object.assign 确保响应式
    Object.assign(formData, row);
  }

  dialogVisible.value = true;
  // nextTick 确保表单渲染后再重置校验状态
  nextTick(() => {
    if (!isView.value) {
      formRef.value?.clearValidate();
    }
  });
}

/**
 * 处理表单提交 (新增或编辑)
 */
function handleSubmit() {
  if (isView.value) return; // 查看模式下不处理提交
  
  formRef.value.validate((valid) => {
    if (valid) {
      // 实际开发中：这里应调用后端 API
      
      if (isEdit.value) {
        // --- 编辑逻辑 (模拟) ---
        const index = adminList.value.findIndex(a => a.id === formData.id);
        if (index !== -1) {
          // 更新列表中的数据
          adminList.value[index].name = formData.name;
          adminList.value[index].password = formData.password; // 注意：实际中密码不应直接传给前端并存入列表
          ElMessage.success(`管理员 ${formData.name} 更新成功！`);
        }
      } else {
        // --- 新增逻辑 ---
        const newId = Math.max(...adminList.value.map(a => a.id), 0) + 1;
        const newAdmin = {
          id: newId,
          username: formData.name,
          password: formData.password,
        };
        adminList.value.push(newAdmin);
        addAdmin1(newAdmin); // 调用后端接口
        ElMessage.success(`新增管理员 ${formData.name} 成功！`);
      }

      // 关闭对话框
      dialogVisible.value = false;
    } else {
      ElMessage.warning('请检查表单输入！');
      return false;
    }
  });
}

function addAdmin1(newAdmin) {
  const result = HttpManager.addAdmin(newAdmin);
  getAdmin();
  return result.code;
}

/**
 * 处理删除操作
 * @param {object} row - 要删除的管理员数据
 */
function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除管理员用户 **${row.name}** (ID: ${row.id}) 吗？`,
    '⚠️ 删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true,
    }
  )
    .then(() => {
      // 实际开发中：这里应调用后端 API
      const result = HttpManager.deleteAdmin(row.id);
      if (result.code === 200) {
        ElMessage.success(`删除成功：ID ${row.id}`);
      } else {
        ElMessage.error(`删除失败：${result.message}`);
      }
      // --- 删除逻辑 (模拟) ---
      const index = adminList.value.findIndex(a => a.id === row.id);
      if (index !== -1) {
        adminList.value.splice(index, 1);
        ElMessage.success(`管理员 ${row.name} 删除成功！`);
      }
    })
    .catch(() => {
      // 用户取消操作
    });
}
</script>

<style scoped>
.admin-manager {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
/* 调整 el-icon 的样式，因为使用了 Element Plus 的图标组件 */
:deep(.el-icon) {
  margin-right: 4px;
}
</style>