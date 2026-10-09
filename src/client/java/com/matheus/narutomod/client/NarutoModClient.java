package com.matheus.narutomod.client;

import com.matheus.narutomod.ModEntities;
import com.matheus.narutomod.NarutoMod;
import com.matheus.narutomod.network.HiraishinPayload;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class NarutoModClient implements ClientModInitializer {
	// Categoria "Naruto" na tela de Controles, e a tecla do Hiraishin (R por padrão,
	// o jogador pode trocar em Opções > Controles).
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(NarutoMod.id("naruto"));
	public static final KeyMapping HIRAISHIN_KEY =KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.narutomod.hiraishin", InputConstants.KEY_R, CATEGORY));

	// Roda só no cliente (o jogo que você vê). Coisas visuais e teclas ficam aqui.
	@Override
	public void onInitializeClient() {
		EntityRenderers.register(ModEntities.KUNAI, KunaiRenderer::new);

		// A cada tick, vê se a tecla foi apertada. Se foi, pede ao servidor para teleportar.
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (HIRAISHIN_KEY.consumeClick()) {
				if (client.player != null) {
					ClientPlayNetworking.send(new HiraishinPayload());
				}
			}
		});
	}
}
