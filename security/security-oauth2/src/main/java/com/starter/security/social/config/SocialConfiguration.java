package com.starter.security.social.config;

import com.starter.security.social.DefaultSocialUserDetailsService;
import com.starter.security.social.properties.SocialProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.security.SocialUserDetailsService;

/**
 * @author Shoven
 * @date 2019-08-19
 */
@Configuration
@EnableConfigurationProperties(SocialProperties.class)
public class SocialConfiguration {

    /**
     * 默认认证器
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public SocialUserDetailsService socialUserDetailsService() {
        return new DefaultSocialUserDetailsService();
    }
}
