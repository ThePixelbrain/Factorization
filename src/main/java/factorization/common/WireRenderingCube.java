package factorization.common;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import factorization.api.VectorUV;

public class WireRenderingCube {
    int icon;
    public VectorUV corner, origin, axis;
    public double ul, vl;
    public float theta;

    /**
     * Creates a lovely cube used to render with. The vectors are in texels with the center of the tile as the origin. The rotations will also be done around
     * the center of the tile.
     */
    public WireRenderingCube(int icon, VectorUV corner, VectorUV origin) {
        if (origin == null) {
            origin = new VectorUV(0, 0, 0, 0, 0);
        }
        this.corner = corner;
        this.origin = origin;
        this.axis = new VectorUV(0, 0, 0);
        this.theta = 0;

        setIcon(icon);
    }
    
    void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("icon", icon);
        corner.writeToTag(tag, "c");
        origin.writeToTag(tag, "o");
        axis.writeToTag(tag, "a");
        tag.setFloat("theta", theta);
    }
    
    static WireRenderingCube loadFromNBT(NBTTagCompound tag) {
        int icon = tag.getInteger("icon");
        VectorUV c = VectorUV.readFromTag(tag, "c");
        VectorUV o = VectorUV.readFromTag(tag, "o");
        VectorUV a = VectorUV.readFromTag(tag, "a");
        WireRenderingCube rc = new WireRenderingCube(icon, c, o);
        rc.axis = a;
        rc.theta = tag.getFloat("theta");
        return rc;
    }
    
    void writeToArray(ArrayList<Object> args) {
        args.add(icon);
        corner.addInfoToArray(args);
        origin.addInfoToArray(args);
        axis.addInfoToArray(args);
        args.add(theta);
    }
    
    static float takeFloat(ArrayList<Object> args) {
        return (Float) args.remove(0);
    }
    
    static WireRenderingCube readFromArray(ArrayList<Object> args) {
        int icon = (Integer) args.remove(0);
        VectorUV c = new VectorUV(takeFloat(args), takeFloat(args), takeFloat(args));
        VectorUV o = new VectorUV(takeFloat(args), takeFloat(args), takeFloat(args));
        VectorUV a = new VectorUV(takeFloat(args), takeFloat(args), takeFloat(args));
        WireRenderingCube rc = new WireRenderingCube(icon, c, o);
        rc.axis = a;
        rc.theta = takeFloat(args);
        return rc;
    }
    
    public boolean equals(WireRenderingCube other) {
        return this.corner.equals(other.corner) && this.origin.equals(other.origin) && this.icon == other.icon; 
    }

    public WireRenderingCube copy() {
        WireRenderingCube ret = new WireRenderingCube(this.icon, this.corner.copy(), this.origin.copy());
        ret.ul = this.ul;
        ret.vl = this.vl;
        ret.axis = this.axis.copy();
        ret.theta = this.theta;
        return ret;
    }

    public WireRenderingCube normalize() {
        VectorUV newCorner = corner.copy();
        VectorUV newOrigin = origin.copy();
        newCorner.rotate(axis.x, axis.y, axis.z, theta);
        newOrigin.rotate(axis.x, axis.y, axis.z, theta);
        newCorner.x = Math.abs(newCorner.x);
        newCorner.y = Math.abs(newCorner.y);
        newCorner.z = Math.abs(newCorner.z);
        return new WireRenderingCube(icon, newCorner, newOrigin);
    }

    public void toBlockBounds(Block b) {
        WireRenderingCube cube = normalize();
        VectorUV c = cube.corner;
        VectorUV o = cube.origin;
        c.scale(1F / 16F);
        o = o.add(8, 8, 8);
        o.scale(1F / 16F);
        b.setBlockBounds(
                o.x - c.x, o.y - c.y, o.z - c.z,
                o.x + c.x, o.y + c.y, o.z + c.z);
    }

    public WireRenderingCube rotate(double ax, double ay, double az, int theta) {
        return rotate((float) ax, (float) ay, (float) az, theta);
    }

    public WireRenderingCube rotate(float ax, float ay, float az, int theta) {
        if (theta == 0) {
            this.axis = new VectorUV(0, 0, 0);
            this.theta = 0;
            return this;
        }
        this.axis = new VectorUV(ax, ay, az);
        this.theta = theta;
        return this;
    }
    
    public void setIcon(int newIcon) {
        icon = newIcon;
        //XXX TODO NOTE: This might not work properly with large texture packs?
        ul = ((icon & 0xf) << 4) / 256.0;
        vl = (icon & 0xf0) / 256.0;
    }

    public VectorUV[] faceVerts(int face) {
        VectorUV ret[] = new VectorUV[4];
        VectorUV v = corner;
        int c = 8;
        switch (face) {
        case 0: //-y
            ret[0] = new VectorUV(v.x, -v.y, v.z);
            ret[1] = new VectorUV(-v.x, -v.y, v.z);
            ret[2] = new VectorUV(-v.x, -v.y, -v.z);
            ret[3] = new VectorUV(v.x, -v.y, -v.z);
            break;
        case 1: //+y
            ret[0] = new VectorUV(v.x, v.y, -v.z);
            ret[1] = new VectorUV(-v.x, v.y, -v.z);
            ret[2] = new VectorUV(-v.x, v.y, v.z);
            ret[3] = new VectorUV(v.x, v.y, v.z);
            break;
        case 2: //-z
            ret[0] = new VectorUV(v.x, v.y, -v.z);
            ret[1] = new VectorUV(v.x, -v.y, -v.z);
            ret[2] = new VectorUV(-v.x, -v.y, -v.z);
            ret[3] = new VectorUV(-v.x, v.y, -v.z);
            break;
        case 3: //+z
            ret[0] = new VectorUV(v.x, v.y, v.z);
            ret[1] = new VectorUV(-v.x, v.y, v.z);
            ret[2] = new VectorUV(-v.x, -v.y, v.z);
            ret[3] = new VectorUV(v.x, -v.y, v.z);
            break;
        case 4: //-x
            ret[0] = new VectorUV(-v.x, v.y, v.z);
            ret[1] = new VectorUV(-v.x, v.y, -v.z);
            ret[2] = new VectorUV(-v.x, -v.y, -v.z);
            ret[3] = new VectorUV(-v.x, -v.y, v.z);
            break;
        case 5: //+x
            ret[0] = new VectorUV(v.x, v.y, v.z);
            ret[1] = new VectorUV(v.x, -v.y, v.z);
            ret[2] = new VectorUV(v.x, -v.y, -v.z);
            ret[3] = new VectorUV(v.x, v.y, -v.z);
            break;
        }
        for (VectorUV vert : ret) {
            vert.incr(origin);
        }
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            switch (face) {
            case 0: //-y
            case 1: //+y
                //Mirror these like MC does.
                for (VectorUV vert : ret) {
                    vert.u = vert.x + 8;
                    vert.v = vert.z + 8;
                }
                break;
            case 2: //-z
                for (VectorUV vert : ret) {
                    vert.u = 16 - (vert.x + 8);
                    vert.v = 16 - (vert.y + 8);
                }
                break;
            case 3: //+z
                for (VectorUV vert : ret) {
                    vert.u = vert.x + 8;
                    vert.v = 16 - (vert.y + 8);
                }
                break;
            case 4: //-x
                for (VectorUV vert : ret) {
                    vert.u = 16 - (vert.y + 8);
                    vert.v = (vert.z + 8);
                }
                break;
            case 5: //+x
                for (VectorUV vert : ret) {
                    vert.u = 16 - (vert.y + 8);
                    vert.v = 16 - (vert.z + 8);
                }
                break;
            }
            for (VectorUV main : ret) {
                float udelta = 0, vdelta = 0;
                int nada = 0;
                if (main.u > 16) {
                    udelta = main.u - 16;
                } else if (main.u < 0) {
                    udelta = main.u;
                } else {
                    nada++;
                }
                if (main.v > 16) {
                    vdelta = main.v - 16;
                } else if (main.v < 0) {
                    vdelta = main.v;
                } else {
                    nada++;
                }
                if (nada == 2) {
                    continue;
                }
                for (VectorUV other : ret) {
                    other.u -= udelta;
                    other.v -= vdelta;
                }
                //vert.u = Math.max(0, Math.min(vert.u, 16));
                //vert.v = Math.max(0, Math.min(vert.v, 16));
            }
        }
        if (theta != 0) {
            for (VectorUV vert : ret) {
                vert.rotate(axis.x, axis.y, axis.z, theta);
            }
        }
        return ret;
    }

}
