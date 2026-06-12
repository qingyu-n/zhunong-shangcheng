<template>
  <div class="about-page">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <!-- Logo -->
        <div class="logo" @click="navigateTo('home')">
          <i class="fas fa-leaf logo-icon"></i>
          <span class="logo-text">农鲜生</span>
        </div>

        <!-- 导航链接 -->
        <nav class="nav-menu">
          <a class="nav-link" @click="navigateTo('home')">首页</a>
          <a class="nav-link" @click="navigateTo('products')">全部商品</a>
          <a class="nav-link" @click="navigateTo('category')">分类浏览</a>
          <a class="nav-link" @click="navigateTo('help')">助农专区</a>
          <a class="nav-link" @click="navigateTo('sale')">限时特惠</a>
          <a class="nav-link" @click="navigateTo('fresh')">新鲜直达</a>
          <a class="nav-link active">关于我们</a>
        </nav>

        <!-- 搜索框 -->
        <div class="search-box">
          <i class="fas fa-search search-icon"></i>
          <input
            type="text"
            placeholder="搜索新鲜农产品..."
            class="search-input"
            v-model="searchKeyword"
            @keyup.enter="handleSearch"
          >
        </div>

        <!-- 功能图标 -->
        <div class="header-icons">
          <div class="icon-wrapper cart-icon-wrapper">
            <a class="icon-btn" @click="navigateTo('cart')">
              <i class="fas fa-shopping-cart"></i>
            </a>
          </div>
          <a class="icon-btn" @click="navigateTo('user')">
            <i class="fas fa-user"></i>
          </a>
        </div>
      </div>
    </header>

    <header class="page-header">
      <div class="container">
        <h1 class="page-title">关于我们</h1>
        <p class="page-subtitle">助力乡村振兴，连接城乡桥梁</p>
      </div>
    </header>

    <section class="about-section">
      <div class="container">
        <div class="about-content">
          <div class="about-image">
            <img src="https://images.unsplash.com/photo-1464226184884-fa280b87c399?w=800&h=600&fit=crop" alt="关于我们">
          </div>
          <div class="about-text">
            <h2>我们的使命</h2>
            <p>农鲜生致力于搭建农产品从田间到餐桌的直通桥梁，帮助农民解决农产品销售难题，同时为城市消费者提供新鲜、优质、实惠的农产品。</p>
            <p>我们相信，通过互联网技术，可以让优质农产品不再受地域限制，让每一份努力都能得到应有的回报。</p>
            
            <h2>我们的愿景</h2>
            <p>成为中国最受信赖的农产品电商平台，让乡村振兴的梦想照进现实。</p>
            
            <h2>核心价值观</h2>
            <div class="values-list">
              <div class="value-item">
                <i class="fas fa-leaf"></i>
                <h3>绿色健康</h3>
                <p>严格把控产品质量，只为您提供最放心的农产品</p>
              </div>
              <div class="value-item">
                <i class="fas fa-hands-helping"></i>
                <h3>助农惠农</h3>
                <p>直接对接农户，减少中间环节，让利于民</p>
              </div>
              <div class="value-item">
                <i class="fas fa-shield-alt"></i>
                <h3>品质保证</h3>
                <p>完善的品控体系，让每一份农产品都值得信赖</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="stats-section">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-number">500+</div>
            <div class="stat-label">合作农户</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">100+</div>
            <div class="stat-label">农产品种类</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">10万+</div>
            <div class="stat-label">服务用户</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">50+</div>
            <div class="stat-label">覆盖地区</div>
          </div>
        </div>
      </div>
    </section>

    <section class="team-section">
      <div class="container">
        <h2 class="section-title">联系我们</h2>
        <div class="contact-grid">
          <div class="contact-item">
            <i class="fas fa-phone"></i>
            <h3>客服热线</h3>
            <p>400-888-8888</p>
            <p class="contact-desc">周一至周日 9:00-21:00</p>
          </div>
          <div class="contact-item">
            <i class="fas fa-envelope"></i>
            <h3>电子邮箱</h3>
            <p>aikun@qq.com</p>
            <p class="contact-desc">24小时内回复</p>
          </div>
          <div class="contact-item">
            <i class="fas fa-map-marker-alt"></i>
            <h3>公司地址</h3>
            <p>广西南宁爱坤农业科技有限公司</p>
            <p class="contact-desc">欢迎来访</p>
          </div>
        </div>
      </div>
    </section>

    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="copyright">© 2026 农鲜生. 保留所有权利</div>
          <div class="footer-links">
            <a class="footer-link" @click="navigateTo('home')">返回首页</a>
            <a class="footer-link">隐私政策</a>
            <a class="footer-link">用户协议</a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 搜索关键词
const searchKeyword = ref('')

// 导航跳转函数
const navigateTo = (page) => {
  const routes = {
    home: '/',
    category: '/category',
    products: '/products',
    help: '/help',
    cart: '/cart',
    user: '/user',
    sale: '/products?sale=1',
    fresh: '/products?fresh=1',
    about: '/about'
  }
  if (routes[page]) {
    router.push(routes[page])
  }
}

// 搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push('/search?keyword=' + encodeURIComponent(searchKeyword.value.trim()))
  }
}
</script>

<style scoped>
.about-page {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.container {
  width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 顶部导航栏 */
.header {
  width: 100%;
  height: 64px;
  background: linear-gradient(135deg, #43A047 0%, #4CAF50 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-container {
  width: 1200px;
  height: 100%;
  margin: 0 auto;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  margin-right: 40px;
  cursor: pointer;
}

.logo-icon {
  font-size: 28px;
  color: #ffffff;
  margin-right: 8px;
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 28px;
  flex: 1;
}

.nav-link {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.95);
  text-decoration: none;
  transition: all 0.3s ease;
  font-weight: 500;
  cursor: pointer;
}

.nav-link:hover,
.nav-link.active {
  color: #ffffff;
}

.search-box {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 6px 16px;
  width: 240px;
  margin-right: 24px;
}

.search-icon {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin-right: 8px;
}

.search-input {
  background: transparent;
  border: none;
  outline: none;
  color: #ffffff;
  font-size: 13px;
  width: 100%;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.7);
}

.header-icons {
  display: flex;
  align-items: center;
  gap: 16px;
}

.icon-wrapper {
  position: relative;
}

.icon-btn {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
}

.icon-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.page-header {
  background: linear-gradient(135deg, #43A047 0%, #4CAF50 100%);
  padding: 80px 0;
  text-align: center;
  color: #ffffff;
}

.page-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 16px;
}

.page-subtitle {
  font-size: 18px;
  opacity: 0.95;
}

.about-section {
  padding: 80px 0;
  background: #ffffff;
}

.about-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.about-image img {
  width: 100%;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.about-text h2 {
  font-size: 28px;
  font-weight: 700;
  color: #333333;
  margin-bottom: 16px;
  margin-top: 32px;
}

.about-text h2:first-child {
  margin-top: 0;
}

.about-text p {
  font-size: 15px;
  line-height: 1.8;
  color: #666666;
  margin-bottom: 16px;
}

.values-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-top: 32px;
}

.value-item {
  text-align: center;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
}

.value-item i {
  font-size: 36px;
  color: #4CAF50;
  margin-bottom: 16px;
}

.value-item h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 8px;
}

.value-item p {
  font-size: 13px;
  color: #666666;
  line-height: 1.6;
  margin-bottom: 0;
}

.stats-section {
  padding: 60px 0;
  background: linear-gradient(135deg, #43A047 0%, #4CAF50 100%);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
  text-align: center;
  color: #ffffff;
}

.stat-number {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 16px;
  opacity: 0.95;
}

.team-section {
  padding: 80px 0;
  background: #ffffff;
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  color: #333333;
  text-align: center;
  margin-bottom: 48px;
}

.contact-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.contact-item {
  text-align: center;
  padding: 40px 24px;
  background: #f8f9fa;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.contact-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.contact-item i {
  font-size: 40px;
  color: #4CAF50;
  margin-bottom: 20px;
}

.contact-item h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 12px;
}

.contact-item p {
  font-size: 15px;
  color: #666666;
  margin-bottom: 4px;
}

.contact-desc {
  font-size: 13px !important;
  color: #999999 !important;
}

.footer {
  background: #333333;
  padding: 32px 0;
  margin-top: auto;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.copyright {
  font-size: 14px;
  color: #999999;
}

.footer-links {
  display: flex;
  gap: 32px;
}

.footer-link {
  font-size: 14px;
  color: #999999;
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
}

.footer-link:hover {
  color: #4CAF50;
}

@media (max-width: 1240px) {
  .container,
  .header-container {
    width: 100%;
    padding: 0 16px;
  }
}

@media (max-width: 1024px) {
  .about-content {
    grid-template-columns: 1fr;
  }

  .values-list {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .contact-grid {
    grid-template-columns: 1fr;
  }

  .nav-menu {
    display: none;
  }

  .search-box {
    width: 180px;
  }
}
</style>
