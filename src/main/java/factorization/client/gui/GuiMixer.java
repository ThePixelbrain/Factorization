package factorization.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;

import org.lwjgl.opengl.GL11;

import factorization.common.ContainerFactorization;
import factorization.common.ContainerMixer;
import factorization.common.Core;
import factorization.common.TileEntityMixer;

public class GuiMixer extends GuiContainer {
    ContainerMixer factContainer;
    TileEntityMixer mixer;

    public GuiMixer(ContainerFactorization cont) {
        super(cont);
        factContainer = (ContainerMixer) cont;
        mixer = factContainer.mixer;
    }

    protected void drawGuiContainerForegroundLayer() {
        fontRenderer.drawString(mixer.getInvName(), 60, 6, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        int var4 = mc.renderEngine.getTexture(Core.texture_dir + "mixer.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(var4);
        int var5 = (this.width - this.xSize) / 2;
        int var6 = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.ySize);
        int var7;
        var7 = mixer.getMixProgressScaled(24);
        this.drawTexturedModalRect(var5 + 79, var6 + 34, 176, 14, var7 + 1, 16);
    }

}
