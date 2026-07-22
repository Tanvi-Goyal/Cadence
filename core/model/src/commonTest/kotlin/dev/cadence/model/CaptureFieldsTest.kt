package dev.cadence.model

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The compiler already guarantees totality (the `when` in [CaptureFields.of] has no `else`, so a new
 * [MetricType] won't compile until it's mapped). This test pins the specific mappings so a wrong
 * edit is caught, and asserts every metric resolves.
 */
class CaptureFieldsTest {

    @Test
    fun maps_each_metric_to_its_fields() {
        assertEquals(CaptureFields.WeightReps, CaptureFields.of(MetricType.WEIGHT_REPS))
        assertEquals(CaptureFields.RepsOnly, CaptureFields.of(MetricType.REPS_ONLY))
        assertEquals(CaptureFields.DistanceTime, CaptureFields.of(MetricType.DISTANCE_TIME))
        assertEquals(CaptureFields.Duration, CaptureFields.of(MetricType.DURATION))
        assertEquals(CaptureFields.Calories, CaptureFields.of(MetricType.CALORIES))
    }

    @Test
    fun every_metric_resolves() {
        assertEquals(MetricType.entries.size, MetricType.entries.map { CaptureFields.of(it) }.size)
    }
}
