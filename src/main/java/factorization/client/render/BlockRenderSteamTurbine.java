package factorization.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import factorization.api.Coord;
import factorization.common.Core;
import factorization.common.FactoryType;
import factorization.common.Texture;
import factorization.common.TileEntityWire;
import factorization.common.WireConnections;
import factorization.common.WireRenderingCube;

public class BlockRenderSteamTurbine extends FactorizationBlockRender {

    TileEntityWire fake_wire = new TileEntityWire();
    @Override
    void render(RenderBlocks rb) {
        int glass = Texture.lamp_iron + 10;
        float m = 0.0001F;
        renderNormalBlock(rb, getFactoryType().md);
        
        int out = 3*16 + 3;
        
        renderMotor(rb, 0);
        if (world_mode) {
            Coord me = getCoord();
            fake_wire.worldObj = me.w;
            fake_wire.xCoord = me.x;
            fake_wire.yCoord = me.y;
            fake_wire.zCoord = me.z;
            fake_wire.supporting_side = 0;
            WireConnections con = new WireConnections(fake_wire);
            con.conductorRestrict();
            for (WireRenderingCube rc : con.getParts()) {
                renderCube(rc);
            }
        }
        
        if (world_mode) {
            //render interior bits
            Block b = Core.registry.factory_rendering_block;
            float f = 1F - (3F/16F);
            
            Tessellator.instance.zOffset += f;
            rb.renderEastFace(b, x, y, z, out);
            Tessellator.instance.zOffset -= f;
            Tessellator.instance.zOffset -= f;
            rb.renderWestFace(b, x, y, z, out);
            Tessellator.instance.zOffset += f;
            
            Tessellator.instance.xOffset += f;
            rb.renderNorthFace(b, x, y, z, out);
            Tessellator.instance.xOffset -= f;
            Tessellator.instance.xOffset -= f;
            rb.renderSouthFace(b, x, y, z, out);
            Tessellator.instance.xOffset += f;
            
            Tessellator.instance.yOffset += f;
            rb.renderBottomFace(b, x, y, z, out - 1);
            Tessellator.instance.yOffset -= f;
            Tessellator.instance.yOffset -= f;
            rb.renderTopFace(b, x, y, z, 11);
            Tessellator.instance.yOffset += f;
        } else {
            //render fan
            GL11.glPushMatrix();
            float s = 0.60F;
            GL11.glScalef(s, s, s);
            GL11.glTranslatef(-0.5F, 0.1F, -0.5F);
            GL11.glRotatef(90, 1, 0, 0);
            renderItemIn2D(10);
            GL11.glPopMatrix();
        }
    }

    @Override
    FactoryType getFactoryType() {
        return FactoryType.STEAMTURBINE;
    }

}
