# MSMP Console

<img src="assets/icon.png" width="128" align="right">

A server-side Fabric mod that extends the [Minecraft Server Management Protocol](https://minecraft.wiki/w/Minecraft_Server_Management_Protocol) (MSMP) by forwarding every server console log event to all connected clients as a JSON-RPC 2.0 notification.

<br>

## Installation

1. Download the mod `.jar` and place it in your server's `mods/` folder.
2. Enable the Management Server in `server.properties`:
   ```properties
   management-server-enabled=true
   ```
3. Start the server. The Management Server will listen on `localhost:25576` by default.

## Notification

Once a client connects to the WebSocket endpoint, it will receive a notification for every log event produced by the server.

**Method:** `console:notification/log_event`

### Payload

| Field       | Type   | Description                                                               |
|-------------|--------|---------------------------------------------------------------------------|
| `timestamp` | string | ISO-8601 timestamp of when the log event occurred                         |
| `level`     | string | Log level: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR` or `FATAL`           |
| `thread`    | string | Name of the thread that produced the log event                            |
| `logger`    | string | Fully qualified name of the originating logger (e.g. the class name)      |
| `message`   | string | The fully interpolated log message                                        |
| `throwable` | string | Serialized stacktrace if an exception was attached, omitted otherwise     |

### Example

```json
{
  "jsonrpc": "2.0",
  "method": "console:notification/log_event",
  "params": [{
    "timestamp": "2026-03-21T15:06:06.146Z",
    "level": "INFO",
    "thread": "Server thread",
    "logger": "net.minecraft.server.MinecraftServer",
    "message": "Done (1.019s)! For help, type \"help\""
  }]
}
```

```json
{
  "jsonrpc": "2.0",
  "method": "console:notification/log_event",
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

## How It Works

1. A custom [Log4j2](https://logging.apache.org/log4j/2.x/) appender is attached to the root logger on mod initialization, intercepting every log event produced by the server.
2. On `SERVER_STARTED`, the mod locates the internal `ManagementServer` instance via reflection.
3. For each log event, the appender builds a `ConsoleLogPayload` and broadcasts it to all active connections via `Connection#sendNotification`.
4. The notification is registered under the custom namespace `console:notification/log_event` using a Mixin accessor that bypasses the default `minecraft:` namespace restriction.

## License

[LGPL-3.0](LICENSE)
