package me.ligaram.app.ui.components

import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.delay
import me.ligaram.app.R

@Composable
fun NativeAdCard(adUnitId: String, slotIndex: Int, onAdFailed: (Int) -> Unit) {
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var adFailed by remember { mutableStateOf(false) }
    val context  = LocalContext.current

    DisposableEffect(adUnitId) {
        adFailed = false
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad -> nativeAd = ad }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("NativeAdCard", "Falha ao carregar anúncio: ${error.code} — ${error.message}")
                    adFailed = true
                    onAdFailed(slotIndex)
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
        onDispose { nativeAd?.destroy() }
    }

    // Fallback para ad blockers que não disparam onAdFailedToLoad (bloqueio a nível de DNS/VPN)
    LaunchedEffect(adUnitId) {
        delay(10_000L)
        if (nativeAd == null && !adFailed) {
            adFailed = true
            onAdFailed(slotIndex)
        }
    }

    if (adFailed) return

    // animateContentSize: suaviza a transição skeleton→ad quando diferem ligeiramente de altura
    Card(
        modifier  = Modifier.fillMaxWidth().animateContentSize(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        if (nativeAd == null) {
            // Skeleton: bloco neutro calibrado para a altura mínima do ad nativo (~140dp)
            // Garante que skeleton ≤ ad → transição é sempre expansão suave, nunca contração
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
            )
        } else {
            val ad = nativeAd!!
            AndroidView(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                factory  = { ctx ->
                    val view = LayoutInflater.from(ctx)
                        .inflate(R.layout.native_ad_card, null) as NativeAdView
                    view.headlineView     = view.findViewById(R.id.ad_headline)
                    view.bodyView         = view.findViewById(R.id.ad_body)
                    view.callToActionView = view.findViewById(R.id.ad_call_to_action)
                    view.iconView         = view.findViewById(R.id.ad_app_icon)
                    view.advertiserView   = view.findViewById(R.id.ad_advertiser)
                    view
                },
                update   = { view ->
                    (view.headlineView as? TextView)?.text           = ad.headline
                    (view.bodyView as? TextView)?.text               = ad.body
                    (view.callToActionView as? Button)?.text         = ad.callToAction
                    (view.iconView as? ImageView)?.setImageDrawable(ad.icon?.drawable)
                    (view.advertiserView as? TextView)?.text         = ad.advertiser
                    view.setNativeAd(ad)
                }
            )
        }
    }
}
