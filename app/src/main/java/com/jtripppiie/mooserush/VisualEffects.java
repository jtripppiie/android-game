package com.jtripppiie.mooserush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

final class VisualEffects {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final List<Particle> particles = new ArrayList<>();
    private final List<ScorePopup> scorePopups = new ArrayList<>();
    private final ReusableObjectPool<Particle> particlePool =
            new ReusableObjectPool<>(80, Particle::new);
    private final ReusableObjectPool<ScorePopup> popupPool =
            new ReusableObjectPool<>(12, ScorePopup::new);
    private final float density;

    VisualEffects(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
    }

    void clearAll() {
        particlePool.releaseAll(particles);
        popupPool.releaseAll(scorePopups);
    }

    void clearParticles() {
        particlePool.releaseAll(particles);
    }

    void update(float dt) {
        Iterator<Particle> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            Particle particle = particleIterator.next();
            particle.age += dt;
            particle.x += particle.vx * dt;
            particle.y += particle.vy * dt;
            particle.vy += dp(280) * dt;
            if (particle.age >= particle.life) {
                particleIterator.remove();
                particlePool.release(particle);
            }
        }

        Iterator<ScorePopup> popupIterator = scorePopups.iterator();
        while (popupIterator.hasNext()) {
            ScorePopup popup = popupIterator.next();
            popup.age += dt;
            popup.y += popup.vy * dt;
            if (popup.age >= popup.life) {
                popupIterator.remove();
                popupPool.release(popup);
            }
        }
    }

    void spawnScorePopup(String label, float x, float y, int color) {
        ScorePopup popup = popupPool.acquire();
        popup.reset(label, x, y, -dp(36), color, 0.78f);
        scorePopups.add(popup);
        while (scorePopups.size() > 8) {
            popupPool.release(scorePopups.remove(0));
        }
    }

    void spawnSparkBurst(float x, float y, int count, int color) {
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat() * (float) Math.PI * 2f;
            float speed = dp(42 + random.nextFloat() * 92);
            spawnParticle(
                    x,
                    y,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed - dp(30),
                    dp(1.8f + random.nextFloat() * 2.4f),
                    color,
                    0.32f + random.nextFloat() * 0.24f
            );
        }
    }

    void spawnDustBurst(float x, float y, int count, int color) {
        for (int i = 0; i < count; i++) {
            spawnParticle(
                    x + (random.nextFloat() - 0.5f) * dp(28),
                    y - dp(5 + random.nextFloat() * 5),
                    (random.nextFloat() - 0.5f) * dp(84),
                    -dp(18 + random.nextFloat() * 46),
                    dp(3.0f + random.nextFloat() * 4.2f),
                    color,
                    0.34f + random.nextFloat() * 0.24f
            );
        }
    }

    void spawnParticle(float x, float y, float vx, float vy, float radius, int color, float life) {
        Particle particle = particlePool.acquire();
        particle.reset(x, y, vx, vy, radius, color, life);
        particles.add(particle);
        while (particles.size() > 70) {
            particlePool.release(particles.remove(0));
        }
    }

    void drawParticles(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        for (Particle particle : particles) {
            float remaining = 1f - particle.age / particle.life;
            int alpha = Math.max(0, Math.min(255, Math.round(Color.alpha(particle.color) * remaining)));
            paint.setColor(Color.argb(alpha, Color.red(particle.color), Color.green(particle.color), Color.blue(particle.color)));
            canvas.drawCircle(particle.x, particle.y, particle.radius * (0.65f + remaining * 0.55f), paint);
        }
    }

    void drawScorePopups(Canvas canvas) {
        textPaint.setTextSize(dp(14));
        for (ScorePopup popup : scorePopups) {
            float remaining = 1f - popup.age / popup.life;
            int alpha = Math.max(0, Math.min(255, Math.round(255 * remaining)));
            textPaint.setColor(Color.argb(alpha, 12, 18, 28));
            canvas.drawText(popup.label, popup.x + dp(1), popup.y + dp(1), textPaint);
            textPaint.setColor(Color.argb(alpha, Color.red(popup.color), Color.green(popup.color), Color.blue(popup.color)));
            canvas.drawText(popup.label, popup.x, popup.y, textPaint);
        }
    }

    private float dp(float value) {
        return value * density;
    }

    private static class Particle {
        float x;
        float y;
        float vx;
        float vy;
        float radius;
        int color;
        float life;
        float age = 0f;

        Particle() {
        }

        void reset(float x, float y, float vx, float vy, float radius, int color, float life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
            this.color = color;
            this.life = life;
            this.age = 0f;
        }
    }

    private static class ScorePopup {
        String label;
        float x;
        float y;
        float vy;
        int color;
        float life;
        float age = 0f;

        ScorePopup() {
        }

        void reset(String label, float x, float y, float vy, int color, float life) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.vy = vy;
            this.color = color;
            this.life = life;
            this.age = 0f;
        }
    }
}
