# Integração MineSkyEvents ↔ MineSkyProxy

Este documento descreve como o plugin **MineSkyEvents** (Paper, servidor de eventos) se comunica com o **MineSkyProxy** (Velocity, proxy) via Plugin Messaging.

## Canal

```
minesky:proxy
```

Registrado nos dois lados:
- **Velocity**: `channelRegistrar.register(ProxyChannel.IDENTIFIER)`
- **Paper**: `getServer().getMessenger().registerOutgoingPluginChannel(...)` e `registerIncomingPluginChannel(...)`

## Fluxo geral

```
Jogador no lobby (Velocity)
        │
        ▼
MineSkyProxy recebe /evento start spleef arena1
        │
        ▼  Plugin Message (START_EVENT)
Servidor Paper "eventos" (MineSkyEvents)
        │
        ▼
Instância do evento inicia (inscrições → countdown → partida)
        │
        ▼  Plugin Message (REWARD)
MineSkyProxy credita dinheiro ao jogador na rede
```

## Mensagens: Velocity → Paper (entrada)

| Tipo | Campos | Descrição |
|------|--------|-----------|
| `START_EVENT` | `eventId` (UTF), `mapId` (UTF), `manual` (boolean) | Inicia um evento no servidor |
| `STOP_EVENT` | — | Encerra o evento ativo graciosamente |
| `FORCE_STOP` | — | Força encerramento imediato |
| `PING` | — | Verifica se o servidor está online |

### Exemplo START_EVENT (Java/Velocity)

```java
ByteArrayDataOutput out = ByteStreams.newDataOutput();
out.writeUTF("START_EVENT");
out.writeUTF("spleef");       // eventId
out.writeUTF("arena1");       // mapId (vazio = aleatório)
out.writeBoolean(true);       // manual

server.sendPluginMessage(MINECRAFT_CHANNEL, out.toByteArray());
```

## Mensagens: Paper → Velocity (saída)

| Tipo | Campos | Descrição |
|------|--------|-----------|
| `EVENT_STARTED` | `eventId`, `mapId`, `playerCount` | Evento entrou em fase de inscrição |
| `EVENT_ENDED` | `eventId`, `mapId` | Evento finalizado normalmente |
| `EVENT_CANCELLED` | `eventId`, `reason` | Evento cancelado (ex: poucos jogadores) |
| `REWARD` | `playerName`, `amount`, `eventId`, `placement` | Crédito de recompensa |
| `EVENT_STATUS` | `eventId`, `state`, `players` | Status periódico (opcional) |
| `PONG` | `serverName` | Resposta ao PING |
| `CONNECT_HUB` | `playerName`, `targetServer` | Jogador pediu saída (`/sair`) — proxy deve mover ao hub |

### Exemplo REWARD (Paper envia)

```java
out.writeUTF("REWARD");
out.writeUTF("Drawn");        // nick do jogador
out.writeInt(2500);           // quantia em dinheiro
out.writeUTF("spleef");       // eventId
out.writeInt(1);              // colocação (1º, 2º, 3º)
```

O **MineSkyProxy** deve escutar `REWARD` e creditar o saldo do jogador no banco de dados da rede (mesmo se ele não estiver online no servidor de eventos).

## IDs de eventos

| ID | Nome | Categoria |
|----|------|-----------|
| `spleef` | Spleef | Diário |
| `tijolao-wars` | Tijolão Wars | Diário |
| `runners` | Runners | Diário |
| `corrida` | Corrida | Diário |
| `corrida-barco` | Corrida de Barco | Diário |
| `tnt-run` | TNT RUN | Diário |
| `tnt-tag` | TNT TAG | Diário |
| `sumo` | Sumo | Diário |
| `corrida-elytra` | Corrida de Elytra | Diário |
| `mini-wars` | Mini Wars | Agendado |
| `esconde-esconde` | Esconde-Esconde | Agendado |

## Comandos sugeridos no MineSkyProxy (Velocity)

```
/evento start <tipo> [mapa]   → envia START_EVENT ao servidor de eventos
/evento stop                  → envia STOP_EVENT
/evento status                → envia PING e exibe resposta
/evento enviar <jogador>      → envia jogador ao servidor de eventos via Velocity
```

## Configuração no Velocity

```toml
[servers]
eventos = "127.0.0.1:25566"

[forced-hosts]
# opcional
```

## Configuração no Paper (config.yml)

```yaml
proxy:
  channel: minesky:proxy
  server-name: eventos
```

## Agendamento automático

O MineSkyEvents verifica horários automaticamente (configurável em `config.yml`):

- **Diários** (dias úteis): 13h, 14h, 16h, 17h, 19h
- **Agendados** (dias úteis): 13h, 15h, 18h, 20h

Fins de semana desabilitados por padrão (`weekend-schedule-enabled: false`).

## Próximos passos para o MineSkyProxy

1. Criar plugin Velocity com o canal `minesky:proxy`
2. Implementar listener de mensagens recebidas (`REWARD`, `EVENT_STARTED`, etc.)
3. Conectar ao sistema de economia da rede
4. Implementar comandos `/evento start|stop|status`
5. Redirecionar jogadores ao servidor `eventos` quando um evento for anunciado
