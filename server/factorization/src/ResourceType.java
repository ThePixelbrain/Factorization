package factorization.src;

import net.minecraft.src.ItemStack;

public enum ResourceType {
	SILVERORE(0), SILVERBLOCK(1), LEADBLOCK(2), DARKIRONBLOCK(3), MECHAMODDER(4);

	final public int md;

	ResourceType(int metadata) {
		md = metadata;
	}

	public boolean is(int md) {
		return md == this.md;
	}

	ItemStack itemStack(String name) {
		ItemStack ret = new ItemStack(Core.registry.item_resource, 1, this.md);
		Core.instance.AddName(ret, name);
		return ret;
	}
}
