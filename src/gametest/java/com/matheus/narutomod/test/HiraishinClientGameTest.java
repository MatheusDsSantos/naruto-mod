package com.matheus.narutomod.test;

import com.matheus.narutomod.ModEntities;
import com.matheus.narutomod.ModItems;
import com.matheus.narutomod.NarutoMod;
import com.matheus.narutomod.client.NarutoModClient;
import com.matheus.narutomod.entity.KunaiEntity;

import java.util.List;
import java.util.Locale;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Teste de ponta a ponta do Hiraishin, rodando o jogo de verdade:
 * arremessa a kunai numa parede, aperta R e confere se o jogador foi parar lá.
 * Rodar com: .\gradlew runClientGameTest  (os prints ficam em build/run/clientGameTest/screenshots)
 */
public class HiraishinClientGameTest implements FabricClientGameTest {
	// Arena: piso de pedra em y=100 e uma parede no x=15, de frente para o jogador.
	private static final int WALL_X = 15;
	private static final Vec3 START = new Vec3(2.5, 101, 3.5);

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			TestServerContext server = singleplayer.getServer();
			singleplayer.getConnection().waitForChunksRender();

			server.runCommand("time set day");
			server.runCommand("weather clear");
			server.runCommand("fill 0 100 0 20 100 6 minecraft:stone");
			server.runCommand("fill " + WALL_X + " 101 0 " + WALL_X + " 106 6 minecraft:stone");
			server.runCommand("gamemode survival @a");
			server.runCommand("clear @a");
			server.runCommand("give @a narutomod:kunai 4");
			// Olhando para leste (yaw -90), na direção da parede.
			server.runCommand("tp @a " + START.x + " " + START.y + " " + START.z + " -90 0");
			singleplayer.getConnection().waitForChunksRender();
			context.waitTicks(20);
			context.takeScreenshot("hiraishin-1-inicio");

			// Diagnóstico do desenho: uma kunai parada no ar, 2 blocos à frente, de lado para a câmera.
			server.runCommand("summon narutomod:kunai 4.5 102.4 3.5 {NoGravity:1b,Rotation:[0f,0f]}");
			context.waitTicks(10);
			context.takeScreenshot("hiraishin-0-kunai-no-ar");
			server.runCommand("kill @e[type=narutomod:kunai]");
			context.waitTicks(5);

			// Passo 2: botão direito arremessa a kunai.
			context.getInput().pressKey(options -> options.keyUse);
			context.waitTicks(30);
			context.takeScreenshot("hiraishin-2-kunai-cravada");

			Vec3 kunaiPos = server.computeOnServer(s -> {
				List<? extends KunaiEntity> kunais = s.overworld().getEntities(ModEntities.KUNAI, k -> true);
				check(kunais.size() == 1, "esperava 1 kunai no mundo, achei " + kunais.size());
				KunaiEntity kunai = kunais.get(0);
				check(kunai.getStuckFace() != null, "a kunai deveria estar cravada num bloco");
				return kunai.position();
			});
			int afterThrow = server.computeOnServer(s -> countKunais(firstPlayer(s)));
			check(afterThrow == 3, "esperava 3 kunais no inventário depois de arremessar, achei " + afterThrow);
			check(Math.abs(kunaiPos.x - WALL_X) < 0.5, "a kunai deveria estar na parede (x~" + WALL_X + "), está em " + kunaiPos);
			NarutoMod.LOGGER.info("[teste] kunai cravada em {}", kunaiPos);

			// Close de lado, para conferir o desenho da kunai cravada.
			lookFrom(server, kunaiPos.x - 1.8, kunaiPos.z + 1.8, kunaiPos.add(-0.3, 0, 0));
			context.waitTicks(10);
			context.takeScreenshot("hiraishin-2b-close-da-kunai");
			server.runCommand(String.format(Locale.ROOT, "tp @a %.2f 101 %.2f -90 0", START.x, START.z));
			context.waitTicks(5);

			// Passo 4: R teleporta para a kunai.
			context.getInput().pressKey(NarutoModClient.HIRAISHIN_KEY);
			context.waitTicks(20);
			context.takeScreenshot("hiraishin-3-depois-do-teleporte");

			Vec3 playerPos = server.computeOnServer(s -> firstPlayer(s).position());
			int kunaisLeft = server.computeOnServer(s -> s.overworld().getEntities(ModEntities.KUNAI, k -> true).size());
			int afterTeleport = server.computeOnServer(s -> countKunais(firstPlayer(s)));
			NarutoMod.LOGGER.info("[teste] jogador foi de {} para {}", START, playerPos);

			check(playerPos.x > WALL_X - 1.5 && playerPos.x < WALL_X, "o jogador deveria estar encostado na parede, está em " + playerPos);
			check(kunaisLeft == 0, "a kunai deveria ter sumido do mundo, ainda há " + kunaisLeft);
			check(afterTeleport == 4, "a kunai deveria ter voltado ao inventário (esperava 4, achei " + afterTeleport + ")");

			NarutoMod.LOGGER.info("[teste] Hiraishin OK");
		}
	}

	// Coloca o jogador em pé em (x, 101, z) olhando para o alvo. Calcula yaw/pitch na mão
	// porque o "tp ... facing" deixava a câmera inclinada nos prints.
	private static void lookFrom(TestServerContext server, double x, double z, Vec3 target) {
		double dx = target.x - x;
		double dy = target.y - (101 + 1.62);
		double dz = target.z - z;
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
		server.runCommand(String.format(Locale.ROOT, "tp @a %.2f 101 %.2f %.1f %.1f", x, z, yaw, pitch));
	}

	private static ServerPlayer firstPlayer(MinecraftServer server) {
		return server.getPlayerList().getPlayers().get(0);
	}

	private static int countKunais(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		int total = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.is(ModItems.KUNAI)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
