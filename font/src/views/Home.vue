<!-- views/Home.vue -->
<template>
    <div class="home-container">
      <div class="title">洛尘 海龟汤</div>
      <a-button 
        type="primary" 
        size="large" 
        @click="startGame"
        class="start-button"
      >
        点击开始
      </a-button>
    </div>
  </template>
  
  <script setup>
  import { useRouter } from 'vue-router'
  import { Button } from 'ant-design-vue'
  
  const router = useRouter()
  
  const startGame = () => {
    // 导航到聊天页面
    router.push('/chat')
  }
  </script>
  
  <style scoped>
  .home-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100vh;
    background-image:url("../image/1666416928455.jpg");
    background-size: cover;
  }
  
  .title {
    font-size: 4rem;
    font-weight: bold;
    margin-bottom: 2rem;
    color: #1890ff;
  }
  
  .start-button {
    padding: 0rem 2rem 1rem 2rem;
    font-size: 1.2rem;
  }
  </style>