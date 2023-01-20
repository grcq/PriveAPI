package cf.grcq.bungee.priveapi.jda;

import cf.grcq.bungee.priveapi.utils.ClassUtils;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class JdaAPI {

    private final JDAListener listener;

    private final List<JDACommandNode> commands;

    private JDA jda;
    private final JDABuilder jdaBuilder;

    public JdaAPI(String token) {
        this.listener = new JDAListener();
        this.commands = new ArrayList<>();
        this.jdaBuilder = JDABuilder.createDefault(token);
    }

    public JdaAPI build() throws LoginException {
        this.jda = jdaBuilder.build();
        return this;
    }

    public void registerAll(Class<?> mainClass) {
        for (Class<?> clazz : ClassUtils.getClassesInPackage(mainClass, mainClass.getPackage().getName())) {

        }
    }

}
