package robot.flaimMineshield;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class BarrierManager implements Listener {
    protected final List<Timer> timers = new ArrayList<>();

    public BarrierManager() {
        FlaimMineshield.Plugin.getServer().getPluginManager().registerEvents(this, FlaimMineshield.Plugin);
        FlaimMineshield.Plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> event.registrar().register(buildBarrierCommand()));

    }

    LiteralCommandNode<CommandSourceStack> buildBarrierCommand() {
        return literal("barrierTimer")
                .requires(source -> source.getSender().isOp())
                .then(argument("секунды", integer(1))
                        .then(argument("радиус", floatArg())
                                .then(argument("скорость", integer(0))
                                        .then(argument("команда", greedyString())
                                                .executes(ctx -> {
                                                    int seconds = getInteger(ctx, "секунды");
                                                    float radius = getFloat(ctx, "радиус");
                                                    int speed = getInteger(ctx, "скорость");
                                                    String command = getString(ctx, "команда");

                                                    FlaimMineshield.BarrierManager.appendTimer(seconds, radius, speed, command);
                                                    ctx.getSource().getSender().sendMessage(Component.text("⏳ Запущен таймер границы"));
                                                    return 1;
                                                })))))
                .build();
    }

    public void appendTimer(int seconds, float addRadius, int speed, String command) {
        appendTimer(new Timer(FlaimMineshield.Plugin.getServer().getCurrentTick() + seconds * 20, addRadius, speed, command));
    }

    private void appendTimer(Timer timer) {
        timers.add(timer);
        timers.sort(Comparator.comparingInt(t -> t.targetTick));
    }

    @EventHandler
    public void onTick(ServerTickEndEvent event) {
        if (timers.isEmpty()) return;

        int currentTick = event.getTickNumber();
        while (!timers.isEmpty() && timers.getFirst().targetTick <= currentTick) {
            timers.removeFirst().execute();
        }
    }

    public record Timer(int targetTick, float addRadius, int speed, String command) {
        public void execute() {
            var server = FlaimMineshield.Plugin.getServer();
            server.dispatchCommand(Bukkit.getConsoleSender(), "worldborder add " + (addRadius * 2) + " " + speed);
            if (!command.isEmpty())
                server.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}
