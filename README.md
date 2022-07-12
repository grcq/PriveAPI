# PriveAPI
Documentation coming soon at https://docs.grcq.cf/

Examples in my Discord server:
https://discord.gg/ZvHVcBxN3v 

Download the JAR file <a href="https://github.com/grcq/PriveAPI/releases/download/v1.0-beta/PriveAPI-1.0.jar">here</a>.
Create a folder named "libs" in your project. You also need to upload the JAR file to your plugins folder on your server. Go to your pom.xml and add this to your dependencies:

```maven
<dependency>
    <groupId>cf.grcq</groupId>
    <artifactId>PriveAPI</groupId>
    <version>1.0</groupId>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/PriveAPI-1.0.jar</systemPath>
</dependency>
```
