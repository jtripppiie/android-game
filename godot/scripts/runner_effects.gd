class_name RunnerEffects
extends Node2D

const GRAVITY := 260.0
const MAX_PARTICLES := 30

var particles: Array[Dictionary] = []


func _ready() -> void:
	set_process(false)


func emit_snow(power := 1.0) -> void:
	if GameSession.reduced_motion:
		return
	var count := clampi(roundi(7.0 + power * 7.0), 7, 16)
	for index in range(count):
		var ratio := index / float(maxi(1, count - 1))
		var direction := lerpf(-1.0, 1.0, ratio)
		var speed := 72.0 + float((index * 37) % 55) + power * 30.0
		particles.append({
			"position": Vector2(direction * 9.0, -4.0),
			"velocity": Vector2(direction * speed, -58.0 - float((index * 29) % 46)),
			"life": 0.44 + float(index % 3) * 0.05,
			"total": 0.54,
			"radius": 2.3 + float(index % 3) * 0.75
		})
	if particles.size() > MAX_PARTICLES:
		particles = particles.slice(particles.size() - MAX_PARTICLES)
	set_process(true)
	queue_redraw()


func emit_hit() -> void:
	emit_snow(1.35)


func clear() -> void:
	particles.clear()
	set_process(false)
	queue_redraw()


func _process(delta: float) -> void:
	for index in range(particles.size() - 1, -1, -1):
		var particle := particles[index]
		particle.life = float(particle.life) - delta
		if float(particle.life) <= 0.0:
			particles.remove_at(index)
			continue
		var velocity: Vector2 = particle.velocity
		velocity.y += GRAVITY * delta
		particle.velocity = velocity
		particle.position = Vector2(particle.position) + velocity * delta
		particles[index] = particle
	if particles.is_empty():
		set_process(false)
	queue_redraw()


func _draw() -> void:
	for particle in particles:
		var alpha := clampf(float(particle.life) / float(particle.total), 0.0, 1.0)
		var color := Color(0.82, 0.96, 1.0, alpha * 0.86)
		draw_circle(Vector2(particle.position), float(particle.radius), color)
