package com.example.echojournal.ui.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import org.intellij.lang.annotations.Language

@Language("AGSL")
private const val SHADER_CODE = """
    uniform float2 iResolution;
    uniform float iTime;
    uniform float3 iColor1;
    uniform float3 iColor2;

    float random(float2 st) {
        return fract(sin(dot(st.xy, float2(12.9898, 78.233))) * 43758.5453123);
    }

    float noise(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        float2 u = f * f * (3.0 - 2.0 * f);
        return mix(mix(random(i + float2(0.0, 0.0)), random(i + float2(1.0, 0.0)), u.x),
                   mix(random(i + float2(0.0, 1.0)), random(i + float2(1.0, 1.0)), u.x), u.y);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / iResolution.xy;
        
        // Fluid motion
        float n = noise(uv * 2.0 + iTime * 0.1);
        float n2 = noise(uv * 4.0 - iTime * 0.05);
        
        float3 color = mix(iColor1, iColor2, (n + n2) * 0.5);
        
        // Add some film grain/noise for texture
        float grain = random(uv + iTime) * 0.05;
        color += grain;

        return half4(color, 1.0);
    }
"""

@Language("AGSL")
private const val LIQUID_GLASS_SHADER = """
    uniform float2 iResolution;
    uniform float iTime;
    uniform float3 iColor1;
    uniform float3 iColor2;

    float random(float2 st) {
        return fract(sin(dot(st.xy, float2(12.9898, 78.233))) * 43758.5453123);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / iResolution.xy;
        
        // Distorted UV for liquid effect
        float2 p = uv * 2.0 - 1.0;
        p.x *= iResolution.x / iResolution.y;
        
        for(int i=1; i<4; i++) {
            float2 newP = p;
            newP.x += 0.4/float(i) * sin(float(i) * p.y + iTime + 0.3*float(i)) + 1.0;
            newP.y += 0.4/float(i) * sin(float(i) * p.x + iTime + 0.5*float(i)) + 1.5;
            p = newP;
        }
        
        float3 color = mix(iColor1, iColor2, 0.5 + 0.5 * sin(p.x + p.y));
        
        // Specular highlights (glass effect)
        float spec = pow(max(0.0, sin(p.x * 2.0)), 20.0);
        color += spec * 0.1;

        return half4(color, 1.0);
    }
"""

@Composable
fun FluidBackground(
    color1: Color,
    color2: Color,
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = false
) {
    // Advanced animation logic for "breathing" colors
    var displayColor1 by remember { mutableStateOf(color1) }
    var displayColor2 by remember { mutableStateOf(color2) }
    
    val intensity = remember { Animatable(0f) }

    LaunchedEffect(color1, color2) {
        // Fade out
        intensity.animateTo(0f, animationSpec = tween(600, easing = LinearOutSlowInEasing))
        
        // Switch colors at zero visibility
        displayColor1 = color1
        displayColor2 = color2
        
        // Fade in
        intensity.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shaderCode = if (isLiquidGlass) LIQUID_GLASS_SHADER else SHADER_CODE
        val shader = remember(isLiquidGlass) { RuntimeShader(shaderCode) }
        val infiniteTransition = rememberInfiniteTransition(label = "fluid")
        val time by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 100f,
            animationSpec = infiniteRepeatable(
                animation = tween(40000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "time"
        )

        Canvas(modifier = modifier.fillMaxSize()) {
            shader.setFloatUniform("iResolution", size.width, size.height)
            shader.setFloatUniform("iTime", time)
            
            // Mix with black based on intensity
            val c1 = displayColor1.copy(
                red = displayColor1.red * intensity.value,
                green = displayColor1.green * intensity.value,
                blue = displayColor1.blue * intensity.value
            )
            val c2 = displayColor2.copy(
                red = displayColor2.red * intensity.value,
                green = displayColor2.green * intensity.value,
                blue = displayColor2.blue * intensity.value
            )
            
            shader.setFloatUniform("iColor1", c1.red, c1.green, c1.blue)
            shader.setFloatUniform("iColor2", c2.red, c2.green, c2.blue)
            drawRect(brush = ShaderBrush(shader))
        }
    } else {
        Canvas(modifier = modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        displayColor1.copy(alpha = intensity.value),
                        displayColor2.copy(alpha = intensity.value)
                    )
                )
            )
        }
    }
}
