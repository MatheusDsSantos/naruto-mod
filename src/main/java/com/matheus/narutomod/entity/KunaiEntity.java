package com.matheus.narutomod.entity;

import com.matheus.narutomod.ModEntities;
import com.matheus.narutomod.ModItems;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;

/**
 * A kunai arremessada. Herda de AbstractArrow, que já sabe voar com gravidade,
 * cravar em blocos e ser recolhida ao encostar — igual a flecha e o tridente.
 */
public class KunaiEntity extends AbstractArrow {
	private static final float DAMAGE = 5.0F;

	// Momento (em ticks do mundo) em que foi arremessada. O teleporte vai sempre
	// para a kunai mais recente.
	private long thrownAt;

	// Face do bloco onde a kunai cravou (null = não está cravada num bloco).
	// Usada para o jogador aparecer do lado de fora da parede, e não dentro dela.
	private @Nullable Direction stuckFace;

	private boolean dealtDamage;

	// Usado pelo jogo para recriar a entidade (ao carregar o mundo ou no cliente).
	public KunaiEntity(EntityType<? extends KunaiEntity> type, Level level) {
		super(type, level);
	}

	// Usado pelo KunaiItem ao arremessar.
	public KunaiEntity(ServerLevel level, LivingEntity owner, ItemStack stack) {
		super(ModEntities.KUNAI, owner, level, stack, null);
		this.thrownAt = level.getGameTime();
	}

	public long getThrownAt() {
		return this.thrownAt;
	}

	public @Nullable Direction getStuckFace() {
		return this.isInGround() ? this.stuckFace : null;
	}

	// ownedBy() é protected em Projectile; expomos para o teleporte saber de quem é a kunai.
	public boolean isOwnedBy(Player player) {
		return this.ownedBy(player);
	}

	// Só devolve a kunai ao inventário quando ela foi arremessada no sobrevivência.
	// No criativo o item não é gasto, então devolver duplicaria.
	public boolean shouldReturnItem() {
		return this.pickup == Pickup.ALLOWED;
	}

	public ItemStack getReturnStack() {
		return this.getPickupItem();
	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		this.stuckFace = hitResult.getDirection();
		super.onHitBlock(hitResult);
	}

	// Ao acertar um mob: causa dano e quica (como o tridente), em vez de sumir como uma flecha.
	// Assim a kunai cai no chão e continua servindo de marca para o teleporte.
	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		Entity target = hitResult.getEntity();
		Entity owner = this.getOwner();
		DamageSource source = this.damageSources().thrown(this, owner == null ? this : owner);

		this.dealtDamage = true;
		// Dano de verdade só no servidor; no cliente a gente só pergunta se "pareceria" um acerto.
		boolean wasHurt = this.level() instanceof ServerLevel serverLevel
				? target.hurtServer(serverLevel, source, DAMAGE)
				: target.hurtClient(source);
		if (wasHurt && target instanceof LivingEntity living) {
			this.doKnockback(living, source);
			this.doPostHurtEffects(living);
		}

		if (target.projectileReceivesSideEffectsOnHit(wasHurt)) {
			this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.4F);
			this.deflect(ProjectileDeflection.REVERSE, target, this.owner, false, new Vec3(0.02, 0.2, 0.02));
		}
	}

	// Depois de acertar alguém, para de procurar novos alvos (senão acertaria de novo ao quicar).
	@Override
	protected @Nullable EntityHitResult findHitEntity(Vec3 from, Vec3 to) {
		return this.dealtDamage ? null : super.findHitEntity(from, to);
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(ModItems.KUNAI);
	}

	@Override
	protected SoundEvent getDefaultHitGroundSoundEvent() {
		return SoundEvents.TRIDENT_HIT_GROUND;
	}

	// A kunai cravada não some sozinha depois de 1 minuto como as flechas:
	// uma marca do Hiraishin fica onde foi deixada.
	@Override
	public void tickDespawn() {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putLong("ThrownAt", this.thrownAt);
		output.putBoolean("DealtDamage", this.dealtDamage);
		if (this.stuckFace != null) {
			output.putInt("StuckFace", this.stuckFace.get3DDataValue());
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.thrownAt = input.getLongOr("ThrownAt", 0L);
		this.dealtDamage = input.getBooleanOr("DealtDamage", false);
		int face = input.getIntOr("StuckFace", -1);
		this.stuckFace = face >= 0 ? Direction.from3DDataValue(face) : null;
	}
}
