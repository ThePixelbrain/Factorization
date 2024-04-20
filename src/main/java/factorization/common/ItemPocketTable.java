package factorization.common;

import java.util.List;

import factorization.common.Core.TabType;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemPocketTable extends Item {

    public ItemPocketTable(int id) {
        super(id);
        setMaxStackSize(1);
        Core.tab(this, TabType.TOOLS);
        setFull3D();
    }

    @Override
    public String getTextureFile() {
        return Core.texture_file_item;
    }

    public int getIconFromDamage(int damage) {
        return 4;
    }

    //
    // @Override
    // public boolean onItemUseFirst(ItemStack stack, EntityPlayer player,
    // World world, int X, int Y, int Z, int side) {
    // player.openGui(FactorizationCore.instance, FactoryType.POCKETCRAFT.gui,
    // null, 0, 0, 0);
    // return true;
    // }
    //
    // @Override
    // public boolean tryPlaceIntoWorld(ItemStack stack,
    // EntityPlayer player, World world, int X, int Y,
    // int Z, int side) {
    // // TODO Auto-generated method stub
    // return super.onItemUse(par1ItemStack, par2EntityPlayer, par3World, par4,
    // par5,
    // par6, par7);
    // }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        ItemStack save = player.inventory.getItemStack();
        if (save != null) {
            player.inventory.setItemStack(null);
        }
        //XXX TODO: Chests stay open. Man, how do I fix this?
        //player.openGui(Core.instance, FactoryType.NULLGUI.gui, null, 0, 0, 0);
        if (!world.isRemote) {
            //...this may be troublesome for stack saving! :O
            player.openGui(Core.instance, FactoryType.POCKETCRAFTGUI.gui, null, 0, 0, 0);
            if (save != null) {
                player.inventory.setItemStack(save);
                Core.proxy.updateHeldItem(player);
            }
        }
        return stack;
    }

    @Override
    public String getItemName() {
        return "Pocket Crafting Table";
    }

    @Override
    public String getItemNameIS(ItemStack par1ItemStack) {
        return getItemName();
    }

    public ItemStack findPocket(EntityPlayer player) {
        InventoryPlayer inv = player.inventory;
        int need_to_move = -1;
        int a_free_space = -1;
        for (int i = 0; i < inv.mainInventory.length; i++) {
            boolean in_crafting_area = i % 9 >= (9 - 3) && i > 9;
            ItemStack is = inv.mainInventory[i]; // A little bit gross; using it the proper causes us to check armor slots.
            if (is == null) {
                if (!in_crafting_area) {
                    if (a_free_space == -1 || a_free_space < 9) {
                        // Silly condition because: If it's not set, we should set it. If it's < 9, it's in the hotbar, which is a poor choice.
                        // If it is going to the hotbar, it'll end up in the last empty slot.
                        a_free_space = i;
                    }
                }
                continue;
            }
            if (is.getItem() == this) {
                if (in_crafting_area) {
                    need_to_move = i;
                } else {
                    return is;
                }
            }
        }
        ItemStack mouse_item = player.inventory.getItemStack();
        if (mouse_item != null && mouse_item.getItem() == this && player.openContainer instanceof ContainerPocket) {
            return mouse_item;
        }
        if (need_to_move != -1 && a_free_space != -1) {
            ItemStack pocket = inv.getStackInSlot(need_to_move);
            inv.setInventorySlotContents(need_to_move, null);
            inv.setInventorySlotContents(a_free_space, pocket);
            return pocket;
        }
        return null;
    }

    public boolean tryOpen(EntityPlayer player) {
        ItemStack is = findPocket(player);
        if (is == null) {
            return false;
        }
        this.onItemRightClick(is, player.worldObj, player);
        return true;
    }

    @Override
    public void addInformation(ItemStack is, EntityPlayer player, List infoList, boolean verbose) {
        Core.brand(infoList);
    }
}
