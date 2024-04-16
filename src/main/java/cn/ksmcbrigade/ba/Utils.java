package cn.ksmcbrigade.ba;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

public class Utils {

    public static Thread thread;

    public static boolean c = false;

    public static void to(String BV, Player player,boolean whi,boolean list,boolean listWhile){

        // data
        String dataAPI = "https://api.bilibili.com/x/web-interface/view?bvid=";
        //audio
        String audioAPI = "https://api.bilibili.com/x/player/playurl?fnval=80&avid={a}&cid={c}";

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
        thread = new Thread(() -> {
            try {
                c = false;
                player.sendSystemMessage(Component.translatable("command.ba.jx"));
                JsonObject data = JsonParser.parseReader(new JsonReader(new InputStreamReader(toIN(get(dataAPI+BV))))).getAsJsonObject().getAsJsonObject("data");
                JsonArray audioData = JsonParser.parseReader(new JsonReader(new InputStreamReader(toIN(get(audioAPI.replace("{a}",data.get("aid").getAsString()).replace("{c}",data.get("cid").getAsString())))))).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("dash").getAsJsonArray("audio");
                String audioURL = audioData.get(0).getAsJsonObject().get("baseUrl").getAsString();
                if(!whi){
                    if(c){
                        c = false;
                        player.sendSystemMessage(Component.nullToEmpty(I18n.get("command.ba.stop").replace("{}",data.get("title").getAsString())));
                        return;
                    }
                    play(audioURL,player,true,data);
                    player.sendSystemMessage(Component.nullToEmpty(I18n.get("command.ba.stop").replace("{}",data.get("title").getAsString())));
                }
                else{
                    while (true){
                        if(c){
                            c = false;
                            player.sendSystemMessage(Component.nullToEmpty(I18n.get("command.ba.stop").replace("{}",data.get("title").getAsString())));
                            continue;
                        }
                        play(audioURL,player,false,data);
                        player.sendSystemMessage(Component.nullToEmpty(I18n.get("command.ba.stop").replace("{}",data.get("title").getAsString())));
                    }
                }

                //list ugc_season

                if(data.has("ugc_season") && list){
                    JsonArray sections = data.getAsJsonObject("ugc_season").getAsJsonArray("sections").get(0).getAsJsonObject().getAsJsonArray("episodes");
                    AtomicBoolean tz = new AtomicBoolean(false);
                    if(!listWhile){
                        for(JsonElement e:sections){
                            if(e instanceof JsonObject audio){
                                if(!audio.get("bvid").getAsString().equalsIgnoreCase(BV)){
                                    JsonArray audioDataNow = null;
                                    try {
                                        audioDataNow = JsonParser.parseReader(new JsonReader(new InputStreamReader(toIN(get(audioAPI.replace("{a}",audio.get("aid").getAsString()).replace("{c}",audio.get("cid").getAsString())))))).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("dash").getAsJsonArray("audio");
                                        String audioURLNow = audioDataNow.get(0).getAsJsonObject().get("baseUrl").getAsString();
                                        if(c){
                                            c = false;
                                            player.sendSystemMessage(Component.nullToEmpty(I18n.get("command.ba.stop").replace("{}",audio.get("title").getAsString())));
                                            continue;
                                        }
                                        play(audioURLNow,player,true,audio);
                                        player.sendSystemMessage(Component.nullToEmpty(I18n.get("command.ba.stop").replace("{}",audio.get("title").getAsString())));
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                        player.sendSystemMessage(Component.nullToEmpty("Failed to audio: "+ex.getMessage()));
                                    }
                                }
                            }
                        }
                        /*sections.forEach(e -> {
                            if(e instanceof JsonObject audio){

                            }
                        });*/
                    }
                    else{
                        while (true){
                            for(JsonElement e:sections){
                                if(e instanceof JsonObject audio){
                                    boolean yg = false;
                                    if(audio.get("bvid").getAsString().equalsIgnoreCase(BV) && !tz.get()){
                                        tz.set(true);
                                        yg = true;
                                    }
                                    if(!yg){
                                        JsonArray audioDataNow = null;
                                        try {
                                            audioDataNow = JsonParser.parseReader(new JsonReader(new InputStreamReader(toIN(get(audioAPI.replace("{a}",audio.get("aid").getAsString()).replace("{c}",audio.get("cid").getAsString())))))).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("dash").getAsJsonArray("audio");
                                            String audioURLNow = audioDataNow.get(0).getAsJsonObject().get("baseUrl").getAsString();
                                            if(c){
                                                c = false;
                                                player.sendSystemMessage(Component.nullToEmpty(I18n.get("command.ba.stop").replace("{}",audio.get("title").getAsString())));
                                                continue;
                                            }
                                            play(audioURLNow,player,true,audio);
                                            player.sendSystemMessage(Component.translatable("command.ba.stop"));
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                            player.sendSystemMessage(Component.nullToEmpty("Failed to audio: "+ex.getMessage()));
                                        }
                                    }
                                }
                            }
                            /*sections.forEach(e -> {

                            });*/
                        }
                    }
                }
                c = false;
            }
            catch (Exception e){
                e.printStackTrace();
                player.sendSystemMessage(Component.nullToEmpty("Failed to audio: "+e.getMessage()));
            }

        },"audio-"+System.nanoTime());

        thread.start();
    }

    public static String get(String urlz) throws IOException {
        /*String re = "";
        try {
            URL url = new URL(urlz);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            connection.setConnectTimeout(120000);
            connection.setReadTimeout(120000);

            if(connection.getResponseCode()==200){
                re = new String(connection.getInputStream().readAllBytes());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(re);*/
        downloadFile(urlz,"temp.json");
        return Files.readString(Paths.get("temp.json"));
    }

    public static void copyInternalFileToExternal(String internalFilePath, String externalFilePath) throws IOException{
        InputStream in = Utils.class.getClassLoader().getResourceAsStream(internalFilePath);
        if (in != null) {
            byte[] bytes = in.readAllBytes();
            in.close();
            Files.write(Paths.get(externalFilePath),bytes);
        }
    }

    public static String getRun(String... strings) throws InterruptedException, IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Runtime.getRuntime().exec(strings).getInputStream(),
                        "GB2312"
                )
        );
        String line;
        StringBuilder b = new StringBuilder();
        while ((line = br.readLine()) != null) {
            b.append(line).append("\n");
        }
        return b.toString();
    }

    public static void playWAV(String filePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                line.write(buffer, 0, bytesRead);
                if(c){
                    c = false;
                    break;
                }
            }
            line.stop();
            line.close();

            while (line.isOpen() && line.isRunning() && !c) {
                Thread.sleep(10);
            }
            audioInputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void play(String audioURL,Player player,boolean de,JsonObject d){
        try {
            /*URL url = new URL(audioURL);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();

            urlConnection.setUseCaches(false);
            urlConnection.setConnectTimeout(4800000);
            urlConnection.setReadTimeout(4800000);

            if(urlConnection.getResponseCode()!=200){
                return;
            }

            AudioInputStream audioInputStream2 = AudioSystem.getAudioInputStream(new BufferedInputStream(urlConnection.getInputStream()));
            AudioFormat audioFormat = audioInputStream2.getFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = audioInputStream2.read(buffer)) != -1) {
                sourceDataLine.write(buffer, 0, bytesRead);
            }

            sourceDataLine.stop();
            sourceDataLine.close();
            audioInputStream2.close();*/
            File file = new File("temp.wav");

            if(!file.exists() || de){
                downloadFile(audioURL,"temp.m4s");
            }

            Runtime.getRuntime().exec("ffe.exe -i temp.m4s temp.wav").waitFor();

            player.sendSystemMessage(Component.nullToEmpty(I18n.get("command.ba.start").replace("{}",d.get("title").getAsString())));

            playWAV(file.getPath());

            if(de){
                new File("temp.m4s").delete();
                file.delete();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static ByteArrayInputStream toIN(String str) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(str.getBytes());
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public static void downloadFile(String fileUrl, String fileName) throws IOException {
        //if(!new File(fileName).exists()){
            System.out.println("Downloading: "+fileUrl);
            try (BufferedInputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        //}
    }
}
