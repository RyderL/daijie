package org.daijie.shiro;

import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.daijie.core.util.http.CookieUtil;
import org.daijie.core.util.http.HttpConversationUtil;
import org.daijie.shiro.authc.AuthorizationToken;
import org.daijie.shiro.authc.UserToken;
import org.daijie.shiro.session.ShiroRedisSession.Redis;

/**
 * 用户登录后角色权限注入到shiro管理
 * @author daijie
 * @date 2017年6月22日
 */
public class UserAuthorizingRealm extends AuthorizingRealm {

	/**
	 * 设置用户角色信息
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(
			PrincipalCollection principals) {
//		String currentUsername = (String) super.getAvailablePrincipal(principals);
		SimpleAuthorizationInfo simpleAuthorInfo = new SimpleAuthorizationInfo(); 
		Object user = principals.fromRealm(getName()).iterator().next();
		if(user instanceof String){
			simpleAuthorInfo.addRole(null);
		}else{
			List<String> roles = ((UserToken) user).getRoles();
			if(roles != null){
				simpleAuthorInfo.addRoles(roles);
			}
			List<String> permissions = ((UserToken) user).getPermissions();
			if(permissions != null){
				simpleAuthorInfo.addStringPermissions(permissions);
			}
		}
		return simpleAuthorInfo;
	}

	/**
	 * 设置用户登录信息
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(
			AuthenticationToken token) throws AuthenticationException {
		AuthorizationToken authorizationToken = (AuthorizationToken) token;
		String username = (String)token.getPrincipal();
		SimpleAuthenticationInfo authcInfo = null; 
		Session session = Redis.getSession();
		if(authorizationToken.getUser() != null){
			authcInfo = new SimpleAuthenticationInfo(
					authorizationToken.getUser(), 
					authorizationToken.getPassword(), 
					ByteSource.Util.bytes(username), 
					getName()
					);
			session.setAttribute(authorizationToken.getAuthcKey(), authorizationToken.getUser());
		}else{
			authcInfo = new SimpleAuthenticationInfo(
					authorizationToken.getUsername(), 
					authorizationToken.getPassword(), 
					ByteSource.Util.bytes(username), 
					getName()
					);
			session.setAttribute(authorizationToken.getAuthcKey(), username);
		}
		if(authorizationToken.getSalt() != null){
			authcInfo.setCredentialsSalt(ByteSource.Util.bytes(authorizationToken.getSalt()));
		}
		CookieUtil.set(HttpConversationUtil.TOKEN_NAME, session.getId().toString(), null);
		return authcInfo;
	}

}
