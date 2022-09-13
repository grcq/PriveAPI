package cf.grcq.priveapi.utils;

import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Paginate<T> {

    abstract public String getTopHeader(int page, int maxPage);

    abstract public String format(T t, int i);

    public int resultsPerPage() {
        return 9;
    }

    public final void display(CommandSender sender, int page, Collection<T> collection) {
        display(sender, page, new ArrayList<>(collection));
    }

    public final void display(CommandSender sender, int page, List<T> list) {
        if (list.size() == 0) {
            sender.sendMessage(Util.format("&cNo entries."));
            return;
        }

        Preconditions.checkArgument(resultsPerPage() > 0);

        int maxPages = list.size() / resultsPerPage() + 1;
        if (page > 0 && page <= maxPages) {
            sender.sendMessage(Util.format(getTopHeader(page, maxPages)));

            for (int i = resultsPerPage() * (page - 1); i < resultsPerPage() * page && i < list.size(); ++i) {
                sender.sendMessage(Util.format(this.format(list.get(i), i)));
            }

            return;
        }

        sender.sendMessage(Util.format("&cError: Page '&e" + page + "&c' does not exist."));
    }

}
