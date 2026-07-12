<img src="assets/icon.png" width="64" align="right">

# MSMP Console

A server-side Fabric mod that extends the [Minecraft Server Management Protocol](https://minecraft.wiki/w/Minecraft_Server_Management_Protocol) (MSMP) by providing additional functions for interacting with the console.

This mod is designed for tooling, dashboards, automation systems, external monitoring tools, and integrations that need structured access to the server console without relying on RCON or log-file tailing.


## Installation

1. Download the mod `.jar` and place it in your server's `mods/` folder.
2. Enable the Management Server in `server.properties`:
```properties
   management-server-enabled=true
```
3. Start the server. The Management Server will listen on `localhost:25576` by default.


## Configuration

On first start, the mod generates a configuration file at `<server_root_dir>/config/msmp/console/config.yml`:

```yaml
# Main configuration file for MSMP Entity.

# Configuration for log related settings.
log:
  # The minimum log level that gets forwarded as a console:notification/log_event.
  # Events below this level are ignored entirely and never sent to connected clients.
  # @possible: TRACE | DEBUG | INFO | WARN | ERROR | FATAL
  # @default: 'INFO'
  level: INFO
# Configuration for send related settings.
send:
  # Enable logging for the execution of a command in the console, this prevents echoing the send command.
  # @default: true
  log-command-execution: true
```

## RPC Methods

The mod currently provides the following MSMP RPC methods. All of these methods are also automatically discoverable through the standard `rpc.discover` MSMP endpoint.

| Method         | Description                                                                           |
|:---------------|:--------------------------------------------------------------------------------------|
| `console:send` | Executes a command on the server console with full permissions and returns its output |

> If you want more methods or notifications for other purposes, please [open an issue](https://github.com/MinecraftPlayground/msmp-console-mod/issues/new?template=new_method_or_notification_suggesetion.yml)


## RPC Notifications

The mod also provides the following MSMP RPC notification that clients can subscribe to:

| Method                           | Description                                                                         |
|:---------------------------------|:------------------------------------------------------------------------------------|
| `console:notification/log/event` | Fired for every server console log event at or above the configured `log.min-level` |


## Method Reference

### `console:send`

Executes an arbitrary command as if typed by an operator (full permissions) and returns its textual feedback/error output together with a success indicator. The command may be sent with or without a leading `/`.

```jsonc
// Request
{ "command": "say Hello" }

// Response
{
  "command": "say Hello",
  "result": "",
  "success": true
}
```

---

### `console:notification/log/event`

Fired for every server console log event whose level is at or above the configured `log.level`. Events below that level are dropped before ever reaching connected clients.

```jsonc
{
  "jsonrpc": "2.0",
  "method": "console:notification/log/event",
  "params": [{
    "timestamp": "2026-03-21T15:06:06.146Z",
    "level": "INFO",
    "thread": "Server thread",
    "logger": "net.minecraft.server.MinecraftServer",
    "message": "Done (1.019s)! For help, type \"help\"",
    "throwable": ""
  }]
}
```

```jsonc
{
  "jsonrpc": "2.0",
  "method": "console:notification/log/event",
  "params": [{
    "timestamp": "2026-03-21T15:06:07.212Z",
    "level": "ERROR",
    "thread": "Server thread",
    "logger": "net.minecraft.server.MinecraftServer",
    "message": "Encountered an unexpected exception",
    "throwable": "java.lang.NullPointerException: Cannot invoke ...\n\tat net.minecraft.server.MinecraftServer..."
  }]
}
```

## License

[LGPL-3.0](LICENSE)
