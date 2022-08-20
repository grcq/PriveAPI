# PriveAPI
Documentation: https://docs.grcq.cf/#/priveapi

Examples in my Discord server:
https://discord.gg/ZvHVcBxN3v 

Download the JAR file <a href="https://github.com/grcq/PriveAPI/releases/download/v1.0-beta/PriveAPI-1.0.jar">here</a>.
You need to upload the JAR file to your plugins folder on your server. Go to your pom.xml and add this:

```maven
<repositories>
    <repository>
        <id>grcq-releases</id>
        <url>http://repo.grcq.cf/repository/maven-releases/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>cf.grcq</groupId>
        <artifactId>PriveAPI</artifactId>
        <version>1.1</version>
    </dependency>
</dependencies>
```
