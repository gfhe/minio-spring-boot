
## 公开仓库构建方法

### OPTIONAL：获取github TOKEN

位于个人账户的[settings->develop settings->personal access tokens -> Tokens(classic)->Generate new tokens(classic)](https://github.com/settings/tokens):

配置上如图所示的权限：![token 配置权限](./doc/pics/github_auth_config.png)

**注意： Token一定要选择User 权限**

### maven 配置
settings.xml
```
<servers>
  <server>
    <id>github</id>
      <username>your github token username</username>
      <password>your github token</password>
    </server>
  </servers>
```

> NOTE：安装java-extension-pack后， codespace maven 的配置文件位于：`/home/codespace/.vscode-remote/data/User/globalStorage/pleiades.java-extension-pack-jdk/maven/latest/conf/settings.xml`

### 配置deploy 插件

目标:生成包含jar包的maven依赖。
步骤：
1. 在将jar包上传到远程仓库之前，我们需要在本地先生成,所以需要配置一个本地的repository。
    ```
    <distributionManagement>
          <repository>
              <id>maven.repo</id>
              <name>Local Staging Repository</name>
              <url>file://${project.build.directory}/mvn-repo</url>
          </repository>
      </distributionManagement>
    ```
2. 使用`maven-deploy-plugin`指定将打好的包部署到刚刚指定的local仓库中。
    ```
    <plugin>
            <artifactId>maven-deploy-plugin</artifactId>
            <version>2.8.2</version>
            <configuration>
                <altDeploymentRepository>maven.repo::default::file://${project.build.directory}/mvn-repo</altDeploymentRepository>
            </configuration>
        </plugin>
    ```


### github 构建私有仓库

```
<plugin>
    <groupId>com.github.github</groupId>
    <artifactId>site-maven-plugin</artifactId>
    <version>0.12</version>
    <configuration>
        <message>Maven artifacts for ${project.version}</message>
        <noJekyll>true</noJekyll>
        <outputDirectory>${project.build.directory}</outputDirectory>
        <branch>refs/heads/${branch-name}</branch>
        <includes>
            <include>**/*</include>
        </includes>
        <merge>true</merge>
        <repositoryName>${repository-name}</repositoryName>
        <repositoryOwner>${repository-owner}</repositoryOwner>
        <server>github</server>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>site</goal>
            </goals>
            <phase>deploy</phase>
        </execution>
    </executions>
</plugin>
```

注意：
1. `site-maven-plugin`的goals是`site`，它需要跟maven的deploy phase相关联，从而在我们执行`mvn deploy`的时候自动运行site-maven-plugin;
1. repository-name 填写项目名；
1. repository-owner 填写项目作者登陆的username；
1. message表示的是提交到github的消息;
1. 默认情况下的提交到github中的branch是`refs/heads/${branch-name}`，本代码使用的是`refs/heads/repo`;
1. 执行 `mvn deploy`来构建公开的maven 仓库。


## 公开仓库使用

```
<dependency>
    <groupId>YOUR.PROJECT.GROUPID</groupId>
    <artifactId>ARTIFACT-ID</artifactId>
    <version>VERSION</version>
</dependency>
<repository>
    <id>ARTIFACT-ID</id>
    <url>https://github.com/gfhe/minio-spring-boot/repo/</url>
</repository>
```