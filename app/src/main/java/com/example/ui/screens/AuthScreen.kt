package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.components.*
import com.example.ui.theme.*

// ══════════════════════════════════════════════════════════════════════════════
// Journey — Giriş / Kayıt Ekranı
//
// Firebase Auth entegrasyonu için google-services.json gereklidir.
// Şimdilik UI tamamdır, Firebase bağlantısı için:
//   1. Firebase Console'da proje oluştur
//   2. google-services.json'ı app/ klasörüne koy
//   3. build.gradle.kts'deki google-services plugin yorum satırını aç
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Auth ekranı durumları.
 */
sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class Success(val uid: String) : AuthUiState()
}

/**
 * Giriş / Kayıt ekranı.
 *
 * @param onAuthSuccess Auth başarılı olduğunda — yeni oyuncu mu yoksa mevcut mu?
 * @param onGuestMode Misafir olarak devam et
 */
@Composable
fun AuthScreen(
    onAuthSuccess: (uid: String, isNewUser: Boolean) -> Unit,
    onGuestMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEmailForm by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Arka plan renk dalgası animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "auth_bg")
    val bgAlpha by infiniteTransition.animateFloat(
        initialValue = 0.03f, targetValue = 0.07f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOut), RepeatMode.Reverse),
        label = "bg_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        // ── Arkaplan atmosfer gradient ──────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ColorSanctumPrimary.copy(alpha = bgAlpha),
                            ColorCovenantPrimary.copy(alpha = bgAlpha * 0.5f),
                            ColorBackground
                        ),
                        radius = 1200f
                    )
                )
        )

        // ── İçerik ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.SpacingXxxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.weight(0.15f))

            // Logo & başlık
            JourneyBranding()

            Spacer(Modifier.weight(0.1f))

            // Hata mesajı
            AnimatedVisibility(visible = errorMessage.isNotBlank()) {
                ActionBanner(
                    message = errorMessage,
                    isError = true,
                    modifier = Modifier.padding(bottom = Dimens.SpacingL)
                )
            }

            // Auth seçenekleri
            AnimatedContent(
                targetState = showEmailForm,
                transitionSpec = {
                    fadeIn(tween(Dimens.AnimNormal)) togetherWith fadeOut(tween(Dimens.AnimNormal))
                },
                label = "auth_content"
            ) { emailForm ->
                if (emailForm) {
                    EmailPasswordForm(
                        onSignIn = { email, pass ->
                            // TODO: Firebase Auth.signInWithEmailAndPassword(email, pass)
                            errorMessage = "Firebase bağlantısı için google-services.json gereklidir."
                        },
                        onRegister = { email, pass ->
                            // TODO: Firebase Auth.createUserWithEmailAndPassword(email, pass)
                            errorMessage = "Firebase bağlantısı için google-services.json gereklidir."
                        },
                        onBack = { showEmailForm = false; errorMessage = "" }
                    )
                } else {
                    AuthOptions(
                        onGoogleSignIn = {
                            // TODO: Google Sign-In flow
                            // GoogleSignInClient → ActivityResultLauncher → Firebase.auth.signInWithCredential
                            errorMessage = "Firebase bağlantısı için google-services.json gereklidir."
                        },
                        onEmailSignIn = { showEmailForm = true },
                        onGuestMode = onGuestMode
                    )
                }
            }

            Spacer(Modifier.weight(0.2f))

            // Alt bilgi
            Text(
                text = "© Journey — Dark Fantasy Tower RPG",
                style = MaterialTheme.typography.labelSmall,
                color = ColorOnSurfaceSubtle,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Alt Bileşenler ────────────────────────────────────────────────────────────

@Composable
private fun JourneyBranding() {
    // İsim fade-in animasyonu
    val alpha by produceState(0f) {
        kotlinx.coroutines.delay(200)
        value = 1f
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM)
    ) {
        // "JOURNEY" — büyük gothic başlık
        Text(
            text = "JOURNEY",
            style = MaterialTheme.typography.displayLarge,
            color = ColorSanctumPrimary,
            modifier = Modifier.alpha(alpha),
            textAlign = TextAlign.Center
        )

        // Çift çizgi ayırıcı
        Row(
            modifier = Modifier.fillMaxWidth(0.5f),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f).height(Dimens.BorderThin).background(ColorSanctumPrimary.copy(alpha = 0.4f)))
            Box(modifier = Modifier.size(Dimens.SpacingS).background(ColorSanctumPrimary.copy(alpha = 0.6f), RoundedCornerShape(Dimens.RadiusCircle)))
            Box(modifier = Modifier.weight(1f).height(Dimens.BorderThin).background(ColorSanctumPrimary.copy(alpha = 0.4f)))
        }

        Text(
            text = "The Eternal Tower Awaits",
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = ColorOnSurfaceMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AuthOptions(
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: () -> Unit,
    onGuestMode: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "CHOOSE YOUR PATH",
            style = TextStyleFactionTitle,
            color = ColorOnSurfaceMuted,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(Dimens.SpacingS))

        // Google ile giriş — birincil seçenek
        DarkFantasyButton(
            text = "🔵  Google ile Giriş Yap",
            onClick = onGoogleSignIn,
            factionSide = "SANCTUM",
            modifier = Modifier.fillMaxWidth()
        )

        // Email ile giriş
        DarkFantasyButton(
            text = "✉  Email ile Giriş / Kayıt",
            onClick = onEmailSignIn,
            factionSide = "NEUTRAL",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(Dimens.SpacingXs))

        GhostButton(
            text = "Misafir olarak devam et →",
            onClick = onGuestMode,
            color = ColorOnSurfaceMuted
        )
    }
}

@Composable
private fun EmailPasswordForm(
    onSignIn: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingM),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "EMAIL / ŞİFRE",
            style = TextStyleFactionTitle,
            color = ColorOnSurfaceMuted
        )

        Spacer(Modifier.height(Dimens.SpacingXs))

        DarkEmailField(
            value = email,
            onValueChange = { email = it },
            placeholder = "eposta@adres.com"
        )

        DarkPasswordField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Şifre"
        )

        Spacer(Modifier.height(Dimens.SpacingS))

        DarkFantasyButton(
            text = "Giriş Yap",
            onClick = { onSignIn(email, password) },
            factionSide = "SANCTUM",
            modifier = Modifier.fillMaxWidth()
        )

        DarkFantasyButton(
            text = "Yeni Hesap Oluştur",
            onClick = { onRegister(email, password) },
            factionSide = "NEUTRAL",
            modifier = Modifier.fillMaxWidth()
        )

        GhostButton(
            text = "← Geri",
            onClick = onBack,
            color = ColorOnSurfaceMuted
        )
    }
}
