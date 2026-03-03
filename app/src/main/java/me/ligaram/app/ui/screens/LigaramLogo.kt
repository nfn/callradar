package me.ligaram.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.ligaram.app.R

@Composable
fun LigaramLogo(size: Dp = 56.dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_ligaram_logo),
        contentDescription = null,
        modifier = modifier.size(size)
    )
}
 