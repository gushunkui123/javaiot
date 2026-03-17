package com.agileboot.admin.customize.service.login;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.utils.ServletHolderUtil;
import com.agileboot.common.utils.i18n.MessageUtils;
import com.agileboot.domain.common.cache.MapCache;
import com.agileboot.domain.common.cache.RedisCacheService;
import com.agileboot.admin.customize.async.AsyncTaskFactory;
import com.agileboot.infrastructure.thread.ThreadPoolManager;
import com.agileboot.admin.customize.service.login.dto.ConfigDTO;
import com.agileboot.admin.customize.service.login.command.LoginCommand;
import com.agileboot.infrastructure.user.web.SystemLoginUser;
import com.agileboot.common.enums.common.LoginStatusEnum;
import com.agileboot.domain.system.user.db.SysUserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 登录校验方法
 *
 * @author ruoyi
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LoginService {

    private final TokenService tokenService;

    private final RedisCacheService redisCache;

    private final AuthenticationManager authenticationManager;

    /**
     * 登录验证
     *
     * @param loginCommand 登录参数
     * @return 结果
     */
    public String login(LoginCommand loginCommand) {
        // 用户验证
        Authentication authentication;
        try {
            // 该方法会去调用UserDetailsServiceImpl#loadUserByUsername  校验用户名和密码  认证鉴权
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginCommand.getUsername(), loginCommand.getPassword()));
        } catch (BadCredentialsException e) {
            ThreadPoolManager.execute(AsyncTaskFactory.loginInfoTask(loginCommand.getUsername(), LoginStatusEnum.LOGIN_FAIL,
                MessageUtils.message("Business.LOGIN_WRONG_USER_PASSWORD")));
            throw new ApiException(e, ErrorCode.Business.LOGIN_WRONG_USER_PASSWORD);
        } catch (AuthenticationException e) {
            ThreadPoolManager.execute(AsyncTaskFactory.loginInfoTask(loginCommand.getUsername(), LoginStatusEnum.LOGIN_FAIL, e.getMessage()));
            throw new ApiException(e, ErrorCode.Business.LOGIN_ERROR, e.getMessage());
        } catch (Exception e) {
            ThreadPoolManager.execute(AsyncTaskFactory.loginInfoTask(loginCommand.getUsername(), LoginStatusEnum.LOGIN_FAIL, e.getMessage()));
            throw new ApiException(e, Business.LOGIN_ERROR, e.getMessage());
        }
        // 把当前登录用户 放入上下文中
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 这里获取的loginUser是UserDetailsServiceImpl#loadUserByUsername方法返回的LoginUser
        SystemLoginUser loginUser = (SystemLoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser);
        // 生成token
        return tokenService.createTokenAndPutUserInCache(loginUser);
    }

    /**
     * 获取系统配置数据
     *
     * @return {@link ConfigDTO}
     */
    public ConfigDTO getConfig() {
        ConfigDTO configDTO = new ConfigDTO();
        configDTO.setDictionary(MapCache.dictionaryCache());
        return configDTO;
    }

    /**
     * 记录登录信息
     * @param loginUser 登录用户
     */
    public void recordLoginInfo(SystemLoginUser loginUser) {
        ThreadPoolManager.execute(AsyncTaskFactory.loginInfoTask(loginUser.getUsername(), LoginStatusEnum.LOGIN_SUCCESS,
            LoginStatusEnum.LOGIN_SUCCESS.description()));

        SysUserEntity entity = redisCache.userCache.getObjectById(loginUser.getUserId());

        entity.setLoginIp(JakartaServletUtil.getClientIP(ServletHolderUtil.getRequest()));
        entity.setLoginDate(DateUtil.date());
        entity.updateById();
    }

}
