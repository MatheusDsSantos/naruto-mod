package com.matheus.narutomod;

import com.matheus.narutomod.item.KunaiItem;

import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModItems {
	// Kunai do Minato: botão direito arremessa (ver KunaiItem).
	public static final Item KUNAI = register("kunai", KunaiItem::new, new Item.Properties().stacksTo(16));

	// Aba "Naruto" no menu do modo criativo, com o ícone da kunai.
	public static final ResourceKey<CreativeModeTab> NARUTO_TAB_KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, NarutoMod.id("naruto"));

	public static final CreativeModeTab NARUTO_TAB = Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			NARUTO_TAB_KEY,
			FabricCreativeModeTab.builder()
					.title(Component.translatable("itemGroup.narutomod.naruto"))
					.icon(() -> new ItemStack(KUNAI))
					.displayItems((params, output) -> output.accept(KUNAI))
					.build()
	);

	// Todo item precisa de uma "chave" (narutomod:kunai) que vai tanto no registro
	// quanto nas propriedades do item. É essa chave que liga o item aos arquivos JSON.
	// O "factory" (ex: KunaiItem::new) diz qual classe criar; isso permite itens com comportamento próprio.
	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, NarutoMod.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}

	// Os campos static acima só são criados quando a classe é carregada pela primeira vez.
	// Chamar este método vazio no onInitialize garante que isso aconteça na hora certa.
	public static void init() {
	}

	private ModItems() {
	}
}
