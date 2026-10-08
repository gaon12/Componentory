package xyz.gaon.componentory.survivor

internal object GameWaves {
    fun update(s: GameSession, random: () -> Float) {
        for (stage in 1..minOf(s.seconds / 300, 4)) {
            if (s.spawnedBosses.add(stage)) {
                if (s.enemies.size >= GameEngine.ENEMY_LIMIT) {
                    val index = s.enemies.indexOfFirst { it.kind != EnemyKind.BOSS }
                    if (index >= 0) s.enemies.removeAt(index)
                }
                val health = 500f + stage * 350f
                s.enemies +=
                    GameEnemy(
                        s.nextId++,
                        GameEngine.WIDTH / 2,
                        0f,
                        health,
                        health,
                        EnemyKind.BOSS,
                        stage,
                        source =
                            listOf(
                                GameFamily.OREO,
                                GameFamily.JELLY_BEAN,
                                GameFamily.KITKAT,
                                GameFamily.HONEYCOMB,
                            )[stage - 1],
                    )
            }
        }
        if (4 in s.spawnedBosses) return
        if (--s.spawnTicks > 0) return
        s.spawnTicks = (45 - s.seconds / 40).coerceAtLeast(10)
        if (s.enemies.size >= GameEngine.ENEMY_LIMIT) return
        val side = (random() * 4).toInt()
        val x =
            when (side) {
                0 -> 0f
                1 -> GameEngine.WIDTH
                else -> random() * GameEngine.WIDTH
            }
        val y =
            when (side) {
                2 -> 0f
                3 -> GameEngine.HEIGHT
                else -> random() * GameEngine.HEIGHT
            }
        val elite = s.seconds >= 60 && random() < .1f
        val health = (18f + s.seconds / 30f) * if (elite) 4f else 1f
        val source = GameFamily.entries[(random() * GameFamily.entries.size).toInt()]
        s.enemies +=
            GameEnemy(
                s.nextId++,
                x,
                y,
                health,
                health,
                if (elite) EnemyKind.ELITE else EnemyKind.NORMAL,
                source = source,
            )
    }
}
