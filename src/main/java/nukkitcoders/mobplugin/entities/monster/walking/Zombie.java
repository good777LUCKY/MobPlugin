package nukkitcoders.mobplugin.entities.monster.walking;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.entity.EntitySmite;
import cn.nukkit.entity.mob.EntityDrowned;
import cn.nukkit.event.entity.CreatureSpawnEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemShovelIron;
import cn.nukkit.item.ItemSwordIron;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.network.protocol.MobArmorEquipmentPacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;
import nukkitcoders.mobplugin.MobPlugin;
import nukkitcoders.mobplugin.entities.monster.WalkingMonster;
import nukkitcoders.mobplugin.route.WalkerRouteFinder;
import nukkitcoders.mobplugin.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Zombie extends WalkingMonster implements EntityAgeable, EntitySmite {

    public static final int NETWORK_ID = 32;

    public Item tool;

    public Zombie(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.route = new WalkerRouteFinder(this);
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.95f;
    }

    @Override
    public double getSpeed() {
        return 1.1;
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.setDamage(new float[] { 0, 2, 3, 4 });
        this.setMaxHealth(20);

        this.armor = getRandomArmor();

        if (this.namedTag.contains("Item")) {
            this.tool = NBTIO.getItemHelper(this.namedTag.getCompound("Item"));
            if (tool instanceof ItemSwordIron) {
                this.setDamage(new float[]{0, 4, 6, 8});
            } else if (tool instanceof ItemShovelIron) {
                this.setDamage(new float[]{0, 3, 4, 5});
            }
        } else {
            this.setRandomTool();
        }
    }

    @Override
    public void attackEntity(Entity player) {
        if (this.attackDelay > 23 && player.distanceSquared(this) <= 1) {
            this.attackDelay = 0;
            HashMap<EntityDamageEvent.DamageModifier, Float> damage = new HashMap<>();
            damage.put(EntityDamageEvent.DamageModifier.BASE, this.getDamage());

            if (player instanceof Player) {
                HashMap<Integer, Float> armorValues = new ArmorPoints();

                float points = 0;
                for (Item i : ((Player) player).getInventory().getArmorContents()) {
                    points += armorValues.getOrDefault(i.getId(), 0f);
                }

                damage.put(EntityDamageEvent.DamageModifier.ARMOR,
                        (float) (damage.getOrDefault(EntityDamageEvent.DamageModifier.ARMOR, 0f) - Math.floor(damage.getOrDefault(EntityDamageEvent.DamageModifier.BASE, 1f) * points * 0.04)));
            }
            player.attack(new EntityDamageByEntityEvent(this, player, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage));
            EntityEventPacket pk = new EntityEventPacket();
            pk.eid = this.getId();
            pk.event = 4;
            this.level.addChunkPacket(this.getChunkX() >> 4,this.getChunkZ() >> 4, pk);
        }
    }


    @Override
    public boolean entityBaseTick(int tickDiff) {
        if (getServer().getDifficulty() == 0) {
            this.close();
            return true;
        }

        boolean hasUpdate = super.entityBaseTick(tickDiff);

        if (MobPlugin.shouldMobBurn(level, this)) {
            if (this.armor[0] == null) {
                this.setOnFire(100);
            } else if (this.armor[0].getId() == 0) {
                this.setOnFire(100);
            }
        }

        return hasUpdate;
    }

    @Override
    public Item[] getDrops() {
        List<Item> drops = new ArrayList<>();

        if (!this.isBaby()) {
            for (int i = 0; i < Utils.rand(0, 2); i++) {
                drops.add(Item.get(Item.ROTTEN_FLESH, 0, 1));
            }

            if (this.tool != null) {
                if (tool instanceof ItemSwordIron && Utils.rand(1, 3) == 1) {
                    drops.add(tool);
                }

                if (tool instanceof ItemShovelIron && Utils.rand(1, 3) != 1) {
                    drops.add(tool);
                }
            }

            if (Utils.rand(1, 3) == 1) {
                switch (Utils.rand(1, 3)) {
                    case 1:
                        drops.add(Item.get(Item.IRON_INGOT, 0, Utils.rand(0, 1)));
                        break;
                    case 2:
                        drops.add(Item.get(Item.CARROT, 0, Utils.rand(0, 1)));
                        break;
                    case 3:
                        drops.add(Item.get(Item.POTATO, 0, Utils.rand(0, 1)));
                        break;
                }
            }
        }

        return drops.toArray(new Item[0]);
    }

    @Override
    public int getKillExperience() {
        return this.isBaby() ? 12 : 5;
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);

        MobArmorEquipmentPacket pk = new MobArmorEquipmentPacket();
        pk.eid = this.getId();
        pk.slots = this.armor;
        player.dataPacket(pk);

        if (this.tool != null) {
            MobEquipmentPacket pk2 = new MobEquipmentPacket();
            pk2.eid = this.getId();
            pk2.hotbarSlot = 0;
            pk2.item = this.tool;
            player.dataPacket(pk2);
        }
    }

    private void setRandomTool() {
        if (Utils.rand(1, 10) == 5) {
            if (Utils.rand(1, 3) == 1) {
                this.tool = Item.get(Item.IRON_SWORD, Utils.rand(200, 246), 1);
                this.setDamage(new float[]{0, 4, 6, 8});
            } else {
                this.tool = Item.get(Item.IRON_SHOVEL, Utils.rand(200, 246), 1);
                this.setDamage(new float[]{0, 3, 4, 5});
            }
        }
    }

    @Override
    public boolean attack(EntityDamageEvent ev) {
        super.attack(ev);

        if (!ev.isCancelled() && ev.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            CreatureSpawnEvent cse = new CreatureSpawnEvent(EntityDrowned.NETWORK_ID, this, new CompoundTag(), CreatureSpawnEvent.SpawnReason.DROWNED);
            level.getServer().getPluginManager().callEvent(cse);

            if (cse.isCancelled()) {
                this.close();
                return true;
            }

            Entity ent = Entity.createEntity("Drowned", this);
            if (ent != null) {
                this.close();
                ent.spawnToAll();
            } else {
                this.close();
            }
        }

        return true;
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        if (tool != null) {
            this.namedTag.put("Item", NBTIO.putItemHelper(tool));
        }
    }
}
