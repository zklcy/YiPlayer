package com.coder.Control;


import android.util.Log;

import java.io.File;
import java.util.Arrays;

public class FFmpegCmd {
    static final String KEY_AUDIO_BG = "BG";
    static {
        System.loadLibrary("avdevice");
        System.loadLibrary("avutil");
        System.loadLibrary("avcodec");
        System.loadLibrary("swresample");
        System.loadLibrary("avformat");
        System.loadLibrary("swscale");
        System.loadLibrary("avfilter");
        System.loadLibrary("postproc");
        System.loadLibrary("ffmpeg-invoke");
    }

    private static native int run(int cmdLen, String[] cmd);
    private static native String stringFromJNI() ;


    public static int runCmd(String[] cmd){
        return run(cmd.length,cmd);
    }

    public static String test(){
        return stringFromJNI();
    }

    public static void CMD_COMMON(String[] lstcmd, String[] fmt){
        long startTime = System.currentTimeMillis();

        for(int i=0, j = 0 ; i< lstcmd.length; i++){
            String str = lstcmd[i];
            if(str.indexOf("%s")>=0 & j < fmt.length){
                lstcmd[i] = String.format(str,fmt[j++]);
            }
        }

        Log.d("FFmpegTest", "FFMPEG CMD= " + Arrays.toString(lstcmd));
        FFmpegCmd.runCmd(lstcmd);
        Log.d("FFmpegTest", "run: 耗时：" + (System.currentTimeMillis() - startTime));
    }

    public static void CMD_DelVoice(final String filename){
        File f = new File(filename);
        String output = f.getPath().substring(0,f.getPath().lastIndexOf('/'))+"/NoVoice_"+f.getName();

        String strcmd = "ffmpeg -i %s -af pan=1c|c0=0.5*c0-0.5*c1 -c:v copy %s";
        String[] lstcmd = strcmd.split(" ");
        String[] lstfmt = new String[]{
                            filename,
                            output
                            };
        CMD_COMMON(lstcmd,lstfmt);
    }

    public static void CMD_VoiceChange(String videofile, String audiofile){
        File f = new File(videofile);
        String output = f.getPath().substring(0,f.getPath().lastIndexOf('/'))+"/MixVoice_"+f.getName();

        String strcmd = "ffmpeg -i %s -i %s -c:v copy -c:a aac -strict experimental -map 0:v:0 -map 1:a:0 %s";
        String[] lstcmd = strcmd.split(" ");
        String[] lstfmt = new String[]{
                videofile,
                audiofile,
                output
        };
        CMD_COMMON(lstcmd,lstfmt);
    }

    public static void CMD_DelSubtitle(String videofile){
        File f = new File(videofile);
        String output = f.getPath().substring(0,f.getPath().lastIndexOf('/'))+"/NoSubtitle_"+f.getName();

        String strcmd = "ffmpeg -i %s -filter_complex delogo=x=10:y=10:w=250:h=100:show=0 %s";
        String[] lstcmd = strcmd.split(" ");
        String[] lstfmt = new String[]{
                videofile,
                output
        };
        CMD_COMMON(lstcmd,lstfmt);
    }

    public static void Del_TmpFile(String videofile){
        File f = new File(videofile);
        String outputvideo = f.getPath().substring(0,f.getPath().lastIndexOf('/'))+"/tmp.mp4";
        File fv = new File(outputvideo);
        if(fv.exists())
            fv.delete();
    }

    public static void CMD_AddBGTrack(String videofile){
        File f = new File(videofile);
        String outputvideo = f.getPath().substring(0,f.getPath().lastIndexOf('/'))+"/tmp.mp4";
        Del_TmpFile(videofile);

//        String strcmd = "ffmpeg -i %s -af pan=1c|c0=0.5*c0-0.5*c1 %s";
//        String[] lstcmd = strcmd.split(" ");
//        String[] lstfmt = new String[]{
//                videofile,
//                outputwav
//        };
//        CMD_COMMON(lstcmd,lstfmt);

        String strcmd = "ffmpeg -i %s -map 0:v -c:v copy -filter_complex [0:a]pan=stereo|c0=c0|c1=c1[left];[0:a]pan=1c|c0=c0-c1,highpass=f=200,lowpass=f=3000[right] -map [left] -map [right] -metadata:s:a:0 title=eng -metadata:s:a:1 title="+KEY_AUDIO_BG+" %s";
//        strcmd = "ffmpeg -i %s -i %s -map 0:v -map 0:a:0 -map 1:a -metadata:s:a:0 title=eng -metadata:s:a:1 title="+KEY_AUDIO_BG+" -c:v copy %s";
        String[] lstcmd = strcmd.split(" ");
        String[] lstfmt = new String[]{
                videofile,
                outputvideo,
        };
        CMD_COMMON(lstcmd,lstfmt);

        f.delete();
        File of = new File(outputvideo);
        of.renameTo(f);
        of.delete();
        Del_TmpFile(videofile);
    }
}

