package org.moon.figura.ducks;

import com.mojang.blaze3d.vertex.PoseStack;

public interface PoseStackAccessor {

    void pushPose(PoseStack.Pose pose);
}
