
package com.starter.security.base.authentication.configurer;

import com.starter.security.base.properties.SecurityProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 授权信息管理器
 *
 * 用于收集系统中所有 ConfigProvider 并加载其配置
 */
public class AuthorizationConfigurerManager {

	private List<AuthorizationConfigurerProvider> authorizationConfigurerProviders;

    private SecurityProperties securityProperties;

    public AuthorizationConfigurerManager(List<AuthorizationConfigurerProvider> authorizationConfigurerProviders,
                                          SecurityProperties securityProperties) {
        this.authorizationConfigurerProviders = authorizationConfigurerProviders;
        this.securityProperties = securityProperties;
    }

    public void config(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry config) {
		boolean existAnyRequestConfig = false;
		String existAnyRequestConfigName = null;

		for (AuthorizationConfigurerProvider authorizeConfigProvider : authorizationConfigurerProviders) {
			boolean currentIsAnyRequestConfig = authorizeConfigProvider.config(config);

			if (existAnyRequestConfig && currentIsAnyRequestConfig) {
				throw new RuntimeException("重复的anyRequest配置:" + existAnyRequestConfigName + ","
						+ authorizeConfigProvider.getClass().getSimpleName());
			} else if (currentIsAnyRequestConfig) {
				existAnyRequestConfig = true;
				existAnyRequestConfigName = authorizeConfigProvider.getClass().getSimpleName();
			}
		}

        configWhitelist(config);
		if(!existAnyRequestConfig){
			config.anyRequest().authenticated();
		}
	}

	public void configWhitelist(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry config) {
        String whitelistStr = securityProperties.getWhitelist();
        String[] whitelist = StringUtils.split(whitelistStr, ",");
        if (whitelist != null) {
            config.antMatchers(whitelist).permitAll();
        }
    }
}
