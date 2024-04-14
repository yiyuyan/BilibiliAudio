package cn.ksmcbrigade.ba;


import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod("ba")
@Mod.EventBusSubscriber
public class BilibiliAudio {

    public BilibiliAudio() throws IOException, InterruptedException {
        MinecraftForge.EVENT_BUS.register(this);
        String r = Utils.getRun("cmd.exe","/c","ffe.exe").toLowerCase();
        if(!r.contains("ffmpeg version 4.2.2 copyright (c) 2000-2019 the ffmpeg developers")){
           Utils.copyInternalFileToExternal("ff.exe","ffe.exe");
        }

    }

    @SubscribeEvent
    public void command(RegisterClientCommandsEvent event){
        event.getDispatcher().register(Commands.literal("ba").then(Commands.argument("BVID", StringArgumentType.string()).executes(context -> {
            String BV = StringArgumentType.getString(context,"BVID");
            Player player = (Player) context.getSource().getEntity();
            Utils.to(BV,player,false,false,false);
            return 0;
        }).then(Commands.argument("while", BoolArgumentType.bool()).executes(context -> {
            String BV = StringArgumentType.getString(context,"BVID");
            Player player = (Player) context.getSource().getEntity();
            boolean whi = BoolArgumentType.getBool(context,"while");
            Utils.to(BV,player,whi,false,false);
            return 0;
        }).then(Commands.argument("list",BoolArgumentType.bool()).executes(context -> {
            String BV = StringArgumentType.getString(context,"BVID");
            Player player = (Player) context.getSource().getEntity();
            boolean whi = BoolArgumentType.getBool(context,"while");
            boolean list = BoolArgumentType.getBool(context,"list");
            Utils.to(BV,player,whi,list,false);
            return 0;
        }).then(Commands.argument("listWhile",BoolArgumentType.bool()).executes(context -> {
            String BV = StringArgumentType.getString(context,"BVID");
            Player player = (Player) context.getSource().getEntity();
            boolean whi = BoolArgumentType.getBool(context,"while");
            boolean list = BoolArgumentType.getBool(context,"list");
            boolean LW = BoolArgumentType.getBool(context,"listWhile");
            Utils.to(BV,player,whi,list,LW);
            return 0;
        }))))));
    }

    @SubscribeEvent
    public void CloseAudio(LevelEvent.Unload event){
        try {
            if(Utils.thread!=null){
                Utils.thread.stop();
                Utils.thread = null;
            }
        }
        catch (Exception e){
            System.out.println("Thread sleep exception.");
            e.printStackTrace();
        }
    }
}
