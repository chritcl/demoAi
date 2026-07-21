package com.oa.platform;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
/**
 * 协同办公平台后端启动类。
 */
@SpringBootApplication
@EnableAsync
@MapperScan("com.oa.platform.**.mapper")
public class OaPlatformApplication {

    public static void main(String[] args) {
        // 设置JVM默认时区为亚洲/上海
        System.setProperty("user.timezone", "Asia/Shanghai");

        SpringApplication.run(OaPlatformApplication.class, args);
        System.out.println("""

                ====================================================
                  协同办公平台后端启动成功 (OA Platform Backend)
                  API 文档: http://localhost:10001/doc.html
                  默认账号: admin / <REDACTED_DEFAULT_PASSWORD>
                ====================================================""");
    }

    /**
     * 数据库和Redis连接检测组件
     */
    @Slf4j
    @Component
    static class DatabaseConnectionChecker implements ApplicationRunner {

        @Autowired
        private DataSource dataSource;

        @Autowired
        private StringRedisTemplate redisTemplate;

        @Override
        public void run(ApplicationArguments args) throws Exception {
            checkDatabaseConnection();
            checkRedisConnection();
        }

        /**
         * 检测数据库连接是否正常
         */
        private void checkDatabaseConnection() {
            log.info("========== 开始检测数据库连接 ==========");

            try (Connection connection = dataSource.getConnection()) {
                if (connection != null && !connection.isClosed()) {
                    // 执行一个简单的查询来验证连接
                    String url = connection.getMetaData().getURL();
                    String databaseProductName = connection.getMetaData().getDatabaseProductName();
                    String databaseProductVersion = connection.getMetaData().getDatabaseProductVersion();

                    log.info("✅ 数据库连接正常");
                    log.info("📊 数据库类型: {}", databaseProductName);
                    log.info("🔢 数据库版本: {}", databaseProductVersion);
                    log.info("🔗 连接地址: {}", url);
                    log.info("========== 数据库连接检测完成 ==========");
                } else {
                    log.error("❌ 数据库连接失败: 连接为空或已关闭");
                    throw new RuntimeException("数据库连接失败");
                }
            } catch (SQLException e) {
                log.error("❌ 数据库连接检测失败: {}", e.getMessage(), e);
                log.error("========== 数据库连接检测失败，应用启动终止 ==========");
                // 抛出异常，阻止应用启动
                throw new RuntimeException("数据库连接检测失败，请检查数据库配置", e);
            }
        }

        /**
         * 检测Redis连接是否正常
         */
        private void checkRedisConnection() {
            log.info("========== 开始检测Redis连接 ==========");

            try {
                // 获取Redis连接工厂
                RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
                if (connectionFactory == null) {
                    throw new RuntimeException("Redis连接工厂未初始化");
                }

                // 执行一个简单的ping命令来验证Redis连接
                String pingResult = connectionFactory.getConnection().ping();

                if ("PONG".equals(pingResult)) {
                    log.info("✅ Redis连接正常");

                    // 测试基本的读写操作
                    String testKey = "connection:test:key";
                    String testValue = "test-value-" + System.currentTimeMillis();

                    redisTemplate.opsForValue().set(testKey, testValue);
                    String retrievedValue = redisTemplate.opsForValue().get(testKey);

                    if (testValue.equals(retrievedValue)) {
                        log.info("✅ Redis读写操作正常");
                        // 清理测试数据
                        redisTemplate.delete(testKey);
                    } else {
                        log.warn("⚠️ Redis读写操作异常，写入值: {}, 读取值: {}", testValue, retrievedValue);
                    }

                    log.info("🔗 Redis连接状态: 连接正常");
                    log.info("========== Redis连接检测完成 ==========");
                } else {
                    log.error("❌ Redis连接失败: ping返回结果异常 - {}", pingResult);
                    throw new RuntimeException("Redis连接失败");
                }
            } catch (Exception e) {
                log.error("❌ Redis连接检测失败: {}", e.getMessage(), e);
                log.error("========== Redis连接检测失败，应用启动终止 ==========");
                // 抛出异常，阻止应用启动
                throw new RuntimeException("Redis连接检测失败，请检查Redis配置", e);
            }
        }
    }
}

