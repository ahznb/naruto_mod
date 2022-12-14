package net.narutomod;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.world.WorldEvent;
//import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.scoreboard.Team;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

import net.narutomod.entity.EntityNinjaMob;
import net.narutomod.procedure.ProcedureSync;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.item.ItemEightGates;
import net.narutomod.item.ItemJutsu;

import java.util.UUID;
import java.util.List;
import com.google.common.collect.Lists;

@ElementsNarutomodMod.ModElement.Tag
public class PlayerTracker extends ElementsNarutomodMod.ModElement {
	private static final String BATTLEXP = NarutomodModVariables.BATTLEXP;
	private static final String KEEPXP_RULE = "keepNinjaXp";

	public PlayerTracker(ElementsNarutomodMod instance) {
		super(instance, 181);
	}

	public static boolean isNinja(EntityPlayer player) {
		return player.getEntityData().getDouble(BATTLEXP) > 0.0d;
	}

	public static double getBattleXp(EntityPlayer player) {
		return player.getEntityData().getDouble(BATTLEXP);
	}

	public static double getNinjaLevel(EntityPlayer player) {
		return MathHelper.sqrt(getBattleXp(player));
	}

	public static void addBattleXp(EntityPlayerMP entity, double xp) {
		entity.getEntityData().setDouble(BATTLEXP, Math.min(getBattleXp(entity) + xp, 100000.0d));
		sendBattleXPToSelf(entity);
		entity.sendStatusMessage(new TextComponentString(
		 net.minecraft.util.text.translation.I18n.translateToLocal("chattext.ninjaexperience")+
		 String.format("%.1f", getBattleXp(entity))), true);
	}

	public static void logBattleExp(EntityPlayer entity, double xp) {
		if (entity instanceof EntityPlayerMP 
		 && ProcedureUtils.advancementAchieved((EntityPlayerMP)entity, "narutomod:ninjaachievement")) {
			addBattleXp((EntityPlayerMP)entity, xp);
			ItemEightGates.logBattleXP(entity);
			ItemJutsu.logBattleXP(entity);
		}
	}

	private static void sendBattleXPToSelf(EntityPlayerMP player) {
		ProcedureSync.EntityNBTTag.sendToSelf(player, BATTLEXP, getBattleXp(player));
	}

	public static class Deaths {
		private static final List<Deaths> deadPlayers = Lists.newArrayList();
		private final UUID playerId;
		private final double x;
		private final double y;
		private final double z;
		private final long time;
		private final Team team;
		private final double lastXp;
		
		private Deaths(EntityPlayer player) {
			this.playerId = player.getUniqueID();
			this.x = player.posX;
			this.y = player.posY;
			this.z = player.posZ;
			this.time = player.world.getTotalWorldTime();
			this.team = player.getTeam();
			this.lastXp = getBattleXp(player);
		}

		public static void log(EntityPlayer entity) {
			if (deadPlayers.contains(entity)) {
				deadPlayers.remove(entity);
			}
			deadPlayers.add(new Deaths(entity));
			if (!entity.world.getGameRules().getBoolean(KEEPXP_RULE)) {
				entity.getEntityData().setDouble(BATTLEXP, 0.0D);
				if (entity instanceof EntityPlayerMP) {
					sendBattleXPToSelf((EntityPlayerMP)entity);
				}
			}
		}
	
		public static void clear() {
			deadPlayers.clear();
		}
	
		public static Deaths mostRecent() {
			if (!deadPlayers.isEmpty())
				return deadPlayers.get(deadPlayers.size());
			return null;
		}
	
		public static boolean hasRecentNearby(EntityPlayer player, double distance, double timeframe) {
			return hasRecentNearby(player, distance, timeframe, true);
		}

		public static boolean hasRecentNearby(EntityPlayer player, double distance, double timeframe, boolean checkTeam) {
			if (deadPlayers.isEmpty())
				return false;
			for (int i = deadPlayers.size(); --i >= 0;) {
				Deaths deadguy = deadPlayers.get(i);
				if (!deadguy.playerId.equals(player.getUniqueID())) {
					double d0 = deadguy.x - player.posX;
					double d1 = deadguy.y - player.posY;
					double d2 = deadguy.z - player.posZ;
					double d3 = d0 * d0 + d1 * d1 + d2 * d2;
					if (d3 < distance * distance && player.world.getTotalWorldTime() - deadguy.time <= timeframe
					 && (!checkTeam || player.isOnScoreboardTeam(deadguy.team))) {
						return true;
					}
				}
			}
			return false;
		}
	
		public static boolean hasRecentMatching(EntityPlayer player, double timeframe) {
			if (!deadPlayers.isEmpty()) {
				for (int i = deadPlayers.size(); --i >= 0;) {
					Deaths deadguy = deadPlayers.get(i);
					if (deadguy.playerId.equals(player.getUniqueID()))
						return true;
				}
			}
			return false;
		}
	
		public static long mostRecentTime(EntityPlayer player) {
			if (!deadPlayers.isEmpty()) {
				for (int i = deadPlayers.size(); --i >= 0;) {
					Deaths deadguy = deadPlayers.get(i);
					if (deadguy.playerId.equals(player.getUniqueID()))
						return deadguy.time;
				}
			}
			return 0L;
		}

		public static double getXpBeforeDeath(EntityPlayer player) {
			if (!deadPlayers.isEmpty()) {
				for (int i = deadPlayers.size(); --i >= 0;) {
					Deaths deadguy = deadPlayers.get(i);
					if (deadguy.playerId.equals(player.getUniqueID()))
						return deadguy.lastXp;
				}
			}
			return 0.0d;
		}
	}

	public class PlayerHook {
		@SubscribeEvent
		public void onTick(TickEvent.PlayerTickEvent event) {
			if (event.phase == TickEvent.Phase.END && event.player instanceof EntityPlayerMP) {
				if (event.player.ticksExisted < 5) {
					sendBattleXPToSelf((EntityPlayerMP)event.player);
					event.player.setHealth(event.player.getHealth());
				}
				double d = getBattleXp(event.player) * 0.005d;
				if (d > 0d) {
					IAttributeInstance maxHealthAttr = event.player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
					AttributeModifier attr = maxHealthAttr.getModifier(EntityNinjaMob.NINJA_HEALTH);
					if (attr == null || (int)attr.getAmount() / 2 != (int)d / 2) {
						if (attr != null) {
							maxHealthAttr.removeModifier(EntityNinjaMob.NINJA_HEALTH);
						}
						maxHealthAttr.applyModifier(new AttributeModifier(EntityNinjaMob.NINJA_HEALTH, "ninja.maxhealth", d, 0));
						event.player.setHealth(event.player.getHealth() + 0.1f);
					}
				}
			}
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void onDeath(LivingDeathEvent event) {
			Entity entity = event.getEntityLiving();
			if (entity instanceof EntityPlayerMP) {
				Deaths.log((EntityPlayer) entity);
			}
		}

		@SubscribeEvent
		public void onDamaged(LivingDamageEvent event) {
			Entity targetEntity = event.getEntity();
			Entity sourceEntity = event.getSource().getTrueSource();
			float amount = event.getAmount();
			if (!targetEntity.equals(sourceEntity) && amount > 0f) {
				if (targetEntity instanceof EntityPlayer && amount < ((EntityPlayer)targetEntity).getHealth()) {
					//logBattleExp((EntityPlayer)targetEntity, 1d);
					double bxp = getBattleXp((EntityPlayer)targetEntity);
					logBattleExp((EntityPlayer)targetEntity, bxp < 1d ? 1d : (amount / MathHelper.sqrt(MathHelper.sqrt(bxp))));
				}
				if (sourceEntity instanceof EntityPlayer) {
					//logBattleExp((EntityPlayer)sourceEntity, 1d);
					double xp = 1d;
					if (targetEntity instanceof EntityLivingBase) {
						EntityLivingBase target = (EntityLivingBase)targetEntity;
						int resistance = target.isPotionActive(MobEffects.RESISTANCE) 
						 ? target.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() : 1;
						double x = MathHelper.sqrt(target.getMaxHealth() * ProcedureUtils.getModifiedAttackDamage(target)
						 * MathHelper.sqrt(ProcedureUtils.getArmorValue(target)+1d)) * resistance;
						xp = Math.min(x * Math.min(amount / target.getMaxHealth(), 1f) * 0.5d, 50d);
//System.out.println(">>> target:"+target.getName()+", x="+x+", amount="+amount+", maxhp="+target.getMaxHealth()+", xp="+xp);
					}
					if (xp > 0d) {
						logBattleExp((EntityPlayer)sourceEntity, xp);
					}
				}
			}
		}

		@SubscribeEvent
		public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
			if (!event.player.world.isRemote) {
				event.player.setAlwaysRenderNameTag(true);
				sendBattleXPToSelf((EntityPlayerMP)event.player);
			}
		}

		@SubscribeEvent
		public void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
			if (event.player instanceof EntityPlayerMP) {
				sendBattleXPToSelf((EntityPlayerMP)event.player);
			}
		}

		@SubscribeEvent
		public void onClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
			//if (!event.isWasDeath()) {
				EntityPlayer newPlayer = event.getEntityPlayer();
				newPlayer.getEntityData().setDouble(BATTLEXP, getBattleXp(event.getOriginal()));
				sendBattleXPToSelf((EntityPlayerMP)newPlayer);
			//}
		}

		@SubscribeEvent
		public void onWorldLoad(WorldEvent.Load event) {
			World world = event.getWorld();
			if (!world.isRemote && !world.getGameRules().hasRule(KEEPXP_RULE)) {
				world.getGameRules().addGameRule(KEEPXP_RULE, "false", net.minecraft.world.GameRules.ValueType.BOOLEAN_VALUE);
			}
		}
	}
	
	@Override
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new PlayerHook());
	}
}
