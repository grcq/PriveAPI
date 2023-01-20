package cf.grcq.bungee.priveapi.jda;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class JDACommandNode {
    private final JDA jda;

    private final Method method;
    private final String name;
    private final CommandType type;

    public JDACommandNode(Method method, JDA jda, JDACommand annotation, CommandType type) {
        this.method = method;
        this.jda = jda;
        this.name = annotation.names()[0];
        this.type = type;

        if (type == CommandType.SLASH_COMMAND) {
            for (String name : annotation.names()) {
                CommandCreateAction action = jda.upsertCommand(name, annotation.description());
                action.queue();
            }

            jda.updateCommands().queue();
        }
    }

    public List<Object> getParameters(Object executor) {
        List<Object> parameters = new ArrayList<>();
        parameters.add(executor);

        List<Parameter> parameterList = Lists.newArrayList(method.getParameters());
        parameterList.remove(0);

        for (Parameter parameter : parameterList) {
            if (!parameter.isAnnotationPresent(JDAParam.class)) continue;
            JDAParam param = parameter.getAnnotation(JDAParam.class);

            OptionType optionType = param.type();
        }

        return parameters;
    }

    @SneakyThrows
    protected void invoke() {
        if (method.getParameterCount() == 0) {
            throw new IllegalArgumentException("JDA command method must contain at least 1 parameter.");
        }

        List<Object> parameters = getParameters((method.getParameters()[0]));


        method.invoke(null, parameters);
    }
}
