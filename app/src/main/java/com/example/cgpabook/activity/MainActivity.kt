package com.example.cgpabook.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.cgpabook.R
import com.example.cgpabook.utils.HelperStrings
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


class MainActivity : AppCompatActivity() {
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 0
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        // init on create
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // disable night mode support
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id)).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<Button>(R.id.googlesignin).setOnClickListener { signIn() }
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("GoogleSignIn", "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            val intent = Intent(this, NavigationActivity::class.java)
            intent.putExtra(HelperStrings.name, account.displayName)
            intent.putExtra(HelperStrings.email, account.email)
            account.photoUrl?.let { photoUrl ->
                intent.putExtra(HelperStrings.photoUrl, photoUrl.toString())
            }
            intent.putExtra(HelperStrings.tokenId, account.idToken)

            // Debug:println("token: ${account.idToken}")

            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        progressBar = progressBarInit()
        mGoogleSignInClient.silentSignIn()
            .addOnCompleteListener(
                this
            ) { task ->
                progressBarDestroy(progressBar)
                handleSignInResult(task)
            }
        super.onStart()

    }

    fun progressBarInit(): ProgressBar {
        // Disable taps when progress bar is being shown
        let {
            it.window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        }

        // init progress bar
        progressBar = ProgressBar(this)
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        val relativeLayout = RelativeLayout(this)
        relativeLayout.addView(progressBar, params)
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        findViewById<FrameLayout>(R.id.progress_overlay).addView(relativeLayout, layoutParams)
        progressBar.visibility = View.VISIBLE

        // overlay.xml layout should be included in the file where you need to show progressbar
        val progressOverlay = findViewById<FrameLayout>(R.id.progress_overlay)
        progressOverlay.visibility = View.VISIBLE
        return progressBar
    }

    fun progressBarDestroy(p: ProgressBar) {
        val progressOverlay = findViewById<FrameLayout>(R.id.progress_overlay)
        // Restores taps
        this.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        progressOverlay.visibility = View.GONE
        p.visibility = View.GONE
    }
}