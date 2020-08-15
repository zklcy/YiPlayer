package com.coder.Control;

import android.util.Log;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import static android.os.SystemClock.sleep;
import static com.coder.Control.FFmpegCmd.KEY_AUDIO_BG;

public class VideoCheck {
    private String TAG = VideoCheck.class.getName();

    private LibVLC mLibVLC;
    private MediaPlayer mMediaPlayer;


    public VideoCheck() {
        mLibVLC = new LibVLC();
        mMediaPlayer = new MediaPlayer(mLibVLC);
    }

    public boolean isHaveBGTrack(String filename){
        boolean ret = false;
        Media media = new Media(mLibVLC, filename);
        mMediaPlayer.setMedia(media);
        media.addOption(":video-paused");
        mMediaPlayer.play();
        mMediaPlayer.pause();
        sleep(1000);

        int i = 0;
        int channels = mMediaPlayer.getAudioTracksCount();
        int num = mMediaPlayer.getAudioTrack();
        MediaPlayer.TrackDescription[] tracks = mMediaPlayer.getAudioTracks();
        Log.d(TAG, "isHaveBGTrack: "+channels);

        if(tracks == null){
            Log.d(TAG, "Audio Tracks is null");
            return false;
        }

        for (MediaPlayer.TrackDescription track : tracks){
            Log.d(TAG, "Track"+ (i++) +" : "+track.name);
            if(track.name.contains(KEY_AUDIO_BG)){
                Log.d(TAG, "Get BG audio track");
                return true;
            }
        }
        mMediaPlayer.stop();
        return ret;
    }

    public void release(){
        mMediaPlayer.release();
        mLibVLC.release();
    }
}
