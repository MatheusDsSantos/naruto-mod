package com.matheus.narutomod.network;

import com.matheus.narutomod.NarutoMod;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Pacote "teleporta para a minha kunai", enviado pelo cliente ao apertar a tecla.
 * Não carrega dados: o servidor já sabe quem mandou e decide sozinho para onde ir
 * (nunca confiar em posição enviada pelo cliente — seria um hack de teleporte grátis).
 */
public record HiraishinPayload() implements CustomPacketPayload {
	public static final Type<HiraishinPayload> TYPE = new Type<>(NarutoMod.id("hiraishin"));
	public static final StreamCodec<RegistryFriendlyByteBuf, HiraishinPayload> CODEC = StreamCodec.unit(new HiraishinPayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
