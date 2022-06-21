package com.klim.nework.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView

class PostRecyclerView : RecyclerView {



    private var thumbnail: ImageView? = null
    private var progressBar: ProgressBar? = null
    private var viewHolderParent: View? = null
    private var videoContainer: FrameLayout? = null
    private lateinit var videoSurfaceView: PlayerView
    private var videoPlayer: SimpleExoPlayer? = null
    private var playIcon: ImageView? = null



    private var isVideoViewAdded = false


    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        videoSurfaceView = PlayerView(context)
        videoSurfaceView.videoSurfaceView
        videoSurfaceView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM


        videoPlayer = SimpleExoPlayer.Builder(context).build()

        videoSurfaceView.controllerShowTimeoutMs = 2_000
        videoSurfaceView.controllerHideOnTouch = true
        videoSurfaceView.setControllerVisibilityListener {
            if (videoPlayer?.isPlaying == false) {
                videoSurfaceView.useController = true
                videoSurfaceView.showController()
            }


        }
        videoSurfaceView.player = videoPlayer
        videoPlayer?.repeatMode = Player.REPEAT_MODE_ONE

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    if (thumbnail != null) { // show the old thumbnail
                        thumbnail?.visibility = View.VISIBLE
                        playIcon?.visibility = View.VISIBLE
                    }
                }
            }

        })
        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                view.setOnClickListener { v ->
                    playVideo(v)
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                if (viewHolderParent != null && viewHolderParent == view) {
                    resetVideoView()
                }
            }
        })
        videoPlayer?.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        progressBar?.visibility = VISIBLE
                    }
                    Player.STATE_READY -> {
                        progressBar?.visibility = GONE
                        if (!isVideoViewAdded) {
                            addVideoView()
                        }
                    }
                    else -> {
                    }
                }
            }
        })
    }

    fun playVideo(view: View) {

        if (viewHolderParent != null && viewHolderParent == view) {
            return
        } else {


            progressBar?.visibility = View.GONE
            resetVideoView()
        }


        if (!::videoSurfaceView.isInitialized) {
            return
        }

        val holder = view.tag as PostViewHolder? ?: return
        thumbnail = holder.videoThumbnail
        playIcon = holder.videoPlayIcon
        progressBar = holder.videoProgressBar
        viewHolderParent = holder.itemView
        videoContainer = holder.videoContainer
        videoSurfaceView.player = videoPlayer



        holder.videoPreview?.let {
            videoPlayer?.setMediaItem(it)
            videoPlayer?.prepare()
            videoPlayer?.playWhenReady = true
        }
    }

    // Remove the old player
    private fun removeVideoView(videoView: PlayerView?) {
        val parent = videoView?.parent as ViewGroup?
        val index = parent?.indexOfChild(videoView)
        if (index != null && index >= 0) {
            parent.removeViewAt(index)
            isVideoViewAdded = false
        }
    }

    private fun addVideoView() {
        videoContainer!!.addView(videoSurfaceView)
        isVideoViewAdded = true
        videoSurfaceView.requestFocus()
        videoSurfaceView.visibility = View.VISIBLE
        videoSurfaceView.alpha = 1f
        thumbnail?.visibility = View.GONE
        playIcon?.visibility = View.GONE

    }

    private fun resetVideoView() {
        if (isVideoViewAdded) {
            videoSurfaceView.player?.stop()
            removeVideoView(videoSurfaceView)
            progressBar?.visibility = View.INVISIBLE
            videoSurfaceView.visibility = View.INVISIBLE
            thumbnail?.visibility = View.VISIBLE
            playIcon?.visibility = View.VISIBLE

        }
    }

    fun releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer?.release()
            videoPlayer = null
        }
        resetVideoView()
        viewHolderParent = null
    }

    fun createPlayer() {
        if (videoPlayer == null) {
            init(context)
        }
    }
}