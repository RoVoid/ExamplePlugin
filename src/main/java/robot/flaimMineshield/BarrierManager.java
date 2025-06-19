package robot.flaimMineshield;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    protected static final List<Timer> timers = new ArrayList<>();
    private static final World overworld = Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .findFirst()
            .orElse(null);


    public BarrierManager() {
        FlaimMineshield.Plugin.getServer().getPluginManager().registerEvents(this, FlaimMineshield.Plugin);
        FlaimMineshield.Plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> event.registrar().register(buildBarrierCommand()));


        File file = new File(FlaimMineshield.Plugin.getDataFolder(), "timers.json");
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            int currentTick = FlaimMineshield.Plugin.getServer().getCurrentTick();
            for (var element : array) appendTimer(Timer.fromJson(element.getAsJsonObject(), currentTick));
        } catch (IOException ignored) {
        }
    }

    public static void onDisable() {
        int currentTick = FlaimMineshield.Plugin.getServer().getCurrentTick();
        JsonArray array = new JsonArray();
        for (Timer timer : timers) array.add(timer.toJson(currentTick));
        FlaimMineshield.Plugin.getDataFolder().mkdirs();
        try (FileWriter writer = new FileWriter(new File(FlaimMineshield.Plugin.getDataFolder(), "timers.json"))) {
            writer.write(array.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static LiteralCommandNode<CommandSourceStack> buildBarrierCommand() {
        return literal("barrierTimer")
                .requires(source -> source.getSender().isOp())
                .then(argument("секунды", integer(1))
                        .then(argument("радиус", floatArg())
                                .then(argument("время", integer(0))
                                        .then(argument("команда", greedyString())
                                                .executes(ctx -> {
                                                    int seconds = getInteger(ctx, "секунды");
                                                    float radius = getFloat(ctx, "радиус");
                                                    int time = getInteger(ctx, "время");
                                                    String command = getString(ctx, "команда");

                                                    appendTimer(seconds, radius, time, command);
                                                    ctx.getSource().getSender().sendMessage(Component.text("⏳ Таймер установлен для мира игрока"));
                                                    return 1;
                                                })
                                        )))).build();
    }

    public static void appendTimer(int seconds, float radiusDelta, int time, String command) {
        appendTimer(new Timer(FlaimMineshield.Plugin.getServer().getCurrentTick() + seconds * 20, radiusDelta, time, command));
    }

    private static void appendTimer(Timer timer) {
        timers.add(timer);
        timers.sort(Comparator.comparingInt(t -> t.targetTick));
    }

    @EventHandler
    public static void onTick(ServerTickEndEvent event) {
        int currentTick = event.getTickNumber();
        while (!timers.isEmpty() && timers.getFirst().targetTick <= currentTick)
            timers.removeFirst().execute();
    }

    public record Timer(int targetTick, float radiusDelta, int time, String command) {
        public void execute() {
            overworld.getWorldBorder().setSize(Math.max(overworld.getWorldBorder().getSize() + radiusDelta * 2, 0), time);
            if (!command.isEmpty())
                FlaimMineshield.Plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        public JsonObject toJson(int currentTick) {
            JsonObject obj = new JsonObject();
            obj.addProperty("targetTick", targetTick - currentTick);
            obj.addProperty("radiusDelta", radiusDelta);
            obj.addProperty("time", time);
            obj.addProperty("command", command);
            return obj;
        }

        public static Timer fromJson(JsonObject obj, int currentTick) {
            return new Timer(
                    obj.get("targetTick").getAsInt() + currentTick,
                    obj.get("radiusDelta").getAsFloat(),
                    obj.get("time").getAsInt(),
                    obj.get("command").getAsString()
            );
        }
    }
}
