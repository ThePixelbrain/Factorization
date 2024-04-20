package factorization.common;

import java.io.DataInput;
import java.io.IOException;
import java.util.Random;

import org.bouncycastle.util.Arrays;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import factorization.api.Coord;
import factorization.api.IChargeConductor;
import factorization.api.ICoord;
import factorization.api.IFactoryType;
import factorization.common.NetworkFactorization.MessageType;

public abstract class TileEntityCommon extends TileEntity implements ICoord, IFactoryType {
    static Random rand = new Random();

    public abstract BlockClass getBlockClass();

    @Override
    public Packet getDescriptionPacket() {
        //Wow. Would you people please *stop* fucking with the names?
        return getAuxillaryInfoPacket();
    }
    
    public Packet getAuxillaryInfoPacket() {
        Packet p = Core.network.TEmessagePacket(getCoord(), MessageType.FactoryType, getFactoryType().md, getExtraInfo(), getExtraInfo2());
        p.isChunkDataPacket = true;
        return p;
    }

    void onRemove() {
        if (this instanceof IChargeConductor) {
            ((IChargeConductor) this).getCharge().remove();
        }
    }

    byte getExtraInfo() {
        return 0;
    }

    byte getExtraInfo2() {
        return 0;
    }

    void useExtraInfo(byte b) {
    }

    void useExtraInfo2(byte b) {
    }

    void sendFullDescription(EntityPlayer player) {
    }

    boolean canPlaceAgainst(EntityPlayer player, Coord c, int side) {
        return true;
    }

    void onPlacedBy(EntityPlayer player, ItemStack is, int side) {
    }

    public void neighborChanged() {
    }
    
    private static TileEntityCommon pulsingTE = null;
    
    public void pulse() {
        Coord here = getCoord();
        if (here.w.isRemote) {
            return;
        }
        pulsingTE = this;
        here.notifyNeighbors();
        here.scheduleUpdate(4);
        pulsingTE = null;
    }
    
    public boolean power() {
        return this == pulsingTE;
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool() {
        setBlockBounds(Core.registry.resource_block);
        AxisAlignedBB ret = Core.registry.resource_block.getCollisionBoundingBoxFromPool(worldObj, xCoord, yCoord, zCoord);
        Core.registry.resource_block.setBlockBounds(0, 0, 0, 1, 1, 1);
        return ret;
    }

    public MovingObjectPosition collisionRayTrace(World w, int x, int y, int z,
            Vec3 startVec, Vec3 endVec) {
        return Block.stone.collisionRayTrace(worldObj, xCoord, yCoord, zCoord, startVec, endVec);
    }

    public void setBlockBounds(Block b) {
        b.setBlockBounds(0, 0, 0, 1, 1, 1);
    }

    Packet getDescriptionPacketWith(Object... args) {
        Object[] suffix = new Object[args.length + 3];
        suffix[0] = getFactoryType().md;
        suffix[1] = getExtraInfo();
        suffix[2] = getExtraInfo2();
        for (int i = 0; i < args.length; i++) {
            suffix[i + 3] = args[i];
        }
        Packet p = Core.network.TEmessagePacket(getCoord(), MessageType.FactoryType, suffix);
        p.isChunkDataPacket = true;
        return p;
    }

    @Override
    public Coord getCoord() {
        return new Coord(this);
    }

    public boolean activate(EntityPlayer entityplayer) {
        FactoryType type = getFactoryType();

        if (type.hasGui) {
            if (!entityplayer.worldObj.isRemote) {
                sendFullDescription(entityplayer);
                entityplayer.openGui(Core.instance, type.gui, worldObj, xCoord, yCoord, zCoord);
            }
            return true;
        }
        return false;
    }

    public boolean isBlockSolidOnSide(int side) {
        return true;
    }
    
    public boolean takeUpgrade(ItemStack is) {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("ver", Core.version);
        getBlockClass().enforceQuiet(getCoord()); //NOTE: This won't actually work for the quiting save; but a second save'll take care of that.
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); }

    public boolean handleMessageFromServer(int messageType, DataInput input) throws IOException {
        return false;
    }

    public boolean handleMessageFromClient(int messageType, DataInput input) throws IOException {
        // There are no base attributes a client can edit
        return false;
    }

    void broadcastMessage(EntityPlayer who, int messageType, Object... msg) {
        Core.network.broadcastMessage(who, getCoord(), messageType, msg);
    }
    
    void broadcastMessage(EntityPlayer who, Packet toSend) {
        Core.network.broadcastPacket(who, getCoord(), toSend);
    }
    
    @Override
    public void invalidate() {
        if (this instanceof IChargeConductor) {
            IChargeConductor me = (IChargeConductor) this;
            me.getCharge().invalidate();
        }
        super.invalidate();
    }
}
