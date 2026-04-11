package com.dietrecord.backend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({
        "com.dietrecord.backend.modules.user.mapper",
        "com.dietrecord.backend.modules.diet.mapper",
        "com.dietrecord.backend.modules.food.mapper",
        "com.dietrecord.backend.modules.weight.mapper",
        "com.dietrecord.backend.modules.period.mapper"
})
public class MybatisPlusConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    @Bean
    public DbType dbType() {
        return DbType.MYSQL;
    }
}
