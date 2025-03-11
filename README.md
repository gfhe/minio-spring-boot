# minio spring boot starter

基于(minio-java-jdk 7.1.2版本)[https://github.com/minio/minio-java] 封装的spring boot starter 库。

> 使用 minio spring boot starter 支持的JDK版本：JDK1.8及以上版本（minio-java-jdk 最新版本不支持JDK1.8以下的版本）

## 使用

项目中引入如下依赖：

```xml
<dependency>
    <groupId>win.hgfdodo</groupId>
    <artifactId>minio-spring-boot-starter</artifactId>
    <version>1.0</version>
</dependency>
```


配置文件中增加配置：
```yaml
spring:
  minio:
    url: https://oss-cn-zhangjiakou.aliyuncs.com
    accessKey: ***
    secretKey: ****
    # not required
    region:
```


如果spring boot 自动配置不生效，需要手动配置生效：

使Minio 配置生效：

```
@EnableConfigurationProperties({ MinioProperties.class})
```


导入Minio 自动化配置
```
@Import(MinioAutoConfiguration.class)
```
