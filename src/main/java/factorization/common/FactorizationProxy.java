package factorization.common;

import java.io.File;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraft.client.multiplayer.WorldClient;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import factorization.api.Coord;

public abstract class FactorizationProxy implements IGuiHandler {
    public abstract void broadcastTranslate(EntityPlayer who, String... msg);

    public abstract EntityPlayer getPlayer(NetHandler handler);

    /** Send packet to other side */
    public void addPacket(EntityPlayer player, Packet packet) {
        if (player.worldObj.isRemote) {
            PacketDispatcher.sendPacketToServer(packet);
        } else {
            PacketDispatcher.sendPacketToPlayer(packet, (Player) player);
        }
    }

    public abstract Profiler getProfiler();

    protected Container getContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == FactoryType.POCKETCRAFTGUI.gui) {
            return new ContainerPocket(player);
        }

        if (ID == FactoryType.EXOTABLEGUICONFIG.gui) {
            return new ContainerExoModder(player, new Coord(world, x, y, z));
        }

        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (!(te instanceof TileEntityFactorization)) {
            return null;
        }
        TileEntityFactorization fac = (TileEntityFactorization) te;
        ContainerFactorization cont;
        if (ID == FactoryType.SLAGFURNACE.gui) {
            cont = new ContainerSlagFurnace(player, fac);
        } else if (ID == FactoryType.GRINDER.gui) {
            cont = new ContainerGrinder(player, fac);
        } else if (ID == FactoryType.MIXER.gui) {
            cont = new ContainerMixer(player, fac);
        } else if (ID == FactoryType.CRYSTALLIZER.gui) {
            cont = new ContainerCrystallizer(player, fac);
        } else {
            cont = new ContainerFactorization(player, fac);
        }
        cont.addSlotsForGui(fac, player.inventory);
        return cont;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return getContainer(ID, player, world, x, y, z);
    }

    //CLIENT
    public void addName(Object objectToName, String name) {
        String objectName;
        if (objectToName instanceof Item) {
            objectName = ((Item) objectToName).getItemName();
        } else if (objectToName instanceof Block) {
            objectName = ((Block) objectToName).getBlockName();
        } else if (objectToName instanceof ItemStack) {
            objectName = ((ItemStack) objectToName).getItem().getItemNameIS((ItemStack) objectToName);
        } else if (objectToName instanceof String) {
            objectName = (String) objectToName;
        } else {
            throw new IllegalArgumentException(String.format("Illegal object for naming %s", objectToName));
        }
        addNameDirect(objectName + ".name", name);
    }
    
    public void addNameDirect(String localId, String translate) {
    }

    public String translateItemStack(ItemStack is) {
        if (is == null) {
            return "<null itemstack; bug?>";
        }
        String n = is.getItem().getItemNameIS(is);
        if (n == null) {
            n = is.getItem().getItemName();
        }
        if (n == null) {
            n = is.getItemName();
        }
        if (n == null) {
            n = "???";
        }
        return n;
    }

    /** Tell the pocket crafting table to update the result */
    public void pokePocketCrafting() {
    }

    public void randomDisplayTickFor(World w, int x, int y, int z, Random rand) {
    }

    public void playSoundFX(String src, float volume, float pitch) {
    }

    public EntityPlayer getClientPlayer() {
        return null;
    }

    public void registerKeys() {
    }

    public void registerRenderers() {
    }

    //SERVER
    /** If on SMP, send packet to tell player what he's holding */
    public void updateHeldItem(EntityPlayer player) {
        if (!player.worldObj.isRemote) {
            ((EntityPlayerMP) player).updateHeldItem();
        }
    }

    public void updatePlayerInventory(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP emp = (EntityPlayerMP) player;
            emp.sendContainerToPlayer(emp.inventoryContainer);
            // updates entire inventory. Inefficient, I know, but... XXX
        }
    }

    public boolean playerListensToCoord(EntityPlayer player, Coord c) {
        //XXX TODO: Figure this out.
        return true;
    }

    public boolean isPlayerAdmin(EntityPlayer player) {
        return false;
    }

    public String getExoKeyBrief(int keyindex) {
        return "ExoKey" + keyindex;
    }
}
