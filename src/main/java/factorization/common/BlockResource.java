package factorization.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import factorization.api.Coord;

public class BlockResource extends Block {
    protected BlockResource(int id) {
        super(id, Material.rock);
        setHardness(2.0F);
        setBlockName("factorizationResourceBlock");
    }

    @Override
    public int getBlockTextureFromSideAndMetadata(int side, int md) {
        if (ResourceType.EXOMODDER.is(md)) {
            if (side == 0) {
                return Texture.exo_bottom;
            }
            if (side == 1) {
                return Texture.exo_config;
            }
            return Texture.exo_side;
        }
        return (16 * 2) + md;
    }

    @Override
    public String getTextureFile() {
        return Core.texture_file_block;
    }
    
    public void addCreativeItems(List itemList) {
        itemList.add(Core.registry.silver_ore_item);
        itemList.add(Core.registry.silver_block_item);
        itemList.add(Core.registry.lead_block_item);
        itemList.add(Core.registry.dark_iron_block_item);
        itemList.add(Core.registry.exoworkshop_item);
    }
    
    @Override
    public void addCreativeItems(ArrayList itemList) {
        addCreativeItems((List) itemList);
    }

    @Override
    public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List) {
        //addCreativeItems(par3List);
        Core.addBlockToCreativeList(par3List, this);
    }

    @Override
    public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer player, int md,
            float vx, float vy, float vz) {
        if (player.isSneaking()) {
            return false;
        }
        Coord here = new Coord(w, x, y, z);
        if (ResourceType.EXOMODDER.is(here.getMd())) {
            player.openGui(Core.instance, FactoryType.EXOTABLEGUICONFIG.gui, w, x, y, z);
            return true;
        }
        return false;
    }

    @Override
    public int damageDropped(int i) {
        return i;
    }
    
    @Override
    public boolean isBeaconBase(World worldObj, int x, int y, int z,
            int beaconX, int beaconY, int beaconZ) {
        Coord here = new Coord(worldObj, x, y, z);
        int md = here.getMd();
        return md == Core.registry.silver_block_item.getItemDamage()
                || md == Core.registry.lead_block_item.getItemDamage()
                || md == Core.registry.dark_iron_block_item.getItemDamage();
    }
}
