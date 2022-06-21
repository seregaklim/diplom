package com.klim.nework.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.klim.nework.R
import com.klim.nework.databinding.ActivityMainBinding
import com.klim.nework.fragments.MakeJobDialogFragment
import com.klim.nework.viewModel.AuthViewModel
import com.klim.nework.viewModel.PageViewModel
import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels()

    val profileViewModel: PageViewModel by viewModels()


    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging

    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    fun setActionBarTitle(title: String) {
        binding.mainToolbar.title = title
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseMessaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(
                    this.localClassName,
                    "Fetching FCM registration token failed",
                    task.exception
                )
                return@addOnCompleteListener
            }

            val token = task.result
            checkGoogleApiAvailability()
        }



        navController = findNavController(R.id.nav_host_fragment_container)

        val toolbar = binding.mainToolbar
        setSupportActionBar(toolbar)

        binding.apply {

            fabOpenDrawer.setOnClickListener {
                drawer.openDrawer(GravityCompat.START)
            }

            nVStartBooton.setNavigationItemSelectedListener{
                when(it.itemId){

                    R.id.fabAddPost -> {Toast.makeText(this@MainActivity,  R.string.fab_Add_Post,Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.makeEditPostFragment)

                    }

                    R.id.fabAddEvent -> {Toast.makeText(this@MainActivity,  R.string.fab_Add_Event,Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.makeEventFragment)
                    }

                    R.id.fabPost -> {Toast.makeText(this@MainActivity,  R.string.view_posts,Toast.LENGTH_SHORT).show()
                        navController.navigate( R.id.nav_posts_fragment)

                    }

                    R.id.fabEvent -> {Toast.makeText(this@MainActivity, R.string. view_evets,Toast.LENGTH_SHORT).show()
                        navController.navigate( R.id.nav_events_fragment)
                    }

                    R.id.fabPage -> {Toast.makeText(this@MainActivity, R.string. your_page,Toast.LENGTH_SHORT).show()
                        navController.navigate(  R.id.nav_page_fragment)
                    }

                    R.id.fabUsers -> {
                        Toast.makeText(this@MainActivity, R.string.watch_users, Toast.LENGTH_SHORT)
                            .show()
                        navController.navigate(R.id.nav_users_fragment,)
                    }

                    R.id.fabOnSignOut-> {Toast.makeText(this@MainActivity, R.string.text_go_out,Toast.LENGTH_SHORT).show()

                        profileViewModel.onSignOut()
                        navController.navigate(  R.id.logInFragment)

                    }

                }
                //после нажатия на кнопку, закрывает
                drawer.closeDrawer(GravityCompat.START)
                true

            }


        }



        viewModel.authState.observe(this) { user ->

            if (!viewModel.checkIfAskedToLogin && user.id == 0L) {
                navController.navigate(R.id.fragmentLogotip)
                viewModel.setCheckLoginTrue()
            }

            if (user.id == 0L) {

                invalidateOptionsMenu()

                binding.fabOpenDrawer.visibility = View.GONE
                binding.drawer.setDrawerLockMode (DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else {
                invalidateOptionsMenu()


                navController.navigate( R.id.nav_posts_fragment)
                binding.fabOpenDrawer.visibility = View.VISIBLE

            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.auth_app_manu, menu)
        menu?.setGroupVisible(R.id.group_sign_in, !viewModel.isAuthenticated)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_in -> {
                navController.navigate(R.id.logInFragment)
                true
            }
            else -> false
        }
    }


    private fun checkGoogleApiAvailability() {
        with(googleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@MainActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@MainActivity, code, 9000).show()
                return
            }
            Toast.makeText(this@MainActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
            googleApiAvailability.makeGooglePlayServicesAvailable(this@MainActivity)
        }
    }
}