package xaero.pac.common.server.claims.protection;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.Projectile;
import xaero.pac.OpenPartiesAndClaims;
import xaero.pac.common.reflect.Reflection;
import xaero.pac.common.server.core.ServerCore;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class ChunkProtectionEntityHelper {

	private static EntityDataAccessor<Optional<UUID>> FOX_TRUSTED_UUID_SECONDARY;
	private static EntityDataAccessor<Optional<UUID>> FOX_TRUSTED_UUID_MAIN;

	static {
		Field foxTrustSecondaryField = null;
		Field foxTrustMainField = null;
		try {
			foxTrustSecondaryField = Reflection.getFieldReflection(Fox.class, "DATA_TRUSTED_ID_0", "f_28439_", "field_17951", "Lnet/minecraft/class_2940;");//DATA_TRUSTED_ID_0
		} catch(Exception e){
			OpenPartiesAndClaims.LOGGER.error("suppressed exception", e);
		}
		try {
			foxTrustMainField = Reflection.getFieldReflection(Fox.class, "DATA_TRUSTED_ID_1", "f_28440_", "field_17952", "Lnet/minecraft/class_2940;");//DATA_TRUSTED_ID_1
		} catch(Exception e){
			OpenPartiesAndClaims.LOGGER.error("suppressed exception", e);
		}
		if(foxTrustSecondaryField != null)
			try {
				foxTrustSecondaryField.setAccessible(true);
				FOX_TRUSTED_UUID_SECONDARY = (EntityDataAccessor<Optional<UUID>>) foxTrustSecondaryField.get(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		if(foxTrustMainField != null)
			try {
				foxTrustMainField.setAccessible(true);
				FOX_TRUSTED_UUID_MAIN = (EntityDataAccessor<Optional<UUID>>) foxTrustMainField.get(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	boolean hostileException(Entity e) {
		if(e instanceof Piglin)
			return ((Piglin)e).isBaby();
		return false;
	}

	boolean isHostile(Entity e) {
		return (e.level().getDifficulty() != Difficulty.PEACEFUL && !hostileException(e) && (e instanceof Enemy || e.getSoundSource() == SoundSource.HOSTILE));
	}

	boolean isOwned(Entity e, UUID potentialOwnerId) {
		if(potentialOwnerId == null)
			return false;
		UUID owner = getOwnerId(e);
		if(potentialOwnerId.equals(owner))
			return true;
		if(e instanceof Fox fox)
			return FOX_TRUSTED_UUID_MAIN != null && potentialOwnerId.equals(fox.getEntityData().get(FOX_TRUSTED_UUID_MAIN).orElse(null));
		return false;
	}

	UUID getOwnerId(Entity e){
		if(e instanceof ItemEntity){
			UUID ownerId = ServerCore.getItemEntityOwner((ItemEntity) e);
			return ownerId == null ? ServerCore.getItemEntityThrower((ItemEntity) e) : ownerId;
		}
		if(e instanceof TamableAnimal tameable)
			return tameable.isTame() ? tameable.getOwnerUUID() : null;
		if(e instanceof OwnableEntity ownable)
			return ownable.getOwnerUUID();
		if(e instanceof AbstractHorse horse)
			return horse.isTamed() ? horse.getOwnerUUID() : null;
		if(e instanceof Fox fox)
			return FOX_TRUSTED_UUID_SECONDARY != null ? fox.getEntityData().get(FOX_TRUSTED_UUID_SECONDARY).orElse(null) : null;
		Entity ownerEntity = null;
		if(e instanceof Projectile)
			ownerEntity = ((Projectile) e).getOwner();
		else if(e instanceof Vex)
			ownerEntity = ((Vex) e).getOwner();
		else if(e instanceof EvokerFangs)
			ownerEntity = ((EvokerFangs) e).getOwner();
		return ownerEntity == null ? null : ownerEntity.getUUID();
	}

}
