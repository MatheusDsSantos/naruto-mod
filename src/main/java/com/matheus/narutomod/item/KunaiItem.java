package com.matheus.narutomod.item;

import com.matheus.narutomod.entity.KunaiEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class KunaiItem extends Item {
	// Velocidade do arremesso (o tridente usa 2.5, a bola de neve 1.5).
	private static final float THROW_POWER = 2.5F;
	// Imprecisão: 0 = sempre exatamente onde você mira.
	private static final float THROW_INACCURACY = 0.0F;
	// Intervalo mínimo entre arremessos, em ticks (20 ticks = 1 segundo).
	private static final int COOLDOWN_TICKS = 5;

	public KunaiItem(Item.Properties properties) {
		super(properties);
	}

	// Botão direito: arremessa na hora, como a bola de neve (sem precisar segurar como o tridente).
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		// Só o servidor cria entidades. O cliente apenas mostra o resultado.
		if (level instanceof ServerLevel serverLevel) {
			// Tira 1 kunai da mão (no criativo não gasta) e devolve essa unidade para virar o projétil.
			ItemStack thrown = stack.consumeAndReturn(1, player);
			KunaiEntity kunai = Projectile.spawnProjectileFromRotation(
					KunaiEntity::new, serverLevel, thrown, player, 0.0F, THROW_POWER, THROW_INACCURACY);
			if (player.hasInfiniteMaterials()) {
				kunai.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
			}
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.6F, 1.6F);
		player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResult.SUCCESS;
	}
}
