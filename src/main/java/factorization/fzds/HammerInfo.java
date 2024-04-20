package factorization.fzds;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.Mod;
import factorization.api.Coord;
import factorization.api.DeltaCoord;
import factorization.common.Core;

public class HammerInfo {
    File worldConfigFile = null;
    Configuration channelConfig;
    Configuration worldState;
    
    private int unsaved_allocations = 0;
    private boolean channel_config_dirty = false;
    boolean world_loaded = false;
    HashMap<Integer, ConfigCategory> channel2category = new HashMap();
    
    private static final int defaultPadding = 16*8;
    
    void setConfigFile(File f) {
        channelConfig = new Configuration(f);
    }
    
    void loadGlobalConfig() {
        if (worldState != null) {
            return;
        }
        WorldServer world = DimensionManager.getWorld(Hammer.dimensionID);
        world_loaded = true;
        File saveDir = world.getChunkSaveLocation();
        saveDir = saveDir.getAbsoluteFile();
        worldConfigFile = new File(saveDir, "hammer.state");
        worldState = new Configuration(worldConfigFile);
        saveChannelConfig();
    }
    
    private static final String channelsCategory = "channels";
    
    public int makeChannelFor(Object modInstance, String channelId, int default_channel, int padding, String comment) {
        if (padding < 0) {
            padding = defaultPadding;
        }
        if (channelConfig == null) {
            throw new IllegalArgumentException("Tried to register channel too early");
        }
        Core.logFine("Allocating Hammer channel for %s: %s", modInstance, comment);
        
        
        Class c = modInstance.getClass();
        Annotation a = c.getAnnotation(Mod.class);
        if (a == null) {
            throw new IllegalArgumentException("modInstance is not a mod");
        }
        Mod info = (Mod) c.getAnnotation(Mod.class);
        String modCategory = (info.modid() + "." + channelId).toLowerCase();
        
        int max = default_channel;
        boolean collision = false;
        
        for (Entry<String, ConfigCategory> entry : channelConfig.categories.entrySet()) {
            if (entry.getKey().equals(modCategory)) {
                continue;
            }
            ConfigCategory cat = entry.getValue();
            if (!cat.containsKey("channel")) {
                continue;
            }
            int here_chan = channelConfig.get(entry.getKey(), "channel", -1).getInt();
            max = Math.max(max, here_chan);
            if (here_chan == default_channel) {
                collision = true;
            }
        }
        if (collision) {
            int newDefault = max + 1;
            Core.logFine("Default channel ID for %s (%s) was already taken, using %s", modCategory, default_channel, newDefault);
            default_channel = newDefault;
        }
        
        channelConfig.addCustomCategoryComment(modCategory, comment);
        int channelRet = channelConfig.get(modCategory, "channel", default_channel).getInt();
        padding = channelConfig.get(modCategory, "padding", padding).getInt();
        
        if (world_loaded) {
            saveChannelConfig();
        } else {
            channel_config_dirty = true;
        }
        channel2category.put(channelRet, channelConfig.getCategory(modCategory));
        return channelRet;
    }
    
    public int getPaddingForChannel(int channel) {
        ConfigCategory cat = channel2category.get(channel);
        Property prop = cat.get("padding");
        int ret = prop.getInt(defaultPadding);
        return ret;
    }
    
    Coord takeCell(int channel, DeltaCoord size) {
        loadGlobalConfig();
        Property chanAllocs = worldState.get("allocations", "channel" + channel, 0);
        int start = chanAllocs.getInt(0);
        int add = size.x + getPaddingForChannel(channel);
        chanAllocs.value = Integer.toString(start + add);
        Coord ret = new Coord(DeltaChunk.getServerShadowWorld(), start, 16, channel*Hammer.channelWidth);
        dirtyCellAllocations();
        return ret;
    }
    
    public void setAllocationCount(int channel, int count) {
        if (channel != 0) {
            throw new IllegalArgumentException("Non-zero channels not yet implemented");
        }
        ConfigCategory cat = channel2category.get(channel);
        cat.get("allocated").value = Integer.toString(count);
        saveCellAllocations();
    }
    
    File getWorldSaveFile() {
        World hammerWorld = DeltaChunk.getServerShadowWorld();
        File base = new File(hammerWorld.getSaveHandler().getSaveDirectoryName());
        return new File(base, "deltaChunk.cfg");
    }
    
    public void dirtyCellAllocations() {
        if (unsaved_allocations == 0) {
            saveCellAllocations();
        }
        unsaved_allocations++;
    }
    
    public void saveCellAllocations() {
        if (channel_config_dirty) {
            channelConfig.save();
            channel_config_dirty = false;
        }
        if (worldState == null) {
            return;
        }
        worldState.save();
        unsaved_allocations = 0;
    }
    
    public void saveChannelConfig() {
        channelConfig.save();
    }
    
}
