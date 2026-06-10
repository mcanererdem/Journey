package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.ui.theme.*

// ══════════════════════════════════════════════════════════════════════════════
// Journey Dark Fantasy — Giriş Alanı Bileşenleri
// Email ve şifre inputları dark fantasy temasında
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Dark fantasy temalı email giriş alanı.
 */
@Composable
fun DarkEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    DarkTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        keyboardType = KeyboardType.Email,
        modifier = modifier
    )
}

/**
 * Dark fantasy temalı şifre giriş alanı.
 */
@Composable
fun DarkPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    DarkTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        keyboardType = KeyboardType.Password,
        isPassword = true,
        modifier = modifier
    )
}

@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) ColorSanctumPrimary.copy(alpha = 0.7f) else ColorBorder

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = ColorOnBackground),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else
            androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusS))
            .background(ColorSurfaceVariant)
            .border(BorderStroke(Dimens.BorderNormal, borderColor), RoundedCornerShape(Dimens.RadiusS))
            .onFocusChanged { isFocused = it.isFocused }
            .padding(horizontal = Dimens.SpacingL, vertical = Dimens.SpacingM),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorOnSurfaceMuted
                    )
                }
                innerTextField()
            }
        }
    )
}
