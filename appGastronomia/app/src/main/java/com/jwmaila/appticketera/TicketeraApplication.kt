package com.jwmaila.appticketera

import android.app.Application
import com.jwmaila.appticketera.utils.StripeConfig
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TicketeraApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		PaymentConfiguration.init(this, StripeConfig.PUBLISHABLE_KEY)
	}
}
