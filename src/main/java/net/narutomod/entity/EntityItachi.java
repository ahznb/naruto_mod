
package net.narutomod.entity;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.common.DungeonHooks;

import net.minecraft.init.MobEffects;
import net.minecraft.init.Biomes;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.World;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumHand;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.PotionEffect;

import net.narutomod.potion.PotionAmaterasuFlame;
import net.narutomod.potion.PotionParalysis;
import net.narutomod.entity.EntitySusanooClothed;
import net.narutomod.entity.EntityCrow;
import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.procedure.ProcedureBasicNinjaSkills;
import net.narutomod.procedure.ProcedureSync;
import net.narutomod.item.ItemSharingan;
import net.narutomod.item.ItemMangekyoSharingan;
import net.narutomod.item.ItemDojutsu;
import net.narutomod.item.ItemKunai;
import net.narutomod.item.ItemKaton;
import net.narutomod.item.ItemAkatsukiRobe;
import net.narutomod.ElementsNarutomodMod;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;

@ElementsNarutomodMod.ModElement.Tag
public class EntityItachi extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 117;
	public static final int ENTITYID_RANGED = 118;

	public EntityItachi(ElementsNarutomodMod instance) {
		super(instance, 336);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityCustom.class)
			.id(new ResourceLocation("narutomod", "itachi"), ENTITYID).name("itachi").tracker(64, 3, true).egg(-16777216, -65485).build());
		//elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityArrowCustom.class)
		//	.id(new ResourceLocation("narutomod", "entitybulletitachi"), ENTITYID_RANGED).name("entitybulletitachi").tracker(64, 1, true)
		//	.build());
	}

	@Override
	public void init(FMLInitializationEvent event) {
		Biome[] spawnBiomes = {
			Biomes.FOREST, Biomes.TAIGA, Biomes.SWAMPLAND, Biomes.RIVER, Biomes.FOREST_HILLS,
			Biomes.TAIGA_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS, Biomes.BIRCH_FOREST,
			Biomes.BIRCH_FOREST_HILLS, Biomes.ROOFED_FOREST, Biomes.SAVANNA, Biomes.EXTREME_HILLS
		};
		EntityRegistry.addSpawn(EntityCustom.class, 1, 1, 1, EnumCreatureType.MONSTER, spawnBiomes);
		DungeonHooks.addDungeonMob(new ResourceLocation("narutomod:itachi"), 180);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, renderManager -> new RenderCustom(renderManager, new ModelBiped()));
		/*RenderingRegistry.registerEntityRenderingHandler(EntityArrowCustom.class, renderManager -> {
			return new RenderSnowball<EntityArrowCustom>(renderManager, null, Minecraft.getMinecraft().getRenderItem()) {
				public ItemStack getStackToRender(EntityArrowCustom entity) {
					return new ItemStack(ItemShuriken.block, (int) (1));
				}
			};
		});*/
	}

	public static class EntityCustom extends EntityNinjaMob.Base implements IMob, IRangedAttackMob {
		private final double GENJUTSU_CHAKRA = 100d;
		private final double FIREBALL_CHAKRA = 50d;
		private final double AMATERASU_CHAKRA = 50d;
		private final double SUSANOO_CHAKRA = 300d;
		private final double INVIS_CHAKRA = 20d;
		private boolean isReal;
		private int lookedAtTime;
		private final int genjutsuDuration = 200;
		private int lastGenjutsuTime;
		private int lastInvisTime;
		private int lastSusanooTime;
		private EntitySusanooClothed.EntityCustom susanooEntity;

		public EntityCustom(World world) {
			super(world, 80, 4000d);
			//this.setItemToInventory(kunaiStack);
			this.isImmuneToFire = true;
			this.setIsReal(this.rand.nextInt(5) == 0);
		}

		@Override
		public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
			ItemStack stack = new ItemStack(ItemMangekyoSharingan.helmet, 1);
			((ItemDojutsu.Base)stack.getItem()).setOwner(stack, this);
			this.setItemStackToSlot(EntityEquipmentSlot.HEAD, stack);
			this.setItemToInventory(new ItemStack(ItemKunai.block), 0);
			this.setItemToInventory(new ItemStack(ItemAkatsukiRobe.helmet), 1);
			return super.onInitialSpawn(difficulty, livingdata);
		}

		public void setIsReal(boolean real) {
			this.isReal = real;
			this.inventoryArmorDropChances[3] = real ? 1.0f : 0.0f;
		}

		@Override
		protected void initEntityAI() {
			super.initEntityAI();
			this.tasks.addTask(0, new EntityAISwimming(this));
			this.tasks.addTask(1, new EntityNinjaMob.AIAttackRangedTactical(this, 1.0D, 50, 16.0F) {
				@Override
				public boolean shouldExecute() {
					return super.shouldExecute() 
					 && EntityCustom.this.getAttackTarget().getDistance(EntityCustom.this) 
					 > (EntityCustom.this.isSusanooActive() ? ProcedureUtils.getReachDistance(EntityCustom.this.susanooEntity) : 4d);
				}
			});
			this.tasks.addTask(2, new EntityNinjaMob.AILeapAtTarget(this, 1.0F) {
				@Override
				public boolean shouldExecute() {
					return super.shouldExecute() && !EntityCustom.this.isRiding()
					 && EntityCustom.this.getAttackTarget().posY - EntityCustom.this.posY > 3d;
				}
			});
			this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0d, true) {
				@Override
				public boolean shouldExecute() {
					return super.shouldExecute() 
					 && EntityCustom.this.getAttackTarget().getDistance(EntityCustom.this) 
					 <= (EntityCustom.this.isSusanooActive() ? ProcedureUtils.getReachDistance(EntityCustom.this.susanooEntity) : 4d);
				}
				@Override
				protected double getAttackReachSqr(EntityLivingBase attackTarget) {
					return EntityCustom.this.isSusanooActive() ? ProcedureUtils.getReachDistanceSq(EntityCustom.this.susanooEntity)
					 : super.getAttackReachSqr(attackTarget);
				}
				@Override
				protected void checkAndPerformAttack(EntityLivingBase p_190102_1_, double p_190102_2_) {
					if (EntityCustom.this.isSusanooActive()) {
						if (p_190102_2_ <= this.getAttackReachSqr(p_190102_1_) && this.attackTick <= 0) {
							this.attackTick = 20;
							EntityCustom.this.susanooEntity.swingArm(EnumHand.MAIN_HAND);
							EntityCustom.this.susanooEntity.attackEntityAsMob(p_190102_1_);
						}
					} else {
						super.checkAndPerformAttack(p_190102_1_, p_190102_2_);
					}
				}
			});
			this.tasks.addTask(4, new EntityAIWander(this, 0.3));
			this.tasks.addTask(5, new EntityAILookIdle(this));
			this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
			this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 10, true, false, 
				new Predicate<EntityPlayer>() {
					public boolean apply(@Nullable EntityPlayer p_apply_1_) {
						return p_apply_1_ != null 
						 && (ItemSharingan.wearingAny(p_apply_1_) || EntityBijuManager.isJinchuriki(p_apply_1_));
					}
				}));
		}

		@Override
		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(100D);
			this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
			this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10D);
		}

		@Override
		protected Item getDropItem() {
			return ItemKunai.block;
		}

		@Override
		public SoundEvent getDeathSound() {
			return (SoundEvent) SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.illusion_illager.death"));
		}

		@Override
		public boolean isOnSameTeam(Entity entityIn) {
			return super.isOnSameTeam(entityIn) || EntityNinjaMob.TeamItachi.contains(entityIn.getClass());
		}

		private boolean isSusanooActive() {
			return this.susanooEntity != null && this.susanooEntity.isEntityAlive();
		}

		@Override
		public boolean attackEntityFrom(DamageSource source, float amount) {
			if (source == DamageSource.FALL) {
				return false;
			}
			if (!this.world.isRemote) {
				Entity entity1 = source.getTrueSource();
				if (this.rand.nextInt(3) <= 1) {
					this.setPositionAndUpdate(this.posX + (this.rand.nextDouble() - 0.5) * 2, this.posY, this.posZ + (this.rand.nextDouble() - 0.5) * 2);
					if (entity1 instanceof EntityLivingBase) {
						this.setRevengeTarget((EntityLivingBase)entity1);
					}
					return false;
				} else if (this.isReal && this.getHealth() > 0 && this.getHealth() - amount <= this.getMaxHealth() / 3 
				 && !this.isRiding() && this.ticksExisted > this.lastSusanooTime + 600 && this.getChakra() >= SUSANOO_CHAKRA) {
					this.susanooEntity = new EntitySusanooClothed.EntityCustom(this, false);
					this.susanooEntity.setLifeSpan(600);
					this.world.spawnEntity(this.susanooEntity);
					this.startRiding(this.susanooEntity);
					this.consumeChakra(SUSANOO_CHAKRA);
					this.lastSusanooTime = this.ticksExisted;
					this.susanooEntity.attackEntityFrom(source, amount);
					if (entity1 instanceof EntityLivingBase) {
						this.setRevengeTarget((EntityLivingBase)entity1);
					}
					return false;
				} else if (this.ticksExisted > this.lastInvisTime + 200 && this.getChakra() >= INVIS_CHAKRA) {
					this.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 200, 1, false, false));
					for (int i = 0; i < 100; i++) {
						Entity entityToSpawn = new EntityCrow.EntityCustom(this.world);
						entityToSpawn.setLocationAndAngles(this.posX, this.posY + 1.4, this.posZ, this.rand.nextFloat() * 360F, 0.0F);
						this.world.spawnEntity(entityToSpawn);
					}
					this.setPositionAndUpdate(this.posX + (this.rand.nextDouble() - 0.5) * 6, this.posY + 1, 
					  this.posZ + (this.rand.nextDouble() - 0.5) * 6);
					this.consumeChakra(INVIS_CHAKRA);
					this.lastInvisTime = this.ticksExisted;
				}
			}
			return super.attackEntityFrom(source, amount);
		}

		@Override
		protected void updateAITasks() {
			super.updateAITasks();
			EntityLivingBase target = this.getAttackTarget();
			if (target != null && target.isEntityAlive()) {
				if (this.lookedAtTime >= 5 && this.ticksExisted > this.lastGenjutsuTime + this.genjutsuDuration 
				 && this.consumeChakra(GENJUTSU_CHAKRA)) {
					if (target instanceof EntityPlayerMP) {
						ProcedureSync.MobAppearanceParticle.send((EntityPlayerMP)target, ENTITYID);
					}
					this.world.playSound(null, target.posX, target.posY, target.posZ,
					  (SoundEvent) SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:genjutsu")), SoundCategory.NEUTRAL, 1f, 1f);
					if (!this.world.isRemote) {
						target.addPotionEffect(new PotionEffect(PotionParalysis.potion, this.genjutsuDuration, 1, false, false));
						target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, this.genjutsuDuration+40, 0, false, true));
						target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 60, 0, false, true));
					}
					this.lastGenjutsuTime = this.ticksExisted;
					this.lookedAtTime = 0;
				}
				if (this.equals(ProcedureUtils.objectEntityLookingAt(target, 24d).entityHit)) {
					++this.lookedAtTime;
				} else {
				 	this.lookedAtTime = 0;
				}
			} else if (this.peacefulTicks > 200) {
				this.setAttackTarget(target = null);
			}
			if ((this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == ItemAkatsukiRobe.helmet) != (target == null)) {
				this.swapWithInventory(EntityEquipmentSlot.HEAD, 1);
			}
		}

		@Override
		public void onEntityUpdate() {
			super.onEntityUpdate();
			{
				java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
				$_dependencies.put("entity", this);
				$_dependencies.put("world", this.world);
				ProcedureBasicNinjaSkills.executeProcedure($_dependencies);
			}
		}

		@Override
		public boolean getCanSpawnHere() {
			return super.getCanSpawnHere() 
			 && this.world.getEntitiesWithinAABB(EntityCustom.class, this.getEntityBoundingBox().grow(128.0D)).isEmpty()
			 && this.rand.nextInt(5) == 0;
		}

		@Override
		public void setSwingingArms(boolean swingingArms) {
		}

		@Override
		public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
			int chance = this.rand.nextInt(12);
			if (this.getRidingEntity() instanceof EntitySusanooClothed.EntityCustom) {
				if (chance < 5) {
					((EntitySusanooClothed.EntityCustom)this.getRidingEntity()).attackEntityWithRangedAttack(target, distanceFactor);
				}
			} else {
				if (chance == 0 && distanceFactor > 0.3333f && this.consumeChakra(AMATERASU_CHAKRA)) {
					this.world.playSound(null, target.posX, target.posY, target.posZ, (SoundEvent) SoundEvent.REGISTRY
					  .getObject(new ResourceLocation("narutomod:sharingansfx")), SoundCategory.NEUTRAL, 1f, 1f);
					target.addPotionEffect(new PotionEffect(PotionAmaterasuFlame.potion, 1200, 1, false, false));
				} else if (chance <= 2 && distanceFactor >= 0.5333f && this.consumeChakra(FIREBALL_CHAKRA)) {
					double d0 = target.posX - this.posX;
					double d1 = target.posY - (this.posY + this.getEyeHeight());
					double d2 = target.posZ - this.posZ;
					new ItemKaton.EntityBigFireball.Jutsu().createJutsu(this, d0, d1, d2, 5f);
				} else if (!this.isRiding()) {
					ItemKunai.EntityArrowCustom kunai = new ItemKunai.EntityArrowCustom(this.world, this);
					Vec3d vec = target.getPositionEyes(1f).subtract(kunai.getPositionVector());
					kunai.shoot(vec.x, vec.y + MathHelper.sqrt(vec.x * vec.x + vec.z * vec.z) * 0.2d, vec.z, 1f, 0);
					kunai.setDamage(5);
					kunai.setKnockbackStrength(1);
					this.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 1, 1f / (this.rand.nextFloat() * 0.5f + 1f) + 0.25f);
					this.world.spawnEntity(kunai);
				}
			}
		}

		private final BossInfoServer bossInfo = new BossInfoServer(this.getDisplayName(), BossInfo.Color.BLUE, BossInfo.Overlay.PROGRESS);

		@Override
		public void addTrackingPlayer(EntityPlayerMP player) {
			super.addTrackingPlayer(player);
			this.bossInfo.addPlayer(player);
		}

		@Override
		public void removeTrackingPlayer(EntityPlayerMP player) {
			super.removeTrackingPlayer(player);
			this.bossInfo.removePlayer(player);
		}

		private void trackAttackedPlayers() {
			Entity entity = this.getAttackingEntity();
			if (entity instanceof EntityPlayerMP || (entity = this.getAttackTarget()) instanceof EntityPlayerMP) {
				this.bossInfo.addPlayer((EntityPlayerMP)entity);
			} else {
				java.util.List<EntityPlayerMP> list = new java.util.ArrayList<EntityPlayerMP>();
				for (EntityPlayerMP entityplayermp : this.bossInfo.getPlayers())
					list.add(entityplayermp);
				for (EntityPlayerMP entityplayermp : list)
					this.bossInfo.removePlayer(entityplayermp);
			}
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			this.trackAttackedPlayers();
			this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
		}

		@Override
		protected boolean canSeeInvisible(Entity entityIn) {
			return !entityIn.isInvisible() || this.getDistanceSq(entityIn) <= 400d;
		}
	}

	@SideOnly(Side.CLIENT)
	public class RenderCustom extends EntityNinjaMob.RenderBase<EntityCustom> {
		private final ResourceLocation TEXTURE = new ResourceLocation("narutomod:textures/itachi.png");

		public RenderCustom(RenderManager renderManagerIn, ModelBiped modelIn) {
			super(renderManagerIn, modelIn);
			//this.addLayer(new net.minecraft.client.renderer.entity.layers.LayerCustomHead(modelIn.bipedHead));
			//this.addLayer(new net.minecraft.client.renderer.entity.layers.LayerHeldItem(this));
			//this.addLayer(new EntityClone.ClientRLM().new BipedArmorLayer(this));
		}

		@Override
		protected void renderLayers(EntityCustom entity, float f0, float f1, float f2, float f3, float f4, float f5, float f6) {
			if (!entity.isInvisible()) {
				super.renderLayers(entity, f0, f1, f2, f3, f4, f5, f6);
			}
		}

	    @Override
		protected void preRenderCallback(EntityCustom entity, float partialTickTime) {
			GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
		}

	    @Override
	    public void transformHeldFull3DItemLayer() {
	        GlStateManager.translate(0.0F, 0.1875F, 0.0F);
	    }

		@Override
		protected ResourceLocation getEntityTexture(EntityCustom entity) {
			return TEXTURE;
		}
	}
}
