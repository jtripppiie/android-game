package com.jtripppiie.mooserush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * WeatherSystem adds environmental "juice" like falling snow, heat haze, and flow-state speed lines.
 * It operates independently of gameplay logic to ensure it doesn't break core mechanics.
 */
final class WeatherSystem {
    private final List<WeatherParticle> particles = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final float density;
    private int width, height;

    WeatherSystem(float density) {
        this.density = density;
    }

    void onSizeChanged(int w, int h) {
        this.width = w;
        this.height = h;
        particles.clear();
        for (int i = 0; i < 50; i++) {
            particles.add(createParticle());
        }
    }

    void update(float dt, int stage, int season, boolean flowActive, float worldSpeed) {
        boolean winter = season == 1 || stage == 4;
        boolean sun = stage == 0;

        int targetCount = flowActive ? 80 : 40;
        if (particles.size() < targetCount) {
            particles.add(createParticle());
        }

        for (int i = 0; i < particles.size(); i++) {
            WeatherParticle p = particles.get(i);
            p.age += dt;

            if (flowActive) {
                p.x -= dp(800) * dt * worldSpeed;
                p.y += (random.nextFloat() - 0.5f) * dp(10);
            } else if (winter) {
                p.x -= dp(40 + p.speedScale * 20) * dt * worldSpeed;
                p.y += dp(60 + p.speedScale * 30) * dt;
            } else if (sun) {
                p.x -= dp(30) * dt * worldSpeed;
                p.y -= dp(20 + p.speedScale * 10) * dt;
                p.x += (float) (Math.sin(p.age * 2f + p.speedScale) * dp(5) * dt);
            } else {
                p.x -= dp(20) * dt * worldSpeed;
                p.y += (float) (Math.sin(p.age) * dp(2) * dt);
            }

            if (p.x < -dp(100)) {
                p.x = width + dp(50);
                p.y = random.nextFloat() * height;
            }
            if (p.y > height + dp(50)) {
                p.y = -dp(40);
                p.x = random.nextFloat() * width;
            }
            if (p.y < -dp(50)) {
                p.y = height + dp(40);
                p.x = random.nextFloat() * width;
            }
        }
    }

    void draw(Canvas canvas, int stage, int season, boolean flowActive) {
        boolean winter = season == 1 || stage == 4;
        boolean sun = stage == 0;

        for (WeatherParticle p : particles) {
            if (flowActive) {
                paint.setColor(Color.argb(100, 255, 255, 255));
                paint.setStrokeWidth(dp(1.5f));
                canvas.drawLine(p.x, p.y, p.x + dp(40), p.y, paint);
            } else if (winter) {
                paint.setColor(Color.argb(180, 255, 255, 255));
                canvas.drawCircle(p.x, p.y, dp(1.5f + p.speedScale * 1.5f), paint);
            } else if (sun) {
                paint.setColor(Color.argb(120, 255, 200, 100));
                canvas.drawCircle(p.x, p.y, dp(1.0f + p.speedScale), paint);
            } else {
                paint.setColor(Color.argb(60, 255, 255, 200));
                canvas.drawCircle(p.x, p.y, dp(1.0f), paint);
            }
        }
    }

    private WeatherParticle createParticle() {
        WeatherParticle p = new WeatherParticle();
        p.x = random.nextFloat() * (width + dp(100));
        p.y = random.nextFloat() * height;
        p.speedScale = random.nextFloat();
        p.age = random.nextFloat() * 10f;
        return p;
    }

    private float dp(float value) {
        return value * density;
    }

    private static class WeatherParticle {
        float x, y, speedScale, age;
    }
}
