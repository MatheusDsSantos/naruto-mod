# Naruto Mod

Mod de Naruto para Minecraft Java **26.3** (Fabric).

## O que tem

- **Kunai do Minato** (aba "Naruto" no criativo)
  - Botão direito: arremessa; a kunai crava no bloco onde bater (ou quica se acertar um mob).
  - **R** — Hiraishin: teleporta para a kunai mais recente e ela volta para o inventário.
    A tecla pode ser trocada em Opções → Controles → Naruto.

## Desenvolvimento

Requer JDK 25.

```
.\gradlew runClient           # abre o Minecraft de teste com o mod
.\gradlew build               # gera o .jar em build/libs
.\gradlew runClientGameTest   # teste automatizado no jogo (prints em build/run/clientGameTest/screenshots)
.\gradlew genSources          # gera o código-fonte do Minecraft para consulta
```

A textura da kunai é gerada por `tools/gerar_textura_kunai.ps1`.
