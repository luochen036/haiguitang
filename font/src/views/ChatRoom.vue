<template>
    <div class="chat-container">
      <!-- 动态背景网格（增强科技感） -->
      <div class="bg-grid"></div>
  
      <!-- 房间号区域（视觉焦点） -->
      <div class="room-info">
        <div class="room-label">AI海龟汤房间</div>
        <div class="room-id" :class="{ 'room-id-glow': isStarted }">
          #{{ roomId }}
          <span class="room-status" v-if="isStarted">
            <i class="status-dot"></i> 游戏中
          </span>
        </div>
      </div>
  
      <!-- 对话区域（带滚动动画） -->
      <div class="chat-messages" ref="messageContainer">
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          :class="['message-item', msg.isUser ? 'user-message' : 'ai-message']"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <img 
            :src="msg.isUser ? userAvatar : aiAvatar" 
            alt="Avatar" 
            class="avatar"
            @mouseenter="avatarHover = index"
            @mouseleave="avatarHover = -1"
          >
          <!-- 消息气泡（带动态效果） -->
          <div class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
            <!-- 加载状态动画（AI回复时显示） -->
            <div class="typing-indicator" v-if="isTyping && !msg.isUser && index === messages.length - 1">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>
      </div>
  
      <!-- 控制按钮（渐变+发光效果） -->
      <div class="control-buttons">
        <a-button 
          type="primary" 
          @click="handleStart"
          :disabled="isStarted"
          class="control-btn start-btn"
        >
          <i class="start-icon"></i> 开始游戏
        </a-button>
        <a-button 
          danger 
          @click="handleEnd"
          :disabled="isGameOver"
          class="control-btn end-btn"
        >
          <i class="end-icon"></i> <span style="color: white;">结束游戏</span>
        </a-button>
      </div>
  
      <!-- 输入区域（玻璃拟态风格） -->
      <div class="input-area">
        <a-input
          v-model:value="inputMessage"
          placeholder="输入问题/答案，或发送'开始'启动游戏..."
          @keyup.enter="sendMessage"
          class="message-input"
          :disabled="isGameOver"
        />
        <a-button 
          type="primary" 
          @click="sendMessage"
          class="send-button"
          :disabled="!inputMessage.trim() || isGameOver"
        >
          发送
          <i class="send-icon"></i>
        </a-button>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, watch } from 'vue'
  import { Button, Input } from 'ant-design-vue'
  import axios from 'axios'
  
  // 头像与状态
  const userAvatar = ref('https://picsum.photos/id/1005/200')
  const aiAvatar = ref('https://picsum.photos/id/1025/200')
  const avatarHover = ref(-1) // 头像hover状态
  const isTyping = ref(false) // AI输入中动画
  
  // 房间与消息
  const roomId = ref(0)
  const messages = ref([])
  const inputMessage = ref('')
  const messageContainer = ref(null)
  
  // 游戏状态
  const isStarted = ref(false)
  const isGameOver = ref(false)
  
  // 生成6位房间号
  const generateRoomId = () => Math.floor(100000 + Math.random() * 900000)
  
  // 发送消息到后端（新增"输入中"状态）
  const sendToBackend = async (message) => {
    isTyping.value = true
    try {
      const response = await axios.post('http://localhost:8888/api/chat/message', null, {
        params: { roomId: roomId.value, message }
      })
      return response.data
    } catch (error) {
      console.error('发送失败:', error)
      return '⚠️ 服务器连接失败，请稍后重试'
    } finally {
      isTyping.value = false // 无论成功失败，关闭输入动画
    }
  }
  
  // 发送消息处理（逻辑不变，新增动画触发）
  const sendMessage = async () => {
    if (!inputMessage.value.trim()) return
    const userMsg = inputMessage.value.trim()
  
    // 添加用户消息
    messages.value.push({ content: userMsg, isUser: true })
    inputMessage.value = ''
  
    // 启动游戏逻辑
    if (userMsg === '开始' && !isStarted.value) {
      handleStart()
      return
    }
    if (!isStarted.value) return
  
    // 获取AI回复
    const aiRes = await sendToBackend(userMsg)
    messages.value.push({ content: aiRes, isUser: false })
    
    // 游戏结束判断
    if (aiRes.includes('【游戏已结束】')) {
      isGameOver.value = true
    }
  }
  
  // 开始/结束游戏（逻辑不变）
  const handleStart = async () => {
    if (isStarted.value) return
    isStarted.value = true
    const aiRes = await sendToBackend('开始')
    messages.value.push({ content: aiRes, isUser: false })
  }
  const handleEnd = async () => {
    if (isGameOver.value) return
    const aiRes = await sendToBackend('结束')
    messages.value.push({ content: aiRes, isUser: false })
    isGameOver.value = true
  }
  
  // 平滑滚动到底部
  const scrollToBottom = () => {
    setTimeout(() => {
      if (messageContainer.value) {
        messageContainer.value.scrollTo({
          top: messageContainer.value.scrollHeight,
          behavior: 'smooth' // 平滑滚动（原优化点）
        })
      }
    }, 50)
  }
  
  // 初始化（新增房间号动画）
  onMounted(() => {
    roomId.value = generateRoomId()
    messages.value.push({
      content: '🎉 欢迎来到洛尘 海龟汤！点击"开始游戏"或输入"开始"启动推理',
      isUser: false
    })
    watch(messages, scrollToBottom)
  
    // 房间号入场动画
    setTimeout(() => {
      document.querySelector('.room-id').classList.add('room-id-enter')
    }, 300)
  })
  </script>
  
  <style scoped>
  /* 基础容器：深色背景+全屏布局 */
  .chat-container {
    position: relative;
    display: flex;
    flex-direction: column;
    height: 100vh;
    padding: 1.5rem;
    box-sizing: border-box;
    background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
    color: #f8fafc;
    overflow: hidden;
  }
  
  /* 动态背景网格：科技感底纹 */
  .bg-grid {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: 
      linear-gradient(rgba(56, 189, 248, 0.1) 1px, transparent 1px),
      linear-gradient(90deg, rgba(56, 189, 248, 0.1) 1px, transparent 1px);
    background-size: 40px 40px;
    z-index: 0;
    animation: gridMove 20s linear infinite;
  }
  @keyframes gridMove {
    0% { background-position: 0 0; }
    100% { background-position: 40px 40px; }
  }
  
  /* 房间号区域：视觉焦点+发光效果 */
  .room-info {
    position: relative;
    z-index: 1;
    text-align: center;
    margin-bottom: 1.5rem;
    padding: 1rem;
    border-radius: 12px;
    background: rgba(30, 41, 59, 0.5);
    backdrop-filter: blur(10px); /* 玻璃拟态 */
    box-shadow: 0 0 20px rgba(14, 165, 233, 0.2);
  }
  .room-label {
    font-size: 1rem;
    color: #94a3b8;
    margin-bottom: 0.5rem;
  }
  .room-id {
    font-size: 2.2rem;
    font-weight: 700;
    color: #38bdf8;
    opacity: 0;
    transform: translateY(20px);
    transition: all 0.8s cubic-bezier(0.22, 1, 0.36, 1);
  }
  .room-id-enter {
    opacity: 1;
    transform: translateY(0);
  }
  .room-id-glow {
    animation: idGlow 2s ease-in-out infinite alternate;
  }
  @keyframes idGlow {
    0% { text-shadow: 0 0 10px #38bdf8, 0 0 20px rgba(56, 189, 248, 0.5); }
    100% { text-shadow: 0 0 20px #38bdf8, 0 0 40px rgba(56, 189, 248, 0.8); }
  }
  .room-status {
    margin-left: 1rem;
    font-size: 1rem;
    color: #a7f3d0;
  }
  .status-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #a7f3d0;
    margin-right: 4px;
    animation: dotPulse 1.5s infinite;
  }
  @keyframes dotPulse {
    0% { opacity: 0.5; transform: scale(0.8); }
    50% { opacity: 1; transform: scale(1.2); }
    100% { opacity: 0.5; transform: scale(0.8); }
  }
  
  /* 对话区域：滚动容器+分层阴影 */
  .chat-messages {
    position: relative;
    z-index: 1;
    flex: 1;
    overflow-y: auto;
    padding: 1.5rem;
    border-radius: 12px;
    background: rgba(15, 23, 42, 0.6);
    backdrop-filter: blur(8px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
    margin-bottom: 1.5rem;
  }
  /* 滚动条美化 */
  .chat-messages::-webkit-scrollbar {
    width: 6px;
  }
  .chat-messages::-webkit-scrollbar-track {
    background: rgba(30, 41, 59, 0.3);
    border-radius: 3px;
  }
  .chat-messages::-webkit-scrollbar-thumb {
    background: #38bdf8;
    border-radius: 3px;
  }
  
  /* 消息项：入场动画+左右布局 */
  .message-item {
    display: flex;
    margin-bottom: 1.2rem;
    max-width: 85%;
    opacity: 0;
    transform: translateY(10px);
    animation: msgFadeIn 0.5s ease forwards;
  }
  @keyframes msgFadeIn {
    to { opacity: 1; transform: translateY(0); }
  }
  .ai-message { align-self: flex-start; }
  .user-message { 
    margin-left: auto; 
    flex-direction: row-reverse; 
  }
  
  /* 头像：hover放大效果 */
  .avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    object-fit: cover;
    margin-right: 0.8rem;
    border: 2px solid transparent;
    transition: all 0.3s ease;
  }
  .user-message .avatar { margin-right: 0; margin-left: 0.8rem; }
  .avatar:hover {
    transform: scale(1.1);
    border-color: #38bdf8;
    box-shadow: 0 0 15px rgba(56, 189, 248, 0.6);
  }
  
  /* 消息气泡：渐变+立体阴影 */
  .message-bubble {
    padding: 1rem 1.2rem;
    border-radius: 20px;
    position: relative;
    line-height: 1.5;
    max-width: 100%;
  }
  .ai-message .message-bubble {
    background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
    box-shadow: 0 2px 10px rgba(14, 165, 233, 0.1);
    border-top-left-radius: 4px;
  }
  .user-message .message-bubble {
    background: linear-gradient(135deg, #0284c7 0%, #38bdf8 100%);
    box-shadow: 0 2px 10px rgba(56, 189, 248, 0.3);
    border-top-right-radius: 4px;
  }
  
  /* AI输入中动画：三点加载 */
  .typing-indicator {
    display: flex;
    gap: 4px;
    margin-top: 8px;
  }
  .typing-indicator .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #94a3b8;
    animation: dotBounce 1.4s infinite ease-in-out both;
  }
  .dot:nth-child(1) { animation-delay: -0.32s; }
  .dot:nth-child(2) { animation-delay: -0.16s; }
  @keyframes dotBounce {
    0%, 80%, 100% { transform: scale(0); }
    40% { transform: scale(1); }
  }
  
  /* 控制按钮：渐变+发光反馈 */
  .control-buttons {
    position: relative;
    z-index: 1;
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }
  .control-btn {
    flex: 1;
    height: 50px;
    font-size: 1.1rem !important;
    border-radius: 8px !important;
    transition: all 0.3s ease !important;
    border: none !important;
  }
  .start-btn {
    background: linear-gradient(135deg, #06b6d4 0%, #38bdf8 100%) !important;
    box-shadow: 0 0 15px rgba(6, 182, 212, 0.3);
  }
  .start-btn:hover {
    box-shadow: 0 0 25px rgba(6, 182, 212, 0.5) !important;
    transform: translateY(-2px);
  }
  .end-btn {
    background: linear-gradient(135deg, #ef4444 0%, #f87171 100%) !important;
    box-shadow: 0 0 15px rgba(239, 68, 68, 0.3);
  }
  .end-btn:hover {
    box-shadow: 0 0 25px rgba(239, 68, 68, 0.5) !important;
    transform: translateY(-2px);
  }
  .start-icon, .end-icon { margin-right: 8px; }
  
  /* 输入区域：玻璃拟态+悬浮效果 */
  .input-area {
    position: relative;
    z-index: 1;
    display: flex;
    gap: 1rem;
    height: 56px;
  }
  .message-input {
    flex: 1;
    background: rgba(30, 41, 59, 0.5) !important;
    border: 1px solid rgba(56, 189, 248, 0.3) !important;
    border-radius: 8px !important;
    color: #f8fafc !important;
    padding: 0 1.2rem !important;
    transition: all 0.3s ease !important;
  }
  .message-input::placeholder { color: #94a3b8 !important; }
  .message-input:focus {
    border-color: #38bdf8 !important;
    box-shadow: 0 0 15px rgba(56, 189, 248, 0.3) !important;
  }
  .send-button {
    width: 120px !important;
    height: 100% !important;
    background: linear-gradient(135deg, #38bdf8 0%, #0284c7 100%) !important;
    border-radius: 8px !important;
    transition: all 0.3s ease !important;
  }
  .send-button:hover {
    box-shadow: 0 0 20px rgba(56, 189, 248, 0.5) !important;
    transform: translateY(-2px);
  }
  .send-icon { margin-left: 8px; }
  
  /* 响应式适配：移动端优化 */
  @media (max-width: 768px) {
    .chat-container { padding: 1rem; }
    .room-id { font-size: 1.8rem; }
    .control-buttons { gap: 0.8rem; }
    .control-btn { font-size: 1rem !important; }
    .send-button { width: 100px !important; }
  }
  </style>