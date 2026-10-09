package com.matheus.narutomod;

import com.matheus.narutomod.entity.KunaiEntity;

import java.util.Comparator;
import java.util.Optional;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Hiraishin no Jutsu (Técnica do Deus do Trovão Voador): o jogador aparece
 * onde está a sua kunai mais recente, e a kunai volta para ele.
 * Roda só no servidor, quando chega o HiraishinPayload.
 */
public final class Hiraishin {
	// Meia largura do jogador (0.6 / 2) + uma folga, para não aparecer dentro da parede.
	private static final double WALL_OFFSET = 0.35;
	// Alturas extras testadas, em ordem, se o lugar exato estiver bloqueado.
	private static final double[] FALLBACK_HEIGHTS = {0.0, 0.5, 1.0, -0.5, 1.5};

	public static void teleport(ServerPlayer player) {
		ServerLevel level = player.level();

		// Procura, entre as kunais carregadas nesta dimensão, a mais recente que é deste jogador.
		Optional<? extends KunaiEntity> latest = level.getEntities(ModEntities.KUNAI, kunai -> kunai.isOwnedBy(player))
				.stream()
				.max(Comparator.comparingLong(KunaiEntity::getThrownAt));
		if (latest.isEmpty()) {
			// Aviso na barra acima do inventário, para não parecer que a tecla não funcionou.
			player.sendOverlayMessage(Component.translatable("message.narutomod.no_kunai"));
			return;
		}

		KunaiEntity kunai = latest.get();
		Vec3 from = player.position();
		Vec3 to = findSafeSpot(player, level, standingSpotNear(kunai, player.getBbHeight()));

		flash(level, from);
		player.teleportTo(to.x, to.y, to.z);
		player.resetFallDistance();
		flash(level, to);

		// A kunai volta para o inventário (ou cai no chão se estiver cheio).
		if (kunai.shouldReturnItem()) {
			ItemStack stack = kunai.getReturnStack();
			if (!player.getInventory().add(stack)) {
				player.spawnAtLocation(level, stack);
			}
		}
		kunai.discard();
	}

	// Onde os pés do jogador devem ficar, dependendo de onde a kunai cravou.
	private static Vec3 standingSpotNear(KunaiEntity kunai, double playerHeight) {
		Vec3 pos = kunai.position();
		Direction face = kunai.getStuckFace();
		if (face == null || face == Direction.UP) {
			// No chão (ou caída depois de acertar um mob): aparece em pé em cima dela.
			return pos;
		}
		if (face == Direction.DOWN) {
			// No teto: aparece pendurado logo abaixo.
			return new Vec3(pos.x, pos.y - playerHeight - 0.05, pos.z);
		}
		// Na parede: aparece encostado nela, com a kunai na altura da cintura.
		return new Vec3(
				pos.x + face.getStepX() * WALL_OFFSET,
				pos.y - playerHeight / 2,
				pos.z + face.getStepZ() * WALL_OFFSET);
	}

	// Se o ponto escolhido prende o jogador num bloco, tenta um pouco mais alto ou mais baixo.
	private static Vec3 findSafeSpot(ServerPlayer player, ServerLevel level, Vec3 feet) {
		for (double dy : FALLBACK_HEIGHTS) {
			Vec3 candidate = feet.add(0, dy, 0);
			AABB box = player.getBoundingBox().move(candidate.subtract(player.position()));
			if (level.noCollision(player, box)) {
				return candidate;
			}
		}
		return feet;
	}

	// O "clarão amarelo" do Minato.
	private static void flash(ServerLevel level, Vec3 pos) {
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 1.0, pos.z, 25, 0.3, 0.6, 0.3, 0.2);
		level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1.0, pos.z, 8, 0.2, 0.5, 0.2, 0.05);
		level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.8F);
	}

	private Hiraishin() {
	}
}
