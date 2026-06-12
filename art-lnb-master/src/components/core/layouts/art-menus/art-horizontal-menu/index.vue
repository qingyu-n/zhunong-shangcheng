<!-- 水平菜单 -->
<template>
  <div class="flex-1 overflow-hidden relative">
    <!-- 鼠标跟随滑块 -->
    <div
      class="menu-slider"
      :style="sliderStyle"
    />

    <ElMenu
      ref="menuRef"
      :ellipsis="true"
      mode="horizontal"
      :default-active="routerPath"
      :text-color="isDark ? 'var(--art-gray-800)' : 'var(--art-gray-700)'"
      :popper-offset="-6"
      background-color="transparent"
      :show-timeout="50"
      :hide-timeout="50"
      popper-class="horizontal-menu-popper"
      class="w-full border-none"
    >
      <HorizontalSubmenu
        v-for="item in filteredMenuItems"
        :key="item.path"
        :ref="el => setMenuItemRef(item.path, el)"
        :item="item"
        :isMobile="false"
        :level="0"
        @mouseenter="handleMouseEnter($event, item.path)"
      />
    </ElMenu>
  </div>
</template>

<script setup lang="ts">
  import type { AppRouteRecord } from '@/types/router'
  import HorizontalSubmenu from './widget/HorizontalSubmenu.vue'
  import { useSettingStore } from '@/store/modules/setting'

  defineOptions({ name: 'ArtHorizontalMenu' })

  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)

  interface Props {
    /** 菜单列表数据 */
    list: AppRouteRecord[]
  }

  const route = useRoute()

  const props = withDefaults(defineProps<Props>(), {
    list: () => []
  })

  // 菜单和滑块相关引用
  const menuRef = ref()
  const menuItemRefs = ref<Map<string, any>>(new Map())

  // 滑块样式状态
  const sliderStyle = ref({
    left: '0px',
    width: '0px',
    opacity: '0'
  })

  /**
   * 设置菜单项引用
   */
  const setMenuItemRef = (path: string, el: any) => {
    if (el) {
      menuItemRefs.value.set(path, el)
    }
  }

  /**
   * 处理鼠标进入菜单项
   * 更新滑块位置到当前悬停的菜单项
   */
  const handleMouseEnter = (event: MouseEvent, path: string) => {
    const target = event.currentTarget as HTMLElement
    if (!target) return

    const rect = target.getBoundingClientRect()
    const parentRect = (target.parentElement as HTMLElement)?.getBoundingClientRect()

    if (parentRect) {
      sliderStyle.value = {
        left: `${rect.left - parentRect.left}px`,
        width: `${rect.width}px`,
        opacity: '1'
      }
    }
  }

  /**
   * 处理鼠标离开菜单区域
   * 隐藏滑块
   */
  const handleMouseLeave = () => {
    sliderStyle.value = {
      ...sliderStyle.value,
      opacity: '0'
    }
  }

  onMounted(() => {
    // 监听菜单容器的鼠标离开事件
    if (menuRef.value?.$el) {
      menuRef.value.$el.addEventListener('mouseleave', handleMouseLeave)
    }
  })

  onUnmounted(() => {
    if (menuRef.value?.$el) {
      menuRef.value.$el.removeEventListener('mouseleave', handleMouseLeave)
    }
  })

  /**
   * 过滤后的菜单项列表
   * 只显示未隐藏的菜单项
   */
  const filteredMenuItems = computed(() => {
    return filterMenuItems(props.list)
  })

  /**
   * 当前激活的路由路径
   * 用于菜单高亮显示
   */
  const routerPath = computed(() => String(route.meta.activePath || route.path))

  /**
   * 递归过滤菜单项，移除隐藏的菜单
   * 如果一个父菜单的所有子菜单都被隐藏，则父菜单也会被隐藏
   * @param items 菜单项数组
   * @returns 过滤后的菜单项数组
   */
  const filterMenuItems = (items: AppRouteRecord[]): AppRouteRecord[] => {
    return items
      .filter((item) => {
        // 如果当前项被隐藏，直接过滤掉
        if (item.meta.isHide) {
          return false
        }

        // 如果有子菜单，递归过滤子菜单
        if (item.children && item.children.length > 0) {
          const filteredChildren = filterMenuItems(item.children)
          // 如果所有子菜单都被过滤掉了，则隐藏父菜单
          return filteredChildren.length > 0
        }

        // 叶子节点且未被隐藏，保留
        return true
      })
      .map((item) => ({
        ...item,
        children: item.children ? filterMenuItems(item.children) : undefined
      }))
  }
</script>

<style scoped>
  /* Remove el-menu bottom border */
  :deep(.el-menu) {
    border-bottom: none !important;
    position: relative;
  }

  /* Remove default styles for first-level menu items */
  :deep(.el-menu-item[tabindex='0']) {
    background-color: transparent !important;
    border: none !important;
  }

  /* Remove bottom border from submenu titles */
  :deep(.el-menu--horizontal .el-sub-menu__title) {
    padding: 0 30px 0 10px !important;
    border: 0 !important;
  }

  /* 鼠标跟随滑块样式 */
  .menu-slider {
    position: absolute;
    top: 0;
    height: 100%;
    background-color: var(--art-primary-color, #409eff);
    opacity: 0.1;
    border-radius: 4px;
    pointer-events: none;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 0;
  }
</style>
