/**
 * 前端API调试脚本 - 直接在浏览器Console运行
 * 用于诊断所有增删改查功能
 */

(async function debugAll() {
  console.log('%c=== 助农商城前端全面调试 ===', 'color: blue; font-size: 16px; font-weight: bold')
  console.log('开始时间:', new Date().toLocaleString())
  console.log('')

  // 1. 检查基础环境
  console.log('%c[1] 环境检查', 'color: green; font-weight: bold')
  const hasVue = typeof window.__vue_app__ !== 'undefined' || document.querySelector('#app').__vue_app__
  const token = localStorage.getItem('token')
  console.log('  Vue实例:', hasVue ? '✅ 存在' : '❌ 不存在')
  console.log('  Token:', token ? '✅ 存在 (' + token.substring(0, 30) + '...)' : '❌ 未登录')
  console.log('')

  // 2. 测试所有关键API
  console.log('%c[2] API连通性测试', 'color: green; font-weight: bold')

  const tests = [
    { name: '商品列表', url: '/api/product/list?current=1&size=2' },
    { name: '收藏列表', url: '/api/favorite/list?page=1&size=5', needToken: true },
    { name: '购物车列表', url: '/api/cart/list', needToken: true },
    { name: '地址列表', url: '/api/address/list', needToken: true },
    { name: '活动列表', url: '/api/activity/list' },
    { name: '评价列表', url: '/api/review/list?current=1&size=2' },
    { name: '系统配置', url: '/api/config' }
  ]

  for (const test of tests) {
    try {
      const headers = {}
      if (test.needToken && token) {
        headers['Authorization'] = 'Bearer ' + token
      }

      const response = await fetch(test.url, { headers })
      const data = await response.json()

      if (response.ok && data.code === 200) {
        console.log(`  ✅ ${test.name}:`, JSON.stringify(data).substring(0, 80) + '...')
      } else {
        console.log(`  ❌ ${test.name}: HTTP ${response.status}`, data.message || data.msg || '')
      }
    } catch (error) {
      console.log(`  ❌ ${test.name}:`, error.message)
    }
  }

  console.log('')

  // 3. 测试写操作（需要token）
  if (!token) {
    console.log('%c[3] 写操作测试 - ⚠️ 跳过（未登录）', 'color: orange; font-weight: bold')
    console.log('  请先登录后再测试写操作')
    return
  }

  console.log('%c[3] 写操作测试（需要登录）', 'color: green; font-weight: bold')

  // 3.1 测试添加购物车
  try {
    console.log('  测试添加购物车...')
    const cartRes = await fetch('/api/cart/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      body: JSON.stringify({ productId: 1000, quantity: 1 })
    })
    const cartData = await cartRes.json()
    if (cartRes.ok && cartData.code === 200) {
      console.log('  ✅ 添加购物车成功:', cartData.message || cartData.msg)
    } else {
      console.log('  ❌ 添加购物车失败:', cartData.message || cartData.msg || `HTTP ${cartRes.status}`)
    }
  } catch (e) {
    console.log('  ❌ 添加购物车异常:', e.message)
  }

  // 3.2 测试添加地址
  try {
    console.log('  测试添加地址...')
    const addrRes = await fetch('/api/address', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      body: JSON.stringify({
        name: '测试用户',
        phone: '13800138000',
        province: '广东省',
        city: '深圳市',
        district: '南山区',
        detailAddress: '测试地址123号',
        isDefault: 0
      })
    })
    const addrData = await addrRes.json()
    if (addrRes.ok && addrData.code === 200) {
      console.log('  ✅ 添加地址成功:', addrData.message || addrData.msg)
    } else {
      console.log('  ❌ 添加地址失败:', addrData.message || addrData.msg || `HTTP ${addrRes.status}`)
    }
  } catch (e) {
    console.log('  ❌ 添加地址异常:', e.message)
  }

  // 3.3 测试添加收藏
  try {
    console.log('  测试添加收藏...')
    const favRes = await fetch('/api/favorite', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      body: JSON.stringify({ productId: 1000 })
    })
    const favData = await favRes.json()
    if (favRes.ok && favData.code === 200) {
      console.log('  ✅ 添加收藏成功:', favData.message || favData.msg)
    } else {
      console.log('  ❌ 添加收藏失败:', favData.message || favData.msg || `HTTP ${favRes.status}`)
    }
  } catch (e) {
    console.log('  ❌ 添加收藏异常:', e.message)
  }

  console.log('')
  console.log('%c=== 调试完成 ===', 'color: blue; font-size: 16px; font-weight: bold')
  console.log('结束时间:', new Date().toLocaleString())
  console.log('')
  console.log('%c💡 如果所有API都返回200，但前端页面仍无变化，请检查：', 'color: purple')
  console.log('   1. Vue组件是否正确使用了ref/reactive')
  console.log('   2. API调用后是否有await等待完成')
  console.log('   3. 数据赋值后是否触发了Vue的响应式更新')
  console.log('   4. 是否有JavaScript错误中断了后续代码执行')
})()
