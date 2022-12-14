
package net.narutomod.entity;

import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import net.minecraft.world.World;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.Item;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
//import net.minecraft.network.datasync.DataParameter;
//import net.minecraft.network.datasync.EntityDataManager;
//import net.minecraft.network.datasync.DataSerializers;

import net.narutomod.procedure.ProcedureUtils;
import net.narutomod.ElementsNarutomodMod;

import com.google.common.collect.Lists;
import java.util.List;
import com.google.common.base.Predicate;
import javax.annotation.Nullable;

@ElementsNarutomodMod.ModElement.Tag
public class EntityGedoStatue extends ElementsNarutomodMod.ModElement {
	public static final int ENTITYID = 330;
	public static final int ENTITYID_RANGED = 331;
	private static final float MODEL_SCALE = 10.0F;
	private static EntityCustom thisEntity = null;
	private static boolean[] bijuSealed = new boolean[9];

	public EntityGedoStatue(ElementsNarutomodMod instance) {
		super(instance, 683);
	}

	@Override
	public void initElements() {
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityCustom.class)
		 .id(new ResourceLocation("narutomod", "gedo_statue"), ENTITYID).name("gedo_statue")
		 .tracker(128, 3, true).egg(-8621734, -10069692).build());
		elements.entities.add(() -> EntityEntryBuilder.create().entity(EntityPurpleDragon.class)
		 .id(new ResourceLocation("narutomod", "purple_dragon"), ENTITYID_RANGED).name("purple_dragon")
		 .tracker(64, 3, true).build());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityCustom.class, renderManager -> {
			return new RenderLiving(renderManager, new ModelGedoMazo(), 5f) {
				private final ResourceLocation texture = new ResourceLocation("narutomod:textures/gedomazo.png");
				@Override
				protected ResourceLocation getEntityTexture(Entity entity) {
					return texture;
				}
			};
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityPurpleDragon.class, renderManager -> {
			return new RenderDragon(renderManager);
		});
	}

	public static boolean isAddedToWorld(World world) {
		//return world.isRemote ? false : thisEntity != null;
		return thisEntity != null;
	}

	public static EntityCustom getThisEntity() {
		return thisEntity;
	}

	public static EntityCustom getThisEntity(World world) {
		if (thisEntity != null) {
			Entity entity = world.getEntityByID(thisEntity.getEntityId());
			if (entity instanceof EntityCustom)
				return (EntityCustom) entity;
		}
		return null;
	}

	public static class EntityCustom extends EntitySummonAnimal.Base {
		//private static final DataParameter<Integer> AGE = EntityDataManager.<Integer>createKey(EntityCustom.class, DataSerializers.VARINT);
		private EntityPurpleDragon dragonEntity;
		private List<EntityLivingBase> dragonTargetList;

		public EntityCustom(World world) {
			super(world);
			this.setOGSize(0.5f, 1.9f);
			this.experienceValue = 100;
			this.isImmuneToFire = true;
			this.postScaleFixup();
		}

		public EntityCustom(EntityLivingBase summonerIn) {
			super(summonerIn);
			this.setOGSize(0.5f, 1.9f);
			this.experienceValue = 100;
			this.isImmuneToFire = true;
			this.postScaleFixup();
		}

		/*@Override
		protected void entityInit() {
			super.entityInit();
			this.getDataManager().register(AGE, Integer.valueOf(0));
		}

		public int getAge() {
			return ((Integer) this.getDataManager().get(AGE)).intValue();
		}

		protected void setAge(int age) {
			this.getDataManager().set(AGE, Integer.valueOf(age));
		}*/

		@Override
		public float getScale() {
			return MODEL_SCALE;
		}

		@Override
		public SoundEvent getAmbientSound() {
			return SoundEvent.REGISTRY.getObject(new ResourceLocation("narutomod:MonsterGrowl"));
		}

		@Override
		public SoundEvent getHurtSound(DamageSource ds) {
			return null;
		}

		@Override
		public SoundEvent getDeathSound() {
			return null;
		}

		@Override
		protected float getSoundVolume() {
			return 10.0F;
		}

		@Override
		protected void applyEntityAttributes() {
			super.applyEntityAttributes();
			this.getAttributeMap().registerAttribute(EntityPlayer.REACH_DISTANCE);
			this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(100D);
			this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.8D);
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10000D);
			this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(100D);
			this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(24.0D);
			this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
			this.getEntityAttribute(EntityPlayer.REACH_DISTANCE).setBaseValue(28.0D);
		}

		@Override
		public void setDead() {
			super.setDead();
			if (!this.world.isRemote) {
				thisEntity = null;
			}
		}

		@Override
		public void onAddedToWorld() {
			super.onAddedToWorld();
			if (!this.world.isRemote) {
				thisEntity = this;
			}
		}

		@Override
		protected void initEntityAI() {
			super.initEntityAI();
			this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.2D, true) {
				@Override
				protected double getAttackReachSqr(EntityLivingBase attackTarget) {
					double d = this.attacker.width * 2 + attackTarget.width;
					return d * d;
				}
			});
		}

		@Override
		protected void dontWander(boolean set) {
		}

		@Override
		protected void updateAITasks() {
			super.updateAITasks();
			if (this.rand.nextInt(100) == 0) {
				if (this.dragonTargetList == null || this.dragonTargetList.size() < 5) {
					this.dragonTargetList = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(20d, 7d, 20d), new Predicate<EntityLivingBase>() {
						@Override
						public boolean apply(@Nullable EntityLivingBase p_apply_1_) {
							EntityLivingBase summoner = EntityCustom.this.getSummoner();
							boolean flag = summoner != null ? p_apply_1_.isOnSameTeam(summoner) : false;
							return p_apply_1_ != null && !p_apply_1_.equals(EntityCustom.this) && !flag
							 && p_apply_1_.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null
							 && EntitySelectors.CAN_AI_TARGET.apply(p_apply_1_);
						}
					});
				}
				if (this.dragonTargetList.size() >= 5) {
					this.dragonTargetList.sort(new ProcedureUtils.EntitySorter(this));
					this.dragonEntity = new EntityPurpleDragon(this);
					this.world.spawnEntity(this.dragonEntity);
					this.setNoAI(true);
				}
			}
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			if (this.dragonEntity != null && !this.dragonEntity.isEntityAlive() && this.isAIDisabled()) {
				this.setNoAI(false);
			}
		}
	}

	public static class EntityPurpleDragon extends EntityScalableProjectile.Base {
		private final int wait = 40;
		private float prevHeadYaw;
		private float prevHeadPitch;
		private Vec3d lastVec;
		private List<EntityLivingBase> targetList;
		private final List<ProcedureUtils.Vec2f> partRot = Lists.newArrayList(
			new ProcedureUtils.Vec2f(0.0f, 0.0f)
		);

		public EntityPurpleDragon(World w) {
			super(w);
			this.setOGSize(1.25F, 1.25F);
			this.setEntityScale(1.5f);
		}

		public EntityPurpleDragon(EntityCustom shooter) {
			super(shooter);
			this.setOGSize(1.25F, 1.25F);
			this.setEntityScale(1.5f);
			this.targetList = shooter.dragonTargetList;
			this.setLocationAndAngles(shooter.posX, shooter.posY + 1.4375d * MODEL_SCALE, shooter.posZ,
			 shooter.rotationYawHead, shooter.rotationPitch);
			this.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation(("narutomod:dragon_roar"))),
			 100f, this.rand.nextFloat() * 0.4f + 0.6f);
		}

		private void setWaitPosition() {
			if (this.shootingEntity != null) {
				Vec3d vec = Vec3d.fromPitchYaw(this.shootingEntity.rotationPitch, this.shootingEntity.rotationYawHead).scale(0.01d);
				this.motionX += vec.x;
				this.motionY += vec.y;
				this.motionZ += vec.z;
			}
		}

		private void shoot(double x, double y, double z) {
			this.shoot(x, y, z, 0.95f, 0f, false);
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			this.updateInFlightRotations();
			if (!this.world.isRemote
			 && (this.ticksAlive > 100 + this.wait || this.shootingEntity == null || !this.shootingEntity.isEntityAlive())) {
				this.setDead();
			} else {
				if (this.ticksAlive <= this.wait) {
					if (this.lastVec == null) {
						this.lastVec = this.getPositionVector();
					}
					this.setWaitPosition();
				} else if (this.targetList != null && !this.targetList.isEmpty()) {
					Vec3d vec = this.targetList.get(0).getPositionVector().subtract(this.getPositionVector());
					this.shoot(vec.x, vec.y, vec.z);
				}
				this.updateSegments();
				this.prevHeadYaw = this.rotationYaw;
				this.prevHeadPitch = this.rotationPitch;
			}
		}

		public void updateSegments() {
			float slength = this.getEntityScale() * 11.0F * 0.0625F;
			ProcedureUtils.Vec2f vec = new ProcedureUtils.Vec2f(this.rotationYaw, this.rotationPitch)
			 .subtract(this.prevHeadYaw, this.prevHeadPitch);
			Vec3d vec4 = this.getPositionVector().subtract(this.lastVec);
			double d4 = vec4.lengthVector();
			if (d4 >= slength) {
				this.partRot.add(0, vec);
				int i = 1;
				for ( ; i < (int)(d4 / slength); i++) {
					this.partRot.add(0, ProcedureUtils.Vec2f.ZERO);
				}
				this.lastVec = vec4.normalize().scale(slength * i).add(this.lastVec);
			} else {
				this.partRot.set(0, this.partRot.get(0).add(vec));
			}
		}

		@Override
		public void renderParticles() {
		}

		@Override
		protected void onImpact(RayTraceResult result) {
			if (result.entityHit != null && result.entityHit.equals(this.shootingEntity))
				return;
			if (!this.world.isRemote && result.entityHit instanceof EntityLivingBase) {
				result.entityHit.onKillCommand();
				if (this.targetList.contains(result.entityHit)) {
					this.targetList.remove(result.entityHit);
					this.targetList.sort(new ProcedureUtils.EntitySorter(this));
				}
			}
			//this.haltMotion();
			//this.setDead();
		}

		@Override
		protected void checkOnGround() {
		}

		@Override
		public boolean isImmuneToExplosions() {
			return true;
		}

		/*public static class Jutsu implements ItemJutsu.IJutsuCallback {
			@Override
			public boolean createJutsu(ItemStack stack, EntityLivingBase entity, float power) {
				if (power >= 1.0f) {
				 	entity.world.spawnEntity(new EC(entity));
				 	if (entity instanceof EntityPlayer && !((EntityPlayer)entity).isCreative()) {
				 		entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 300, 0, false, false));
						ItemJutsu.Base item = (ItemJutsu.Base)stack.getItem();
						item.setCurrentJutsuCooldown(stack, (long)(3600f * item.getModifier(stack, entity)));
				 	}
					return true;
				}
				return false;
			}
		}*/
	}

	@SideOnly(Side.CLIENT)
	public class RenderDragon extends Render<EntityPurpleDragon> {
		private final ResourceLocation texture = new ResourceLocation("narutomod:textures/dragon_purple.png");
		private final ModelDragonHead model = new ModelDragonHead();

		public RenderDragon(RenderManager renderManager) {
			super(renderManager);
			//this.model = new ModelDragonHead();
			this.shadowSize = 0.0F;
		}

		@Override
		public boolean shouldRender(EntityPurpleDragon livingEntity, net.minecraft.client.renderer.culling.ICamera camera,
		 double camX, double camY, double camZ) {
			return true;
		}

		@Override
		public void doRender(EntityPurpleDragon entity, double x, double y, double z, float yaw, float pt) {
			float age = (float)entity.ticksExisted + pt;
			float f0 = Math.min(age / (float)entity.wait, 1.0f);
			float f1 = -entity.prevRotationYaw - MathHelper.wrapDegrees(entity.rotationYaw - entity.prevRotationYaw) * pt;
			float f2 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * pt;
			boolean flag = entity.ticksAlive <= entity.wait;
			float scale = entity.getEntityScale();
			this.model.setRotationAngles(0f, 0f, age, 0f, 0f, 0.0625F, entity);
			this.bindEntityTexture(entity);
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) x, (float) y + scale, (float) z);
			GlStateManager.rotate(f1, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(f2 - 180F, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(scale, scale, scale);
			GlStateManager.enableBlend();
			GlStateManager.disableCull();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
			GlStateManager.color(1.0F, 1.0F, 1.0F, f0 * 0.4F);
			this.model.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableLighting();
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}

		@Override
		protected ResourceLocation getEntityTexture(EntityPurpleDragon entity) {
			return texture;
		}
	}

	// Made with Blockbench 3.5.4
	// Exported for Minecraft version 1.12
	// Paste this class into your mod and generate all required imports
	@SideOnly(Side.CLIENT)
	public class ModelDragonHead extends ModelBase {
		private final ModelRenderer head;
		private final ModelRenderer bone;
		private final ModelRenderer bone2;
		private final ModelRenderer teethUpper;
		private final ModelRenderer teethLower;
		private final ModelRenderer jaw;
		private final ModelRenderer hornRight;
		private final ModelRenderer hornRight0;
		private final ModelRenderer hornRight1;
		private final ModelRenderer hornRight2;
		private final ModelRenderer hornRight3;
		private final ModelRenderer hornRight4;
		private final ModelRenderer hornLeft;
		private final ModelRenderer hornLeft0;
		private final ModelRenderer hornLeft1;
		private final ModelRenderer hornLeft2;
		private final ModelRenderer hornLeft3;
		private final ModelRenderer hornLeft4;
		private final ModelRenderer[] whiskerLeft = new ModelRenderer[6];
		private final ModelRenderer[] whiskerRight = new ModelRenderer[6];
		private final ModelRenderer[] spine = new ModelRenderer[20];
		private final ModelRenderer eyes;

		public ModelDragonHead() {
			textureWidth = 256;
			textureHeight = 256;

			head = new ModelRenderer(this);
			head.setRotationPoint(0.0F, 0.0F, 0.0F);
			head.cubeList.add(new ModelBox(head, 176, 44, -6.0F, 6.0F, -26.0F, 12, 5, 16, 1.0F, false));
			head.cubeList.add(new ModelBox(head, 112, 30, -8.0F, -1.0F, -11.0F, 16, 16, 16, 1.0F, false));
			head.cubeList.add(new ModelBox(head, 112, 0, -5.0F, 5.0F, -26.0F, 2, 2, 4, 1.0F, false));
			head.cubeList.add(new ModelBox(head, 112, 0, 3.0F, 5.0F, -26.0F, 2, 2, 4, 1.0F, true));
	
			teethUpper = new ModelRenderer(this);
			teethUpper.setRotationPoint(0.0F, 24.0F, 0.0F);
			head.addChild(teethUpper);
			teethUpper.cubeList.add(new ModelBox(teethUpper, 152, 146, -6.0F, -12.0F, -26.0F, 12, 2, 16, 0.5F, false));

			bone = new ModelRenderer(this);
			bone.setRotationPoint(9.0F, 7.0F, -11.0F);
			head.addChild(bone);
			setRotationAngle(bone, 0.0F, -0.7854F, 0.0F);
			bone.cubeList.add(new ModelBox(bone, 0, 200, 0.0F, -8.0F, 0.0F, 8, 16, 0, 0.0F, false));
	
			bone2 = new ModelRenderer(this);
			bone2.setRotationPoint(-9.0F, 7.0F, -11.0F);
			head.addChild(bone2);
			setRotationAngle(bone2, 0.0F, 0.7854F, 0.0F);
			bone2.cubeList.add(new ModelBox(bone2, 0, 200, -8.0F, -8.0F, 0.0F, 8, 16, 0, 0.0F, true));
	
			jaw = new ModelRenderer(this);
			jaw.setRotationPoint(0.0F, 11.0F, -9.0F);
			head.addChild(jaw);
			jaw.cubeList.add(new ModelBox(jaw, 176, 65, -6.0F, 0.0F, -16.75F, 12, 4, 16, 1.0F, false));
	
			teethLower = new ModelRenderer(this);
			teethLower.setRotationPoint(0.0F, 13.0F, 9.0F);
			jaw.addChild(teethLower);
			teethLower.cubeList.add(new ModelBox(teethLower, 112, 144, -6.0F, -16.0F, -25.75F, 12, 2, 16, 0.5F, false));

			hornRight = new ModelRenderer(this);
			hornRight.setRotationPoint(-6.0F, -2.0F, -13.0F);
			head.addChild(hornRight);
			setRotationAngle(hornRight, 0.0873F, -0.5236F, 0.0F);
			hornRight.cubeList.add(new ModelBox(hornRight, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, false));
	
			hornRight0 = new ModelRenderer(this);
			hornRight0.setRotationPoint(0.0F, 0.0F, 7.0F);
			hornRight.addChild(hornRight0);
			setRotationAngle(hornRight0, 0.0873F, 0.0873F, 0.0F);
			hornRight0.cubeList.add(new ModelBox(hornRight0, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.8F, false));
	
			hornRight1 = new ModelRenderer(this);
			hornRight1.setRotationPoint(0.0F, 0.0F, 7.0F);
			hornRight0.addChild(hornRight1);
			setRotationAngle(hornRight1, 0.0873F, 0.0873F, 0.0F);
			hornRight1.cubeList.add(new ModelBox(hornRight1, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.6F, false));
	
			hornRight2 = new ModelRenderer(this);
			hornRight2.setRotationPoint(0.0F, 0.0F, 7.0F);
			hornRight1.addChild(hornRight2);
			setRotationAngle(hornRight2, 0.0873F, 0.0873F, 0.0F);
			hornRight2.cubeList.add(new ModelBox(hornRight2, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.4F, false));
	
			hornRight3 = new ModelRenderer(this);
			hornRight3.setRotationPoint(0.0F, 0.0F, 7.0F);
			hornRight2.addChild(hornRight3);
			setRotationAngle(hornRight3, 0.0873F, 0.0873F, 0.0F);
			hornRight3.cubeList.add(new ModelBox(hornRight3, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.2F, false));
	
			hornRight4 = new ModelRenderer(this);
			hornRight4.setRotationPoint(0.0F, 0.0F, 7.0F);
			hornRight3.addChild(hornRight4);
			setRotationAngle(hornRight4, 0.0873F, 0.0873F, 0.0F);
			hornRight4.cubeList.add(new ModelBox(hornRight4, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.0F, false));
	
			hornLeft = new ModelRenderer(this);
			hornLeft.setRotationPoint(6.0F, -2.0F, -13.0F);
			head.addChild(hornLeft);
			setRotationAngle(hornLeft, 0.0873F, 0.5236F, 0.0F);
			hornLeft.cubeList.add(new ModelBox(hornLeft, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 1.0F, true));
	
			hornLeft0 = new ModelRenderer(this);
			hornLeft0.setRotationPoint(0.0F, 0.0F, 7.0F);
			hornLeft.addChild(hornLeft0);
			setRotationAngle(hornLeft0, 0.0873F, -0.0873F, 0.0F);
			hornLeft0.cubeList.add(new ModelBox(hornLeft0, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.8F, true));
	
			hornLeft1 = new ModelRenderer(this);
			hornLeft1.setRotationPoint(0.0F, 0.0F, 7.0F);
			hornLeft0.addChild(hornLeft1);
			setRotationAngle(hornLeft1, 0.0873F, -0.0873F, 0.0F);
			hornLeft1.cubeList.add(new ModelBox(hornLeft1, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.6F, true));
	
			hornLeft2 = new ModelRenderer(this);
			hornLeft2.setRotationPoint(0.0F, 0.0F, 7.0F);
			hornLeft1.addChild(hornLeft2);
			setRotationAngle(hornLeft2, 0.0873F, -0.0873F, 0.0F);
			hornLeft2.cubeList.add(new ModelBox(hornLeft2, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.4F, true));
	
			hornLeft3 = new ModelRenderer(this);
			hornLeft3.setRotationPoint(0.0F, 0.0F, 7.0F);
			hornLeft2.addChild(hornLeft3);
			setRotationAngle(hornLeft3, 0.0873F, -0.0873F, 0.0F);
			hornLeft3.cubeList.add(new ModelBox(hornLeft3, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.2F, true));
	
			hornLeft4 = new ModelRenderer(this);
			hornLeft4.setRotationPoint(0.0F, 0.0F, 7.0F);
			hornLeft3.addChild(hornLeft4);
			setRotationAngle(hornLeft4, 0.0873F, -0.0873F, 0.0F);
			hornLeft4.cubeList.add(new ModelBox(hornLeft4, 0, 0, -1.0F, -2.0F, 0.0F, 2, 4, 6, 0.0F, true));
	
			whiskerLeft[0] = new ModelRenderer(this);
			whiskerLeft[0].setRotationPoint(6.0F, 6.0F, -24.0F);
			head.addChild(whiskerLeft[0]);
			setRotationAngle(whiskerLeft[0], 0.0F, 1.0472F, 0.0F);
			whiskerLeft[0].cubeList.add(new ModelBox(whiskerLeft[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, true));
	
			whiskerLeft[1] = new ModelRenderer(this);
			whiskerLeft[1].setRotationPoint(0.0F, 0.0F, 6.0F);
			whiskerLeft[0].addChild(whiskerLeft[1]);
			setRotationAngle(whiskerLeft[1], -0.0873F, -0.1745F, 0.0F);
			whiskerLeft[1].cubeList.add(new ModelBox(whiskerLeft[1], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.7F, true));
	
			whiskerLeft[2] = new ModelRenderer(this);
			whiskerLeft[2].setRotationPoint(0.0F, 0.0F, 6.0F);
			whiskerLeft[1].addChild(whiskerLeft[2]);
			setRotationAngle(whiskerLeft[2], -0.0873F, -0.1745F, 0.0F);
			whiskerLeft[2].cubeList.add(new ModelBox(whiskerLeft[2], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.6F, true));
	
			whiskerLeft[3] = new ModelRenderer(this);
			whiskerLeft[3].setRotationPoint(0.0F, 0.0F, 6.0F);
			whiskerLeft[2].addChild(whiskerLeft[3]);
			setRotationAngle(whiskerLeft[3], -0.0873F, -0.1745F, 0.0F);
			whiskerLeft[3].cubeList.add(new ModelBox(whiskerLeft[3], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.5F, true));
	
			whiskerLeft[4] = new ModelRenderer(this);
			whiskerLeft[4].setRotationPoint(0.0F, 0.0F, 6.0F);
			whiskerLeft[3].addChild(whiskerLeft[4]);
			setRotationAngle(whiskerLeft[4], -0.0873F, -0.1745F, 0.0F);
			whiskerLeft[4].cubeList.add(new ModelBox(whiskerLeft[4], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.4F, true));
	
			whiskerLeft[5] = new ModelRenderer(this);
			whiskerLeft[5].setRotationPoint(0.0F, 0.0F, 6.0F);
			whiskerLeft[4].addChild(whiskerLeft[5]);
			setRotationAngle(whiskerLeft[5], -0.0873F, -0.1745F, 0.0F);
			whiskerLeft[5].cubeList.add(new ModelBox(whiskerLeft[5], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.2F, true));
	
			whiskerRight[0] = new ModelRenderer(this);
			whiskerRight[0].setRotationPoint(-6.0F, 6.0F, -24.0F);
			head.addChild(whiskerRight[0]);
			setRotationAngle(whiskerRight[0], 0.0F, -1.0472F, 0.0F);
			whiskerRight[0].cubeList.add(new ModelBox(whiskerRight[0], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.8F, false));
	
			whiskerRight[1] = new ModelRenderer(this);
			whiskerRight[1].setRotationPoint(0.0F, 0.0F, 6.0F);
			whiskerRight[0].addChild(whiskerRight[1]);
			setRotationAngle(whiskerRight[1], -0.0873F, 0.1745F, 0.0F);
			whiskerRight[1].cubeList.add(new ModelBox(whiskerRight[1], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.7F, false));
	
			whiskerRight[2] = new ModelRenderer(this);
			whiskerRight[2].setRotationPoint(0.0F, 0.0F, 6.0F);
			whiskerRight[1].addChild(whiskerRight[2]);
			setRotationAngle(whiskerRight[2], -0.0873F, 0.1745F, 0.0F);
			whiskerRight[2].cubeList.add(new ModelBox(whiskerRight[2], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.6F, false));
	
			whiskerRight[3] = new ModelRenderer(this);
			whiskerRight[3].setRotationPoint(0.0F, 0.0F, 6.0F);
			whiskerRight[2].addChild(whiskerRight[3]);
			setRotationAngle(whiskerRight[3], -0.0873F, 0.1745F, 0.0F);
			whiskerRight[3].cubeList.add(new ModelBox(whiskerRight[3], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.5F, false));
	
			whiskerRight[4] = new ModelRenderer(this);
			whiskerRight[4].setRotationPoint(0.0F, 0.0F, 6.0F);
			whiskerRight[3].addChild(whiskerRight[4]);
			setRotationAngle(whiskerRight[4], -0.0873F, 0.1745F, 0.0F);
			whiskerRight[4].cubeList.add(new ModelBox(whiskerRight[4], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.4F, false));
	
			whiskerRight[5] = new ModelRenderer(this);
			whiskerRight[5].setRotationPoint(0.0F, 0.0F, 6.0F);
			whiskerRight[4].addChild(whiskerRight[5]);
			setRotationAngle(whiskerRight[5], -0.0873F, 0.1745F, 0.0F);
			whiskerRight[5].cubeList.add(new ModelBox(whiskerRight[5], 0, 0, -1.0F, -1.0F, 0.0F, 2, 2, 6, 0.2F, false));
	
			for (int i = 0; i < spine.length; i++) {
				spine[i] = new ModelRenderer(this);
				spine[i].cubeList.add(new ModelBox(spine[i], 192, 104, -5.0F, -4.5F, 0.0F, 10, 10, 10, 2.0F, false));
				spine[i].cubeList.add(new ModelBox(spine[i], 48, 0, -1.0F, -10.5F, 2.0F, 2, 4, 6, 1.0F, false));
				if (i == 0) {
					spine[i].setRotationPoint(0.0F, 6.5F, 7.0F);
				} else {
					spine[i].setRotationPoint(0.0F, 0.0F, 11.0F);
					spine[i-1].addChild(spine[i]);
				}
			}

			eyes = new ModelRenderer(this);
			eyes.setRotationPoint(0.0F, 0.0F, 0.0F);
			eyes.cubeList.add(new ModelBox(eyes, 130, 50, -6.6F, 2.6F, -12.1F, 3, 2, 0, 0.0F, false));
			eyes.cubeList.add(new ModelBox(eyes, 130, 50, 3.6F, 2.6F, -12.1F, 3, 2, 0, 0.0F, true));
		}

		@Override
		public void render(Entity entityIn, float f, float f1, float f2, float f3, float f4, float f5) {
			this.head.render(f5);
			this.spine[0].render(f5);
			this.eyes.render(f5);
		}

		public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
			modelRenderer.rotateAngleX = x;
			modelRenderer.rotateAngleY = y;
			modelRenderer.rotateAngleZ = z;
		}

		@Override
		public void setRotationAngles(float f0, float f1, float ageInTicks, float f3, float headPitch, float f5, Entity e) {
			super.setRotationAngles(f0, f1, ageInTicks, f3, headPitch, f5, e);
			EntityPurpleDragon entity = (EntityPurpleDragon)e;
			float pt = ageInTicks - e.ticksExisted;
			float f6 = (float)Math.PI / 180.0F;
			this.head.rotateAngleX = headPitch * f6;
			if (entity.ticksAlive > entity.wait) {
				this.jaw.rotateAngleX = 0.5236F;
			}
			for (int i = 2; i < 6; i++) {
				whiskerLeft[i].rotateAngleZ = 0.2618F * ageInTicks;
				whiskerRight[i].rotateAngleZ = -0.2618F * ageInTicks;
			}
			for (int i = 0; i < this.spine.length; i++) {
				if (i < entity.partRot.size()) {
					this.spine[i].showModel = true;
					ProcedureUtils.Vec2f vec = entity.partRot.get(i);
					this.spine[i].rotateAngleX = -vec.y * f6;
					this.spine[i].rotateAngleY = -vec.x * f6;
				} else {
					this.spine[i].showModel = false;
				}
			}
		}
	}

	// Made with Blockbench 4.0.3
	// Exported for Minecraft version 1.7 - 1.12
	// Paste this class into your mod and generate all required imports
	@SideOnly(Side.CLIENT)
	public class ModelGedoMazo extends ModelBiped {
		//private final ModelRenderer bipedHead;
		//private final ModelRenderer bipedHeadwear;
		//private final ModelRenderer bipedBody;
		private final ModelRenderer bone2;
		private final ModelRenderer bone3;
		private final ModelRenderer bone12;
		private final ModelRenderer bone13;
		private final ModelRenderer bone4;
		private final ModelRenderer bone5;
		private final ModelRenderer bone14;
		private final ModelRenderer bone15;
		private final ModelRenderer bone6;
		private final ModelRenderer bone7;
		private final ModelRenderer bone16;
		private final ModelRenderer bone17;
		private final ModelRenderer bone8;
		private final ModelRenderer bone9;
		private final ModelRenderer bone10;
		private final ModelRenderer bone11;
		//private final ModelRenderer bipedRightArm;
		private final ModelRenderer rightUpperArm;
		private final ModelRenderer rightForeArm;
		//private final ModelRenderer bipedLeftArm;
		private final ModelRenderer leftUpperArm;
		private final ModelRenderer leftForeArm;
		//private final ModelRenderer bipedRightLeg;
		private final ModelRenderer bone30;
		private final ModelRenderer rightThigh;
		private final ModelRenderer rightShank;
		//private final ModelRenderer bipedLeftLeg;
		private final ModelRenderer bone23;
		private final ModelRenderer leftThigh;
		private final ModelRenderer leftShank;
	
		public ModelGedoMazo() {
			textureWidth = 64;
			textureHeight = 64;
	
			bipedHead = new ModelRenderer(this);
			bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
			bipedHead.cubeList.add(new ModelBox(bipedHead, 0, 0, -3.0F, -6.0F, -4.0F, 6, 6, 6, -0.1F, false));
			bipedHead.cubeList.add(new ModelBox(bipedHead, 50, 0, -2.0F, -0.2F, -4.0F, 4, 2, 2, -0.1F, false));
	
			bipedHeadwear = new ModelRenderer(this);
			bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
			bipedHeadwear.cubeList.add(new ModelBox(bipedHeadwear, 24, 0, -3.0F, -6.0F, -4.0F, 6, 6, 6, 0.0F, false));
	
			bipedBody = new ModelRenderer(this);
			bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
			bipedBody.cubeList.add(new ModelBox(bipedBody, 16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.0F, false));
			bipedBody.cubeList.add(new ModelBox(bipedBody, 16, 32, -4.0F, 0.0F, -2.0F, 8, 12, 4, 0.5F, false));
	
			bone2 = new ModelRenderer(this);
			bone2.setRotationPoint(-2.0F, 3.0F, -1.0F);
			bipedBody.addChild(bone2);
			setRotationAngle(bone2, 0.2618F, 0.0F, -0.7854F);
			bone2.cubeList.add(new ModelBox(bone2, 56, 16, -1.0F, -5.0F, -1.0F, 2, 6, 2, 0.0F, false));
	
			bone3 = new ModelRenderer(this);
			bone3.setRotationPoint(0.0F, -4.5F, 0.0F);
			bone2.addChild(bone3);
			setRotationAngle(bone3, 0.0F, 0.0F, 0.6981F);
			bone3.cubeList.add(new ModelBox(bone3, 56, 16, -1.0F, -5.5F, -1.0F, 2, 6, 2, 0.0F, false));
	
			bone12 = new ModelRenderer(this);
			bone12.setRotationPoint(2.0F, 3.0F, -1.0F);
			bipedBody.addChild(bone12);
			setRotationAngle(bone12, 0.2618F, 0.0F, 0.7854F);
			bone12.cubeList.add(new ModelBox(bone12, 56, 16, -1.0F, -5.0F, -1.0F, 2, 6, 2, 0.0F, true));
	
			bone13 = new ModelRenderer(this);
			bone13.setRotationPoint(0.0F, -4.5F, 0.0F);
			bone12.addChild(bone13);
			setRotationAngle(bone13, 0.0F, 0.0F, -0.6981F);
			bone13.cubeList.add(new ModelBox(bone13, 56, 16, -1.0F, -5.5F, -1.0F, 2, 6, 2, 0.0F, true));
	
			bone4 = new ModelRenderer(this);
			bone4.setRotationPoint(-3.0F, 2.0F, 0.0F);
			bipedBody.addChild(bone4);
			setRotationAngle(bone4, -0.1745F, 0.0F, -0.5236F);
			bone4.cubeList.add(new ModelBox(bone4, 56, 16, -1.0F, -5.0F, -1.0F, 2, 6, 2, 0.0F, false));
	
			bone5 = new ModelRenderer(this);
			bone5.setRotationPoint(0.0F, -4.5F, 0.0F);
			bone4.addChild(bone5);
			setRotationAngle(bone5, 0.0F, 0.0F, 0.5236F);
			bone5.cubeList.add(new ModelBox(bone5, 56, 16, -1.0F, -5.5F, -1.0F, 2, 6, 2, 0.0F, false));
	
			bone14 = new ModelRenderer(this);
			bone14.setRotationPoint(3.0F, 2.0F, 0.0F);
			bipedBody.addChild(bone14);
			setRotationAngle(bone14, -0.1745F, 0.0F, 0.5236F);
			bone14.cubeList.add(new ModelBox(bone14, 56, 16, -1.0F, -5.0F, -1.0F, 2, 6, 2, 0.0F, true));
	
			bone15 = new ModelRenderer(this);
			bone15.setRotationPoint(0.0F, -4.5F, 0.0F);
			bone14.addChild(bone15);
			setRotationAngle(bone15, 0.0F, 0.0F, -0.5236F);
			bone15.cubeList.add(new ModelBox(bone15, 56, 16, -1.0F, -5.5F, -1.0F, 2, 6, 2, 0.0F, true));
	
			bone6 = new ModelRenderer(this);
			bone6.setRotationPoint(-3.0F, 5.0F, 1.0F);
			bipedBody.addChild(bone6);
			setRotationAngle(bone6, -0.5236F, 0.0F, -0.7854F);
			bone6.cubeList.add(new ModelBox(bone6, 56, 17, -1.0F, -5.0F, -1.0F, 2, 6, 2, 0.0F, false));
	
			bone7 = new ModelRenderer(this);
			bone7.setRotationPoint(0.0F, -4.5F, 0.0F);
			bone6.addChild(bone7);
			setRotationAngle(bone7, 0.0F, 0.0F, 0.5236F);
			bone7.cubeList.add(new ModelBox(bone7, 56, 16, -1.0F, -5.5F, -1.0F, 2, 6, 2, 0.0F, false));
	
			bone16 = new ModelRenderer(this);
			bone16.setRotationPoint(3.0F, 5.0F, 1.0F);
			bipedBody.addChild(bone16);
			setRotationAngle(bone16, -0.5236F, 0.0F, 0.7854F);
			bone16.cubeList.add(new ModelBox(bone16, 56, 16, -1.0F, -5.0F, -1.0F, 2, 6, 2, 0.0F, true));
	
			bone17 = new ModelRenderer(this);
			bone17.setRotationPoint(0.0F, -4.5F, 0.0F);
			bone16.addChild(bone17);
			setRotationAngle(bone17, 0.0F, 0.0F, -0.5236F);
			bone17.cubeList.add(new ModelBox(bone17, 56, 16, -1.0F, -5.5F, -1.0F, 2, 6, 2, 0.0F, true));
	
			bone8 = new ModelRenderer(this);
			bone8.setRotationPoint(0.0F, 0.0F, 1.0F);
			bipedBody.addChild(bone8);
			setRotationAngle(bone8, -0.6981F, 0.0F, 0.0F);
			bone8.cubeList.add(new ModelBox(bone8, 56, 16, -1.0F, -5.0F, -1.0F, 2, 6, 2, 0.0F, false));
	
			bone9 = new ModelRenderer(this);
			bone9.setRotationPoint(0.0F, -4.5F, 0.0F);
			bone8.addChild(bone9);
			setRotationAngle(bone9, 0.6981F, 0.0F, 0.0F);
			bone9.cubeList.add(new ModelBox(bone9, 56, 16, -1.0F, -5.5F, -1.0F, 2, 6, 2, 0.0F, false));
	
			bone10 = new ModelRenderer(this);
			bone10.setRotationPoint(0.0F, 8.0F, 1.0F);
			bipedBody.addChild(bone10);
			setRotationAngle(bone10, -1.0472F, 0.0F, 0.0F);
			bone10.cubeList.add(new ModelBox(bone10, 56, 16, -1.0F, -5.0F, -1.0F, 2, 6, 2, 0.0F, false));
	
			bone11 = new ModelRenderer(this);
			bone11.setRotationPoint(0.0F, -4.5F, 0.0F);
			bone10.addChild(bone11);
			setRotationAngle(bone11, 0.5236F, 0.0F, 0.0F);
			bone11.cubeList.add(new ModelBox(bone11, 56, 16, -1.0F, -5.5F, -1.0F, 2, 6, 2, 0.0F, false));
	
			bipedRightArm = new ModelRenderer(this);
			bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
			
	
			rightUpperArm = new ModelRenderer(this);
			rightUpperArm.setRotationPoint(0.0F, 0.0F, 0.0F);
			bipedRightArm.addChild(rightUpperArm);
			setRotationAngle(rightUpperArm, 0.0F, -0.5236F, 0.2618F);
			rightUpperArm.cubeList.add(new ModelBox(rightUpperArm, 33, 53, -2.0F, -2.0F, -2.0F, 3, 7, 4, 0.0F, false));
	
			rightForeArm = new ModelRenderer(this);
			rightForeArm.setRotationPoint(-0.5F, 5.0F, 2.0F);
			rightUpperArm.addChild(rightForeArm);
			setRotationAngle(rightForeArm, -0.5236F, 0.0F, 0.0F);
			rightForeArm.cubeList.add(new ModelBox(rightForeArm, 40, 16, -1.5F, 0.0F, -4.0F, 3, 7, 4, 0.0F, false));
			rightForeArm.cubeList.add(new ModelBox(rightForeArm, 40, 37, -1.5F, 0.0F, -4.0F, 3, 7, 4, 0.25F, false));
	
			bipedLeftArm = new ModelRenderer(this);
			bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
			
	
			leftUpperArm = new ModelRenderer(this);
			leftUpperArm.setRotationPoint(0.0F, 0.0F, 0.0F);
			bipedLeftArm.addChild(leftUpperArm);
			setRotationAngle(leftUpperArm, 0.0F, 0.5236F, -0.2618F);
			leftUpperArm.cubeList.add(new ModelBox(leftUpperArm, 33, 53, -1.0F, -2.0F, -2.0F, 3, 7, 4, 0.0F, true));
	
			leftForeArm = new ModelRenderer(this);
			leftForeArm.setRotationPoint(0.5F, 5.0F, 2.0F);
			leftUpperArm.addChild(leftForeArm);
			setRotationAngle(leftForeArm, -0.5236F, 0.0F, 0.0F);
			leftForeArm.cubeList.add(new ModelBox(leftForeArm, 40, 16, -1.5F, 0.0F, -4.0F, 3, 7, 4, 0.0F, true));
			leftForeArm.cubeList.add(new ModelBox(leftForeArm, 40, 37, -1.5F, 0.0F, -4.0F, 3, 7, 4, 0.25F, true));
	
			bipedRightLeg = new ModelRenderer(this);
			bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
			
	
			bone30 = new ModelRenderer(this);
			bone30.setRotationPoint(-2.0F, 0.0F, 0.0F);
			bipedRightLeg.addChild(bone30);
			setRotationAngle(bone30, 0.0F, 0.0F, 0.1745F);
			bone30.cubeList.add(new ModelBox(bone30, 0, 32, 0.0F, 0.0F, -2.0F, 4, 6, 4, 0.5F, false));
	
			rightThigh = new ModelRenderer(this);
			rightThigh.setRotationPoint(-0.1F, 0.0F, 0.0F);
			bipedRightLeg.addChild(rightThigh);
			setRotationAngle(rightThigh, -0.2618F, 0.2618F, 0.0F);
			rightThigh.cubeList.add(new ModelBox(rightThigh, 48, 54, -2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F, false));
	
			rightShank = new ModelRenderer(this);
			rightShank.setRotationPoint(0.0F, 6.0F, -2.0F);
			rightThigh.addChild(rightShank);
			setRotationAngle(rightShank, 0.2618F, 0.0F, 0.0F);
			rightShank.cubeList.add(new ModelBox(rightShank, 0, 16, -2.0F, 0.0F, 0.0F, 4, 7, 4, 0.0F, false));
			rightShank.cubeList.add(new ModelBox(rightShank, 0, 40, -2.0F, 4.0F, 0.0F, 4, 3, 4, 0.25F, false));
	
			bipedLeftLeg = new ModelRenderer(this);
			bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
			
	
			bone23 = new ModelRenderer(this);
			bone23.setRotationPoint(2.0F, 0.0F, 0.0F);
			bipedLeftLeg.addChild(bone23);
			setRotationAngle(bone23, 0.0F, 0.0F, -0.1745F);
			bone23.cubeList.add(new ModelBox(bone23, 0, 32, -4.0F, 0.0F, -2.0F, 4, 6, 4, 0.5F, true));
	
			leftThigh = new ModelRenderer(this);
			leftThigh.setRotationPoint(0.1F, 0.0F, 0.0F);
			bipedLeftLeg.addChild(leftThigh);
			setRotationAngle(leftThigh, -0.2618F, -0.2618F, 0.0F);
			leftThigh.cubeList.add(new ModelBox(leftThigh, 48, 54, -2.0F, 0.0F, -2.0F, 4, 6, 4, 0.0F, true));
	
			leftShank = new ModelRenderer(this);
			leftShank.setRotationPoint(0.0F, 6.0F, -2.0F);
			leftThigh.addChild(leftShank);
			setRotationAngle(leftShank, 0.2618F, 0.0F, 0.0F);
			leftShank.cubeList.add(new ModelBox(leftShank, 0, 16, -2.0F, 0.0F, 0.0F, 4, 7, 4, 0.0F, true));
			leftShank.cubeList.add(new ModelBox(leftShank, 0, 40, -2.0F, 4.0F, 0.0F, 4, 3, 4, 0.25F, true));
		}

		@Override
		public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, 1.5F - 1.5F * MODEL_SCALE, 0.0F);
			GlStateManager.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
			super.render(entity, f, f1, f2, f3, f4, f5);
			GlStateManager.popMatrix();
		}

		public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
			modelRenderer.rotateAngleX = x;
			modelRenderer.rotateAngleY = y;
			modelRenderer.rotateAngleZ = z;
		}

		@Override
		public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity e) {
			f = f * 2.0F / e.height;
			super.setRotationAngles(f, f1, f2, f3, f4, f5, e);
		}

		private void poseSit(boolean sitting) {
			if (sitting) {
				setRotationAngle(rightUpperArm, -0.2618F, 0.0873F, 0.2618F);
				setRotationAngle(leftUpperArm, -0.2618F, -0.0873F, -0.2618F);
				setRotationAngle(rightThigh, -2.0944F, 0.1309F, -1.309F);
				setRotationAngle(rightShank, 1.5272F, 0.0F, 0.0F);
				setRotationAngle(leftThigh, -2.0944F, -0.1309F, 1.309F);
				setRotationAngle(leftShank, 1.5272F, 0.0F, 0.0F);
			} else {
				setRotationAngle(rightUpperArm, 0.0F, -0.5236F, 0.2618F);
				setRotationAngle(leftUpperArm, 0.0F, 0.5236F, -0.2618F);
				setRotationAngle(rightThigh, -0.2618F, 0.2618F, 0.0F);
				setRotationAngle(rightShank, 0.2618F, 0.0F, 0.0F);
				setRotationAngle(leftThigh, -0.2618F, -0.2618F, 0.0F);
				setRotationAngle(leftShank, 0.2618F, 0.0F, 0.0F);
			}
		}
	}
}
