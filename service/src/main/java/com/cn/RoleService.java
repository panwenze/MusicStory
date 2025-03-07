package com.cn;

import com.cn.dao.PermissionRepository;
import com.cn.dao.RoleRepository;
import com.cn.entity.Permission;
import com.cn.entity.Role;
import com.cn.pojo.MenuDTO;
import com.cn.util.MenuUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 角色权限service
 *
 * @author ngcly
 * @date 2018-03-21 18:07
 */
@Service
@AllArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * 根据ID获取角色
     */
    public Role findRole(long roleId) {
        return roleRepository.getById(roleId);
    }

    /**
     * 根据条件查询角色列表
     */
    public Page<Role> getRoleList(Pageable pageable, Role role) {
        return roleRepository.findAll(RoleRepository.getRoleList(role.getRoleName(), role.getAvailable(), role.getRoleType()), pageable);
    }

    /**
     * 获取所有角色
     */
    public List<Role> getAllRole() {
        return roleRepository.findAll();
    }

    /**
     * 获取可用角色
     */
    public Set<Role> getAvailableRoles(byte type) {
        return roleRepository.getAllByAvailableIsTrueAndRoleType(type);
    }

    /**
     * 保存角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRole(Role role) {
        if (role.getId() == null) {
            roleRepository.save(role);
        } else {
            Role role1 = roleRepository.getById(role.getId());
            role1.setRoleName(role.getRoleName());
            role1.setRoleCode(role.getRoleCode());
            role1.setRoleType(role.getRoleType());
            role1.setDescription(role.getDescription());
            role1.setAvailable(role.getAvailable());
        }
    }

    /**
     * 保存授权
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveGrant(long roleId, String menuIds) {
        Role role = roleRepository.getById(roleId);
        List<Permission> permissions = permissionRepository.findMenuList();
        List<Permission> permissionList = new ArrayList<>();
        String[] permissionIds = menuIds.split(",");
        for (String permissionId : permissionIds) {
            for (Permission permission : permissions) {
                if (permissionId.equals(permission.getId().toString())) {
                    permissionList.add(permission);
                }
            }
        }
        role.setPermissions(permissionList);
    }

    /**
     * 修改角色是否可用
     */
    @Transactional(rollbackFor = Exception.class)
    public void altAvailable(long roleId) {
        Role role = roleRepository.getById(roleId);
        role.setAvailable(!role.getAvailable());
    }

    /**
     * 删除角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void delRole(long roleId) {
        roleRepository.deleteById(roleId);
    }

    /**
     * 根据ID获取菜单
     */
    public Permission getPermissionById(long menuId) {
        return permissionRepository.getById(menuId);
    }

    /**
     * 获取资源列表
     */
    public List<Permission> getPermissionList() {
        return permissionRepository.findMenuList();
    }

    /**
     * 获取菜单列表
     * @return List<MenuDTO>
     */
    public List<Permission> getMenuList() {
        List<Permission> list = permissionRepository.findMenuList();
        return MenuUtil.menuTreeSort(list);
    }

    /**
     * 获取带有勾选状态的 菜单列表
     * @param roleId 角色id
     * @return Set<MenuDTO>
     */
    public Set<MenuDTO> getMenuListWithChecked(long roleId){
        List<Permission> permissions = permissionRepository.findMenuList();
        List<Permission> rolePermissions = roleRepository.getById(roleId).getPermissions();
        return MenuUtil.checkMenuSelected(permissions,rolePermissions);
    }

    /**
     * 保存菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveMenu(Permission permission) {
        Permission pms = new Permission();
        if (permission.getId() != null) {
            pms = permissionRepository.getById(permission.getId());
        } else {
            if (!MenuDTO.rootId.equals(permission.getParentId())) {
                Permission parentPermission = permissionRepository.getById(permission.getParentId());
                pms.setParentId(parentPermission.getId());
                pms.setParentIds(parentPermission.getParentIds() + "/" + parentPermission.getId());
            } else {
                pms.setParentId(MenuDTO.rootId);
                pms.setParentIds("0");
            }
        }
        pms.setName(permission.getName());
        pms.setUrl(permission.getUrl());
        pms.setResourceType(permission.getResourceType());
        pms.setSort(permission.getSort());
        pms.setPurview(permission.getPurview());
        pms.setIcon(permission.getIcon());
        permissionRepository.save(pms);
    }

    /**
     * 删除菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void delMenu(long menuId) {
        Permission permission = permissionRepository.getById(menuId);
        permissionRepository.deletePermissionByParentIdsStartingWith(permission.getParentIds() + "/" + permission.getId());
        permissionRepository.delete(permission);
    }

}
