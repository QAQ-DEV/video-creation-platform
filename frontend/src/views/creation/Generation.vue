<template>
  <div class="generation-container">
    <div class="creation-area">
      <!-- 顶部 Logo -->
      <div class="top-logo">
        <svg width="48" height="48" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect width="100" height="100" rx="18" fill="url(#logo-gradient)"/>
          <rect x="20" y="25" width="60" height="50" rx="8" fill="white" opacity="0.95"/>
          <circle cx="50" cy="50" r="14" fill="#3B82F6"/>
          <path d="M40 55L60 45" stroke="white" stroke-width="3" stroke-linecap="round"/>
          <defs>
            <linearGradient id="logo-gradient" x1="0" y1="0" x2="100" y2="100">
              <stop offset="0%" stop-color="#667eea"/>
              <stop offset="100%" stop-color="#764ba2"/>
            </linearGradient>
          </defs>
        </svg>
        <span class="logo-text">VideoAI</span>
        <span class="logo-desc">智能创作平台</span>
      </div>

      <!-- 任务类型选择 -->
      <div class="task-type-section">
        <div class="section-header">
          <span class="section-title-text">创作类型</span>
        </div>
        <div class="task-type-grid">
          <div
            v-for="type in taskTypes"
            :key="type.value"
            class="task-type-card"
            :class="{ active: form.taskType === type.value }"
            @click="handleTaskTypeChange(type.value)"
          >
            <div class="task-type-icon">
              <el-icon :size="28"><component :is="type.icon" /></el-icon>
            </div>
            <div class="task-type-label">{{ type.label }}</div>
          </div>
        </div>
      </div>

      <!-- 主输入区域 -->
      <div class="input-card">
        <!-- AI灵感推荐 -->
        <div class="ai-assist-bar">
          <div class="ai-assist-left">
            <el-button class="ai-btn" @click="showHotspotsDialog">
              <el-icon><Star /></el-icon>
              灵感推荐
            </el-button>
            <el-button class="ai-btn" @click="showScriptDialog">
              <el-icon><Document /></el-icon>
              AI脚本生成
            </el-button>
          </div>
        </div>

        <div class="textarea-wrapper">
          <textarea
            v-model="form.inputConfig.prompt"
            :placeholder="getPlaceholder()"
            class="prompt-textarea"
            rows="4"
          ></textarea>
        </div>

        <!-- 底部工具栏 -->
        <div class="input-toolbar">
          <div class="toolbar-left">
            <button class="tool-btn style-btn">
              <el-icon><Collection /></el-icon>
              <span>风格模型</span>
            </button>

            <button
              v-if="form.taskType === 'IMAGE_TO_VIDEO'"
              class="tool-btn upload-btn"
              @click="triggerUpload"
            >
              <el-icon><Plus /></el-icon>
              <span>图片</span>
            </button>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleImageUpload"
            />

            <div v-if="uploadedImage" class="upload-preview-inline">
              <img :src="uploadedImage" alt="预览" />
              <el-icon class="remove-btn" @click="removeImage"><Close /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 模型选择区域 -->
      <div class="model-selection-section">
        <div class="section-header">
          <span class="section-title-text">选择模型</span>
          <span class="section-desc">{{ getModelTypeLabel() }}</span>
        </div>
        <div class="models-grid">
          <div
            v-for="model in currentModels"
            :key="model.value"
            class="model-selection-card"
            :class="{ selected: form.modelName === model.value }"
            @click="selectModel(model.value)"
          >
            <div class="model-card-icon">
              <el-icon :size="24"><component :is="getModelIcon(model.label)" /></el-icon>
            </div>
            <div class="model-card-content">
              <div class="model-name">{{ model.label }}</div>
              <div class="model-desc">{{ getModelDescription(model.label) }}</div>
              <div class="model-tag" v-if="getModelTag(model.label)">
                {{ getModelTag(model.label) }}
              </div>
            </div>
            <div class="model-check" v-if="form.modelName === model.value">
              <el-icon><Check /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 参数配置栏 -->
      <div class="param-bar">
        <div class="param-left">
          <div class="param-item" v-if="form.taskType === 'TEXT_TO_VIDEO' || form.taskType === 'IMAGE_TO_VIDEO'">
            <el-icon><Timer /></el-icon>
            <el-select v-model="form.inputConfig.duration" class="param-select" size="small">
              <el-option label="5s" :value="5" />
              <el-option label="10s" :value="10" />
              <el-option label="15s" :value="15" />
              <el-option label="30s" :value="30" />
            </el-select>
          </div>

          <div class="param-item" v-if="form.taskType === 'TEXT_TO_IMAGE'">
            <el-icon><Crop /></el-icon>
            <el-select v-model="form.inputConfig.ratio" class="param-select" size="small">
              <el-option label="1:1" value="1:1" />
              <el-option label="16:9" value="16:9" />
              <el-option label="4:3" value="4:3" />
              <el-option label="3:4" value="3:4" />
              <el-option label="9:16" value="9:16" />
            </el-select>
          </div>
        </div>

        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          @click="submitTask"
          class="generate-btn"
        >
          <el-icon><VideoPlay /></el-icon>
          {{ submitting ? '生成中...' : '生成' }}
        </el-button>
      </div>
    </div>

    <!-- 灵感推荐弹窗 -->
    <el-dialog
      v-model="hotspotsDialogVisible"
      title="AI灵感推荐"
      width="600px"
    >
      <div class="hotspots-content">
        <el-alert
          title="全网热门话题"
          type="info"
          :closable="false"
          class="hotspots-alert"
        />
        <div class="hotspots-list">
          <div
            v-for="item in hotspots"
            :key="item.id"
            class="hotspot-item"
            @click="selectHotspot(item)"
          >
            <div class="hotspot-rank">#{{ item.rank }}</div>
            <div class="hotspot-content">
              <div class="hotspot-title">{{ item.title }}</div>
              <div class="hotspot-stats">
                <el-tag size="small">{{ item.category }}</el-tag>
                <span class="hotspot-views">
                  <el-icon><View /></el-icon>
                  {{ item.views }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="hotspotsDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="refreshHotspots">
          <el-icon><Refresh /></el-icon>
          刷新热点
        </el-button>
      </template>
    </el-dialog>

    <!-- AI脚本生成弹窗 -->
    <el-dialog
      v-model="scriptDialogVisible"
      title="AI脚本生成"
      width="700px"
    >
      <el-form :model="scriptForm" label-width="100px">
        <el-form-item label="主题/类型">
          <el-select v-model="scriptForm.theme" placeholder="选择脚本主题">
            <el-option label="短视频" value="short-video" />
            <el-option label="广告片" value="commercial" />
            <el-option label="产品介绍" value="product" />
            <el-option label="品牌宣传" value="brand" />
            <el-option label="故事片" value="story" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="scriptForm.keywords"
            placeholder="输入关键词，用逗号分隔"
            type="textarea"
            :rows="2"
          />
        </el-form-item>
        <el-form-item label="风格偏好">
          <el-checkbox-group v-model="scriptForm.style">
            <el-checkbox value="幽默风">幽默风</el-checkbox>
            <el-checkbox value="科技感">科技感</el-checkbox>
            <el-checkbox value="温馨治愈">温馨治愈</el-checkbox>
            <el-checkbox value="高端大气">高端大气</el-checkbox>
            <el-checkbox value="年轻活力">年轻活力</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="时长要求">
          <el-radio-group v-model="scriptForm.duration">
            <el-radio :value="15">15秒</el-radio>
            <el-radio :value="30">30秒</el-radio>
            <el-radio :value="60">60秒</el-radio>
            <el-radio :value="90">90秒</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scriptDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="generateScript" :loading="generatingScript">
          <el-icon><MagicStick /></el-icon>
          生成脚本
        </el-button>
      </template>
    </el-dialog>

    <!-- 生成历史 -->
    <div class="history-section" v-if="generatedVideos.length > 0">
      <h2 class="section-title">创作记录</h2>
      <div class="video-grid">
        <div
          v-for="(video, index) in generatedVideos"
          :key="index"
          class="video-item"
          @click="playVideo(video)"
        >
          <div class="video-thumbnail">
            <video :src="video.url" muted loop autoplay playsinline>
              <source :src="video.url" type="video/mp4">
            </video>
            <div class="play-overlay">
              <el-icon><VideoPlay /></el-icon>
            </div>
          </div>
          <div class="video-info">
            <h3>{{ video.title }}</h3>
            <p>{{ video.description }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { submitGenerationTask } from '@/api/creation/generation'
import {
  VideoPlay,
  Plus,
  Close,
  Picture,
  PictureFilled,
  Document,
  Collection,
  Cpu,
  Timer,
  Crop,
  Star,
  View,
  Refresh,
  MagicStick,
  Check,
  ChatDotRound,
  VideoCamera,
  DocumentCopy,
  TrendCharts
} from '@element-plus/icons-vue'

const router = useRouter()

// 模拟视频数据
const generatedVideos = ref([
  {
    id: 'video-1',
    title: '赛博朋克风格',
    description: '未来科技城市夜景，霓虹灯光',
    url: '/videos/cyberpunk_10s.mp4'
  },
  {
    id: 'video-2',
    title: '萌宠熊猫',
    description: '可爱熊猫玩耍，憨态可掬',
    url: '/videos/pandas-2b.mp4'
  },
  {
    id: 'video-3',
    title: 'Kpop舞蹈',
    description: '精彩偶像团体舞蹈表演',
    url: '/videos/mochi_kpop_fancam.mp4'
  },
  {
    id: 'video-4',
    title: '动画特效',
    description: 'AI生成的动态视觉效果',
    url: '/videos/AnimateDiff_00003.mp4'
  },
  {
    id: 'video-5',
    title: '商业广告',
    description: '专业产品展示视频',
    url: '/videos/8ad1a29fab8541b387f425c05b0d9801.mp4'
  },
  {
    id: 'video-6',
    title: '自然风光',
    description: '壮丽山川与河流',
    url: '/videos/8b21085e2a7d470c98282b433918ec0b.mp4'
  }
])

// 任务类型枚举
const TaskTypes = {
  TEXT_TO_TEXT: 'TEXT_TO_TEXT',
  TEXT_TO_IMAGE: 'TEXT_TO_IMAGE',
  TEXT_TO_VIDEO: 'TEXT_TO_VIDEO',
  IMAGE_TO_VIDEO: 'IMAGE_TO_VIDEO'
}

// 模型配置
const modelsByTaskType = {
  [TaskTypes.TEXT_TO_TEXT]: [
    { label: 'DeepSeek V3', value: 'DeepSeek V3' },
    { label: 'Qwen Flash', value: 'Qwen Flash' }
  ],
  [TaskTypes.TEXT_TO_IMAGE]: [
    { label: 'Wanx v1', value: 'Wanx v1' }
  ],
  [TaskTypes.TEXT_TO_VIDEO]: [
    { label: 'Kling', value: 'Kling' },
    { label: 'Minimax', value: 'Minimax' },
    { label: 'Doubao Seedance', value: 'Doubao Seedance' }
  ],
  [TaskTypes.IMAGE_TO_VIDEO]: [
    { label: 'Kling', value: 'Kling' },
    { label: 'Minimax', value: 'Minimax' }
  ]
}

const currentModels = ref(modelsByTaskType[TaskTypes.TEXT_TO_VIDEO])
const uploadedImage = ref('')
const fileInput = ref(null)
const submitting = ref(false)

// AI辅助功能状态
const hotspotsDialogVisible = ref(false)
const scriptDialogVisible = ref(false)
const generatingScript = ref(false)

// 全网热点数据
const hotspots = ref([
  {
    id: 1,
    rank: 1,
    title: 'AI智能助手使用技巧大全',
    category: '科技',
    views: '2.5万'
  },
  {
    id: 2,
    rank: 2,
    title: '2025年最火短视频拍摄技巧',
    category: '影视',
    views: '1.8万'
  },
  {
    id: 3,
    rank: 3,
    title: '职场穿搭指南，提升个人形象',
    category: '时尚',
    views: '1.5万'
  },
  {
    id: 4,
    rank: 4,
    title: '美食探店，寻找城市美味',
    category: '生活',
    views: '1.2万'
  },
  {
    id: 5,
    rank: 5,
    title: '宠物日常，治愈系萌宠瞬间',
    category: '娱乐',
    views: '9800'
  }
])

// 脚本生成表单
const scriptForm = reactive({
  theme: 'short-video',
  keywords: '',
  style: ['温馨治愈'],
  duration: 15
})

const taskTypes = [
  { value: 'TEXT_TO_VIDEO', label: '视频生成', icon: 'VideoPlay' },
  { value: 'IMAGE_TO_VIDEO', label: '图生视频', icon: 'Picture' },
  { value: 'TEXT_TO_IMAGE', label: '图片生成', icon: 'PictureFilled' },
  { value: 'TEXT_TO_TEXT', label: '文本生成', icon: 'Document' }
]

const form = reactive({
  taskType: 'TEXT_TO_VIDEO',
  modelName: 'Kling',
  inputConfig: {
    prompt: '',
    negativePrompt: '',
    duration: 15,
    ratio: '16:9',
    image: null
  }
})

const getPlaceholder = () => {
  const placeholders = {
    TEXT_TO_VIDEO: '输入视频生成的提示词，例如：一只可爱的熊猫在竹林里玩耍...',
    IMAGE_TO_VIDEO: '输入图生视频的提示词...',
    TEXT_TO_IMAGE: '输入图片生成的提示词...',
    TEXT_TO_TEXT: '输入你想生成的文本内容描述...'
  }
  return placeholders[form.taskType] || '请输入提示词...'
}

const handleTaskTypeChange = (typeValue) => {
  form.taskType = typeValue
  currentModels.value = modelsByTaskType[form.taskType]
  if (currentModels.value.length > 0) {
    form.modelName = currentModels.value[0].value
  }
  uploadedImage.value = ''
  form.inputConfig.image = null
  form.inputConfig.prompt = ''
  form.inputConfig.negativePrompt = ''
}

// 选择模型
const selectModel = (modelName) => {
  form.modelName = modelName
}

// 获取模型图标
const getModelIcon = (modelName) => {
  const iconMap = {
    'DeepSeek V3': 'DocumentCopy',
    'Qwen Flash': 'ChatDotRound',
    'Wanx v1': 'PictureFilled',
    'Kling': 'VideoCamera',
    'Minimax': 'TrendCharts',
    'Doubao Seedance': 'VideoPlay'
  }
  return iconMap[modelName] || 'Cpu'
}

// 获取模型描述
const getModelDescription = (modelName) => {
  const descMap = {
    'DeepSeek V3': '强大的文本理解与生成能力',
    'Qwen Flash': '快速响应的文本生成模型',
    'Wanx v1': '高精度图像生成模型',
    'Kling': '专业级视频生成模型',
    'Minimax': '高质量视频与图像生成',
    'Doubao Seedance': '多模态视频创作模型'
  }
  return descMap[modelName] || 'AI智能模型'
}

// 获取模型标签
const getModelTag = (modelName) => {
  const tagMap = {
    'DeepSeek V3': 'NEW',
    'Qwen Flash': '推荐',
    'Kling': '热门',
    'Minimax': 'NEW'
  }
  return tagMap[modelName] || ''
}

// 获取模型类型标签
const getModelTypeLabel = () => {
  const typeLabelMap = {
    'TEXT_TO_TEXT': '文本生成',
    'TEXT_TO_IMAGE': '图片生成',
    'TEXT_TO_VIDEO': '视频生成',
    'IMAGE_TO_VIDEO': '图生视频'
  }
  return typeLabelMap[form.taskType] || 'AI模型'
}

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleImageUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      uploadedImage.value = e.target.result
      form.inputConfig.image = uploadedImage.value
    }
    reader.readAsDataURL(file)
  }
}

const removeImage = () => {
  uploadedImage.value = ''
  form.inputConfig.image = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const playVideo = (video) => {
  console.log('播放视频:', video)
}

// 显示灵感推荐弹窗
const showHotspotsDialog = () => {
  hotspotsDialogVisible.value = true
}

// 选择热点
const selectHotspot = (hotspot) => {
  form.inputConfig.prompt = `创作关于${hotspot.title}的视频内容，${hotspot.category}风格，吸引人的开头和结尾`
  hotspotsDialogVisible.value = false
}

// 刷新热点
const refreshHotspots = async () => {
  // 模拟加载
  ElMessage.success('热点已刷新')
}

// 显示脚本生成弹窗
const showScriptDialog = () => {
  scriptDialogVisible.value = true
}

// 生成AI脚本
const generateScript = async () => {
  if (!scriptForm.keywords) {
    ElMessage.warning('请输入关键词')
    return
  }

  try {
    generatingScript.value = true

    // 模拟AI生成
    await new Promise(resolve => setTimeout(resolve, 1500))

    const script = generateMockScript()

    form.inputConfig.prompt = script
    scriptDialogVisible.value = false
    ElMessage.success('AI脚本生成成功！')

  } catch (error) {
    ElMessage.error('脚本生成失败：' + error.message)
  } finally {
    generatingScript.value = false
  }
}

// 模拟生成脚本
const generateMockScript = () => {
  const keywords = scriptForm.keywords
  const theme = scriptForm.theme
  const duration = scriptForm.duration

  const templates = {
    'short-video': `[${keywords}主题短视频脚本 - ${duration}秒版本]
场景1：开场（0-3秒）
- 镜头：特写/全景
- 画面：${keywords}的精彩瞬间
- 文案："${keywords}，让你看见不一样的精彩"

场景2：核心展示（3-${duration - 3}秒）
- 镜头：中景/特写切换
- 画面：详细展示${keywords}的特点和魅力
- 文案/配音：详细解说${keywords}的亮点和特色

场景3：结尾（${duration - 3}-${duration}秒）
- 镜头：全景/特写
- 画面：${keywords}的总结性镜头
- 文案："${keywords}，期待你的关注和互动"

拍摄建议：
- 使用稳定器或三脚架
- 注意光线，选择自然光或补光灯
- 多角度拍摄，后期剪辑更丰富`,

    'commercial': `[${keywords}产品介绍短片脚本 - ${duration}秒版本]
开场白（0-5秒）：
"今天给大家介绍一款${keywords}"

产品展示（5-${duration - 5}秒）：
- 产品外观：360度展示，突出设计感
- 产品功能：演示核心功能和使用场景
- 产品优势：对比展示，凸显性价比

结尾（${duration - 5}-${duration}秒）：
"${keywords}，值得你的选择，现在下单享优惠！"
行动号召：立即购买/了解详情`,

    'product': `[${keywords}产品宣传片脚本 - ${duration}秒版本]
吸引眼球（0-5秒）：
${keywords}的独特魅力

核心卖点（5-${duration - 5}秒）：
详细介绍${keywords}的特点和优势
通过实例或对比展示价值

用户见证（${duration - 5}-${duration}秒）：
用户的真实反馈和评价
展示${keywords}带来的改变`,

    'brand': `[${keywords}品牌故事短片脚本 - ${duration}秒版本]
品牌起源：${keywords}的创立初心
发展历程：从0到1的成长故事
品牌理念：传递的核心价值
未来愿景：${keywords}的发展方向`,

    'story': `[${keywords}主题故事短片脚本 - ${duration}秒版本]
开篇：设定场景，引入${keywords}主题
发展：通过${keywords}展现故事情节
高潮：${keywords}带来的转折或高潮
结尾：${keywords}主题的升华和思考`
  }

  return templates[theme] || templates['short-video']
}

const submitTask = async () => {
  if (!form.inputConfig.prompt && !uploadedImage.value) {
    ElMessage.warning('请输入提示词或上传图片')
    return
  }

  submitting.value = true

  try {
    // 构建提交数据
    const submitData = {
      taskType: form.taskType,
      modelName: form.modelName,
      inputConfig: {
        prompt: form.inputConfig.prompt,
        negativePrompt: form.inputConfig.negativePrompt,
        duration: form.inputConfig.duration,
        ratio: form.inputConfig.ratio,
        image: form.inputConfig.image
      }
    }

    // 跳转到对话式创作界面，携带初始参数
    router.push({
      path: '/creation/chat',
      query: {
        taskType: form.taskType,
        modelName: form.modelName,
        prompt: form.inputConfig.prompt,
        image: form.inputConfig.image || '',
        duration: form.inputConfig.duration,
        ratio: form.inputConfig.ratio
      }
    })

    submitting.value = false
  } catch (error) {
    console.error('跳转失败:', error)
    ElMessage.error('跳转失败，请稍后重试')
    submitting.value = false
  }
}
</script>

<style scoped>
.generation-container {
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
  min-height: calc(100vh - 80px);
}

.creation-area {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 顶部Logo */
.top-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
}

.top-logo svg {
  flex-shrink: 0;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.logo-desc {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

/* 任务类型选择区域 */
.task-type-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.task-type-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.task-type-card {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  padding: 20px 16px;
  border: 2px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.task-type-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.task-type-card.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
}

.task-type-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea20 0%, #764ba220 100%);
  color: #667eea;
  transition: all 0.3s ease;
}

.task-type-card:hover .task-type-icon,
.task-type-card.active .task-type-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.task-type-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  text-align: center;
}

.task-type-card:hover .task-type-label,
.task-type-card.active .task-type-label {
  color: #667eea;
}

.input-card {
  background: #fafafa;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.textarea-wrapper {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.prompt-textarea {
  width: 100%;
  border: none;
  border-radius: 8px;
  padding: 16px;
  font-size: 15px;
  line-height: 1.6;
  resize: vertical;
  min-height: 120px;
  outline: none;
  background: transparent;
  color: #303133;
  font-family: inherit;
}

.prompt-textarea::placeholder {
  color: #c0c4cc;
}

/* AI辅助功能栏 */
.ai-assist-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #667eea0d 0%, #764ba20d 100%);
  border-radius: 8px;
}

.ai-assist-left {
  display: flex;
  gap: 12px;
}

.ai-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid #667eea;
  background: white;
  color: #667eea;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.ai-btn:hover {
  background: #667eea;
  color: white;
}

.ai-btn .el-icon {
  font-size: 16px;
}

/* 灵感推荐弹窗样式 */
.hotspots-content {
  padding: 0 8px;
}

.hotspots-alert {
  margin-bottom: 16px;
}

.hotspots-list {
  max-height: 400px;
  overflow-y: auto;
}

.hotspot-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.3s ease;
}

.hotspot-item:hover {
  background: #e8f4fd;
  transform: translateX(4px);
}

.hotspot-rank {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
  width: 48px;
  flex-shrink: 0;
  text-align: center;
}

.hotspot-content {
  flex: 1;
}

.hotspot-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.hotspot-stats {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hotspot-views {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
}

.input-toolbar {
  display: flex;
  align-items: center;
  margin-top: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px dashed #d9d9d9;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
}

.tool-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.style-btn {
  background: #f0f5ff;
  border: 1px solid #d6e4ff;
  color: #2f54eb;
}

.upload-preview-inline {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #d9d9d9;
}

.upload-preview-inline img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-preview-inline .remove-btn {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ff4d4f;
  color: #fff;
  border-radius: 50%;
  padding: 2px;
  cursor: pointer;
  font-size: 10px;
}

.param-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 模型选择区域 */
.model-selection-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-desc {
  font-size: 13px;
  color: #909399;
}

.models-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.model-selection-card {
  position: relative;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  padding: 16px;
  border: 2px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.model-selection-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.model-selection-card.selected {
  border-color: #667eea;
  background: linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
}

.model-card-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.model-card-content {
  flex: 1;
  min-width: 0;
}

.model-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.model-desc {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.model-tag {
  display: inline-block;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 6px;
  font-weight: 600;
  background: linear-gradient(135deg, #fecaca 0%, #fca5a5 100%);
  color: #dc2626;
}

.model-check {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 20px;
  height: 20px;
  background: #667eea;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
}

.param-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.param-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #f5f5f5;
  border-radius: 6px;
  color: #666;
  font-size: 13px;
}

.param-select {
  width: auto;
  min-width: 100px;
}

.generate-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  padding: 0 32px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 8px;
}

.generate-btn:hover {
  background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
}

.history-section {
  margin-top: 32px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.video-item {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
}

.video-thumbnail {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.video-thumbnail video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.video-item:hover .play-overlay {
  opacity: 1;
}

.play-overlay .el-icon {
  font-size: 40px;
  color: #fff;
}

.video-info {
  padding: 16px;
}

.video-info h3 {
  margin: 0 0 8px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.video-info p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

/* 响应式 */
@media (max-width: 768px) {
  .task-type-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .models-grid {
    grid-template-columns: 1fr;
  }

  .generation-container {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .task-type-grid {
    grid-template-columns: 1fr;
  }
}
</style>
