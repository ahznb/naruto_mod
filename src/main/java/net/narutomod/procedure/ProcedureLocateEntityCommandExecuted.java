package net.narutomod.procedure;

import net.narutomod.entity.EntityBijuManager;
import net.narutomod.command.CommandLocateEntity;
import net.narutomod.ElementsNarutomodMod;

import net.minecraftforge.fml.common.FMLCommonHandler;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

@ElementsNarutomodMod.ModElement.Tag
public class ProcedureLocateEntityCommandExecuted extends ElementsNarutomodMod.ModElement {
	public ProcedureLocateEntityCommandExecuted(ElementsNarutomodMod instance) {
		super(instance, 255);
	}

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure LocateEntityCommandExecuted!");
			return;
		}
		if (dependencies.get("cmdparams") == null) {
			System.err.println("Failed to load dependency cmdparams for procedure LocateEntityCommandExecuted!");
			return;
		}
		Entity entity = (Entity) dependencies.get("entity");
		HashMap cmdparams = (HashMap) dependencies.get("cmdparams");
		boolean f1 = false;
		String string = "";
		double x = 0;
		double y = 0;
		double z = 0;
		double tailnum = 0;
		Vec3d vec3d = null;
		if ((((new Object() {
			public String getText() {
				String param = (String) cmdparams.get("0");
				if (param != null) {
					return param;
				}
				return "";
			}
		}.getText())).equals(CommandLocateEntity.Level1.BIJU.toString()))) {
			tailnum = (double) new Object() {
				int convert(String s) {
					try {
						return Integer.parseInt(s.trim());
					} catch (Exception e) {
					}
					return 0;
				}
			}.convert((new Object() {
				public String getText() {
					String param = (String) cmdparams.get("1");
					if (param != null) {
						return param;
					}
					return "";
				}
			}.getText()));
			Entity biju = EntityBijuManager.getEntityByTails((int) tailnum);
			if (biju != null) {
				vec3d = biju.getPositionVector();
				string = (String) biju.getName();
			}
		} else if ((((new Object() {
			public String getText() {
				String param = (String) cmdparams.get("0");
				if (param != null) {
					return param;
				}
				return "";
			}
		}.getText())).equals(CommandLocateEntity.Level1.JINCHURIKI.toString()))) {
			if ((((new Object() {
				public String getText() {
					String param = (String) cmdparams.get("1");
					if (param != null) {
						return param;
					}
					return "";
				}
			}.getText())).equals(CommandLocateEntity.JinchurikiLevel2.LIST.toString()))) {
				List<String> list = EntityBijuManager.listJinchuriki();
				for (int index0 = 0; index0 < (int) (list.size()); index0++) {
					if (entity instanceof EntityPlayer && !entity.world.isRemote) {
						((EntityPlayer) entity).sendStatusMessage(new TextComponentString(list.get(index0)), (false));
					}
				}
			} else if ((((new Object() {
				public String getText() {
					String param = (String) cmdparams.get("1");
					if (param != null) {
						return param;
					}
					return "";
				}
			}.getText())).equals(CommandLocateEntity.JinchurikiLevel2.REVOKE.toString()))) {
				if ((((new Object() {
					public String getText() {
						String param = (String) cmdparams.get("2");
						if (param != null) {
							return param;
						}
						return "";
					}
				}.getText())).equals("all"))) {
					EntityBijuManager.revokeAllJinchuriki();
				} else {
					tailnum = (double) new Object() {
						int convert(String s) {
							try {
								return Integer.parseInt(s.trim());
							} catch (Exception e) {
							}
							return 0;
						}
					}.convert((new Object() {
						public String getText() {
							String param = (String) cmdparams.get("2");
							if (param != null) {
								return param;
							}
							return "";
						}
					}.getText()));
					if ((((tailnum) >= 1) && ((tailnum) <= 10))) {
						EntityBijuManager.revokeJinchurikiByTails((int) tailnum);
					}
				}
			} else if ((((new Object() {
				public String getText() {
					String param = (String) cmdparams.get("1");
					if (param != null) {
						return param;
					}
					return "";
				}
			}.getText())).equals(CommandLocateEntity.JinchurikiLevel2.ASSIGN.toString()))) {
				string = (String) (new Object() {
					public String getText() {
						String param = (String) cmdparams.get("2");
						if (param != null) {
							return param;
						}
						return "";
					}
				}.getText());
				EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(string);
				tailnum = (double) new Object() {
					int convert(String s) {
						try {
							return Integer.parseInt(s.trim());
						} catch (Exception e) {
						}
						return 0;
					}
				}.convert((new Object() {
					public String getText() {
						String param = (String) cmdparams.get("3");
						if (param != null) {
							return param;
						}
						return "";
					}
				}.getText()));
				if (player != null) {
					EntityBijuManager.setPlayerAsJinchurikiByTails(player, (int) tailnum);
				}
			} else {
				if (entity instanceof EntityPlayer && !entity.world.isRemote) {
					((EntityPlayer) entity).sendStatusMessage(
							new TextComponentString(
									"Usage: /locateEntity <biju [num] | jinchuriki {list | revoke {all | [num]} | assign [playername] [num]}>"),
							(false));
				}
			}
			return;
		} else {
			if (entity instanceof EntityPlayer && !entity.world.isRemote) {
				((EntityPlayer) entity).sendStatusMessage(new TextComponentString(
						"Usage: /locateEntity <biju [num] | jinchuriki {list | revoke {all | [num]} | assign [playername] [num]}>"), (false));
			}
			return;
		}
		if (vec3d != null) {
			string = (String) (((string)) + "" + (" entity last known position at: ") + "" + (Math.floor(vec3d.x)) + "" + (", ") + ""
					+ (Math.floor(vec3d.y)) + "" + (", ") + "" + (Math.floor(vec3d.z)));
		} else {
			string = (String) "Entity not found";
		}
		if (entity instanceof EntityPlayer && !entity.world.isRemote) {
			((EntityPlayer) entity).sendStatusMessage(new TextComponentString((string)), (false));
		}
	}
}
