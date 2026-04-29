<template>
    <div class="play-list-container">
        
        <div class="user-playlist-header">
            <h2>我的歌单管理 ({{ allPlayList.length }} 个)</h2>
            <el-button 
                type="primary" 
                size="small" 
                :icon="Plus"
                @click="handleCreate"
            >
                创建新歌单
            </el-button>
        </div>
        
        <!-- 添加加载状态 -->
        <div v-if="loading" class="loading">加载中...</div>
        
        <!-- 未登录提示 -->
        <div v-else-if="!isLoggedIn" class="login-prompt">
            <el-empty description="请先登录查看您的歌单">
                <el-button type="primary" @click="goToLogin">立即登录</el-button>
            </el-empty>
        </div>
        
        <!-- 使用 PlayList 组件来显示歌单 -->
        <div v-else-if="showAsGrid" class="play-list-view">
            <play-list 
                :playList="allPlayList" 
                path="my-song-sheet-detail"
                :title="`我的歌单 (${allPlayList.length} 个)`"
            ></play-list>
        </div>
        
        <!-- 表格视图 -->
        <div v-else>
            <div class="view-toggle">
                <el-radio-group v-model="viewMode" size="small">
                    <el-radio-button label="table">表格视图</el-radio-button>
                    <el-radio-button label="grid">网格视图</el-radio-button>
                </el-radio-group>
            </div>
            
            <el-table 
                :data="tableData" 
                stripe 
                style="width: 100%"
                empty-text="您还没有创建任何歌单"
            >
                <el-table-column label="封面" width="100">
                    <template #default="scope">
                        <el-image 
                            :src="attachImageUrl(scope.row.pic)" 
                            style="width: 80px; height: 80px; border-radius: 4px; cursor: pointer;" 
                            fit="cover"
                            @click="goToSongListDetail(scope.row)"
                        />
                    </template>
                </el-table-column>
                
                <el-table-column prop="title" label="标题" min-width="180">
                    <template #default="scope">
                        <span class="song-list-title" @click="goToSongListDetail(scope.row)">
                            {{ scope.row.title }}
                        </span>
                    </template>
                </el-table-column>
                
                <el-table-column prop="style" label="风格" width="100"></el-table-column>
                
                <el-table-column label="状态" width="100">
                    <template #default="scope">
                        <el-tag :type="mapStatus(scope.row.type).type" effect="dark">
                            {{ mapStatus(scope.row.type).label }}
                        </el-tag>
                    </template>
                </el-table-column>
      
                <el-table-column label="操作" width="200" align="center">
                    <template #default="scope">
                        <el-button 
                            :icon="View" 
                            size="small" 
                            @click="goToSongListDetail(scope.row)"
                            text
                        >
                            查看
                        </el-button>
                        <el-button 
                            :icon="Edit" 
                            size="small" 
                            @click="handleEdit(scope.row)"
                            :disabled="scope.row.type !== 1"
                            text
                        >
                            修改
                        </el-button>
                        <el-button 
                            :icon="Delete" 
                            size="small" 
                            type="danger" 
                            @click="handleDelete(scope.row.id)"
                            text
                        >
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
      
            <el-pagination
                v-if="allPlayList.length > pageSize"
                class="pagination"
                background
                layout="total, prev, pager, next"
                :current-page="currentPage"
                :page-size="pageSize"
                :total="allPlayList.length"
                @current-change="handleCurrentChange"
            >
            </el-pagination>
        </div>
  
        <!-- 修改歌单弹窗 -->
        <el-dialog 
            :title="isCreating ? '创建新歌单' : '修改歌单信息'" 
            v-model="dialogVisible" 
            width="600px"
            @close="handleDialogClose"
        >
            <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
                <el-form-item v-if="!isCreating" label="歌单ID">
                    <el-input v-model="form.id" disabled></el-input>
                </el-form-item>
                <el-form-item label="标题" prop="title">
                    <el-input v-model="form.title" placeholder="请输入歌单标题"></el-input>
                </el-form-item>
                <el-form-item label="介绍" prop="introduction">
                    <el-input 
                        type="textarea" 
                        :rows="3" 
                        v-model="form.introduction" 
                        placeholder="请输入歌单介绍"
                    ></el-input>
                </el-form-item>
                <el-form-item label="风格" prop="style">
                    <el-input 
                        v-model="form.style" 
                        placeholder="请输入歌单风格，如：流行,摇滚"
                    ></el-input>
                </el-form-item>
                
                <!-- 修改图片上传部分 -->
                <el-form-item label="封面">
                    <div class="upload-container">
                        <el-upload
                            class="avatar-uploader"
                            action="#"
                            :show-file-list="false"
                            :before-upload="beforeAvatarUpload"
                            :http-request="handleAvatarUpload"
                            :disabled="uploading"
                        >
                            <el-image 
                                v-if="form.pic && !uploading" 
                                :src="attachImageUrl(form.pic)" 
                                style="width: 120px; height: 120px; border-radius: 4px;" 
                                fit="cover"
                            />
                            <div v-else-if="uploading" class="uploading">
                                <el-icon class="is-loading"><Loading /></el-icon>
                                <div>上传中...</div>
                            </div>
                            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                        </el-upload>
                        <div class="upload-info">
                            <div class="upload-tip">建议尺寸：300x300px，支持 JPG、PNG 格式</div>
                            <el-button 
                                v-if="form.pic && !isCreating" 
                                type="text" 
                                size="small" 
                                @click="handleResetPic"
                            >
                                重置为默认封面
                            </el-button>
                        </div>
                    </div>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogVisible = false" :disabled="uploading">取 消</el-button>
                    <el-button 
                        type="primary" 
                        @click="submitForm"
                        :loading="submitting"
                        :disabled="uploading"
                    >
                        {{ isCreating ? '创 建' : '保 存' }}
                    </el-button>
                </span>
            </template>
        </el-dialog>
        
    </div>
</template>
  
<script lang="ts">
import { defineComponent, ref, computed, onMounted, reactive, watch } from "vue";
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import { HttpManager } from "@/api";
import { ElMessage, ElMessageBox, type FormRules, type UploadProps } from "element-plus";
import { Edit, Delete, Plus, View, Loading } from "@element-plus/icons-vue";
import type { UploadRequestHandler } from 'element-plus';

// 导入 PlayList 组件
import PlayList from "@/components/PlayList.vue";

// 响应数据类型定义
interface SongListItem {
    id: number;
    title: string;
    pic: string;
    style: string;
    introduction: string;
    type: number;
    consumer: number;
}

interface ResponseBody {
    code: number;
    message: string;
    type: string;
    success: boolean;
    data: any;
}

interface SongListStatus {
    label: string;
    type: 'success' | 'warning' | 'danger' | 'info';
}

interface SongListForm {
    id?: number | null;
    title: string;
    introduction: string;
    style: string;
    pic?: string;
    type?: number;
    consumer?: number;
}

export default defineComponent({
    name: 'UserPlaylist',
    components: {
        PlayList
    },
    setup() {
        const store = useStore();
        const router = useRouter();
        const userId = computed(() => store.getters.userId); 
        const isLoggedIn = computed(() => !!userId.value);
        
        const formRef = ref();
        
        const pageSize = ref(15); 
        const currentPage = ref(1); 
        const allPlayList = ref<SongListItem[]>([]);
        const loading = ref(false);
        const dialogVisible = ref(false);
        const isCreating = ref(false);
        const uploading = ref(false);
        const submitting = ref(false);
        const viewMode = ref('table'); // 视图模式：table 或 grid
        
        // 计算属性
        const showAsGrid = computed(() => viewMode.value === 'grid');
        
        // 表单数据
        const form = reactive<SongListForm>({
            id: null,
            title: '',
            introduction: '',
            style: '',
            pic: '/img/songListPic/123.jpg' // 默认封面
        });

        // 表单验证规则
        const formRules: FormRules = {
            title: [
                { required: true, message: '请输入歌单标题', trigger: 'blur' },
                { min: 1, max: 30, message: '标题长度在 1 到 30 个字符', trigger: 'blur' }
            ],
            introduction: [
                { required: true, message: '请输入歌单介绍', trigger: 'blur' },
                { min: 1, max: 200, message: '介绍长度在 1 到 200 个字符', trigger: 'blur' }
            ],
            style: [
                { required: true, message: '请输入歌单风格', trigger: 'blur' }
            ]
        };

        // 计算属性 - 分页数据
        const tableData = computed(() => {
            if (!allPlayList.value.length) return [];
            
            const startIndex = (currentPage.value - 1) * pageSize.value;
            const endIndex = currentPage.value * pageSize.value;
            return allPlayList.value.slice(startIndex, endIndex);
        });

        // 检查登录状态
        function checkLoginStatus() {
            if (!isLoggedIn.value) {
                ElMessage.warning('请先登录');
                router.push('/sign-in');
                return false;
            }
            return true;
        }

        // 跳转到登录页
        function goToLogin() {
            router.push('/sign-in');
        }

        // 跳转到歌单详情页 - 修复版本
        function goToSongListDetail(songList: SongListItem) {
            // 设置歌单详情到 store
            store.commit("setSongDetails", songList);
            
            // 使用路由跳转
            router.push({
                path: `/my-song-sheet-detail/${songList.id}`,
                query: {
                    id: songList.id.toString()
                }
            });
        }

        // 图片路径处理
        function attachImageUrl(url: string): string {
            if (!url) return '';
            return HttpManager.attachImageUrl(url);
        }

        function mapStatus(type: number): SongListStatus {
            switch (type) {
                case 1:
                    return { label: '启用', type: 'success' };
                case 2:
                    return { label: '审核中', type: 'warning' };
                case 3:
                    return { label: '禁用', type: 'danger' };
                default:
                    return { label: '未知', type: 'info' };
            }
        }

        // 获取用户歌单方法
        async function getUserSongList() {
            if (!isLoggedIn.value) {
                return;
            }
            
            loading.value = true;
            try {
                const result = await HttpManager.getSongListByConsumerId(userId.value) as ResponseBody;
                
                if (result.success && result.data) {
                    allPlayList.value = result.data;
                    console.log("获取用户歌单成功，数量:", allPlayList.value.length);
                    currentPage.value = 1; 
                } else {
                    allPlayList.value = [];
                    ElMessage.warning(result.message || "获取歌单失败");
                }
            } catch (error) {
                console.error("获取用户歌单失败:", error);
                ElMessage.error("获取歌单失败，请检查网络连接");
                allPlayList.value = [];
            } finally {
                loading.value = false;
            }
        }
        
        // 创建歌单
        function handleCreate() {
            if (!checkLoginStatus()) return;
            
            isCreating.value = true;
            dialogVisible.value = true;
            
            // 重置表单
            Object.assign(form, {
                id: null,
                title: '',
                introduction: '',
                style: '',
                pic: '/img/songListPic/123.jpg',
                type: 1,
                consumer: userId.value
            });
        }

        // 修改歌单
        function handleEdit(row: SongListItem) {
            if (!checkLoginStatus()) return;
            
            isCreating.value = false;
            dialogVisible.value = true;
            
            Object.assign(form, {
                id: row.id,
                title: row.title,
                introduction: row.introduction,
                style: row.style,
                pic: row.pic
            });
        }

        // 重置为默认封面
        function handleResetPic() {
            form.pic = '/img/songListPic/123.jpg';
            ElMessage.info('已重置为默认封面');
        }

        // 提交表单（创建或修改）
        async function submitForm() {
            if (!formRef.value || !checkLoginStatus()) return;
            
            submitting.value = true;
            try {
                // 验证表单
                await formRef.value.validate();
                
                if (isCreating.value) {
                    // 创建歌单
                    await createSongList();
                } else {
                    // 修改歌单
                    await updateSongList();
                }
            } catch (error) {
                console.log('表单验证失败或提交错误:', error);
            } finally {
                submitting.value = false;
            }
        }

        // 创建歌单的具体逻辑
        async function createSongList() {
            try {
                const createData = {
                    title: form.title,
                    introduction: form.introduction,
                    style: form.style,
                    pic: form.pic,
                    type: 1, // 默认启用状态
                    consumer: userId.value
                };
                
                const result = await HttpManager.addSongList(createData) as ResponseBody;
                if (result.success) {
                    ElMessage.success("创建歌单成功");
                    dialogVisible.value = false;
                    await getUserSongList(); // 刷新列表
                } else {
                    ElMessage.error(result.message || "创建歌单失败");
                }
            } catch (error) {
                console.error("创建歌单失败:", error);
                ElMessage.error("创建歌单失败");
            }
        }

        // 修改歌单的具体逻辑
        async function updateSongList() {
            try {
                const updateData = {
                    id: form.id,
                    title: form.title,
                    introduction: form.introduction,
                    style: form.style,
                    pic: form.pic // 包含图片
                };
                
                const result = await HttpManager.updateSongList(updateData) as ResponseBody;
                if (result.success) {
                    ElMessage.success("修改成功");
                    dialogVisible.value = false;
                    await getUserSongList();
                } else {
                    ElMessage.error(result.message || "修改失败");
                }
            } catch (error) {
                ElMessage.error("修改歌单失败");
            }
        }

        // 图片上传处理
        const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
            const isJPGOrPNG = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png';
            const isLt2M = rawFile.size / 1024 / 1024 < 2;

            if (!isJPGOrPNG) {
                ElMessage.error('封面图片必须是 JPG 或 PNG 格式!');
                return false;
            }
            if (!isLt2M) {
                ElMessage.error('封面图片大小不能超过 2MB!');
                return false;
            }
            return true;
        };

        const handleAvatarUpload: UploadRequestHandler = async (options) => {
            if (!form.id && !isCreating.value) {
                ElMessage.warning('请先保存歌单基本信息再上传图片');
                return;
            }

            uploading.value = true;
            try {
                // 创建FormData对象来发送multipart/form-data请求
                const formData = new FormData();
                formData.append('file', options.file);
                formData.append('id', String(isCreating.value ? 0 : form.id));

                const result = await HttpManager.updateSongListPic(formData) as ResponseBody;
                
                if (result.success) {
                    form.pic = result.data; // 假设返回图片URL
                    ElMessage.success('图片上传成功');
                } else {
                    ElMessage.error(result.message || '图片上传失败');
                }
            } catch (error) {
                console.error('图片上传失败:', error);
                ElMessage.error('图片上传失败');
            } finally {
                uploading.value = false;
            }
        };

        // 弹窗关闭处理
        function handleDialogClose() {
            formRef.value?.clearValidate();
            uploading.value = false;
        }

        // 删除歌单
        function handleDelete(id: number) {
            if (!checkLoginStatus()) return;
            
            ElMessageBox.confirm("确定要删除这个歌单吗？此操作不可逆！", "警告", {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                type: "warning",
            }).then(async () => {
                try {
                    const result = await HttpManager.deleteSongList(id) as ResponseBody;
                    if (result.success) {
                        ElMessage.success("删除成功");
                        await getUserSongList();
                    } else {
                        ElMessage.error(result.message || "删除失败");
                    }
                } catch (error) {
                    ElMessage.error("删除歌单失败");
                }
            }).catch(() => {
                ElMessage.info("已取消删除");
            });
        }

        function handleCurrentChange(val: number) {
            currentPage.value = val;
        }
        
        onMounted(() => {
            if (!isLoggedIn.value) {
                ElMessage.warning('请先登录查看歌单');
                return;
            }
            
            getUserSongList();
        });

        watch(userId, (newId) => {
            if (newId) {
                getUserSongList();
            } else {
                allPlayList.value = [];
            }
        });

        return {
            pageSize,
            currentPage,
            allPlayList,
            tableData,
            loading,
            dialogVisible,
            isCreating,
            isLoggedIn,
            form,
            formRef,
            formRules,
            uploading,
            submitting,
            viewMode,
            showAsGrid,
            Plus,
            Delete,
            Edit,
            View,
            Loading,
            handleCurrentChange,
            handleCreate,
            handleEdit,
            handleDelete,
            submitForm,
            mapStatus,
            attachImageUrl,
            beforeAvatarUpload,
            handleAvatarUpload,
            handleDialogClose,
            goToLogin,
            goToSongListDetail,
            handleResetPic,
        };
    },
});
</script>
  
<style scoped>
.user-playlist-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 0 10px;
    border-bottom: 1px solid #ebeef5;
}
.user-playlist-header h2 {
    font-size: 20px;
    font-weight: bold;
    color: #333;
}
.pagination {
    margin-top: 20px;
    text-align: center;
}
.loading {
    text-align: center;
    padding: 50px;
    color: #666;
}
.login-prompt {
    text-align: center;
    padding: 50px 0;
}
.song-list-title {
    cursor: pointer;
    color: #333;
    transition: color 0.3s;
}
.song-list-title:hover {
    color: #409eff;
}
.upload-container {
    display: flex;
    align-items: flex-start;
    gap: 16px;
}
.avatar-uploader {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    width: 120px;
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: border-color 0.3s;
}
.avatar-uploader:hover {
    border-color: #409eff;
}
.avatar-uploader-icon {
    font-size: 32px;
    color: #8c939d;
}
.uploading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #409eff;
}
.upload-info {
    flex: 1;
}
.upload-tip {
    font-size: 12px;
    color: #909399;
    margin-bottom: 8px;
}
.view-toggle {
    margin-bottom: 20px;
    text-align: right;
}
.play-list-view {
    margin-top: 20px;
}
</style>