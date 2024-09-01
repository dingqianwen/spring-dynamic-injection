package com.spring.dynamic.injection.config;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/9/1
 * @since 1.0.0
 */
@EnableApolloConfig
@Component
@Profile("apollo")
public class ApolloConfig {
}
