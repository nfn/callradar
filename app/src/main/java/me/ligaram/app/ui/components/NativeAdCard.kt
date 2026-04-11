package me.ligaram.app.ui.components

import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
        delay(5_000L)
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
            ShimmerBox(heightDp = 140)
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

@Composable
private fun ShimmerBox(heightDp: Int) {
    val base      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue  = -600f,
        targetValue   = 1600f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start  = Offset(translateX, 0f),         // topo esquerdo
                    end    = Offset(translateX + 600f, 600f) // fundo direito (~45°)
                )
            )
    )
}
