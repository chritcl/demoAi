package com.oa.platform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.constant.Constants;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.system.entity.SysMenu;
import com.oa.platform.system.mapper.SysMenuMapper;
import com.oa.platform.system.vo.RouterVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单/权限服务。
 */
@Service
public class SysMenuService {

    private final SysMenuMapper menuMapper;

    public SysMenuService(SysMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    /**
     * 列表查询（树形结构由前端构建，这里返回平铺列表）。
     */
    public List<SysMenu> list(String menuName, String status) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        if (menuName != null && !menuName.isBlank()) {
            wrapper.like(SysMenu::getMenuName, menuName);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(SysMenu::getStatus, Integer.parseInt(status));
        }
        wrapper.orderByAsc(SysMenu::getSort);
        return menuMapper.selectList(wrapper);
    }

    /** 当前用户可见的菜单（含目录/菜单，不含按钮） */
    public List<SysMenu> listForCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return new ArrayList<>();
        }
        if (Constants.SUPER_ADMIN_ID.equals(userId)) {
            return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .ne(SysMenu::getMenuType, Constants.MENU_BTN)
                    .eq(SysMenu::getStatus, 0)
                    .orderByAsc(SysMenu::getSort));
        }
        return menuMapper.selectMenusByUserId(userId);
    }

    public SysMenu getById(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        return menu;
    }

    public void create(SysMenu menu) {
        menuMapper.insert(menu);
    }

    public void update(SysMenu menu) {
        if (menu.getId() != null && menu.getId().equals(menu.getParentId())) {
            throw new BusinessException("上级菜单不能选择自己");
        }
        menuMapper.updateById(menu);
    }

    public void delete(Long id) {
        // 校验是否有子菜单
        Long childCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("存在子菜单，不允许删除");
        }
        menuMapper.deleteById(id);
    }

    /**
     * 构建（前端）动态路由树。
     */
    public List<RouterVO> buildRouters(Long userId) {
        List<SysMenu> menus;
        if (userId != null && Constants.SUPER_ADMIN_ID.equals(userId)) {
            menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .ne(SysMenu::getMenuType, Constants.MENU_BTN)
                    .eq(SysMenu::getStatus, 0)
                    .orderByAsc(SysMenu::getSort));
        } else if (userId != null) {
            menus = menuMapper.selectMenusByUserId(userId);
        } else {
            menus = new ArrayList<>();
        }
        List<SysMenu> tree = buildTree(menus, 0L);
        return tree.stream().map(this::toRouter).collect(Collectors.toList());
    }

    /** 递归构建菜单树 */
    private List<SysMenu> buildTree(List<SysMenu> all, Long parentId) {
        return all.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .peek(m -> m.setChildren(buildTree(all, m.getId())))
                .sorted(Comparator.comparingInt(m -> m.getSort() == null ? 0 : m.getSort()))
                .collect(Collectors.toList());
    }

    /** SysMenu -> RouterVO */
    private RouterVO toRouter(SysMenu menu) {
        RouterVO router = new RouterVO();
        router.setMenuId(menu.getId());
        router.setName(routeName(menu.getPath()));
        router.setPath(routerPath(menu));
        router.setComponent(componentOf(menu));
        router.setHidden(menu.getVisible() != null && menu.getVisible() == 1);
        router.setKeepAlive(menu.getIsCache() != null && menu.getIsCache() == 0);
        router.setIsFrame(menu.getIsFrame() != null && menu.getIsFrame() == 0);

        RouterVO.Meta meta = new RouterVO.Meta();
        meta.setTitle(menu.getMenuName());
        meta.setIcon(menu.getIcon());
        meta.setPerms(menu.getPerms());
        router.setMeta(meta);

        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            router.setChildren(menu.getChildren().stream().map(this::toRouter).collect(Collectors.toList()));
            if (menu.getRedirect() != null && !menu.getRedirect().isBlank()) {
                router.setRedirect(menu.getRedirect());
            } else if (!menu.getChildren().isEmpty()) {
                router.setRedirect(firstChildPath(menu.getChildren().get(0), menu));
            }
        }
        return router;
    }

    private String firstChildPath(SysMenu child, SysMenu parent) {
        String p = parent.getPath();
        String c = child.getPath();
        if (c == null) {
            return null;
        }
        if (c.startsWith("/")) {
            return c;
        }
        return (p == null ? "" : p) + "/" + c;
    }

    private String routeName(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        // 首字母大写，去掉斜杠
        String cleaned = path.replace("/", "");
        if (cleaned.isEmpty()) {
            return "";
        }
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }

    private String routerPath(SysMenu menu) {
        String path = menu.getPath();
        if (path == null) {
            path = "";
        }
        // 顶级目录统一加前导 /
        if (menu.getParentId() != null && menu.getParentId() == 0L) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
        }
        return path;
    }

    private String componentOf(SysMenu menu) {
        if (Constants.MENU_DIR.equals(menu.getMenuType())) {
            return menu.getParentId() != null && menu.getParentId() == 0L ? "Layout" : "ParentView";
        }
        // 菜单 C
        if (menu.getComponent() == null || menu.getComponent().isBlank()) {
            return null;
        }
        return menu.getComponent();
    }
}
