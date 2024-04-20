package factorization.common;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import factorization.api.Coord;
import factorization.api.IActOnCraft;
import factorization.common.Core.TabType;

public class ItemWrathIgniter extends Item {
    public ItemWrathIgniter(int par1) {
        super(par1);
        setMaxStackSize(1);
        setMaxDamage((6 * 2) - 1);
        setNoRepair();
        Core.tab(this, TabType.TOOLS);
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public String getTextureFile() {
        return Core.texture_file_item;
    }

    @Override
    public String getItemName() {
        return "item.wrathigniter";
    }

    @Override
    public String getItemNameIS(ItemStack par1ItemStack) {
        return getItemName();
    }

    @Override
    //hello, server.
    public int getIconFromDamage(int par1) {
        return (16 * 3) + 1;
    }

    
    @Override
    public boolean onItemUse(ItemStack par1ItemStack,
            EntityPlayer par2EntityPlayer, World par3World, int par4, int par5,
            int par6, int par7, float par8, float par9, float par10) {
        return tryPlaceIntoWorld(par1ItemStack, par2EntityPlayer, par3World, par4, par5,
                par6, par7, par8, par9, par10);
    }
    
    public boolean tryPlaceIntoWorld(ItemStack is, EntityPlayer player, World w, int x, int y,
            int z, int side, float vecx, float vecy, float vecz) {
        Coord baseBlock = new Coord(w, x, y, z);
        Coord fireBlock = baseBlock.copy().towardSide(side);
        if (fireBlock.getId() != 0) {
            if (!fireBlock.isAir()) {
                return true;
            }
        }
        is.damageItem(2, player);
        TileEntityWrathFire.ignite(baseBlock, fireBlock, player);
        return true;
    }
    
    @Override
    public boolean hasContainerItem() {
        return true;
    }
    
    @Override
    public ItemStack getContainerItemStack(ItemStack is) {
        is.setItemDamage(is.getItemDamage() + 1);
        if (is.getItemDamage() > getMaxDamage()) {
            is.stackSize = 0;
        }
        return is;
    }
    
    @Override
    public boolean doesContainerItemLeaveCraftingGrid(ItemStack par1ItemStack) {
        return false;
    }

    @Override
    public void addInformation(ItemStack is, EntityPlayer player, List infoList, boolean verbose) {
        Core.brand(infoList);
    }
}
