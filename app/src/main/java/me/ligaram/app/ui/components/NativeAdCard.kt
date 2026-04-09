package me.ligaram.app.ui.components

import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import me.ligaram.app.R

@Composable
fun NativeAdCard(adUnitId: String) {
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var adFailed by remember { mutableStateOf(false) }
    val context = LocalContext.current

    DisposableEffect(adUnitId) {
        adFailed = false
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad -> nativeAd = ad }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("NativeAdCard", "Falha ao carregar anúncio: ${error.code} — ${error.message}")
                    adFailed = true
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
        onDispose { nativeAd?.destroy() }
    }

    when {
        adFailed -> {
            // Falhou — não ocupa espaço no feed
        }
        nativeAd == null -> {
            // A carregar — placeholder subtil para evitar saltos de layout
            Surface(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0x221E293B)
            ) {
                Box(Modifier.fillMaxWidth())
            }
        }
        else -> {
            val ad = nativeAd!!
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    factory = { ctx ->
                        val view = LayoutInflater.from(ctx)
                            .inflate(R.layout.native_ad_card, null) as NativeAdView
                        view.headlineView     = view.findViewById(R.id.ad_headline)
                        view.bodyView         = view.findViewById(R.id.ad_body)
                        view.callToActionView = view.findViewById(R.id.ad_call_to_action)
                        view.iconView         = view.findViewById(R.id.ad_app_icon)
                        view.advertiserView   = view.findViewById(R.id.ad_advertiser)
                        view
                    },
                    update = { view ->
                        (view.headlineView as? TextView)?.text = ad.headline
                        (view.bodyView as? TextView)?.text = ad.body
                        (view.callToActionView as? Button)?.text = ad.callToAction
                        (view.iconView as? ImageView)?.setImageDrawable(ad.icon?.drawable)
                        (view.advertiserView as? TextView)?.text = ad.advertiser
                        view.setNativeAd(ad)
                    }
                )
            }
        }
    }
}
