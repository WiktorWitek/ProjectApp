package com.example.travelapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.travelapp.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

val roundFontFamily = FontFamily(
    Font(R.font.nunito_variablefont_wght),
    Font(R.font.nunito_semibolditalic),
    Font(R.font.nunito_semibold),
    Font(R.font.nunito_regular),
    Font(R.font.nunito_mediumitalic),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_lightitalic),
    Font(R.font.nunito_light, FontWeight.Light),
    Font(R.font.nunito_italic_variablefont_wght),
    Font(R.font.nunito_italic),
    Font(R.font.nunito_extralightitalic),
    Font(R.font.nunito_extralight, FontWeight.ExtraLight),
    Font(R.font.nunito_extrabolditalic),
    Font(R.font.nunito_bolditalic),
    Font(R.font.nunito_bold, FontWeight.ExtraBold),
    Font(R.font.nunito_blackitalic, FontWeight.Bold),
    Font(R.font.nunito_black, FontWeight.Black),
)