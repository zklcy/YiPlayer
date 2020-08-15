package com.coder.Control;

import android.os.Message;
import android.util.Log;


import org.videolan.libvlc.media.VideoView;
import org.videolan.libvlc.subtitle.Caption;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;



public class PlayBackControl {
    private final String TAG = PlayBackControl.class.getName();

    private Caption[] mSubtitles;
    private int mPlayTimes;

    VideoView mPlayer;

    //Control Moive
    private PlayControlMethod mMethod;
    private int mPlayedTimes = 0;
    private int mCurrentCaptionIndex = 0;
    private int mMinWaitTime = 3000;

    //MSG
    private final int MSG_PLAY_RESUME = 100;

    public PlayBackControl(VideoView player,PlayControlMethod method) {
        this.mPlayer = player;
        if(mPlayer != null){
            mPlayer.setmOnMessageExtendListener(mMessage);
        }

        this.mMethod = method;
        this.mSubtitles = null;
        try{
            Collection<Caption> lstCaption = mPlayer.getSubtitle();
            if(lstCaption!= null && lstCaption.size()>0) {
                this.mSubtitles = lstCaption.toArray(new Caption[lstCaption.size()]);
            }else {
                Log.d(TAG, "PlayBackControl: Can't find subtitle");
            }
        }catch (Exception exception){
            Log.d(TAG, "PlayBackControl: Something wrong, can't find subtitle!");
        }

        init(mMethod);
        Log.d(TAG, "PlayBackControl: Create");
    }

    public PlayControlMethod getmMethod() {
        return mMethod;
    }

    public void init(PlayControlMethod method){
        mMethod = method;
        switch (mMethod){
            case CTRL_NORMAL:{
                mPlayTimes=0;
            }
            break;
            case CTRL_REPLAY:{
                mPlayTimes = 3;
            }
            break;
            case CTRL_READ_FOLLOW:{

            }
        }
        mPlayedTimes = 0;
        mCurrentCaptionIndex = 0;
    }

    public void setmPlayTimes(int mPlayTimes) {
        this.mPlayTimes = mPlayTimes;
    }

    public void setmSubtitles(Collection<Caption> mSubtitles) {
        if(mSubtitles.size()>0) {
            this.mSubtitles = mSubtitles.toArray(new Caption[mSubtitles.size()]);
        }
    }

    public Caption getNextCaption(){
        if(mCurrentCaptionIndex >=0 && mCurrentCaptionIndex < mSubtitles.length - 1){
            return mSubtitles[++mCurrentCaptionIndex];
        }else{
            mCurrentCaptionIndex = -1;
            return null;
        }
    }

    public void reSetTime(){
        if(mSubtitles == null){
            return;
        }

        long currenttime = mPlayer.getTime();
        int i = 0;

        for (Caption caption : mSubtitles) {
            if (currenttime >= caption.start.getMseconds()
                    && currenttime < caption.end.getMseconds()) {
                mCurrentCaptionIndex = i;
                break;
            }
            i++;
        }
    }

    //0:playing
    //1:pause,wait user record
    //2:resume
    private int state=0;

    public void ControlProcess() throws InterruptedException {
        if(mMethod != PlayControlMethod.CTRL_REPLAY && mMethod != PlayControlMethod.CTRL_READ_FOLLOW ){
            return;
        }

        if(mSubtitles == null){
            return;
        }

        if(mCurrentCaptionIndex < 0 || mCurrentCaptionIndex >= mSubtitles.length){
            return;
        }

        //refresh current subtitle
        Caption currentCaption = mSubtitles[mCurrentCaptionIndex];
        Log.d(TAG, "ControlProcess: current caption time from "+currentCaption.start.getMseconds() +
                " to "+currentCaption.end.getMseconds());

        long currenttime = mPlayer.getTime();

        if(currenttime > currentCaption.end.getMseconds()) {
            Log.d(TAG, "ControlProcess: index="+mCurrentCaptionIndex +" time="+currenttime +
                    " endtime="+currentCaption.end.getMseconds() +
                    "Playedtimes="+mPlayedTimes);
            //need replay
            if (mPlayedTimes < mPlayTimes) {
                Log.d(TAG, "ControlProcess: Play again， played "+mPlayedTimes+" times");

                //pause video
//                mActivity.mVideoControllerPlayOrPause.callOnClick();
                mPlayer.pause();
                mPlayer.seekTo(currentCaption.start.getMseconds());
                //Open mic and wait kids to read sentence and record voice,calc wait time
                long slice = currentCaption.end.getMseconds()-currentCaption.start.getMseconds();
                //if slice is too short, we need min time to wait
                slice = slice < mMinWaitTime ? mMinWaitTime : slice;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what = MSG_PLAY_RESUME;
                        mPlayer.getHandler().sendMessage(msg);
                        Log.d(TAG, "Start video ,current time is " + System.currentTimeMillis() + " ms");
                    }
                },slice);
                Log.d(TAG, "Pause video ,current time is " + System.currentTimeMillis() + " ms , wait "+ slice +" ms");

            } else {
                Log.d(TAG, "ControlProcess: Next phase");
                getNextCaption();
                mPlayedTimes = 0;
            }
        }else {
            //do nothing,continue to play
        }
    }

    private VideoView.OnMessageExtendListener mMessage = new VideoView.OnMessageExtendListener() {
        @Override
        public void onMessageExtendListener(Message msg) {
            switch (msg.what){
                case MSG_PLAY_RESUME:
                    mPlayer.resume();
                    mPlayedTimes++;
                    break;
            }
        }
    };

}



