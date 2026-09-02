package com.agileboot.admin.customize.config;

import cn.hutool.json.JSONUtil;
import com.agileboot.admin.customize.service.login.LoginService;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.common.utils.ServletHolderUtil;
import com.agileboot.domain.common.cache.RedisCacheService;
import com.agileboot.admin.customize.async.AsyncTaskFactory;
import com.agileboot.infrastructure.thread.ThreadPoolManager;
import com.agileboot.infrastructure.user.web.SystemLoginUser;
import com.agileboot.admin.customize.service.login.TokenService;
import com.agileboot.common.enums.common.LoginStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.filter.CorsFilter;

/**
 * 主要配置登录流程逻辑涉及以下几个类
 * @see UserDetailsServiceImpl#loadUserByUsername  用于登录流程通过用户名加载用户
 * @see this#unauthorizedHandler()  用于用户未授权或登录失败处理
 * @see this#logOutSuccessHandler 用于退出登录成功后的逻辑
 * @see JwtAuthenticationTokenFilter#doFilter token的校验和刷新
 * @see LoginService#login 登录逻辑
 * @author valarchie
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenService tokenService;

    private final RedisCacheService redisCache;

    /**
     * token认证过滤器
     */
    private final JwtAuthenticationTokenFilter jwtTokenFilter;

    private final UserDetailsService userDetailsService;

    /**
     * 跨域过滤器
     */
    private final CorsFilter corsFilter;


    /**
     * 登录异常处理类
     * 用户未登陆的话  在这个Bean中处理
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedHandler() {
        return (request, response, exception) -> {
            ResponseDTO<Object> responseDTO = ResponseDTO.fail(
                new ApiException(Client.COMMON_NO_AUTHORIZATION, request.getRequestURI())
            );
            ServletHolderUtil.renderString(response, JSONUtil.toJsonStr(responseDTO));
        };
    }


    /**
     *  退出成功处理类 返回成功
     *  在SecurityConfig类当中 定义了/logout 路径对应处理逻辑
     */
    @Bean
    public LogoutSuccessHandler logOutSuccessHandler() {
        return (request, response, authentication) -> {
            SystemLoginUser loginUser = tokenService.getLoginUser(request);
            if (loginUser != null) {
                String userName = loginUser.getUsername();
                // 删除用户缓存记录
                redisCache.loginUserCache.delete(loginUser.getCachedKey());
                // 记录用户退出日志
                ThreadPoolManager.execute(AsyncTaskFactory.loginInfoTask(
                    userName, LoginStatusEnum.LOGOUT, LoginStatusEnum.LOGOUT.description()));
            }
            ServletHolderUtil.renderString(response, JSONUtil.toJsonStr(ResponseDTO.ok()));
        };
    }

    /**
     * 强散列哈希加密实现
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * 鉴权管理类
     * @see UserDetailsServiceImpl#loadUserByUsername
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService)
            .passwordEncoder(bCryptPasswordEncoder());
        return authBuilder.build();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // CSRF禁用，因为不使用session
            .csrf(AbstractHttpConfigurer::disable)
            // 认证失败处理类
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler()))
            // 基于token，所以不需要session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 过滤请求
            .authorizeHttpRequests(authorize -> authorize
                // 对于登录login 注册register 以及公共Api的请求允许匿名访问
                // 注意： 当携带token请求以下这几个接口时 会返回403的错误
                .requestMatchers("/login", "/register", "/getConfig").anonymous()
                // 报警、PLC数据、机器列表、站点、字段映射和大屏排期接口允许所有访问（匿名和已认证）
                .requestMatchers(HttpMethod.GET, "/api/shoot/machines", "/api/shoot/stations/{machineId}", "/api/shoot/station-mold-models", "/api/shoot/alarm/**", "/api/shoot/board/fixed-fields-batch", "/api/field-mapping/**", "/factorylink/plc/**").permitAll()
                // 射出机管理接口需要认证
                .requestMatchers("/api/shoot/machines/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/", "/*.html", "/*.css", "/*.js",
                    "/profile/**").permitAll()
                // TODO this is danger.
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**").anonymous()
                .requestMatchers("/swagger-resources/**").anonymous()
                .requestMatchers("/webjars/**").anonymous()
                .requestMatchers("/v3/api-docs/**").anonymous()
                .requestMatchers("/*/api-docs.yaml").anonymous()
                .requestMatchers("/druid/**").anonymous()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated()
            )
            // 禁用 X-Frame-Options 响应头。下面是具体解释：
            // X-Frame-Options 是一个 HTTP 响应头，用于防止网页被嵌入到其他网页的 <frame>、<iframe> 或 <object> 标签中，从而可以减少点击劫持攻击的风险
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logOutSuccessHandler()));
        // 添加JWT filter   需要一开始就通过token识别出登录用户 并放到上下文中   所以jwtFilter需要放前面
        httpSecurity.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        // 添加CORS filter
        httpSecurity.addFilterBefore(corsFilter, JwtAuthenticationTokenFilter.class);
        httpSecurity.addFilterBefore(corsFilter, LogoutFilter.class);

        return httpSecurity.build();
    }


}
