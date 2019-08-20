
package com.test.security.app.config;

import com.test.security.core.authentication.configurer.AuthorizationConfigurerManager;
import com.test.security.oauth2.config.SmsCodeAuthenticationSecurityConfig;
import com.test.security.validate.config.ValidationSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.social.security.SpringSocialConfigurer;

/**
 * 资源服务器配置
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    private SmsCodeAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig;

    @Autowired
    private OpenIdAuthenticationSecurityConfig openIdAuthenticationSecurityConfig;

    @Autowired
    private ValidationSecurityConfig validationSecurityConfig;

    @Autowired
    private SpringSocialConfigurer socialSecurityConfig;

    @Autowired
    private WxMiniAuthenticationSecurityConfig wxMiniAuthenticationSecurityConfig;

    @Autowired
    private AuthorizationConfigurerManager authorizationConfigurerManager;

    @Autowired
    private AccessDeniedHandler appAccessDeniedHandler;

    @Autowired
    private AuthenticationEntryPoint appOAuth2AuthenticationExceptionEntryPoint;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources
                .authenticationEntryPoint(appOAuth2AuthenticationExceptionEntryPoint)
                .accessDeniedHandler(appAccessDeniedHandler);
        super.configure(resources);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .exceptionHandling()
                .authenticationEntryPoint(appOAuth2AuthenticationExceptionEntryPoint)
                .accessDeniedHandler(appAccessDeniedHandler)
                .and()
                .apply(validationSecurityConfig)
                .and()
                .apply(smsCodeAuthenticationSecurityConfig)
                .and()
                .apply(wxMiniAuthenticationSecurityConfig)
                .and()
                .apply(socialSecurityConfig)
                .and()
                .apply(openIdAuthenticationSecurityConfig)
                .and()
                .csrf().disable();

        authorizationConfigurerManager.config(http.authorizeRequests());
    }
}
