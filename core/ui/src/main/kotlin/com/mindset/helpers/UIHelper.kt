package com.mindset.helpers

import com.mindset.MindSetIcons
import com.mindset.icons.Bolt
import com.mindset.icons.Burpee
import com.mindset.icons.Dumbbell
import com.mindset.icons.LowerBody
import com.mindset.icons.Rowing
import com.mindset.icons.Run
import com.mindset.icons.SkiErg
import com.mindset.icons.SledPull
import com.mindset.icons.WallBall

object UIHelper {
    fun stationIcon(segmentKey: String?): androidx.compose.ui.graphics.vector.ImageVector = when {
        segmentKey == null -> MindSetIcons.Bolt
        "run" in segmentKey -> MindSetIcons.Run
        "ski" in segmentKey -> MindSetIcons.SkiErg
        "sled" in segmentKey -> MindSetIcons.SledPull
        "burpee" in segmentKey -> MindSetIcons.Burpee
        "rowing" in segmentKey -> MindSetIcons.Rowing
        "farmers" in segmentKey -> MindSetIcons.Dumbbell
        "sandbag" in segmentKey || "lunge" in segmentKey -> MindSetIcons.LowerBody
        "wall-ball" in segmentKey -> MindSetIcons.WallBall
        else -> MindSetIcons.Bolt
    }
}
