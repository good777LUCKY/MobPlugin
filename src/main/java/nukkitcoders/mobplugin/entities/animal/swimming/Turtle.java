package nukkitcoders.mobplugin.entities.animal.swimming;

import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import nukkitcoders.mobplugin.entities.animal.SwimmingAnimal;
import nukkitcoders.mobplugin.utils.Utils;

public class Turtle extends SwimmingAnimal {

    public static final int NETWORK_ID = 74;

    public Turtle(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.36f;
        }
        return 1.2f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.12f;
        }
        return 0.4f;
    }
    
    @Override
    public float getBabyScale() {
        return 0.16f;
    }
    
    @Override
    public void initEntity() {
        super.initEntity();
        
        this.setMaxHealth(30);
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 0 : Utils.rand(1, 3);
    }
}
