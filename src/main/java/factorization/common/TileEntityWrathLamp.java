package factorization.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class TileEntityWrathLamp extends TileEntityCommon {
    static final int radius = 6;
    static final int radiusSq = radius * radius;
    static final int diameter = radius * 2;
    static final int maxDepth = 24; //XXX TODO
    private short beamDepths[] = new short[(diameter + 1) * (diameter + 1)];
    private Updater updater = new InitialBuild();
    static boolean isUpdating = false;

    static int update_count;
    static final int update_limit = 64;
    static PriorityQueue<Coord> airToUpdate = new PriorityQueue(1024);

    private static class Coord implements Comparable<Coord> {
        World w;
        int x, y, z;

        Coord(World w, int x, int y, int z) {
            this.w = w;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        void check() {
            doAirCheck(w, x, y, z);
        }

        @Override
        public int compareTo(Coord o) {
            return o.y - y;
        }
    }

    public static void handleAirUpdates() {
        update_count = 0;
        while (update_count < update_limit && airToUpdate.size() > 0) {
            airToUpdate.remove().check();
        }
    }

    static void doAirCheck(World w, int x, int y, int z) {
        if (w.isRemote) {
            return;
        }
        if (update_count > update_limit) {
            airToUpdate.add(new Coord(w, x, y, z));
            return;
        }
        TileEntityWrathLamp lamp = TileEntityWrathLamp.findLightAirParent(w, x, y, z);
        if (lamp == null) {
            update_count += 1;
            w.setBlockWithNotify(x, y, z, 0);
        }
        else {
            lamp.activate(y);
        }
    }

    private int getDepthIndex(int x, int z) {
        int dx = xCoord - x, dz = zCoord - z;
        dx += radius;
        dz += radius;
        if (dx < 0 || dx > diameter) {
            throw new IndexOutOfBoundsException("x = " + dx);
        }
        if (dz < 0 || dz > diameter) {
            throw new IndexOutOfBoundsException("z = " + dz);
        }
        return dx + dz * diameter;
    }

    static final int deltas[] = new int[] { 0, -16, +16 };

    static HashSet<Chunk> toVisit = new HashSet(9 * 5);

    static TileEntityWrathLamp findLightAirParent(World world, int x, int y, int z) {
        //NOTE: This could be optimized. Probably not really worth it tho.
        toVisit.clear();
        for (int dcx : deltas) {
            for (int dcz : deltas) {
                if (!world.blockExists(x + dcx, y, z + dcz)) {
                    continue;
                }
                toVisit.add(world.getChunkFromBlockCoords(x + dcx, z + dcz));
            }
        }
        for (Chunk chunk : toVisit) {
            for (Object o : chunk.chunkTileEntityMap.values()) {
                if (!(o instanceof TileEntityWrathLamp)) {
                    continue;
                }
                TileEntityWrathLamp lamp = (TileEntityWrathLamp) o;
                if (lamp.lightsBlock(x, y, z)) {
                    // NOTE: It's possible for two lamps to overlap eachother...
                    return lamp;
                }
            }
        }
        return null;
    }

    private boolean inArea(int x, int y, int z) {
        if (y > yCoord) {
            return false;
        }
        if (y < yCoord - maxDepth) {
            return false;
        }
        return xCoord - radius <= x && x <= xCoord + radius && zCoord - radius <= z && z <= zCoord + radius;
    }

    private boolean lightsBlock(int x, int y, int z) {
        if (!inArea(x, y, z)) {
            return false;
        }
        int depth = beamDepths[getDepthIndex(x, z)];
        return y >= depth && depth != -1;
    }

    @Override
    public void updateEntity() {
        Core.profileStart("WrathLamp");
        isUpdating = true;
        this.updater = this.updater.update();
        isUpdating = false;
        Core.profileEnd();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        Core.profileStart("WrathLamp");
        for (int x = xCoord - radius; x <= xCoord + radius; x++) {
            for (int z = zCoord - radius; z <= zCoord + radius; z++) {
                int id = worldObj.getBlockId(x, yCoord, z);
                if (id == Core.registry.lightair_block.blockID) {
                    worldObj.setBlockWithNotify(x, yCoord, z, 0);
                    //worldObj.setBlock(x, yCoord, z, 0);
                }
            }
        }
        Core.profileEnd();
        if (worldObj.isRemote) {
            RelightTask task = new RelightTask(worldObj);
            task.setPosition(xCoord, yCoord, zCoord);
            worldObj.spawnEntityInWorld(task); //No comods, right? Right?
        }
    }

    double dist(int x, int y, int z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    boolean eq(double a, double b) {
        return Math.abs(a - b) < 0.9;
    }

    float div(int a, int b) {
        if (b == 0) {
            return Math.signum(a) * 0xFFF;
        }
        return a / b;
    }

    boolean myTrace(int x, int z) {
        int dx = x - xCoord, dz = z - zCoord;
        float idealm = div(dz, dx);

        float old_dist = Float.MAX_VALUE;
        while (true) {
            if (x == xCoord && z == zCoord) {
                return true;
            }
            int id = worldObj.getBlockId(x, yCoord, z);
            if (Block.blocksList[id] != null && Block.blocksList[id].isOpaqueCube() && Block.lightOpacity[id] != 0) {
                return false;
            }
            dx = x - xCoord;
            dz = z - zCoord;
            float m = div(dz, dx);
            int addx = (int) -Math.signum(dx), addz = (int) -Math.signum(dz);
            if (addx == 0 && addz == 0) {
                return true;
            }
            if (addx == 0) {
                z += addz;
                continue;
            }
            if (addz == 0) {
                x += addx;
                continue;
            }
            float m_x = div(dz, dx + addx);
            float m_z = div(dz + addz, dx);
            if (Math.abs(idealm - m_x) <= Math.abs(idealm - m_z)) {
                x += addx;
            }
            else {
                z += addz;
            }
        }

    }

    boolean clearTo(int x, int y, int z) {
        return myTrace(x, z);
        //		float a = 0, b = 0;
        //		Vec3D me = Vec3D.createVector(xCoord + a, yCoord + a, zCoord + a);
        //		Vec3D other = Vec3D.createVector(x + b, y + b, z + b);
        //		return trace(other, me);
    }

    @Override
    public void neighborChanged() {
        activate(yCoord);
    }

    void activate(int at_height) {
        updater = updater.getActive(at_height);
    }

    abstract class Updater {
        int start_height = -100;

        abstract Updater update();

        abstract Updater getActive(int start_height);
    }

    class InitialBuild extends Updater {
        int height = -100;
        int start_delay = 20;
        Updater next_updater = new Idler();

        @Override
        public Updater update() {
            if (start_delay >= 0) {
                start_delay--;
                return this;
            }
            if (height == -100) {
                if (start_height == -100) {
                    height = yCoord;
                }
                else {
                    height = start_height;
                }
            }
            else {
                height -= 1;
            }
            if (height < yCoord - maxDepth) {
                return next_updater;
            }
            if (height == yCoord) {
                //we are level with the lamp.
                //Set areas we can't reach to -1
                Arrays.fill(beamDepths, (short) 0);
                for (int x = xCoord - radius; x <= xCoord + radius; x++) {
                    for (int z = zCoord - radius; z <= zCoord + radius; z++) {
                        if (!clearTo(x, yCoord, z)) {
                            beamDepths[getDepthIndex(x, z)] = -1;
                        }
                    }
                }
            }
            for (int x = xCoord - radius; x <= xCoord + radius; x++) {
                for (int z = zCoord - radius; z <= zCoord + radius; z++) {
                    // If it is air, make it LightAir™
                    // If already lightair, carry on
                    // if a (solid?) block, stop
                    int index = getDepthIndex(x, z);
                    if (beamDepths[index] != 0) {
                        continue;
                    }
                    int block = worldObj.getBlockId(x, height, z);
                    int belowBlock = worldObj.getBlockId(x, height - 3, z);
                    if (belowBlock != 0 && belowBlock != Core.registry.lightair_block.blockID && height != yCoord) {
                        beamDepths[index] = (short) height;
                        continue;
                    }
                    if (block == 0 && worldObj.getBlockId(x, height - 1, z) == Block.cobblestoneWall.blockID) {
                        block = -1;
                    }
                    if (block == 0) {
                        worldObj.setBlockWithNotify(x, height, z, Core.registry.lightair_block.blockID);
                    } else if (block == Core.registry.lightair_block.blockID) {
                    } else if (x == xCoord && height == yCoord && z == zCoord) {
                        //this is ourself. Hi, self.
                        //Don't terminate the beamDepth early.
                    } else {
                        beamDepths[index] = (short) height;
                    }

                }
            }

            if (height == 0) {
                return next_updater;
            }
            else {
                return this;
            }
        }

        @Override
        Updater getActive(int start_height) {
            if (!(this.next_updater instanceof InitialBuild)) {
                this.next_updater = new InitialBuild();
            }
            start_height++;
            next_updater.start_height = Math.max(start_height, next_updater.start_height);
            return this;
        }
    }

    class Idler extends Updater {
        boolean couldUpdate(int dx, int dz) {
            return worldObj.getBlockId(xCoord + dx, yCoord, zCoord + dz) == 0;
        }

        @Override
        public Updater update() {
            if (couldUpdate(0, +1) || couldUpdate(0, -1) || couldUpdate(+1, 0) || couldUpdate(-1, 0)) {
                return new InitialBuild();
            }
            return this;
        }

        @Override
        Updater getActive(int start_height) {
            Updater r = new InitialBuild();
            r.start_height = Math.max(r.start_height, start_height);
            return r;
        }
    }

    public static class RelightTask extends Entity {
        int delay;

        public RelightTask(World par1World) {
            super(par1World);
        }

        @Override
        protected void entityInit() {
            delay = 20 * 2;
        }

        //No need to bother saving this.
        @Override
        protected void readEntityFromNBT(NBTTagCompound var1) {
        }

        @Override
        protected void writeEntityToNBT(NBTTagCompound var1) {
        }

        @Override
        public void onUpdate() {
            delay -= 1;
            if (delay == 0) {
                int r = radius + 15;
                for (int x = (int) (posX - r); x <= posX + r; x++) {
                    for (int z = (int) (posZ - r); z <= posZ + r; z++) {
                        for (int y = (int) posY; y >= posY - maxDepth; y--) {
                            worldObj.updateAllLightTypes(x, y, z);
                        }
                    }
                }
                this.setDead();
            }
        }
    }

    @Override
    public FactoryType getFactoryType() {
        return FactoryType.LAMP;
    }

    @Override
    public BlockClass getBlockClass() {
        return BlockClass.Lamp;
    }

    @Override
    public boolean isBlockSolidOnSide(int side) {
        return false;
    }
}
