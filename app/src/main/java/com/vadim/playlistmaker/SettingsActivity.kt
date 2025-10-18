package com.vadim.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backBTN = findViewById<TextView>(R.id.toolBar_BTN)
        backBTN.setOnClickListener {
            finish()
        }

        val switchBTN = findViewById<SwitchMaterial>(R.id.switch_BTN)
        switchBTN.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                this,
                "switch is $isChecked",
                Toast.LENGTH_SHORT
            ).show()
        }


        val shareUpBTN = findViewById<TextView>(R.id.shareUp_BTN)
        shareUpBTN.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.course_android_developer))
            }
            startActivity(sendIntent)

        }

        val supportBTN = findViewById<TextView>(R.id.support_BTN)
        supportBTN.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_mail))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.text_mail))
            }
            runCatching {
                startActivity(sendIntent)
            }.onFailure {
                Toast.makeText(
                    this,
                    getString(R.string.error_mail),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val userAgreementBTN = findViewById<TextView>(R.id.userAgreement_BTN)
        val webpage = Uri.parse(getString(R.string.uri_user_agreement))

        userAgreementBTN.setOnClickListener {
            val intentUserAgreement = Intent(Intent.ACTION_VIEW, webpage)
            runCatching {
                startActivity(intentUserAgreement)
            }.onFailure {
                Toast.makeText(
                    this,
                    getString(R.string.error_user_agreement),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}