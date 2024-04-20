package factorization.common;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;
import factorization.api.Coord;
import factorization.api.IExoUpgrade;
import factorization.common.Core.TabType;

public class ExoMountedPiston extends Item implements IExoUpgrade {
    protected ExoMountedPiston(int par1) {
        super(par1);
        setItemName("exo.mountedpiston");
        setIconIndex(16 * 10 + 2);
        Core.tab(this, TabType.MISC);
        setMaxStackSize(1);
        setTextureFile(Core.texture_file_item);
    }
    
    @Override
    public boolean canUpgradeArmor(ItemStack is, int armorIndex) {
        return armorIndex == 1;
    }

    boolean tryPush(Coord c, int orientation) {
        if (!c.isAir()) {
            return false;
        }
        if (c.copy().towardSide(orientation).isAir()) {
            return false;
        }
        Block.pistonBase.onBlockEventReceived(c.w, c.x, c.y, c.z, 0, orientation);
        c.setId(0);
        Core.network.broadcastMessage(null, c, NetworkFactorization.MessageType.PistonPush, orientation);
        return true;
    }

    @Override
    public ItemStack tickUpgrade(EntityPlayer player, ItemStack armor, ItemStack upgrade,
            boolean isEnabled) {
        if (!isEnabled) {
            return null;
        }
        if (player.worldObj.isRemote) {
            return null;
        }

        if (!FactorizationUtil.itemCanFire(player.worldObj, upgrade, 15)) {
            return null;
        }

        Coord head;
        Coord foot;
        head = new Coord(player).add(0, 1, 0);
        foot = new Coord(player);

        Coord order[];
        if (player.rotationPitch <= 45) {
            order = new Coord[] { head, foot };
        }
        else {
            order = new Coord[] { foot, head };
        }
        for (Coord c : order) {
            if (tryPush(c, FactorizationUtil.determineOrientation(player))) {
                return upgrade;
            }
        }

        return null;
    }

    @Override
    public void addArmorProperties(ItemStack is, ArmorProperties armor) {
    }

    @Override
    public int getArmorDisplay(ItemStack is) {
        return 0;
    }

    @Override
    public boolean damageArmor(EntityLiving entity, ItemStack stack, DamageSource source,
            int damage, int slot) {
        return false;
    }

    @Override
    public String getDescription() {
        return "Push blocks";
    }

    @Override
    public void addInformation(ItemStack is, EntityPlayer player, List infoList, boolean verbose) {
        infoList.add("Exo-Upgrade");
        Core.brand(infoList);
    }
}
