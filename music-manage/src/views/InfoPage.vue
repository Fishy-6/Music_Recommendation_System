<template>
  <div class="admin-dashboard">
    <el-row :gutter="20" class="kpi-row">
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '0px' }" class="kpi-card">
          <div class="card-content user-theme">
            <div class="card-left">
              <el-icon><User /></el-icon>
            </div>
            <div class="card-right">
              <div class="card-label">用户总数</div>
              <div class="card-num">{{ userCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '0px' }" class="kpi-card">
          <div class="card-content song-theme">
            <div class="card-left">
              <el-icon><Headset /></el-icon>
            </div>
            <div class="card-right">
              <div class="card-label">歌曲总数</div>
              <div class="card-num">{{ songCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '0px' }" class="kpi-card">
          <div class="card-content singer-theme">
            <div class="card-left">
              <el-icon><Mic /></el-icon>
            </div>
            <div class="card-right">
              <div class="card-label">歌手数量</div>
              <div class="card-num">{{ singerCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{ padding: '0px' }" class="kpi-card">
          <div class="card-content list-theme">
            <div class="card-left">
              <el-icon><Document /></el-icon>
            </div>
            <div class="card-right">
              <div class="card-label">歌单数量</div>
              <div class="card-num">{{ songListCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="cav-info" shadow="hover">
          <h3>近 30 日用户活跃趋势</h3>
          <div class="chart-box" id="userActivityChart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="cav-info" shadow="hover">
          <h3>歌曲播放量 Top 10</h3>
          <div class="chart-box" id="topSongsChart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="cav-info" shadow="hover">
          <h3>新增内容趋势</h3>
          <div class="chart-box" id="newContentTrendChart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="cav-info" shadow="hover">
          <h3>用户年龄分布</h3>
          <div class="chart-box" id="userAgeDistributionChart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from "vue";
import * as echarts from "echarts";
import { Mic, Document, User, Headset } from "@element-plus/icons-vue";

// ⚠️ 假设您的 HttpManager 中已封装以下新接口
import { HttpManager } from "@/api/index"; 

// --- 核心数据统计 ---
const userCount = ref(0);
const songCount = ref(0);
const singerCount = ref(0);
const songListCount = ref(0);

// --- ECharts 配置 ---

// 1. 近 30 日用户活跃趋势 (Line Chart)
const userActivityOption = reactive({
  title: { text: '活跃用户', left: 'center', show: false },
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: [] as string[] },
  yAxis: { type: 'value' },
  series: [{ name: '活跃用户', type: 'line', smooth: true, data: [] as number[], itemStyle: { color: '#67C23A' } }],
});

// 2. 歌曲播放量 Top 10 (Bar Chart)
const topSongsOption = reactive({
  title: { text: '播放量', left: 'center', show: false },
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: [] as string[], axisLabel: { interval: 0, rotate: 0 } },
  series: [{ name: '播放次数', type: 'bar', data: [] as number[], itemStyle: { color: '#E6A23C' } }],
});

// 3. 新增内容趋势 (Multi-Line Chart)
const newContentTrendOption = reactive({
  title: { text: '新增内容', left: 'center', show: false },
  tooltip: { trigger: 'axis' },
  legend: { data: ['用户', '歌曲', '歌手'], top: 'bottom' },
  grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: [] as string[] },
  yAxis: { type: 'value' },
  series: [
    { name: '用户', type: 'line', smooth: true, data: [] as number[], itemStyle: { color: '#409EFF' } },
    { name: '歌曲', type: 'line', smooth: true, data: [] as number[], itemStyle: { color: '#F56C6C' } },
    { name: '歌手', type: 'line', smooth: true, data: [] as number[], itemStyle: { color: '#67C23A' } },
  ],
});

// 4. 用户年龄分布 (Pie Chart)
const userAgeOption = reactive({
  title: { text: '年龄分布', left: 'center', show: false },
  tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
  legend: { orient: 'vertical', left: 'left', data: [] as string[] },
  series: [{
    name: '用户年龄',
    type: 'pie',
    radius: '50%',
    center: ['50%', '60%'],
    data: [] as { value: number, name: string }[],
    emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
  }],
});

// --- API 调用与图表初始化函数 ---

// 1. 获取核心统计数据
async function getKpiCounts() {
  try {
    // 假设 HttpManager.getHomeCount() 返回 { userCount: 100, songCount: 500, ... }
    const res = (await HttpManager.getHomeCount()) as ResponseBody;
    if (res.success) {
      userCount.value = res.data.userCount;
      songCount.value = res.data.songCount;
      singerCount.value = res.data.singerCount;
      songListCount.value = res.data.songListCount;
    }
  } catch (error) {
    console.error("获取 KPI 数据失败:", error);
  }
}

// 2. 初始化用户活跃趋势图
async function initUserActivityChart() {
  try {
    // 假设返回 [{ date: '2025-11-01', count: 50 }, ...]
    const res = (await HttpManager.getRecentUserActivity()) as ResponseBody;
    if (res.success && res.data.length > 0) {
      const dates = res.data.map(item => item.date);
      const counts = res.data.map(item => item.count);

      userActivityOption.xAxis.data = dates;
      userActivityOption.series[0].data = counts;
      
      const chart = echarts.init(document.getElementById("userActivityChart"));
      chart.setOption(userActivityOption);
    }
  } catch (error) {
    console.error("初始化用户活跃趋势图失败:", error);
  }
}

// 3. 初始化 Top 10 歌曲图表
async function initTopSongsChart() {
  try {
    // 假设返回 [{ songName: '歌名A', playCount: 1200 }, ...]
    const res = (await HttpManager.getTop10Songs()) as ResponseBody;
    if (res.success && res.data.length > 0) {
      // Bar Chart 默认是垂直的，这里反转数据实现水平 Bar Chart
      const songNames = res.data.map(item => item.songName).reverse();
      const playCounts = res.data.map(item => item.playCount).reverse();

      topSongsOption.yAxis.data = songNames;
      topSongsOption.series[0].data = playCounts;
      
      const chart = echarts.init(document.getElementById("topSongsChart"));
      chart.setOption(topSongsOption);
    }
  } catch (error) {
    console.error("初始化 Top 10 歌曲图表失败:", error);
  }
}

// 4. 初始化新增内容趋势图
async function initNewContentTrendChart() {
  try {
    // 假设返回 { dates: ['周一', '周二', ...], newUser: [10, 20, ...], newSong: [5, 10, ...], newSinger: [1, 2, ...] }
    const res = (await HttpManager.getNewContentTrend()) as ResponseBody;
    if (res.success) {
      newContentTrendOption.xAxis.data = res.data.dates;
      newContentTrendOption.series[0].data = res.data.newUser;
      newContentTrendOption.series[1].data = res.data.newSong;
      newContentTrendOption.series[2].data = res.data.newSinger;
      
      const chart = echarts.init(document.getElementById("newContentTrendChart"));
      chart.setOption(newContentTrendOption);
    }
  } catch (error) {
    console.error("初始化新增内容趋势图失败:", error);
  }
}

// 5. 初始化用户年龄分布图
async function initUserAgeDistributionChart() {
  try {
    // 假设返回 [{ name: '18-24岁', value: 300 }, ...]
    const res = (await HttpManager.getUserAgeDistribution()) as ResponseBody;
    if (res.success && res.data.length > 0) {
      const names = res.data.map(item => item.name);
      
      userAgeOption.legend.data = names;
      userAgeOption.series[0].data = res.data;
      
      const chart = echarts.init(document.getElementById("userAgeDistributionChart"));
      chart.setOption(userAgeOption);
    }
  } catch (error) {
    console.error("初始化用户年龄分布图失败:", error);
  }
}


// --- 生命周期钩子 ---
onMounted(() => {
  // 获取顶部 KPI 数据
  getKpiCounts();
  
  // 初始化所有图表
  initUserActivityChart();
  initTopSongsChart();
  initNewContentTrendChart();
  initUserAgeDistributionChart();
});
</script>

<style scoped>
/* --- 布局和容器样式 --- */
.admin-dashboard {
  padding: 20px;
}

.kpi-row {
  margin-bottom: 20px !important;
}

.chart-row {
  margin-top: 20px;
}

/* --- KPI 卡片样式 --- */
.kpi-card {
  border-radius: 8px;
  overflow: hidden;
}

.card-content {
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 120px;
  padding: 15px 20px;
  color: white;
}

.card-left {
  display: flex;
  font-size: 4rem;
  opacity: 0.8;
}

.card-right {
  flex: 1;
  text-align: right;
}

.card-label {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 5px;
}

.card-num {
  font-size: 36px;
  font-weight: bold;
}

/* KPI 主题色 */
.user-theme { background: linear-gradient(45deg, #409EFF, #79bbff); } /* Primary Blue */
.song-theme { background: linear-gradient(45deg, #67C23A, #95d475); } /* Success Green */
.singer-theme { background: linear-gradient(45deg, #E6A23C, #eebe77); } /* Warning Yellow */
.list-theme { background: linear-gradient(45deg, #F56C6C, #fab6b6); } /* Danger Red */


/* --- 图表容器样式 --- */
h3 {
  margin: 15px 0 10px;
  text-align: center;
  font-weight: 600;
  color: #303133;
}

.cav-info {
  border-radius: 8px;
  overflow: hidden;
  height: 400px;
}

.chart-box {
  /* 为 ECharts 容器设置高度 */
  height: 350px; 
  width: 100%;
}
</style>