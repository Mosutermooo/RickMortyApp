package com.example.rickmorty.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.rickmorty.R
import com.example.rickmorty.models.Episode
import com.example.rickmorty.utils.Resources
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

class EpisodePlayActivity : AppCompatActivity() {
    private lateinit var exoPlayer: SimpleExoPlayer
    var isLock: Boolean = false
    private var menu: Menu? = null
    lateinit var singleEpisode : Episode
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_play)
        Resources.initLoadingDialog(this)
        val player = findViewById<PlayerView>(R.id.player)
        val episodeTitle = findViewById<TextView>(R.id.EpisodeTitle)
        val episodePlayToolBar = findViewById<Toolbar>(R.id.episodePlayToolBar)
        val episode = intent.getSerializableExtra("episode")
        singleEpisode = episode as Episode
        episodeTitle.text = "${singleEpisode.episode} : ${singleEpisode.name}"


        setupToolBar(episodePlayToolBar)

        exoPlayer = SimpleExoPlayer.Builder(this)
            .setSeekBackIncrementMs(5000)
            .setSeekForwardIncrementMs(5000)
            .build()

        player.player = exoPlayer
        player.keepScreenOn = true
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Resources.loadingDialog()
                    }
                    Player.STATE_ENDED -> {
                        finish()
                    }
                    else -> {
                        Resources.hideLoadingDialog()
                    }
                }
            }
        })
        val videoSource = Uri.parse("https://firebasestorage.googleapis.com/v0/b/discordclone-d81d8.appspot.com/o/How%20to%20Download%20Rick%20And%20Morty%20Completely%20for%20Free%20%5BNOT%20WORKING%20AS%20OF%20APRIL%202022%5D.mp4?alt=media&token=fab1410f-9730-4cfc-bca1-3cd752d2b676")
        val mediaItem = MediaItem.fromUri(videoSource)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.episode_play_menu, menu)
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.Lock -> {
                if(!isLock){
                    menu?.getItem(0)?.setIcon(R.drawable.ic_baseline_lock_24)

                }else{
                    menu?.getItem(0)?.setIcon(R.drawable.ic_baseline_lock_open_24)
                }
                isLock = !isLock
                lockScreen(isLock)
            }
            R.id.Cast -> {
                startActivity(Intent("android.settings.CAST_SETTINGS")  )
            }
        }
        return true
    }

    private fun lockScreen(lock: Boolean) {
        val llPlayer = findViewById<LinearLayout>(R.id.ll_player)
        val indicator = findViewById<LinearLayout>(R.id.indicator)
        if(lock){
            llPlayer.visibility = View.GONE
            indicator.visibility = View.GONE
        }else{
            llPlayer.visibility = View.VISIBLE
            indicator.visibility = View.VISIBLE
        }
    }

    private fun setupToolBar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24)
            actionBar.title = ""
        }

        toolbar.setNavigationOnClickListener {
            if(isLock){
                return@setNavigationOnClickListener
            }else{
                onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.stop()
        isLock = false
    }

    override fun onBackPressed() {
        if(isLock){
            return
        }
        super.onBackPressed()
    }
}