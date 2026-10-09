package com.matheus.narutomod.client;

import com.matheus.narutomod.ModItems;
import com.matheus.narutomod.entity.KunaiEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

/**
 * Desenha a kunai voando/cravada usando o próprio modelo do item, apontado na direção
 * do movimento (como a flecha). São dois planos cruzados para ela não sumir quando
 * vista de lado.
 */
public class KunaiRenderer extends EntityRenderer<KunaiEntity, KunaiRenderState> {
	// Com 0.6, a kunai fica com ~0.85 bloco de comprimento (a textura ocupa a diagonal inteira).
	private static final float SCALE = 0.6F;
	private static final float TEXTURE_ANGLE = -45.0F;

	private final ItemModelResolver itemModelResolver;
	// Criado só no primeiro desenho: o renderizador nasce durante o carregamento de recursos,
	// antes de os itens estarem prontos, e criar um ItemStack ali quebra o jogo.
	private @Nullable ItemStack kunaiStack;

	public KunaiRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
	}

	@Override
	public KunaiRenderState createRenderState() {
		return new KunaiRenderState();
	}

	@Override
	public void extractRenderState(KunaiEntity entity, KunaiRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.yRot = entity.getYRot(partialTicks);
		state.xRot = entity.getXRot(partialTicks);
		state.shake = entity.shakeTime - partialTicks;
		if (this.kunaiStack == null) {
			this.kunaiStack = new ItemStack(ModItems.KUNAI);
		}
		this.itemModelResolver.updateForNonLiving(state.item, this.kunaiStack, ItemDisplayContext.NONE, entity);
	}

	@Override
	public void submit(KunaiRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		// Mesmo giro da flecha: o eixo X passa a apontar para onde a kunai está indo.
		poseStack.rotateDegrees(Axis.YP, state.yRot - 90.0F);
		poseStack.rotateDegrees(Axis.ZP, state.xRot);
		if (state.shake > 0.0F) {
			poseStack.rotateDegrees(Axis.XP, -Mth.sin(state.shake * 3.0F) * state.shake);
		}
		poseStack.scale(SCALE, SCALE, SCALE);

		// Atenção à ordem: no PoseStack, a última rotação escrita é a primeira aplicada ao
		// desenho. Então em cada plano o -45° (que endireita a textura diagonal) vem por último.
		this.submitPlane(state, poseStack, submitNodeCollector, 0.0F);
		// Segundo plano, girado 90° em torno da lâmina já endireitada.
		this.submitPlane(state, poseStack, submitNodeCollector, 90.0F);

		poseStack.popPose();
		super.submit(state, poseStack, submitNodeCollector, camera);
	}

	private void submitPlane(KunaiRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float rollDegrees) {
		poseStack.pushPose();
		poseStack.rotateDegrees(Axis.XP, rollDegrees);
		// A textura é desenhada na diagonal (ponta no canto superior direito);
		// girar -45° deixa a ponta no eixo X, a direção do voo.
		poseStack.rotateDegrees(Axis.ZP, TEXTURE_ANGLE);
		state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
	}
}
