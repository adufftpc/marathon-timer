package com.kartimer.util

import com.kartimer.data.entity.PilotEntity

object HandicapCalculator {

    private const val BASE_WEIGHT_MEN_KG = 80.0
    private const val BASE_WEIGHT_WOMEN_KG = 70.0

    // Seconds of handicap per kg over base weight
    // e.g. 2 seconds per kg over base
    private const val SECONDS_PER_KG = 2.0

    /**
     * Calculate handicap in seconds for a team based on pilot weights.
     * The assumption: heavier pilots give an advantage, so lighter ones get a start handicap.
     * Logic: compare average team weight vs a reference; if lighter, they start earlier (positive handicap).
     *
     * Simple formula used here:
     *   For each pilot over the base weight for their gender, add SECONDS_PER_KG * overweight.
     *   Since we don't track gender, we use the men's base of 80kg as the universal baseline.
     *   handicap = sum over pilots of max(0, (weight - BASE_WEIGHT) * SECONDS_PER_KG)
     *   Then divide by pilot count for an average.
     *
     * Returns handicap in integer seconds.
     */
    fun calculateHandicap(pilots: List<PilotEntity>): Int {
        val pilotsWithWeight = pilots.filter { it.weight != null && it.weight > 0 }
        if (pilotsWithWeight.isEmpty()) return 0

        val totalHandicapSeconds = pilotsWithWeight.sumOf { pilot ->
            val weight = pilot.weight ?: BASE_WEIGHT_MEN_KG
            val overWeight = weight - BASE_WEIGHT_MEN_KG
            if (overWeight > 0) overWeight * SECONDS_PER_KG else 0.0
        }

        return totalHandicapSeconds.toInt()
    }

    /**
     * Calculate per-pilot handicap contribution in seconds.
     */
    fun pilotHandicapSeconds(weight: Double?, isFemale: Boolean = false): Int {
        val base = if (isFemale) BASE_WEIGHT_WOMEN_KG else BASE_WEIGHT_MEN_KG
        val w = weight ?: base
        val over = w - base
        return if (over > 0) (over * SECONDS_PER_KG).toInt() else 0
    }
}
