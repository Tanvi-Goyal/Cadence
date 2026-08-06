package com.mindset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindset.icons.Apple
import com.mindset.icons.Google
import com.mindset.icons.Visibility

/*
 * Login (Figma "Login"). The app's entry screen: a MindSet logo, email + password fields, a primary
 * "Continue with Email" CTA, Google / Apple sign-in, and a Create Account link. Design-static — there
 * is no auth backend yet, so every sign-in action just enters the app (onSignedIn). The top header
 * (wordmark / close / skip) is intentionally omitted. Firebase Auth lands in a later phase.
 */

private val Gutter = 20.dp
private val FieldHeight = 56.dp

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    MindSetTheme {
        val colors = MaterialTheme.colorScheme
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var passwordVisible by rememberSaveable { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Gutter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))
            LogoCard()
            Spacer(Modifier.height(24.dp))
            Text(
                "Welcome back",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Log in to your performance dashboard",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))

            InputField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                keyboardType = KeyboardType.Email,
            )
            Spacer(Modifier.height(16.dp))
            InputField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailing = {
                    Icon(
                        MindSetIcons.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = if (passwordVisible) colors.primary else colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp).clip(CircleShape).clickable { passwordVisible = !passwordVisible },
                    )
                },
            )
            Spacer(Modifier.height(20.dp))

            PrimaryButton("Continue with Email", onClick = onSignedIn)
            Spacer(Modifier.height(16.dp))
            Text(
                "Forgot password?",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.primary,
                modifier = Modifier.clip(CircleShape).clickable(onClick = onForgotPassword).padding(horizontal = 12.dp, vertical = 4.dp),
            )
            Spacer(Modifier.height(28.dp))

            OrDivider("Or login with")
            Spacer(Modifier.height(24.dp))
            OutlinedPillButton(
                icon = MindSetIcons.Google,
                iconTint = Color.Unspecified, // preserve the brand's own colors
                label = "Sign in with Google",
                onClick = onSignedIn,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedPillButton(
                icon = MindSetIcons.Apple,
                iconTint = colors.onSurface,
                label = "Sign in with Apple",
                onClick = onSignedIn,
            )
            Spacer(Modifier.height(28.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    "Create Account",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    modifier = Modifier.clip(CircleShape).clickable(onClick = onCreateAccount).padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
            Spacer(Modifier.height(28.dp))

            Text(
                "By continuing, you agree to MindSet's Terms of Service and Privacy Policy. " +
                    "Obsidian Performance data engine v4.8.2",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Logo ─────────────────────────────────────────────────────────────────────────────────────────

/** MindSet wordmark placeholder in a dark card (swap the real logo asset in later). */
@Composable
private fun LogoCard() {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .size(width = 150.dp, height = 120.dp)
            .clip(MaterialTheme.shapes.large)
            .background(colors.surfaceContainerHigh),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "MindSet".uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = colors.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Hybrid Fitness".uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.5.sp,
            color = colors.primary,
        )
    }
}

// ── Fields ───────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction = ImeAction.Next,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(FieldHeight)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainerHigh)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (trailing != null) trailing()
    }
}

// ── Buttons ──────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(FieldHeight)
            .shadow(16.dp, CircleShape)
            .clip(CircleShape)
            .background(colors.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onPrimary,
        )
    }
}

@Composable
private fun OutlinedPillButton(icon: ImageVector, iconTint: Color, label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(FieldHeight)
            .clip(CircleShape)
            .border(1.dp, colors.outlineVariant, CircleShape)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
        )
    }
}

// ── Divider ──────────────────────────────────────────────────────────────────────────────────────

@Composable
private fun OrDivider(label: String) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.weight(1f).height(1.dp).background(colors.outlineVariant.copy(alpha = 0.5f)))
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.5.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Box(Modifier.weight(1f).height(1.dp).background(colors.outlineVariant.copy(alpha = 0.5f)))
    }
}
