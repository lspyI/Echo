package com.example.echojournal.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.Chat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiClient @Inject constructor() {
    private var apiKey: String = "YOUR_GEMINI_API_KEY_HERE"
    private var currentPersonality: String = "Психолог"

    fun updatePersonality(personality: String) {
        currentPersonality = personality
    }

    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-3.6-flash",
            apiKey = apiKey,
            requestOptions = RequestOptions(apiVersion = "v1"),
            systemInstruction = content {
                val instruction = when(currentPersonality) {
                    "Любящая девушка" -> "Ты — любящая, нежная и заботливая девушка. Обращайся к пользователю ласково (милый, родной, солнышко). Твоя цель — окутать его теплом. Анализируй мысли и отвечай на русском языке. НИКОГДА не пиши слова 'Настроение', 'Теги', 'Суммаризация'. СТРОГО соблюдай формат: [Mood] | [Tags] | [Your loving response]."
                    "Друг" -> "Ты — лучший бро. Общайся неформально, подбадривай, используй сленг. Анализируй мысли на русском. НИКОГДА не пиши слова 'Настроение', 'Теги', 'Суммаризация'. СТРОГО соблюдай формат: [Mood] | [Tags] | [Your friendly response]."
                    "Философ" -> "Ты — мудрый философ. Рассуждай о смысле происходящего, цитируй (ненавязчиво) великих, ищи глубокие связи. Анализируй мысли на русском. НИКОГДА не пиши слова 'Настроение', 'Теги', 'Суммаризация'. СТРОГО соблюдай формат: [Mood] | [Tags] | [Your philosophical reflection]."
                    "Строгий коуч" -> "Ты — строгий и результативный коуч. Будь прямолинеен, мотивируй к действию, не давай пользователю жалеть себя, ставь цели. Анализируй мысли на русском. НИКОГДА не пиши слова 'Настроение', 'Теги', 'Суммаризация'. СТРОГО соблюдай формат: [Mood] | [Tags] | [Your direct coaching advice]."
                    "Дзен-мастер" -> "Ты — спокойный Дзен-мастер. Твои ответы кратки, полны тишины и акцента на моменте 'здесь и сейчас'. Напоминай о дыхании и принятии. Анализируй мысли на русском. НИКОГДА не пиши слова 'Настроение', 'Теги', 'Суммаризация'. СТРОГО соблюдай формат: [Mood] | [Tags] | [Your zen insight]."
                    else -> "Ты — профессиональный эмпатичный психолог. Помогай анализировать чувства. Отвечай на русском. НИКОГДА не пиши слова 'Настроение', 'Теги', 'Суммаризация'. СТРОГО соблюдай формат: [Mood] | [Tags] | [Your analytical response]."
                }
                text(instruction)
            }
        )
    }

    private var currentChat: Chat? = null

    fun startChatSession() {
        currentChat = model.startChat()
    }

    suspend fun sendMessage(message: String): String {
        return try {
            if (currentChat == null) startChatSession()
            val response = currentChat?.sendMessage(message)
            val text = response?.text ?: "Я слушаю тебя..."
            text.split("|").lastOrNull()?.trim() ?: text
        } catch (e: Exception) {
            val msg = e.message ?: ""
            if (msg.contains("quota", true)) {
                "Эхо отдыхает. Пожалуйста, подождите минутку..."
            } else {
                "Ошибка связи: ${e.message}"
            }
        }
    }

    suspend fun analyzeMoodAndSummarize(transcript: String): Triple<String, String, String> {
        if (apiKey.isEmpty()) return Triple("Neutral", "", "API Key is missing")
        
        return try {
            val response = model.generateContent(transcript)
            val text = response.text ?: ""
            
            val parts = text.split("|")
            val moodCandidate = parts.getOrNull(0)?.trim() ?: "Neutral"
            val tags = parts.getOrNull(1)?.trim() ?: ""
            val summary = parts.getOrNull(2)?.trim() ?: "Мысль сохранена"

            val finalMood = when {
                moodCandidate.contains("Happy", true) || moodCandidate.contains("Радость", true) -> "Happy"
                moodCandidate.contains("Sad", true) || moodCandidate.contains("Грусть", true) -> "Sad"
                moodCandidate.contains("Stress", true) || moodCandidate.contains("Стресс", true) -> "Stressed"
                else -> "Neutral"
            }

            Triple(finalMood, tags, summary)
        } catch (e: Exception) {
            val msg = e.message ?: ""
            val userMessage = if (msg.contains("quota", true)) {
                "ИИ слишком загружен, подождите 30-60 секунд..."
            } else {
                "Анализ временно недоступен"
            }
            Triple("Neutral", "", userMessage)
        }
    }

    suspend fun generateWeeklyInsight(entries: List<String>): String {
        val prompt = "На основе этих записей сделай краткий психологический отчет за неделю на русском языке: ${entries.joinToString("\n")}"
        return try {
            val response = model.generateContent(prompt)
            response.text ?: "Не удалось проанализировать неделю."
        } catch (e: Exception) {
            "Ошибка ИИ: ${e.message}"
        }
    }
}
