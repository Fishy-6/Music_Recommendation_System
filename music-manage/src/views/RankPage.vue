<template>
    <div class="rank-management-container">
      <h1 class="page-title">评分管理</h1>
      
      <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
        
        <el-tab-pane label="歌曲评分管理" name="songRank">
          <RankTable 
            :type="0" 
            :data="songRankData" 
            :total="songRankTotal"
            :loading="songLoading"
            @search="fetchRankData"
            @delete="handleDeleteRank"
            @page-change="fetchRankData"
          />
        </el-tab-pane>
        
        <el-tab-pane label="歌单评分管理" name="listRank">
          <RankTable 
            :type="1" 
            :data="listRankData" 
            :total="listRankTotal"
            :loading="listLoading"
            @search="fetchRankData"
            @delete="handleDeleteRank"
            @page-change="fetchRankData"
          />
        </el-tab-pane>
        
      </el-tabs>
    </div>
  </template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
// 假设您已经有 HttpManager 和 RankTable 组件
import { HttpManager } from '@/api'; 
import RankTable from '@/components/RankTable.vue'; // 这是一个我们稍后要创建的通用子组件

// 定义评分数据结构（根据您的后端返回调整）
interface RankItem {
  id: number;
  songId?: number; 
  songListName?: string; 
  consumerId: number;
  consumerName: string;
  score: number;
  createTime: string;
  name: string; // 歌曲名或歌单名
}

export default defineComponent({
  components: {
    RankTable,
  },
  setup() {
    const activeTab = ref('songRank');
    
    // 歌曲评分数据
    const songRankData = ref<RankItem[]>([]);
    const songRankTotal = ref(0);
    const songLoading = ref(false);

    // 歌单评分数据
    const listRankData = ref<RankItem[]>([]);
    const listRankTotal = ref(0);
    const listLoading = ref(false);

    /**
     * @param type 0: 歌曲, 1: 歌单
     * @param params 搜索和分页参数
     */
    const fetchRankData = async (type: 0 | 1, params: {
      currentPage: number, 
      pageSize: number, 
      name: string, 
      consumerName: string 
    }) => {
      const isSong = type === 0;
      const loadingRef = isSong ? songLoading : listLoading;
      const dataRef = isSong ? songRankData : listRankData;
      const totalRef = isSong ? songRankTotal : listRankTotal;

      loadingRef.value = true;
      try {
        // 假设后端有一个统一的评分查询接口，通过 type 区分
        // 您可能需要根据您的实际后端接口进行调整
        const result = (await HttpManager.getAllRankByType({
          type: type, // 0 for song, 1 for list
          ...params
        })) as any; 

        if (result.code === 200) {
          dataRef.value = result.data.records.map((item: any) => ({
            ...item,
            // 确保歌曲名/歌单名有一个统一的字段 'name'
            
            name: isSong ? item.songName : item.songListName, 
            consumerName: item.consumerName || '未知用户' ,// 确保有用户名字段
            score: parseFloat(item.score) || 0
          })) as RankItem[];



          totalRef.value = result.data.total;
        } else {
          ElMessage.error(`获取${isSong ? '歌曲' : '歌单'}评分失败：${result.message}`);
          dataRef.value = [];
          totalRef.value = 0;
        }

      } catch (error) {
        ElMessage.error(`网络请求失败，请检查接口！`);
      } finally {
        loadingRef.value = false;
      }
    };
    
    // Tab 切换时触发数据加载
    const handleTabChange = (name: string) => {
      const type = name === 'songRank' ? 0 : 1;
      fetchRankData(type, { currentPage: 1, pageSize: 10, name: '', consumerName: '' });
    };

    // 删除/取消评分操作
    const handleDeleteRank = async (id: number, type: 0 | 1) => {
      const isSong = type === 0;
      const rankType = isSong ? '歌曲' : '歌单';

      try {
        await ElMessageBox.confirm(`确定要删除这条 ID 为 ${id} 的${rankType}评分吗？`, '警告', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
        });
        
        // 假设有一个删除评分的接口
        const result = (await HttpManager.deleteRank(type,id)) as any;

        if (result.code === 200) {
          ElMessage.success(`${rankType}评分删除成功！`);
          // 重新加载当前 Tab 的数据
          const currentTabType = activeTab.value === 'songRank' ? 0 : 1;
          fetchRankData(currentTabType, { currentPage: 1, pageSize: 10, name: '', consumerName: '' });
        } else {
          ElMessage.error(`删除失败: ${result.message}`);
        }
      } catch (e) {
        if (e !== 'cancel') {
          ElMessage.info('已取消删除操作');
        }
      }
    };

    // 页面加载时默认加载歌曲评分
    onMounted(() => {
      fetchRankData(0, { currentPage: 1, pageSize: 10, name: '', consumerName: '' });
    });

    return {
      activeTab,
      songRankData,
      songRankTotal,
      songLoading,
      listRankData,
      listRankTotal,
      listLoading,
      handleTabChange,
      fetchRankData,
      handleDeleteRank,
    };
  },
});
</script>


<style scoped>
.rank-management-container {
  padding: 20px;
}
.page-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}
/* 调整 Tab 样式，使其更符合后台管理界面风格 */
:deep(.el-tabs--border-card) {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}
</style>