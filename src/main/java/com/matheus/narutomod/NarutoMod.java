package com.matheus.narutomod;

import com.matheus.narutomod.network.HiraishinPayload;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NarutoMod implements ModInitializer {
	public static final String MOD_ID = "narutomod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Roda no cliente e no servidor, assim que o Minecraft carrega os mods.
	// É aqui que vamos registrar itens, entidades e pacotes de rede.
	@Override
	public void onInitialize() {
		ModItems.init();
		ModEntities.init();

		// Ensina o jogo a ler o pacote do teleporte (cliente -> servidor)...
		PayloadTypeRegistry.serverboundPlay().register(HiraishinPayload.TYPE, HiraishinPayload.CODEC);
		// ...e o que fazer quando ele chega: teleportar quem mandou.
		ServerPlayNetworking.registerGlobalReceiver(HiraishinPayload.TYPE,
				(payload, context) -> Hiraishin.teleport(context.player()));

		LOGGER.info("Naruto Mod carregado!");
	}

	// Cria IDs no formato "narutomod:<nome>", ex: narutomod:kunai
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
