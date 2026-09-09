package org.nha.project.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.nha.project.core.ui.theme.HemFocusBorder
import org.nha.project.core.ui.theme.HemPrimary

private val OTP_BOX_SPACING = 8.dp

/**
 * A boxed, one-digit-per-cell OTP entry field. Each box is a real, independently focusable
 * text field (not one hidden field behind drawn boxes), so tapping a box focuses exactly that
 * box via the platform's own tap-to-focus - no synthetic touch-area sizing needed. Typing a
 * digit auto-advances to the next box; Backspace on an already-empty box moves back and clears
 * the previous one. Each box selects its whole contents on focus, so retyping over a filled box
 * replaces it instead of appending - which also cleanly disambiguates a genuine multi-digit paste
 * or SMS autofill (which replaces the selection with the whole code at once) from a normal single
 * keystroke, so OTP autofill still works landing on any box.
 */
@Composable
fun OtpInputFields(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    boxSize: Dp = 44.dp,
) {
    val focusRequesters = remember(length) { List(length) { FocusRequester() } }
    var focusedIndex by remember { mutableStateOf<Int?>(null) }

    Row(horizontalArrangement = Arrangement.spacedBy(OTP_BOX_SPACING), modifier = modifier) {
        repeat(length) { index ->
            OtpDigitField(
                digit = value.getOrNull(index),
                isActive = focusedIndex == index,
                size = boxSize,
                focusRequester = focusRequesters[index],
                onFocusChanged = { focused ->
                    if (focused) {
                        focusedIndex = index
                    } else if (focusedIndex == index) {
                        focusedIndex = null
                    }
                },
                onDigitsEntered = { digits ->
                    val updated = applyBoxInput(value, index, digits, length)
                    onValueChange(updated)
                    if (digits.isNotEmpty() && updated.length < length) {
                        focusRequesters[updated.length].requestFocus()
                    }
                },
                onBackspaceOnEmpty = {
                    if (index > 0) {
                        onValueChange(value.take(index - 1) + value.drop(index))
                        focusRequesters[index - 1].requestFocus()
                    }
                },
            )
        }
    }
}

/**
 * Applies [digits] typed/pasted into the box at [index] to the full OTP [value]. A single digit
 * replaces whatever was at that position (the box selects-all on focus, so a keystroke always
 * arrives as exactly one replacement digit); more than one digit means a paste/autofill landed
 * here, so it's spread starting at this position as the new tail of the code.
 */
private fun applyBoxInput(
    value: String,
    index: Int,
    digits: String,
    length: Int,
): String =
    if (digits.length > 1) {
        (value.take(index) + digits).take(length)
    } else {
        (value.take(index) + digits + value.drop(index + 1)).take(length)
    }

@Composable
private fun OtpDigitField(
    digit: Char?,
    isActive: Boolean,
    size: Dp,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    onDigitsEntered: (String) -> Unit,
    onBackspaceOnEmpty: () -> Unit,
) {
    var fieldValue by remember(digit) { mutableStateOf(TextFieldValue(text = digit?.toString().orEmpty())) }
    val borderColor =
        when {
            isActive -> HemFocusBorder
            digit != null -> HemPrimary.copy(alpha = 0.35f)
            else -> HemPrimary.copy(alpha = 0.2f)
        }

    BasicTextField(
        value = fieldValue,
        onValueChange = { new ->
            val digits = new.text.filter(Char::isDigit)
            if (digits == digit?.toString().orEmpty()) {
                // Selection/cursor move only, no actual digit change - just track it locally.
                fieldValue = new
            } else {
                onDigitsEntered(digits)
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle =
            MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
            ),
        cursorBrush = SolidColor(HemFocusBorder),
        decorationBox = { innerTextField ->
            Box(
                modifier =
                    Modifier
                        .size(size)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActive) HemFocusBorder.copy(alpha = 0.06f) else Color.White)
                        .border(
                            width = if (isActive) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(10.dp),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                innerTextField()
            }
        },
        modifier =
            Modifier
                .size(size)
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    onFocusChanged(focusState.isFocused)
                    if (focusState.isFocused) {
                        fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
                    }
                }.onKeyEvent { keyEvent ->
                    if (keyEvent.key == Key.Backspace && digit == null) {
                        onBackspaceOnEmpty()
                        true
                    } else {
                        false
                    }
                },
    )
}
