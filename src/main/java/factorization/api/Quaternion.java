package factorization.api;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataOutput;

public class Quaternion {
    public double w, x, y, z;
    
    //Data functions
    public Quaternion() {
        this(1, 0, 0, 0);
    }
    
    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Quaternion(Quaternion orig) {
        this.w = orig.w;
        this.x = orig.x;
        this.y = orig.y;
        this.z = orig.z;
    }
    
    public Quaternion(double[] init) {
        this(init[0], init[1], init[2], init[3]);
        assert init.length == 4;
    }
    
    public Quaternion(double w, Vec3 v) {
        this(w, v.xCoord, v.yCoord, v.zCoord);
    }
    
    public boolean isEqual(Quaternion other) {
        return w == other.w && x == other.x && y == other.y && z == other.z; 
    }
    
    @Override
    public String toString() {
        return "Quaternion(" + w + ", " + x + ", " + y + ", " + z + ")";
    }
    
    public void writeToTag(NBTTagCompound tag, String prefix) {
        tag.setDouble(prefix+"w", w);
        tag.setDouble(prefix+"x", x);
        tag.setDouble(prefix+"y", y);
        tag.setDouble(prefix+"z", z);
    }
    
    public static Quaternion loadFromTag(NBTTagCompound tag, String prefix) {
        return new Quaternion(tag.getDouble(prefix+"w"), tag.getDouble(prefix+"x"), tag.getDouble(prefix+"y"), tag.getDouble(prefix+"z"));
    }
    
    public void write(ByteArrayDataOutput out) {
        double[] d = toStaticArray();
        for (int i = 0; i < d.length; i++) {
            out.writeDouble(d[i]);
        }
    }
    
    public static Quaternion read(DataInput in) throws IOException {
        double[] d = localStaticArray.get();
        for (int i = 0; i < d.length; i++) {
            d[i] = in.readDouble();
        }
        return new Quaternion(d);
    }
    
    public double[] fillArray(double[] out) {
        out[0] = w;
        out[1] = x;
        out[2] = y;
        out[3] = z;
        return out;
    }
    
    public double[] toArray() {
        return fillArray(new double[4]);
    }
    
    private static ThreadLocal<double[]> localStaticArray = new ThreadLocal<double[]>() {
        protected double[] initialValue() {
            return new double[4];
        };
    };
    
    public double[] toStaticArray() {
        return fillArray(localStaticArray.get());
    }
    
    public boolean isZero() {
        return x == 0 && y == 0 && z == 0;
    }
    
    public void update(double nw, double nx, double ny, double nz) {
        w = nw;
        x = nx;
        y = ny;
        z = nz;
    }
    
    public void update(Quaternion other) {
        update(other.w, other.x, other.y, other.z);
    }
    
    public void updateVector(Vec3 v) {
        v.xCoord = x;
        v.yCoord = y;
        v.zCoord = z;
    }
    
    public Vec3 toVector() {
        return Vec3.createVectorHelper(x, y, z);
    }
    
    //Math functions
    public void incrNormalize() {
        double normSquared = magnitudeSquared();
        if (normSquared == 1 || normSquared == 0) {
            return;
        }
        double norm = Math.sqrt(normSquared);
        w /= norm;
        x /= norm;
        y /= norm;
        z /= norm;
    }
    
    public static Quaternion getRotationQuaternion(double angle, Vec3 axis) {
        double halfAngle = angle/2;
        double sin = Math.sin(halfAngle);
        return new Quaternion(Math.cos(halfAngle), axis.xCoord*sin, axis.yCoord*sin, axis.zCoord*sin);
    }
    
    public static Quaternion getRotationQuaternion(double angle, ForgeDirection axis) {
        double halfAngle = angle/2;
        double sin = Math.sin(halfAngle);
        return new Quaternion(Math.cos(halfAngle), axis.offsetX*sin, axis.offsetY*sin, axis.offsetZ*sin);
    }
    
    public static Quaternion getRotationQuaternion(double angle, double ax, double ay, double az) {
        double halfAngle = angle/2;
        double sin = Math.sin(halfAngle);
        return new Quaternion(Math.cos(halfAngle), ax*sin, ay*sin, az*sin);
    }
    
    /**
     * @param Vec3 that gets mutated to the axis of rotation
     * @return the rotation
     */
    public double setVector(Vec3 axis) {
        double halfAngle = Math.acos(w);
        double sin = Math.sin(halfAngle);
        axis.xCoord = x/sin;
        axis.yCoord = y/sin;
        axis.zCoord = z/sin;
        return halfAngle*2;
    }
    
    public double dotProduct(Quaternion other) {
        return w*other.w + x*other.x + y*other.y + z*other.z;
    }
    
    private static final double DOT_THRESHOLD = 0.9995;
    public Quaternion slerp(Quaternion other, double t) {
        //From http://number-none.com/product/Understanding%20Slerp,%20Then%20Not%20Using%20It/index.html (encoding = Western; ISO-8859-1)
        
        
        // v0 and v1 should be unit length or else
        // something broken will happen.

        // Compute the cosine of the angle between the two vectors.
        double dot = dotProduct(other);

        if (dot > DOT_THRESHOLD) {
            // If the inputs are too close for comfort, linearly interpolate
            // and normalize the result.
            Quaternion result = new Quaternion(this);
            Quaternion temp = other.add(this, -1);
            temp.incrScale(t);
            result.incrAdd(temp);
            result.incrNormalize();
            return result;
        }
        dot = Math.min(-1, Math.max(1, dot)); 	// Robustness: Stay within domain of acos()
        double theta_0 = Math.acos(dot);  		// theta_0 = angle between input vectors
        double theta = theta_0*t;    			// theta = angle between v0 and result 

        Quaternion v2 = other.add(this, -dot);
        v2.incrNormalize();              		// { v0, v2 } is now an orthonormal basis
        
        Quaternion ret = this.scale(Math.cos(theta));
        v2.incrScale(Math.sin(theta));
        ret.incrAdd(v2);
        return ret;
    }
    
    /**
     * Also called the norm
     */
    public double magnitude() {
        return Math.sqrt(w*w + x*x + y*y + z*z);
    }
    
    public double magnitudeSquared() {
        return w*w + x*x + y*y + z*z;
    }
    
    public double incrDistance(Quaternion other) {
        incrAdd(other);
        return magnitude();
    }
    
    public void incrConjugate() {
        x *= -1;
        y *= -1;
        z *= -1;
    }
    
    public void incrAdd(Quaternion other) {
        w += other.w;
        x += other.x;
        y += other.y;
        z += other.z;
    }
    
    public void incrAdd(Quaternion other, double scale) {
        w += other.w*scale;
        x += other.x*scale;
        y += other.y*scale;
        z += other.z*scale;
    }
    
    public void incrMultiply(Quaternion other) {
        double nw, nx, ny, nz;
        nw = w*other.w - x*other.x - y*other.y - z*other.z;
        nx = w*other.x + x*other.w + y*other.z - z*other.y;
        ny = w*other.y - x*other.z + y*other.w + z*other.x;
        nz = w*other.z + x*other.y - y*other.x + z*other.w;
        update(nw, nx, ny, nz);
    }
    
    public void incrScale(double scaler) {
        this.w *= scaler;
        this.x *= scaler;
        this.y *= scaler;
        this.z *= scaler;
    }
    
    public void incrUnit() {
        incrScale(1/magnitude());
    }
    
    public void incrReciprocal() {
        double m = magnitude();
        incrConjugate();
        incrScale(1/(m*m));
    }
    
    /**
     * Note: This assumes that this quaternion is normal (magnitude = 1).
     * @param p
     */
    public void applyRotation(Vec3 p) {
        //TODO: Optimize!
        //also, rather inefficient
        //return this * p * this^-1
        Quaternion point = new Quaternion(0, p);
        Quaternion trans = this.multiply(point).multiply(this.conjugate());
//		Quaternion trans = this.multiply(point);
//		incrConjugate();
//		trans.multiply(this);
//		incrConjugate();
        p.xCoord = trans.x;
        p.yCoord = trans.y;
        p.zCoord = trans.z;
    }
    
    //Other math forms
    public double distance(Quaternion other) {
        return add(other).magnitude();
    }
    
    public Quaternion conjugate() {
        Quaternion ret = new Quaternion(this);
        ret.incrConjugate();
        return ret;
    }
    
    public Quaternion add(Quaternion other) {
        Quaternion ret = new Quaternion(this);
        ret.incrAdd(other);
        return ret;
    }
    
    public Quaternion add(Quaternion other, double scale) {
        Quaternion ret = new Quaternion(this);
        ret.incrAdd(other, scale);
        return ret;
    }
    
    public Quaternion multiply(Quaternion other) {
        Quaternion a = new Quaternion(this);
        a.incrMultiply(other);
        return a;
    }
    
    public Quaternion scale(double scaler) {
        Quaternion a = new Quaternion(this);
        a.incrScale(scaler);
        return a;
    }
    
    public Quaternion unit() {
        Quaternion r = new Quaternion(this);
        r.incrUnit();
        return r;
    }
    
    public Quaternion reciprocal() {
        Quaternion r = new Quaternion(this);
        r.incrReciprocal();
        return r;
    }
}
