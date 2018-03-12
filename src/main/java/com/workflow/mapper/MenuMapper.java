package com.workflow.mapper;

import com.workflow.entity.Menu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MenuMapper {
    @Select("select a.id id,name,url,icon,parent from menu a left join permission b on a.id=b.menuId left join user_role c on b.roleId=c.id where c.role=#{role}")
    List<Menu> getMenusForRole(String role);

    @Select("select id,name,url,icon,parent from menu")
    List<Menu> getAllMenus();

    @Select("select a.id id from menu a left join permission b on a.id=b.menuId where b.roleId=#{roleId}")
    List<Integer> getMenuIdsForRole(String roleId);

    //    @InsertProvider(type = PrivilegeMapperProvider.class, method = "insertAll")
    int insertPermission(@Param("list") List<String> menuList, @Param("roleId") String roleId);

    @Delete("delete from permission where roleId=#{roleId}")
    int deletePermission(String roleId);
}