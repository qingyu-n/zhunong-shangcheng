/**
 * 购物车状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi } from '../api'

export const useCartStore = defineStore('cart', () => {
  // State
  const cartList = ref([])
  const loading = ref(false)

  // Getters
  // 购物车商品总数
  const totalCount = computed(() => {
    return cartList.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  // 购物车选中商品总价
  const totalPrice = computed(() => {
    return cartList.value
      .filter(item => item.selected)
      .reduce((sum, item) => sum + item.price * item.quantity, 0)
  })

  // 选中商品数量
  const selectedCount = computed(() => {
    return cartList.value.filter(item => item.selected).length
  })

  // 是否全选
  const isAllSelected = computed(() => {
    return cartList.value.length > 0 && cartList.value.every(item => item.selected)
  })

  // Actions
  // 获取购物车列表
  const fetchCartList = async () => {
    loading.value = true
    try {
      const res = await cartApi.getCartList()
      if (Array.isArray(res)) {
        cartList.value = res.map(item => ({ ...item, selected: false }))
      } else {
        console.warn('购物车数据格式异常:', res)
        cartList.value = []
      }
      return res
    } catch (error) {
      console.error('获取购物车列表失败:', error)
      cartList.value = []
      throw error
    } finally {
      loading.value = false
    }
  }

  // 添加商品到购物车
  const addToCart = async (product) => {
    try {
      const params = typeof product === 'object' && product.productId !== undefined
        ? { productId: product.productId || product.id, quantity: product.quantity || 1 }
        : { productId: product, quantity: 1 }

      console.log('添加到购物车，参数:', params)
      const res = await cartApi.addToCart(params)
      await fetchCartList()
      return res
    } catch (error) {
      console.error('添加购物车失败:', error)
      throw error
    }
  }

  // 更新商品数量
  const updateQuantity = async (id, quantity) => {
    const res = await cartApi.updateCartItem(id, { quantity })
    const item = cartList.value.find(item => item.id === id)
    if (item) {
      item.quantity = quantity
    }
    return res
  }

  // 删除商品
  const removeItem = async (id) => {
    await cartApi.deleteCartItem(id)
    cartList.value = cartList.value.filter(item => item.id !== id)
  }

  // 批量删除
  const batchRemove = async (ids) => {
    await cartApi.batchDelete(ids)
    cartList.value = cartList.value.filter(item => !ids.includes(item.id))
  }

  // 切换选中状态
  const toggleSelect = (id) => {
    const item = cartList.value.find(item => item.id === id)
    if (item) {
      item.selected = !item.selected
    }
  }

  // 全选/取消全选
  const toggleSelectAll = () => {
    const newValue = !isAllSelected.value
    cartList.value.forEach(item => {
      item.selected = newValue
    })
  }

  // 获取选中的商品
  const getSelectedItems = () => {
    return cartList.value.filter(item => item.selected)
  }

  // 清空购物车
  const clearCart = async () => {
    await cartApi.clearCart()
    cartList.value = []
  }

  // 清空本地购物车数据（用于退出登录）
  const resetCart = () => {
    cartList.value = []
  }

  return {
    cartList,
    loading,
    totalCount,
    totalPrice,
    selectedCount,
    isAllSelected,
    fetchCartList,
    addToCart,
    updateQuantity,
    removeItem,
    batchRemove,
    toggleSelect,
    toggleSelectAll,
    getSelectedItems,
    clearCart,
    resetCart
  }
})
