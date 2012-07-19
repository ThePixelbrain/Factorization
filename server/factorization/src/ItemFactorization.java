package factorization.src;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Packet;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class ItemFactorization extends ItemBlock {
	public ItemFactorization() {
		super(Core.registry.factory_block.blockID + Core.block_item_id_offset);
		new Exception().printStackTrace();
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	public ItemFactorization(int id) {
		super(id);
		//Y'know, that -256 is really retarded.
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player,
			World w, int x, int y, int z, int side) {
		Coord here = new Coord(w, x, y, z);
		int id = here.getId();
		if (id == Block.snow.blockID) {
			side = 1;
		}
		else if (id != Block.vine.blockID && id != Block.tallGrass.blockID && id != Block.deadBush.blockID)
		{
			if (side == 0) {
				here.y--;
			}
			if (side == 1) {
				here.y++;
			}
			if (side == 2) {
				here.z--;
			}
			if (side == 3) {
				here.z++;
			}
			if (side == 4) {
				here.x--;
			}
			if (side == 5) {
				here.x++;
			}
		}

		boolean ret = super.onItemUse(is, player, w, x, y, z, side);
		if (ret) {
			//create our TileEntityFactorization
			//Coord c = new Coord(w, x, y, z).towardSide(side);
			FactoryType f = FactoryType.fromMd(is.getItemDamage());
			if (f == null) {
				is.stackSize = 0;
				return false;
			}
			TileEntity te = f.makeTileEntity();
			w.setBlockTileEntity(here.x, here.y, here.z, te);
			if (Core.instance.isCannonical(w)) {
				if (te instanceof TileEntityCommon) {
					Packet p = ((TileEntityCommon) te).getDescriptionPacket();
					Core.network.broadcastPacket(null, here, p);
				}
			}
			if (te instanceof TileEntityFactorization) {
				((TileEntityFactorization) te).onPlacedBy(player, is);
			}
		}
		return ret;
	}

	public int getIconFromDamage(int damage) {
		return Core.registry.factory_block.getBlockTextureFromSideAndMetadata(0, damage);
	}

	public int getMetadata(int i) {
		return 15;
		//return i;
	}

	@Override
	public String getItemNameIS(ItemStack itemstack) {
		// I don't think this actually gets called...
		int md = itemstack.getItemDamage();
		if (md == FactoryType.ROUTER.md) {
			return "Router";
		}
		if (md == FactoryType.CUTTER.md) {
			return "Cutter";
		}
		if (md == FactoryType.MAKER.md) {
			return "Craftpacket Maker";
		}
		if (md == FactoryType.STAMPER.md) {
			return "Craftpacket Stamper";
		}
		if (md == FactoryType.QUEUE.md) {
			return "Queue";
		}
		if (md == FactoryType.BARREL.md) {
			return "Barrel";
		}
		if (FactoryType.LAMP.is(md)) {
			return "Wrathlamp";
		}
		if (FactoryType.PACKAGER.is(md)) {
			return "Packager";
		}
		if (FactoryType.SENTRYDEMON.is(md)) {
			return "Sentry Demon";
		}
		if (FactoryType.SLAGFURNACE.is(md)) {
			return "Slag Furnace";
		}
		return "??? It's a Mystery!!!";
	}

	@Override
	public String getItemName() {
		return "ItemFactorization";
	}
}
