package xyz.gaon.componentory.survivor

import org.json.JSONArray
import org.json.JSONObject

/** Explicit fields keep saves independent from reflection and Android UI classes. */
internal object GameJson {
    private fun obj(vararg fields: Pair<String, Any?>) =
        JSONObject().apply {
            fields.forEach { (name, value) -> put(name, value ?: JSONObject.NULL) }
        }

    private fun array(values: Iterable<*>) = JSONArray().apply { values.forEach { put(it) } }

    private fun JSONObject.rows(name: String, limit: Int = 100000): List<JSONObject> {
        val a = getJSONArray(name)
        require(a.length() <= limit)
        return List(a.length()) { a.getJSONObject(it) }
    }

    private fun JSONObject.names(name: String): List<String> {
        val a = getJSONArray(name)
        require(a.length() <= 256)
        return List(a.length()) { a.getString(it) }
    }

    private fun JSONObject.nullable(name: String) = if (isNull(name)) null else getString(name)

    private fun levels(p: PermanentLevels) =
        obj("health" to p.health, "damage" to p.damage, "experience" to p.experience)

    private fun levels(o: JSONObject) =
        PermanentLevels(o.getInt("health"), o.getInt("damage"), o.getInt("experience"))

    private fun supports(items: Map<SupportId, Int>) =
        array(
            items.entries
                .sortedBy { it.key.ordinal }
                .map { obj("id" to it.key.name, "level" to it.value) }
        )

    private fun supports(o: JSONObject) =
        o.rows("supports", 4)
            .associate { SupportId.valueOf(it.getString("id")) to it.getInt("level") }
            .also { require(it.values.all { level -> level in 1..5 }) }

    private fun weapon(w: FinalWeapon) =
        obj("id" to w.id.name, "level" to w.level, "evolved" to w.evolved)

    private fun weapon(o: JSONObject) =
        FinalWeapon(WeaponId.valueOf(o.getString("id")), o.getInt("level"), o.getBoolean("evolved"))
            .also { require(it.level in 1..5 && (!it.evolved || it.level == 5)) }

    fun session(s: GameSession): String =
        obj(
                "id" to s.id,
                "startingWeapon" to s.startingWeapon.name,
                "mode" to s.mode.name,
                "seed" to s.seed,
                "ruleset" to s.ruleset,
                "startedAt" to s.startedAtEpochMillis,
                "profileId" to s.rankedProfileId,
                "permanent" to levels(s.permanent),
                "unlocked" to array(s.unlocked.sortedBy { it.ordinal }.map { it.name }),
                "randomState" to s.randomState,
                "offerRandomState" to s.offerRandomState,
                "tick" to s.tick,
                "nextId" to s.nextId,
                "x" to s.x,
                "y" to s.y,
                "health" to s.health,
                "experienceRemainder" to s.experienceRemainder,
                "experience" to s.experience,
                "level" to s.level,
                "skillTicks" to s.skillTicks,
                "shieldTicks" to s.shieldTicks,
                "freezeTicks" to s.freezeTicks,
                "hurtTicks" to s.hurtTicks,
                "spawnTicks" to s.spawnTicks,
                "regularKills" to s.regularKills,
                "eliteKills" to s.eliteKills,
                "bossKills" to s.bossKills,
                "bonusCurrency" to s.bonusCurrency,
                "outcome" to s.outcome.name,
                "spawnedBosses" to array(s.spawnedBosses.sorted()),
                "weapons" to
                    array(
                        s.weapons.map {
                            weapon(FinalWeapon(it.id, it.level, it.evolved)).put("ticks", it.ticks)
                        }
                    ),
                "supports" to supports(s.supports),
                "choices" to
                    array(
                        s.choices.map {
                            obj(
                                "kind" to it.kind.name,
                                "weapon" to it.weapon?.name,
                                "support" to it.support?.name,
                            )
                        }
                    ),
                "enemies" to
                    array(
                        s.enemies.map {
                            obj(
                                "id" to it.id,
                                "x" to it.x,
                                "y" to it.y,
                                "health" to it.health,
                                "maxHealth" to it.maxHealth,
                                "kind" to it.kind.name,
                                "bossStage" to it.bossStage,
                                "attackTicks" to it.attackTicks,
                                "source" to it.source.name,
                            )
                        }
                    ),
                "shots" to
                    array(
                        s.shots.map {
                            obj(
                                "id" to it.id,
                                "x" to it.x,
                                "y" to it.y,
                                "vx" to it.vx,
                                "vy" to it.vy,
                                "damage" to it.damage,
                                "life" to it.life,
                                "art" to it.art,
                                "pierce" to it.pierce,
                                "radius" to it.radius,
                                "orbit" to it.orbit,
                                "homing" to it.homing,
                                "hostile" to it.hostile,
                                "angle" to it.angle,
                                "delay" to it.delay,
                                "bounces" to it.bounces,
                                "hits" to array(it.hits.sorted()),
                            )
                        }
                    ),
                "drops" to
                    array(s.drops.map { obj("x" to it.x, "y" to it.y, "amount" to it.amount) }),
            )
            .toString()

    fun session(json: String): GameSession {
        val o = JSONObject(json)
        val s =
            GameSession(
                o.getString("id"),
                WeaponId.valueOf(o.getString("startingWeapon")),
                RunMode.valueOf(o.getString("mode")),
                o.getLong("seed"),
                levels(o.getJSONObject("permanent")),
                o.names("unlocked").map { SupportId.valueOf(it) }.toSet(),
                o.getString("ruleset"),
                o.getLong("startedAt"),
                o.nullable("profileId"),
            )
        s.randomState = o.getLong("randomState")
        s.offerRandomState = o.getLong("offerRandomState")
        s.tick = o.getInt("tick")
        s.nextId = o.getInt("nextId")
        s.x = o.getDouble("x").toFloat()
        s.y = o.getDouble("y").toFloat()
        s.health = o.getDouble("health").toFloat()
        s.experienceRemainder = o.getDouble("experienceRemainder")
        s.experience = o.getInt("experience")
        s.level = o.getInt("level")
        s.skillTicks = o.getInt("skillTicks")
        s.shieldTicks = o.getInt("shieldTicks")
        s.freezeTicks = o.getInt("freezeTicks")
        s.hurtTicks = o.getInt("hurtTicks")
        s.spawnTicks = o.getInt("spawnTicks")
        s.regularKills = o.getInt("regularKills")
        s.eliteKills = o.getInt("eliteKills")
        s.bossKills = o.getInt("bossKills")
        s.bonusCurrency = o.getInt("bonusCurrency")
        s.outcome = RunOutcome.valueOf(o.getString("outcome"))
        val bosses = o.getJSONArray("spawnedBosses")
        repeat(bosses.length()) {
            s.spawnedBosses += bosses.getInt(it).also { stage -> require(stage in 1..4) }
        }
        s.weapons.clear()
        o.rows("weapons", 4).forEach { row ->
            val w = weapon(row)
            s.weapons += GameWeapon(w.id, w.level, w.evolved, row.getInt("ticks"))
        }
        s.supports.putAll(supports(o))
        o.rows("choices", 3).forEach { row ->
            s.choices +=
                UpgradeChoice(
                    UpgradeKind.valueOf(row.getString("kind")),
                    row.nullable("weapon")?.let { WeaponId.valueOf(it) },
                    row.nullable("support")?.let { SupportId.valueOf(it) },
                )
        }
        o.rows("enemies", GameEngine.ENEMY_LIMIT).forEach { e ->
            s.enemies +=
                GameEnemy(
                    e.getInt("id"),
                    e.getDouble("x").toFloat(),
                    e.getDouble("y").toFloat(),
                    e.getDouble("health").toFloat(),
                    e.getDouble("maxHealth").toFloat(),
                    EnemyKind.valueOf(e.getString("kind")),
                    e.getInt("bossStage"),
                    e.getInt("attackTicks"),
                    GameFamily.valueOf(e.getString("source")),
                )
        }
        o.rows("shots", GameEngine.SHOT_LIMIT).forEach { e ->
            val hits = e.getJSONArray("hits")
            require(hits.length() <= 100000)
            s.shots +=
                GameShot(
                    e.getInt("id"),
                    e.getDouble("x").toFloat(),
                    e.getDouble("y").toFloat(),
                    e.getDouble("vx").toFloat(),
                    e.getDouble("vy").toFloat(),
                    e.getDouble("damage").toFloat(),
                    e.getInt("life"),
                    e.getString("art"),
                    e.getInt("pierce"),
                    e.getDouble("radius").toFloat(),
                    e.getBoolean("orbit"),
                    e.getBoolean("homing"),
                    e.getBoolean("hostile"),
                    e.getDouble("angle").toFloat(),
                    e.getInt("delay"),
                    e.getInt("bounces"),
                    MutableList(hits.length()) { hits.getInt(it) }.toMutableSet(),
                )
        }
        o.rows("drops", GameEngine.DROP_LIMIT).forEach { e ->
            s.drops +=
                ExperienceDrop(
                    e.getDouble("x").toFloat(),
                    e.getDouble("y").toFloat(),
                    e.getInt("amount"),
                )
        }
        validate(s)
        return s
    }

    private fun validate(s: GameSession) {
        require(s.id.isNotBlank() && s.ruleset.isNotBlank() && s.startedAtEpochMillis >= 0)
        require(s.randomState != 0L && s.offerRandomState != 0L)
        require(
            s.tick >= 0 &&
                s.level >= 1 &&
                s.experience >= 0 &&
                s.experienceRemainder in 0.0..0.999999999999
        )
        require(
            s.x in 0f..GameEngine.WIDTH &&
                s.y in 0f..GameEngine.HEIGHT &&
                s.health in 0f..s.maxHealth
        )
        require(
            listOf(
                    s.skillTicks,
                    s.shieldTicks,
                    s.freezeTicks,
                    s.hurtTicks,
                    s.regularKills,
                    s.eliteKills,
                    s.bossKills,
                    s.bonusCurrency,
                )
                .all { it >= 0 }
        )
        require(s.weapons.isNotEmpty() && s.weapons.map { it.id }.distinct().size == s.weapons.size)
        require(s.choices.map { it.key }.distinct().size == s.choices.size)
        require(s.choices.isEmpty() || s.experience >= s.requiredExperience)
        require(
            s.choices.all {
                it.kind in setOf(UpgradeKind.HEAL, UpgradeKind.CURRENCY, UpgradeKind.RECOVERY) ||
                    it in GameGrowth.available(s)
            }
        )
        val ids = s.enemies.map { it.id } + s.shots.map { it.id }
        require(ids.distinct().size == ids.size && ids.all { it > 0 && it < s.nextId })
        require(
            s.enemies.all {
                it.x.isFinite() &&
                    it.y.isFinite() &&
                    it.health.isFinite() &&
                    it.maxHealth > 0 &&
                    it.maxHealth.isFinite() &&
                    it.health <= it.maxHealth &&
                    it.bossStage in 0..4
            }
        )
        val art =
            GameFamily.entries.map { it.art }.toSet() +
                setOf("button", "slider", "switch", "spinner", "progress")
        require(
            s.shots.all {
                it.art in art &&
                    listOf(it.x, it.y, it.vx, it.vy, it.damage, it.radius, it.angle).all { value ->
                        value.isFinite()
                    } &&
                    it.life > 0 &&
                    it.damage >= 0 &&
                    it.radius > 0 &&
                    it.delay >= 0 &&
                    it.bounces >= 0
            }
        )
        require(s.drops.all { it.x.isFinite() && it.y.isFinite() && it.amount > 0 })
        if (s.mode == RunMode.RANKED)
            require(s.permanent == PermanentLevels() && s.unlocked == SupportId.entries.toSet())
    }

    private fun record(r: GameRunRecord) =
        obj(
            "id" to r.id,
            "startedAt" to r.startedAtEpochMillis,
            "completedAt" to r.completedAtEpochMillis,
            "playerArt" to r.playerArt,
            "startingWeapon" to r.startingWeapon.name,
            "mode" to r.mode.name,
            "ruleset" to r.ruleset,
            "seed" to r.seed,
            "permanent" to levels(r.permanent),
            "weapons" to array(r.weapons.map(::weapon)),
            "supports" to supports(r.supports),
            "survivalTicks" to r.survivalTicks,
            "regularKills" to r.regularKills,
            "eliteKills" to r.eliteKills,
            "bossKills" to r.bossKills,
            "outcome" to r.outcome.name,
            "score" to r.score,
            "currency" to r.currency,
        )

    private fun record(o: JSONObject) =
        GameRunRecord(
                o.getString("id"),
                o.getLong("startedAt"),
                o.getLong("completedAt"),
                o.getString("playerArt"),
                WeaponId.valueOf(o.getString("startingWeapon")),
                RunMode.valueOf(o.getString("mode")),
                o.getString("ruleset"),
                o.getLong("seed"),
                levels(o.getJSONObject("permanent")),
                o.rows("weapons", 4).map(::weapon),
                supports(o),
                o.getInt("survivalTicks"),
                o.getInt("regularKills"),
                o.getInt("eliteKills"),
                o.getInt("bossKills"),
                RunOutcome.valueOf(o.getString("outcome")),
                o.getLong("score"),
                o.getLong("currency"),
            )
            .also { require(it.outcome != RunOutcome.ACTIVE && it.score >= 0 && it.currency >= 0) }

    fun save(s: GameSave): String =
        obj(
                "schema" to 1,
                "currency" to s.progress.currency,
                "permanent" to levels(s.progress.permanent),
                "unlocked" to array(s.progress.unlocked.sortedBy { it.ordinal }.map { it.name }),
                "activeId" to s.activeId,
                "activeJson" to s.activeJson,
                "records" to array(s.records.map(::record)),
                "submissions" to
                    array(
                        s.submissions.map {
                            obj(
                                "runId" to it.runId,
                                "ruleset" to it.ruleset,
                                "score" to it.score,
                                "profileId" to it.profileId,
                                "status" to it.status.name,
                                "attempts" to it.attempts,
                                "lastAttempt" to it.lastAttemptEpochMillis,
                            )
                        }
                    ),
            )
            .toString()

    fun save(json: String): GameSave {
        val o = JSONObject(json)
        require(o.getInt("schema") == 1)
        return GameSave(
                GameProgress(
                    o.getLong("currency"),
                    levels(o.getJSONObject("permanent")),
                    o.names("unlocked").map { SupportId.valueOf(it) }.toSet(),
                ),
                o.nullable("activeId"),
                o.nullable("activeJson"),
                o.rows("records").map(::record),
                o.rows("submissions").map {
                    GameSubmission(
                        it.getString("runId"),
                        it.getString("ruleset"),
                        it.getLong("score"),
                        it.nullable("profileId"),
                        SubmissionStatus.valueOf(it.getString("status")),
                        it.getInt("attempts"),
                        it.optLong("lastAttempt", 0),
                    )
                },
            )
            .also { s ->
                require((s.activeId == null) == (s.activeJson == null))
                require(s.records.map { it.id }.distinct().size == s.records.size)
                require(s.submissions.map { it.runId }.distinct().size == s.submissions.size)
                require(
                    s.submissions.all { submission ->
                        submission.score >= 0 &&
                            submission.attempts >= 0 &&
                            submission.lastAttemptEpochMillis >= 0 &&
                            s.records.any {
                                it.id == submission.runId &&
                                    it.mode == RunMode.RANKED &&
                                    it.outcome in setOf(RunOutcome.WON, RunOutcome.DEFEATED) &&
                                    it.score == submission.score &&
                                    it.ruleset == submission.ruleset
                            }
                    }
                )
                s.activeJson?.let { require(session(it).id == s.activeId) }
            }
    }
}
