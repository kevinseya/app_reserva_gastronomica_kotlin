package com.mespinoza.appgastronomia

import android.app.Application
import com.mespinoza.appgastronomia.utils.StripeConfig
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GastronomiaApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		PaymentConfiguration.init(this, StripeConfig.PUBLISHABLE_KEY)
	}
}
