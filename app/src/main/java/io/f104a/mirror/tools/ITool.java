package io.f104a.mirror.tools;

import android.content.Context;
import android.graphics.drawable.Icon;

import io.f104a.mirror.controllers.CameraStreamController;

public interface ITool {
    String id();
    String getTitle();
    Icon getIconGlyph();
    boolean isAvailable();
    void onClick(CameraStreamController cameraStreamController, ToolContext context);
}
