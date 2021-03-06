package ewing.user;

import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQuery;
import ewing.application.BeanDao;
import ewing.application.QueryHelper;
import ewing.application.paging.Page;
import ewing.application.paging.Pager;
import ewing.entity.User;
import ewing.security.AuthorityOrRole;
import ewing.security.SecurityUser;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户数据访问实现。
 */
@Repository
public class UserDaoImpl extends BeanDao implements UserDao {

    public UserDaoImpl() {
        super(qUser);
    }

    @Override
    public Page<User> findUsers(Pager pager, String username, String roleName) {
        SQLQuery<User> query = queryFactory.selectFrom(qUser);
        if (StringUtils.hasText(username)) {
            query.where(qUser.name.contains(username));
        }
        if (roleName != null) {
            query.leftJoin(qUserRole).on(qUser.userId.eq(qUserRole.userId))
                    .leftJoin(qRole).on(qUserRole.roleId.eq(qRole.roleId))
                    .where(qRole.name.contains(roleName));
        }
        return QueryHelper.queryPage(pager, query);
    }

    @Override
    public SecurityUser getByUsername(String username) {
        return queryFactory.select(
                Projections.bean(SecurityUser.class, qUser.all()))
                .from(qUser)
                .where(qUser.name.eq(username))
                .fetchOne();
    }

    @Override
    public List<AuthorityOrRole> getUserAuthorities(Long userId) {
        // 用户->角色->权限
        return queryFactory.select(Projections
                .bean(AuthorityOrRole.class, qAuthority.all()))
                .from(qAuthority)
                .join(qRoleAuthority)
                .on(qAuthority.authorityId.eq(qRoleAuthority.authorityId))
                .join(qUserRole)
                .on(qRoleAuthority.roleId.eq(qUserRole.roleId))
                .where(qUserRole.userId.eq(userId))
                .fetch();
    }

    @Override
    public List<PermissionTree> getUserPermissions(Long userId) {
        // 用户->角色->许可
        return queryFactory.select(Projections
                .bean(PermissionTree.class, qPermission.all()))
                .from(qPermission)
                .join(qRolePermission)
                .on(qPermission.permissionId.eq(qRolePermission.permissionId))
                .join(qUserRole)
                .on(qRolePermission.roleId.eq(qUserRole.roleId))
                .where(qUserRole.userId.eq(userId))
                .fetch();
    }

}
