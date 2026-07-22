import { beforeEach, describe, expect, it } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAppStore } from './app';

describe('应用布局状态', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('切换后应更新侧边栏折叠状态', () => {
    const appStore = useAppStore();

    appStore.toggleSidebar();

    expect(appStore.sidebarCollapsed).toBe(true);
  });
});
