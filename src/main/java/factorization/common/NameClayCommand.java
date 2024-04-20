package factorization.common;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class NameClayCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "namesculpture";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayer)) {
            sender.sendChatToPlayer("You are not logged in");
            return;
        }
        String name = "";
        boolean first = true;
        for (String s : args) {
            if (!first) {
                name += " ";
            }
            first = false;
            name += s;
        }
        EntityPlayer player = (EntityPlayer) sender;
        ItemStack is = player.getCurrentEquippedItem();
        if (is != null && is.isItemEqual(Core.registry.greenware_item) /* no NBT okay */ ) {
            NBTTagCompound tag = is.getTagCompound();
            if (tag != null && tag.hasKey("parts")) {
                tag.setString("sculptureName", name);
                return;
            }
        }
        player.sendChatToPlayer("That item can not be named.");
    }

    @Override
    public String getCommandUsage(ICommandSender par1iCommandSender) {
        return super.getCommandUsage(par1iCommandSender) + " name for held sculpture";
    }
    
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return sender instanceof EntityPlayer;
    }

    @Override
    public int compareTo(Object o) {
        return getCommandName().compareTo(((ICommand) o).getCommandName());
    }
}
