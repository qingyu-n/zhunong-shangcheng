// 前端功能诊断脚本
// 运行方式：在浏览器控制台粘贴执行

console.log('=== 助农商城前端诊断开始 ===\n');

// 1. 检查基本环境
console.log('1. 检查基本环境:');
console.log('   - Vue版本:', Vue?.version || '未检测到');
console.log('   - Router状态:', window.__VUE_ROUTER__ ? '已加载' : '未加载');
console.log('   - Pinia状态:', window.__PINIA__ ? '已加载' : '未加载');

// 2. 检查localStorage
console.log('\n2. 检查登录状态:');
const token = localStorage.getItem('token');
const userInfo = localStorage.getItem('userInfo');
console.log('   - Token:', token ? '存在 (' + token.substring(0, 20) + '...)' : '不存在');
console.log('   - 用户信息:', userInfo ? '存在' : '不存在');

// 3. 测试API连接
console.log('\n3. 测试API连接:');

async function testAPI() {
  try {
    // 测试商品列表
    console.log('   测试商品列表API...');
    const productRes = await fetch('/api/product/list?current=1&size=3');
    const productData = await productRes.json();
    console.log('   ✅ 商品列表API:', productData.code === 200 ? '成功' : '失败 (' + productData.code + ')');
    if (productData.data?.list) {
      console.log('      返回', productData.data.list.length, '个商品');
    }

    // 测试收藏列表（需要token）
    if (token) {
      console.log('   测试收藏列表API...');
      const favRes = await fetch('/api/favorite/list?page=1&size=10', {
        headers: { 'Authorization': 'Bearer ' + token }
      });
      const favData = await favRes.json();
      console.log('   ✅ 收藏列表API:', favData.code === 200 ? '成功' : '失败 (' + favData.code + ')');
      if (favData.data?.records) {
        console.log('      返回', favData.data.records.length, '条收藏记录');
      }
    } else {
      console.log('   ⚠️ 跳过收藏API测试（未登录）');
    }

    // 测试购物车（需要token）
    if (token) {
      console.log('   测试购物车API...');
      const cartRes = await fetch('/api/cart/list', {
        headers: { 'Authorization': 'Bearer ' + token }
      });
      const cartData = await cartRes.json();
      console.log('   ✅ 购物车API:', cartData.code === 200 ? '成功' : '失败 (' + cartData.code + ')');
      if (Array.isArray(cartData.data)) {
        console.log('      购物车有', cartData.data.length, '件商品');
      }
    } else {
      console.log('   ⚠️ 跳过购物车API测试（未登录）');
    }

  } catch (error) {
    console.error('   ❌ API测试失败:', error.message);
  }
}

// 4. 检查Vue组件状态
console.log('\n4. 检查Vue应用状态:');
if (document.querySelector('#app')) {
  const app = document.querySelector('#app');
  console.log('   ✅ #app元素存在');
  console.log('   - 内部HTML长度:', app.innerHTML.length);
  console.log('   - 是否有内容:', app.innerHTML.length > 100 ? '是' : '可能为空');
} else {
  console.log('   ❌ #app元素不存在');
}

// 5. 检查常见错误
console.log('\n5. 检查常见问题:');
const errors = [];

// 检查是否有错误样式
if (document.querySelector('[class*="error"]')) {
  errors.push('发现error类名元素');
}

// 检查控制台错误（无法自动获取，提示用户手动查看）
errors.push('请查看上方Console标签中的红色错误信息');

if (errors.length === 0) {
  console.log('   ✅ 未发现明显问题');
} else {
  errors.forEach(err => console.log('   ⚠️', err));
}

// 执行异步测试
testAPI().then(() => {
  console.log('\n=== 诊断完成 ===');
  console.log('\n如果所有API都返回200，但页面仍无响应，可能原因：');
  console.log('1. 前端代码在API调用后有逻辑错误');
  console.log('2. 状态更新后没有触发重新渲染');
  console.log('3. 错误被try-catch捕获但未显示给用户');
  console.log('\n建议操作：');
  console.log('- 打开浏览器开发者工具(F12)');
  console.log('- 切换到Network标签，重新操作功能');
  console.log('- 查看失败的请求和错误信息');
  console.log('- 切换到Console标签，查看JavaScript错误');
});
