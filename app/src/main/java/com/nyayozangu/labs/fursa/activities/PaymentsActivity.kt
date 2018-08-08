package com.nyayozangu.labs.fursa.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.nyayozangu.labs.fursa.R
import com.pesapal.pesapalandroid.PesapalPayActivity
import kotlinx.android.synthetic.main.activity_payments.*
import org.jetbrains.anko.startActivity
import android.content.Intent
import android.content.ComponentName
import android.util.Log
import com.pesapal.pesapalandroid.data.Payment
import org.jetbrains.anko.startActivityForResult

const val SETTINGS_ACTIVITY_REQUEST_CODE = 1
const val PAYMENT_ACTIVITY_REQUEST_CODE = 2

const val TAG = "PaymentsActivity"
class PaymentsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payments)

        val actionBar = supportActionBar
        actionBar?.title = resources.getString(R.string.title_activity_payments)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        payButton.setOnClickListener{

            val payment = Payment().apply {
                amount = 100.00
                account = "2"
                description = "test_payment"
                email = "admin@nyayoangu.com"
                currency = "TZS"
                firstName = "Paul"
                lastName = "Sean"
                phoneNumber = "0713810803"
            }
            val cn = ComponentName(this, "com.pesapal.pesapalandroid.PesapalPayActivity")
            val intent = Intent().setComponent(cn)
            intent.putExtra("payment", payment)
            startActivityForResult(intent, PAYMENT_ACTIVITY_REQUEST_CODE)
        }

        paymentSettingsButton.setOnClickListener{
            val cn = ComponentName(this, "com.pesapal.pesapalandroid.PesapalSettingsActivity")
            val intent = Intent().setComponent(cn)
            intent.putExtra("pkg", "com.pesapal")
            startActivityForResult(intent, SETTINGS_ACTIVITY_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SETTINGS_ACTIVITY_REQUEST_CODE -> Log.d(TAG, "returned from settings")
            PAYMENT_ACTIVITY_REQUEST_CODE -> Log.d(TAG, "returned from payments")
        }
    }
}
