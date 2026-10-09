package com.matheus.narutomod.client;

import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;

// "Foto" da kunai tirada a cada quadro: o renderizador lê daqui, nunca direto da entidade.
public class KunaiRenderState extends ThrownItemRenderState {
	public float yRot;
	public float xRot;
	// Tremidinha quando crava no bloco, igual à flecha.
	public float shake;
}
