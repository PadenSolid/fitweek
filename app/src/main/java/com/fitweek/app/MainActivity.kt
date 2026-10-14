package de.fitweek.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

private val days = listOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")
private val sports = listOf("Rudern", "Joggen", "Krafttraining", "Regeneration")
private data class Meal(val name: String, val kcal: Int, val protein: Int)
private val meals = listOf(
    Meal("Skyr mit Haferflocken, Beeren und Nüssen", 480, 35),
    Meal("Hähnchen-Reis-Bowl mit Gemüse", 690, 48),
    Meal("Quark mit Banane und Vollkornbrot", 420, 37),
    Meal("Lachs mit Kartoffeln und Brokkoli", 650, 43),
    Meal("Linsencurry mit Naturreis", 610, 27),
    Meal("Omelett mit Spinat und Vollkornbrot", 510, 34),
    Meal("Hüttenkäse mit Tomaten und Roggenbrot", 430, 32)
)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { MaterialTheme { Planner() } } }
}
@Composable private fun Planner() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("fitweek", Activity.MODE_PRIVATE) }
    var monday by remember { mutableStateOf(LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))) }
    var currentWeight by remember { mutableStateOf(prefs.getString("weight", "77.5") ?: "77.5") }
    var goalWeight by remember { mutableStateOf(prefs.getString("goal", "74") ?: "74") }
    var selected by remember { mutableStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }
    fun key(day: Int) = "plan_${monday.plusDays(day.toLong())}"
    fun entries(day: Int): List<String> { val raw = prefs.getString(key(day), "[]") ?: "[]"; return try { val a = JSONArray(raw); (0 until a.length()).map { a.getString(it) } } catch (_: Exception) { emptyList() } }
    fun save(day: Int, values: List<String>) { prefs.edit().putString(key(day), JSONArray(values).toString()).apply(); refresh++ }
    val unused = refresh
    val planned = (0..6).sumOf { entries(it).count { e -> e != "Regeneration" } }
    val target = currentWeight.toDoubleOrNull()?.let { if (it > (goalWeight.toDoubleOrNull() ?: 74.0)) "Abnahme" else "Erhaltung / Muskelaufbau" } ?: "Gewicht eingeben"
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("FitWeek", style = MaterialTheme.typography.headlineMedium)
        Text("Wochenplanung für Training & Ernährung")
        Spacer(Modifier.height(10.dp))
        Row { Button(onClick = { monday = monday.minusWeeks(1) }) { Text("‹") }; Spacer(Modifier.width(8.dp)); Text("${monday} – ${monday.plusDays(6)}", modifier = Modifier.padding(top = 12.dp)); Spacer(Modifier.width(8.dp)); Button(onClick = { monday = monday.plusWeeks(1) }) { Text("›") } }
        Row { OutlinedTextField(currentWeight, { currentWeight = it; prefs.edit().putString("weight", it).apply() }, label = { Text("Gewicht kg") }, modifier = Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); OutlinedTextField(goalWeight, { goalWeight = it; prefs.edit().putString("goal", it).apply() }, label = { Text("Ziel kg") }, modifier = Modifier.weight(1f)) }
        Text("Phase: $target · $planned Einheiten geplant", modifier = Modifier.padding(vertical = 8.dp))
        HorizontalDivider()
        LazyColumn {
            items((0..6).toList()) { day ->
                val sessions = entries(day)
                val load = sessions.count { it != "Regeneration" }
                val suggested = if (load >= 2) 2450 else if (load == 1) 2200 else 1950
                val estimated = if (target == "Abnahme") suggested else suggested + 350
                val mealList = listOf(meals[(day + 0) % meals.size], meals[(day + 1) % meals.size], meals[(day + 2) % meals.size])
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${days[day]} · ${monday.plusDays(day.toLong())}", style = MaterialTheme.typography.titleMedium)
                        if (sessions.isEmpty()) Text("Ruhetag")
                        sessions.forEach { session -> Row { Text("• $session", Modifier.weight(1f)); TextButton(onClick = { save(day, sessions.toMutableList().also { it.remove(session) }) }) { Text("Entfernen") } } }
                        Row(Modifier.horizontalScroll(rememberScrollState())) { sports.forEach { sport -> TextButton(onClick = { save(day, sessions + sport) }) { Text("+ $sport") } } }
                        Text("Ernährungsvorschlag:", style = MaterialTheme.typography.titleSmall)
                        mealList.forEachIndexed { index, meal -> Text("${listOf("Früh", "Mittag", "Abend")[index]}: ${meal.name} (${meal.kcal} kcal, ${meal.protein} g Eiweiß)") }
                        Text("Orientierungswert: ca. $estimated kcal/Tag (nicht individuell berechnet)", style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "${days[day]} ${monday.plusDays(day.toLong())}: ${sessions.joinToString(", ")}") }
                            context.startActivity(Intent.createChooser(intent, "Tagesplan teilen"))
                        }) { Text("Tagesplan teilen") }
                    }
                }
            }
        }
    }
}
