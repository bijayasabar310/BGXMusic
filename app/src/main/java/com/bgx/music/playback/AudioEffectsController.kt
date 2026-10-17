package com.bgx.music.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

data class EqBand(val index: Short, val frequencyHz: Int, val levelMillibel: Short, val minMb: Short, val maxMb: Short)

data class AudioEffectsCapabilities(
    val equalizerAvailable: Boolean,
    val bassBoostAvailable: Boolean,
    val virtualizerAvailable: Boolean,
    val loudnessEnhancerAvailable: Boolean
)

@Singleton
class AudioEffectsController @Inject constructor() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var attachedSessionId: Int = -1

    fun attach(audioSessionId: Int) {
        if (audioSessionId == attachedSessionId && audioSessionId != 0) return
        release()
        attachedSessionId = audioSessionId
        if (audioSessionId == 0) return

        equalizer = runCatching { Equalizer(0, audioSessionId).apply { enabled = true } }
            .onFailure { Log.w(TAG, "Equalizer unavailable", it) }.getOrNull()

        bassBoost = runCatching { BassBoost(0, audioSessionId).apply { enabled = false } }
            .onFailure { Log.w(TAG, "BassBoost unavailable", it) }.getOrNull()

        virtualizer = runCatching { Virtualizer(0, audioSessionId).apply { enabled = false } }
            .onFailure { Log.w(TAG, "Virtualizer unavailable", it) }.getOrNull()

        loudnessEnhancer = runCatching { LoudnessEnhancer(audioSessionId).apply { enabled = false } }
            .onFailure { Log.w(TAG, "LoudnessEnhancer unavailable", it) }.getOrNull()
    }

    fun capabilities(): AudioEffectsCapabilities = AudioEffectsCapabilities(
        equalizerAvailable = equalizer != null,
        bassBoostAvailable = bassBoost?.strengthSupported == true,
        virtualizerAvailable = virtualizer?.strengthSupported == true,
        loudnessEnhancerAvailable = loudnessEnhancer != null
    )

    fun getBands(): List<EqBand> {
        val eq = equalizer ?: return emptyList()
        return runCatching {
            (0 until eq.numberOfBands).map { i ->
                val idx = i.toShort()
                val range = eq.bandLevelRange
                EqBand(
                    index = idx,
                    frequencyHz = eq.getCenterFreq(idx) / 1000,
                    levelMillibel = eq.getBandLevel(idx),
                    minMb = range[0],
                    maxMb = range[1]
                )
            }
        }.getOrDefault(emptyList())
    }

    fun setBandLevel(band: Short, levelMillibel: Short) {
        runCatching { equalizer?.setBandLevel(band, levelMillibel) }
    }

    fun getPresetNames(): List<String> = runCatching {
        val eq = equalizer ?: return emptyList()
        (0 until eq.numberOfPresets).map { eq.getPresetName(it.toShort()) }
    }.getOrDefault(emptyList())

    fun usePreset(index: Short) {
        runCatching { equalizer?.usePreset(index) }
    }

    fun setBassBoostStrength(strength: Short) {
        runCatching {
            bassBoost?.let {
                it.enabled = strength > 0
                if (it.strengthSupported) it.setStrength(strength)
            }
        }
    }

    fun setVirtualizerStrength(strength: Short) {
        runCatching {
            virtualizer?.let {
                it.enabled = strength > 0
                if (it.strengthSupported) it.setStrength(strength)
            }
        }
    }

    fun setLoudnessGainMillibel(gain: Int) {
        runCatching {
            loudnessEnhancer?.let {
                it.enabled = gain > 0
                it.setTargetGain(gain)
            }
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        runCatching { equalizer?.enabled = enabled }
    }

    fun release() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        runCatching { loudnessEnhancer?.release() }
        equalizer = null; bassBoost = null; virtualizer = null; loudnessEnhancer = null
        attachedSessionId = -1
    }

    companion object {
        private const val TAG = "AudioEffectsController"
    }
}
