package dev.ragnarok.fenrir.view.media;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;

import dev.ragnarok.fenrir.util.Utils;

public class PathAnimator {

    private final Path path = new Path();
    private final float scale;
    private final float tx;
    private final float ty;
    private final float durationScale;
    private final ArrayList<KeyFrame> keyFrames = new ArrayList<>();

    public PathAnimator(float sc, float x, float y, float dsc) {
        scale = sc;
        tx = x;
        ty = y;
        durationScale = dsc;
    }

    public float getDurationScale() {
        return durationScale;
    }

    public void addSvgKeyFrame(String svg, float ms) {
        if (svg == null) {
            return;
        }
        try {
            KeyFrame keyFrame = new KeyFrame();
            keyFrame.time = ms * durationScale;
            String[] args = svg.split(" ");
            for (int a = 0; a < args.length; a++) {
                switch (args[a].charAt(0)) {
                    case 'M': {
                        MoveTo moveTo = new MoveTo();
                        moveTo.x = (Float.parseFloat(args[a + 1]) + tx) * scale;
                        moveTo.y = (Float.parseFloat(args[a + 2]) + ty) * scale;
                        keyFrame.commands.add(moveTo);
                        a += 2;
                        break;
                    }
                    case 'C': {
                        CurveTo curveTo = new CurveTo();
                        curveTo.x1 = (Float.parseFloat(args[a + 1]) + tx) * scale;
                        curveTo.y1 = (Float.parseFloat(args[a + 2]) + ty) * scale;
                        curveTo.x2 = (Float.parseFloat(args[a + 3]) + tx) * scale;
                        curveTo.y2 = (Float.parseFloat(args[a + 4]) + ty) * scale;
                        curveTo.x = (Float.parseFloat(args[a + 5]) + tx) * scale;
                        curveTo.y = (Float.parseFloat(args[a + 6]) + ty) * scale;
                        keyFrame.commands.add(curveTo);
                        a += 6;
                        break;
                    }
                    case 'L': {
                        LineTo lineTo = new LineTo();
                        lineTo.x = (Float.parseFloat(args[a + 1]) + tx) * scale;
                        lineTo.y = (Float.parseFloat(args[a + 2]) + ty) * scale;
                        keyFrame.commands.add(lineTo);
                        a += 2;
                        break;
                    }
                }
            }
            keyFrames.add(keyFrame);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Canvas canvas, Paint paint, float time) {
        KeyFrame startKeyFrame = null;
        KeyFrame endKeyFrame = null;
        for (int a = 0, N = keyFrames.size(); a < N; a++) {
            KeyFrame keyFrame = keyFrames.get(a);
            if ((startKeyFrame == null || startKeyFrame.time < keyFrame.time) && keyFrame.time <= time) {
                startKeyFrame = keyFrame;
            }
            if ((endKeyFrame == null || endKeyFrame.time > keyFrame.time) && keyFrame.time >= time) {
                endKeyFrame = keyFrame;
            }
        }
        if (endKeyFrame == startKeyFrame) {
            startKeyFrame = null;
        }
        if (startKeyFrame != null && endKeyFrame == null) {
            endKeyFrame = startKeyFrame;
            startKeyFrame = null;
        }
        if (endKeyFrame == null || startKeyFrame != null && startKeyFrame.commands.size() != endKeyFrame.commands.size()) {
            return;
        }
        path.reset();
        for (int a = 0, N = endKeyFrame.commands.size(); a < N; a++) {
            Object startCommand = startKeyFrame != null ? startKeyFrame.commands.get(a) : null;
            Object endCommand = endKeyFrame.commands.get(a);
            if (startCommand != null && startCommand.getClass() != endCommand.getClass()) {
                return;
            }
            float progress;
            if (startKeyFrame != null) {
                progress = (time - startKeyFrame.time) / (endKeyFrame.time - startKeyFrame.time);
            } else {
                progress = 1.0f;
            }
            if (endCommand instanceof MoveTo) {
                MoveTo end = (MoveTo) endCommand;
                MoveTo start = (MoveTo) startCommand;
                if (start != null) {
                    path.moveTo(Utils.dp(start.x + (end.x - start.x) * progress), Utils.dp(start.y + (end.y - start.y) * progress));
                } else {
                    path.moveTo(Utils.dp(end.x), Utils.dp(end.y));
                }
            } else if (endCommand instanceof LineTo) {
                LineTo end = (LineTo) endCommand;
                LineTo start = (LineTo) startCommand;
                if (start != null) {
                    path.lineTo(Utils.dp(start.x + (end.x - start.x) * progress), Utils.dp(start.y + (end.y - start.y) * progress));
                } else {
                    path.lineTo(Utils.dp(end.x), Utils.dp(end.y));
                }
            } else if (endCommand instanceof CurveTo) {
                CurveTo end = (CurveTo) endCommand;
                CurveTo start = (CurveTo) startCommand;
                if (start != null) {
                    path.cubicTo(Utils.dp(start.x1 + (end.x1 - start.x1) * progress), Utils.dp(start.y1 + (end.y1 - start.y1) * progress),
                            Utils.dp(start.x2 + (end.x2 - start.x2) * progress), Utils.dp(start.y2 + (end.y2 - start.y2) * progress),
                            Utils.dp(start.x + (end.x - start.x) * progress), Utils.dp(start.y + (end.y - start.y) * progress));
                } else {
                    path.cubicTo(Utils.dp(end.x1), Utils.dp(end.y1), Utils.dp(end.x2), Utils.dp(end.y2), Utils.dp(end.x), Utils.dp(end.y));
                }
            }
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    private static class KeyFrame {
        public final ArrayList<Object> commands = new ArrayList<>();
        public float time;
    }

    private static class MoveTo {
        public float x;
        public float y;
    }

    private static class LineTo {
        public float x;
        public float y;
    }

    private static class CurveTo {
        public float x;
        public float y;
        public float x1;
        public float y1;
        public float x2;
        public float y2;
    }
}
