/**
 * 🔧 助农商城 - 一键修复脚本
 * 
 * 使用方法：
 * 1. 打开浏览器访问 http://localhost:5173
 * 2. 按 F12 打开开发者工具
 * 3. 切换到 Console 标签
 * 4. 粘贴此脚本并按回车执行
 * 5. 脚本会自动登录并测试所有功能
 */

(async function autoFix() {
  const style = 'color: #fff; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 10px 20px; border-radius: 8px; font-size: 14px; font-weight: bold;'
  
  console.log('%c🔧 助农商城自动修复工具', style)
  console.log('=====================================\n')
  
  // 步骤1：自动登录
  console.log('[步骤1/5] 正在自动登录...')
  
  try {
    const loginRes = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: 'admin', password: '123456' })
    })
    
    const loginData = await loginRes.json()
    
    if (loginData.code === 200 && loginData.data) {
      // 保存Token到localStorage
      localStorage.setItem('token', loginData.data.token)
      localStorage.setItem('userInfo', JSON.stringify(loginData.data.userInfo))
      
      console.log('%c✅ 登录成功！', 'color: green; font-weight: bold')
      console.log('   用户:', loginData.data.userInfo.username)
      console.log('   角色:', loginData.data.userInfo.role)
      console.log('   Token已保存到localStorage')
      console.log('')
    } else {
      console.log('%c❌ 登录失败:', 'color: red', loginData.message || loginData.msg)
      return
    }
  } catch (e) {
    console.log('%c❌ 登录异常:', 'color: red', e.message)
    return
  }
  
  // 步骤2：获取Token并测试收藏功能
  console.log('[步骤2/5] 测试收藏功能...')
  const token = localStorage.getItem('token')
  const headers = { 'Authorization': `Bearer ${token}` }
  
  try {
    // 先添加一个收藏
    await fetch('/api/favorite', {
      method: 'POST',
      headers: { ...headers, 'Content-Type': 'application/json' },
      body: JSON.stringify({ productId: 1000 })
    })
    
    // 再获取收藏列表
    const favRes = await fetch('/api/favorite/list?page=1&size=10', { headers })
    const favData = await favRes.json()
    
    if (favData.code === 200) {
      const list = favData.data?.list || favData.data?.records || []
      console.log('%c✅ 收藏功能正常！', 'color: green; font-weight: bold')
      console.log(`   当前收藏数: ${list.length}`)
      console.log('   数据格式:', favData.data ? Object.keys(favData.data).join(', ') : '无')
      console.log('')
    } else {
      console.log('%c❌ 收藏功能异常:', 'color: red', favData.message)
    }
  } catch (e) {
    console.log('%c❌ 收藏功能异常:', 'color: red', e.message)
  }
  
  // 步骤3：测试地址管理
  console.log('[步骤3/5] 测试地址管理...')
  
  try {
    const addrRes = await fetch('/api/address', {
      method: 'POST',
      headers: { ...headers, 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: '测试用户',
        phone: '13900139000',
        province: '广东省',
        city: '深圳市',
        district: '南山区',
        detailAddress: '科技园南区XXX号',
        isDefault: 0
      })
    })
    const addrData = await addrRes.json()
    
    if (addrData.code === 200) {
      console.log('%c✅ 添加地址成功！', 'color: green; font-weight: bold')
      console.log('   地址ID:', addrData.data?.id)
      console.log('')
    } else {
      console.log('%c❌ 添加地址失败:', 'color: red', addrData.message || addrData.msg)
    }
  } catch (e) {
    console.log('%c❌ 地址功能异常:', 'color: red', e.message)
  }
  
  // 步骤4：测试购物车
  console.log('[步骤4/5] 测试购物车...')
  
  try {
    const cartRes = await fetch('/api/cart/add', {
      method: 'POST',
      headers: { ...headers, 'Content-Type': 'application/json' },
      body: JSON.stringify({ productId: 1001, quantity: 2 })
    })
    const cartData = await cartRes.json()
    
    if (cartData.code === 200) {
      console.log('%c✅ 添加购物车成功！', 'color: green; font-weight: bold')
      console.log('')
      
      // 获取购物车列表
      const cartListRes = await fetch('/api/cart/list', { headers })
      const cartListData = await cartListRes.json()
      
      if (cartListData.code === 200) {
        const cartItems = Array.isArray(cartListData.data) ? cartListData.data : []
        console.log(`   购物车商品数: ${cartItems.length}`)
      }
    } else {
      console.log('%c❌ 添加购物车失败:', 'color: red', cartData.message || cartData.msg)
    }
  } catch (e) {
    console.log('%c❌ 购物车功能异常:', 'color: red', e.message)
  }
  
  // 步骤5：总结
  console.log('\n=====================================')
  console.log('%c🎉 自动修复完成！', style)
  console.log('')
  console.log('%c现在请执行以下操作：', 'color: blue; font-weight: bold')
  console.log('')
  console.log('1️⃣  按 F5 或 Ctrl+R 刷新页面')
  console.log('2️⃣  访问 "我的" → "我的收藏" （应该能看到数据）')
  console.log('3️⃣  访问 "我的" → "收货地址" （应该能看到刚添加的地址）')
  console.log('4️⃣  点击商品详情页的 "加入购物车" 按钮')
  console.log('5️⃣  查看购物车页面是否显示商品')
  console.log('')
  console.log('%c💡 如果仍然有问题，请查看Console中的红色错误信息', 'color: orange')
  console.log('=====================================\n')
})()
