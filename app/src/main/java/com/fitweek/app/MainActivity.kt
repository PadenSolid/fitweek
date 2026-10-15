package com.fitweek.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import java.util.UUID

private data class Exercise(val name:String,val sets:Int,val reps:String,val rest:Int,val tip:String)
private data class Template(val name:String,val category:String,val duration:Int,val rpe:Int,val detail:String,val exercises:List<Exercise> = emptyList())
private val templates = listOf(
 Template("Kraft A – Oberkörper","Kraft",55,7,"1–3 Wiederholungen in Reserve. Saubere Technik vor Steigerung.",listOf(
  Exercise("Klimmzüge",4,"1–2",150,"Wenn nötig Bandunterstützung nutzen"),Exercise("TRX Rudern",3,"8–12",90,"Schwierigkeit über Körperwinkel"),Exercise("Liegestütze",3,"8–10",90,"Nicht bis zum Muskelversagen"),Exercise("TRX Chest Press",3,"8–12",90,"Körperwinkel progressiv steigern"),Exercise("Band Face Pull",3,"12–15",60,"Schulterblätter kontrolliert bewegen"))),
 Template("Kraft B – Beine & Rumpf","Kraft",50,7,"Beinübungen kontrolliert; 1–3 Wiederholungen in Reserve.",listOf(
  Exercise("Bulgarian Split Squat",3,"8–12 je Bein",120,"Langsame exzentrische Phase"),Exercise("TRX Hamstring Curl",3,"8–12",90,"Becken oben halten"),Exercise("Ausfallschritte",3,"10 je Bein",90,"Optional Bandwiderstand"),Exercise("Wadenheben",3,"12–20",60,"Volle Bewegungsamplitude"),Exercise("Side Plank",3,"30–45 s je Seite",60,"Rumpf stabil"))),
 Template("Kraft C – Ganzkörper","Kraft",55,7,"Alternative Reize; Fortschritt nach tatsächlicher Leistung.",listOf(
  Exercise("Bandunterstützte Klimmzüge",3,"6–10",120,"Unterstützung schrittweise reduzieren"),Exercise("TRX Rudern",3,"8–12",90,"Schwieriger als bei Kraft A wenn möglich"),Exercise("Band Schulterdrücken",3,"8–12",90,"Widerstand passend wählen"),Exercise("TRX Squat",3,"10–15",90,"Bei Bedarf mit Band erschweren"),Exercise("Dead Bug",3,"10 je Seite",60,"Langsam und kontrolliert"))),
 Template("Rudern – Grundlage","Rudern",45,4,"10 Min. locker, 30 Min. gleichmäßig RPE 4–5, 5 Min. locker"),
 Template("Rudern – Intervalle","Rudern",45,8,"10 Min. einrudern; 5 × 4 Min. RPE 8 mit 2 Min. locker dazwischen; 7 Min. ausrudern"),
 Template("Rudern – Tempo","Rudern",40,7,"10 Min. locker, 3 × 7 Min. RPE 7 mit je 2 Min. locker, 5 Min. ausrudern"),
 Template("Joggen – Grundlage","Laufen",35,4,"5 Min. locker, 25 Min. RPE 4–5, 5 Min. locker"),
 Template("Aktive Erholung","Regeneration",25,2,"Spaziergang oder lockere Mobilität; keine harte Belastung")
)
private fun JSONObject.str(k:String,d:String="") = optString(k,d)
private fun initialSessions():JSONArray { val a=JSONArray(); listOf(0 to 0,0 to 3,1 to 4,3 to 1,4 to 6,5 to 2,5 to 3).forEach { (day,index) -> a.put(session(templates[index],LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusDays(day.toLong()).toString())) };return a }
private fun session(t:Template,date:String):JSONObject = JSONObject().apply {put("id",UUID.randomUUID().toString());put("date",date);put("name",t.name);put("category",t.category);put("duration",t.duration);put("rpe",t.rpe);put("detail",t.detail);put("done",false);put("actualMinutes","");put("actualRpe","");val ex=JSONArray();t.exercises.forEach { e->ex.put(JSONObject().apply {put("name",e.name);put("sets",e.sets);put("reps",e.reps);put("rest",e.rest);put("tip",e.tip);put("actual","")}) };put("exercises",ex) }
private data class Food(val name:String,val kcal:Double,val protein:Double,val carbs:Double,val fat:Double)
private val foods=listOf(Food("Magerquark",67.0,12.0,4.0,0.2),Food("Haferflocken",372.0,13.5,59.0,7.0),Food("Heidelbeeren",57.0,0.7,12.0,0.3),Food("Walnüsse",654.0,15.0,7.0,65.0),Food("Hüttenkäse",98.0,12.0,3.0,4.0),Food("Harzer Käse",126.0,30.0,0.0,0.5),Food("Ei",143.0,12.6,0.7,9.5),Food("Banane",89.0,1.1,20.0,0.3),Food("Kartoffeln",77.0,2.0,17.0,0.1),Food("Brokkoli",34.0,2.8,4.0,0.4),Food("Hähnchenbrust roh",110.0,23.0,0.0,1.5),Food("Reis trocken",350.0,7.0,77.0,0.6),Food("Olivenöl",884.0,0.0,0.0,100.0))
private fun meal(date:String,name:String,ingredients:List<Pair<Int,Int>>):JSONObject=JSONObject().apply {put("id",UUID.randomUUID().toString());put("date",date);put("name",name);put("eaten",false);val a=JSONArray();ingredients.forEach{(i,g)->a.put(JSONObject().apply{put("food",i);put("grams",g)})};put("ingredients",a)}
private fun mealTotals(m:JSONObject):List<Double>{val a=m.optJSONArray("ingredients")?:JSONArray();val totals=MutableList(4){0.0};for(i in 0 until a.length()){val x=a.getJSONObject(i);val f=foods.getOrNull(x.optInt("food"))?:continue;val q=x.optDouble("grams",0.0)/100;listOf(f.kcal,f.protein,f.carbs,f.fat).forEachIndexed{j,v->totals[j]+=v*q}};return totals}
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent { MaterialTheme { FitWeekApp() } }}}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun FitWeekApp(){
 val ctx=androidx.compose.ui.platform.LocalContext.current
 val prefs=remember{ctx.getSharedPreferences("fitweek_v2",Context.MODE_PRIVATE)}
 var sessions by remember { mutableStateOf(try {JSONArray(prefs.getString("sessions",null)?:initialSessions().toString())}catch(_:Exception){initialSessions()}) }
 var meals by remember { mutableStateOf(try{JSONArray(prefs.getString("meals","[]"))}catch(_:Exception){JSONArray()}) }
 var weight by remember{mutableStateOf(prefs.getString("weight","77.5")?:"77.5")}
 var goal by remember{mutableStateOf(prefs.getString("goal","74")?:"74")}
 var monday by remember{mutableStateOf(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))}
 var tab by remember{mutableIntStateOf(0)}
 var editing by remember{mutableStateOf<String?>(null)}
 var editingMeal by remember{mutableStateOf<String?>(null)}
 fun saveSessions(a:JSONArray){sessions=JSONArray(a.toString());prefs.edit().putString("sessions",sessions.toString()).apply()}
 fun saveMeals(a:JSONArray){meals=JSONArray(a.toString());prefs.edit().putString("meals",meals.toString()).apply()}
 fun modifySession(id:String,change:(JSONObject)->Unit){val a=JSONArray(sessions.toString());for(i in 0 until a.length()){val o=a.getJSONObject(i);if(o.str("id")==id){change(o);break}};saveSessions(a)}
 fun modifyMeal(id:String,change:(JSONObject)->Unit){val a=JSONArray(meals.toString());for(i in 0 until a.length()){val o=a.getJSONObject(i);if(o.str("id")==id){change(o);break}};saveMeals(a)}
 val selectedSession=(0 until sessions.length()).map{sessions.getJSONObject(it)}.firstOrNull{it.str("id")==editing}
 val selectedMeal=(0 until meals.length()).map{meals.getJSONObject(it)}.firstOrNull{it.str("id")==editingMeal}
 Scaffold(topBar={TopAppBar(title={Text(if(editing!=null)"Training bearbeiten" else if(editingMeal!=null)"Mahlzeit bearbeiten" else "FitWeek · Version 2")})},bottomBar={if(editing==null&&editingMeal==null) NavigationBar {listOf("Woche","Ernährung","Fortschritt").forEachIndexed{i,s->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Text(listOf("▦","◉","↗")[i])},label={Text(s)})}}}) { padding ->
 Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=14.dp)) {
 if(selectedSession!=null){
  val id=selectedSession.str("id")
  var name by remember(id){mutableStateOf(selectedSession.str("name"))}
  var duration by remember(id){mutableStateOf(selectedSession.optInt("duration").toString())}
  var intensity by remember(id){mutableStateOf(selectedSession.optInt("rpe").toString())}
  var detail by remember(id){mutableStateOf(selectedSession.str("detail"))}
  var actualMinutes by remember(id){mutableStateOf(selectedSession.str("actualMinutes"))}
  var actualRpe by remember(id){mutableStateOf(selectedSession.str("actualRpe"))}
  val ex=selectedSession.optJSONArray("exercises")?:JSONArray()
  Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
   TextButton(onClick={editing=null}){Text("← Zurück")}
   OutlinedTextField(name,{name=it},label={Text("Training")},modifier=Modifier.fillMaxWidth())
   Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(duration,{duration=it},label={Text("Dauer (Min.)")},modifier=Modifier.weight(1f),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number));OutlinedTextField(intensity,{intensity=it},label={Text("Ziel-RPE 1–10")},modifier=Modifier.weight(1f),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))}
   OutlinedTextField(detail,{detail=it},label={Text("Trainingsablauf / Intensität")},modifier=Modifier.fillMaxWidth(),minLines=3)
   Button(onClick={modifySession(id){it.put("name",name);it.put("duration",duration.toIntOrNull()?.coerceAtLeast(1)?:45);it.put("rpe",intensity.toIntOrNull()?.coerceIn(1,10)?:5);it.put("detail",detail)};editing=null}){Text("Plan speichern")}
   if(ex.length()>0){Text("Übungen & tatsächliche Leistung",style=MaterialTheme.typography.titleMedium)
    for(i in 0 until ex.length()){val e=ex.getJSONObject(i);val n=e.str("name");var actual by remember(id,i){mutableStateOf(e.str("actual"))};Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(n,fontWeight=FontWeight.Bold);Text("${e.optInt("sets")} Sätze × ${e.str("reps")} · Pause ${e.optInt("rest")} s");Text(e.str("tip"),style=MaterialTheme.typography.bodySmall);OutlinedTextField(actual,{actual=it;modifySession(id){s->s.getJSONArray("exercises").getJSONObject(i).put("actual",actual)}},label={Text("Ist: z. B. 8/8/7, Band leicht")},modifier=Modifier.fillMaxWidth())}}}
   }
   Text("Trainingsprotokoll",style=MaterialTheme.typography.titleMedium)
   Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(actualMinutes,{actualMinutes=it},label={Text("Ist-Minuten")},modifier=Modifier.weight(1f),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number));OutlinedTextField(actualRpe,{actualRpe=it},label={Text("Ist-RPE")},modifier=Modifier.weight(1f),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))}
   Button(onClick={modifySession(id){it.put("actualMinutes",actualMinutes);it.put("actualRpe",actualRpe);it.put("done",true)};editing=null}){Text("Als absolviert speichern")}
   OutlinedButton(onClick={modifySession(id){it.put("done",false)};editing=null}){Text("Als nicht absolviert markieren")}
   Text("Progression: Zielbereich erreichen, Technik und Wiederholungsreserve prüfen; erst danach Schwierigkeit erhöhen. Die App speichert die Ist-Leistung. Eine automatische Anpassung folgt in einem weiteren Ausbau.",style=MaterialTheme.typography.bodySmall)
  }
 }else if(selectedMeal!=null){
  val id=selectedMeal.str("id");var title by remember(id){mutableStateOf(selectedMeal.str("name"))}
  Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
   TextButton(onClick={editingMeal=null}){Text("← Zurück")};OutlinedTextField(title,{title=it;modifyMeal(id){o->o.put("name",title)}},label={Text("Mahlzeit")},modifier=Modifier.fillMaxWidth())
   val arr=selectedMeal.optJSONArray("ingredients")?:JSONArray()
   for(i in 0 until arr.length()){val ing=arr.getJSONObject(i);val fi=ing.optInt("food");var grams by remember(id,i){mutableStateOf(ing.optInt("grams").toString())};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Text(foods[fi].name,Modifier.weight(1f).padding(top=15.dp));OutlinedTextField(grams,{grams=it;modifyMeal(id){o->o.getJSONArray("ingredients").getJSONObject(i).put("grams",grams.toIntOrNull()?.coerceAtLeast(0)?:0)}},label={Text("Gramm")},modifier=Modifier.width(125.dp),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number));TextButton(onClick={modifyMeal(id){o->val old=o.getJSONArray("ingredients");val next=JSONArray();for(j in 0 until old.length())if(j!=i)next.put(old.getJSONObject(j));o.put("ingredients",next)}}){Text("×")}}}
   var chosen by remember(id){mutableIntStateOf(0)};var expanded by remember{mutableStateOf(false)}
   ExposedDropdownMenuBox(expanded=expanded,onExpandedChange={expanded=it}){OutlinedTextField(foods[chosen].name,{},readOnly=true,label={Text("Lebensmittel hinzufügen")},trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded)},modifier=Modifier.menuAnchor().fillMaxWidth());ExposedDropdownMenu(expanded=expanded,onDismissRequest={expanded=false}){foods.forEachIndexed{i,f->DropdownMenuItem(text={Text(f.name)},onClick={chosen=i;expanded=false})}}}
   Button(onClick={modifyMeal(id){it.getJSONArray("ingredients").put(JSONObject().apply{put("food",chosen);put("grams",100)})}}){Text("100 g hinzufügen (Menge anschließend ändern)")}
   val totals=mealTotals(selectedMeal);Text("${totals[0].toInt()} kcal · ${"%.1f".format(totals[1])} g Protein · ${"%.1f".format(totals[2])} g KH · ${"%.1f".format(totals[3])} g Fett")
   Button(onClick={modifyMeal(id){it.put("eaten",!it.optBoolean("eaten"))};editingMeal=null}){Text(if(selectedMeal.optBoolean("eaten"))"Als nicht gegessen markieren" else "Als gegessen markieren")}
   Text("Nährwerte aus hinterlegten Durchschnittswerten pro 100 g; Produktangaben können abweichen.",style=MaterialTheme.typography.bodySmall)
  }
 }else if(tab==0){
  Row(horizontalArrangement=Arrangement.SpaceBetween,modifier=Modifier.fillMaxWidth()){TextButton(onClick={monday=monday.minusWeeks(1)}){Text("← Woche")};Text("${monday} – ${monday.plusDays(6)}",modifier=Modifier.padding(top=12.dp));TextButton(onClick={monday=monday.plusWeeks(1)}){Text("Woche →")}}
  LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)) {items((0..6).toList()){d->val date=monday.plusDays(d.toLong()).toString();val list=(0 until sessions.length()).map{sessions.getJSONObject(it)}.filter{it.str("date")==date};Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Text("${listOf("Mo","Di","Mi","Do","Fr","Sa","So")[d]} · $date",fontWeight=FontWeight.Bold);if(list.isEmpty())Text("Noch kein Training");list.forEach{s->Row{Column(Modifier.weight(1f)){Text(s.str("name"),fontWeight=FontWeight.SemiBold);Text("${s.optInt("duration")} Min. · RPE ${s.optInt("rpe")} · ${if(s.optBoolean("done"))"Absolviert ✓" else "Geplant"}",style=MaterialTheme.typography.bodySmall)};TextButton(onClick={editing=s.str("id")}){Text("Details")};TextButton(onClick={val a=JSONArray();for(i in 0 until sessions.length())if(sessions.getJSONObject(i).str("id")!=s.str("id"))a.put(sessions.getJSONObject(i));saveSessions(a)}){Text("×")}}};var expanded by remember(date){mutableStateOf(false)};var selectedTemplate by remember(date){mutableIntStateOf(0)};ExposedDropdownMenuBox(expanded=expanded,onExpandedChange={expanded=it}){OutlinedTextField(templates[selectedTemplate].name,{},readOnly=true,label={Text("Training auswählen")},trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded)},modifier=Modifier.menuAnchor().fillMaxWidth());ExposedDropdownMenu(expanded=expanded,onDismissRequest={expanded=false}){templates.forEachIndexed{i,t->DropdownMenuItem(text={Text(t.name)},onClick={selectedTemplate=i;expanded=false})}}};OutlinedButton(onClick={val a=JSONArray(sessions.toString());a.put(session(templates[selectedTemplate],date));saveSessions(a)}){Text("+ Training hinzufügen")}}}}}
 }else if(tab==1){
  Text("Ernährungsplan mit Grammangaben",style=MaterialTheme.typography.titleMedium)
  Text("Nur als gegessen markierte Mahlzeiten zählen zur Ist-Aufnahme.",style=MaterialTheme.typography.bodySmall)
  LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){items((0..6).toList()){d->val date=monday.plusDays(d.toLong()).toString();val dayMeals=(0 until meals.length()).map{meals.getJSONObject(it)}.filter{it.str("date")==date};val eaten=dayMeals.filter{it.optBoolean("eaten")};val energy=eaten.sumOf{mealTotals(it)[0]};val protein=eaten.sumOf{mealTotals(it)[1]};Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(date,fontWeight=FontWeight.Bold);Text("Gegessen: ${energy.toInt()} kcal · ${protein.toInt()} g Protein");dayMeals.forEach{m->Row{Text("${if(m.optBoolean("eaten"))"✓ " else "○ "}${m.str("name")} · ${mealTotals(m)[0].toInt()} kcal",Modifier.weight(1f));TextButton(onClick={editingMeal=m.str("id")}){Text("Edit")};TextButton(onClick={val a=JSONArray();for(i in 0 until meals.length())if(meals.getJSONObject(i).str("id")!=m.str("id"))a.put(meals.getJSONObject(i));saveMeals(a)}){Text("×")}}};OutlinedButton(onClick={val a=JSONArray(meals.toString());a.put(meal(date,"Quark-Frühstück",listOf(0 to 250,1 to 60,2 to 100,3 to 15)));saveMeals(a)}){Text("+ Quark-Frühstück")};OutlinedButton(onClick={val a=JSONArray(meals.toString());a.put(meal(date,"Neue Mahlzeit",listOf(4 to 200)));saveMeals(a)}){Text("+ Eigene Mahlzeit")}}}}}
 }else{
  Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)){
   Text("Ziele & Fortschritt",style=MaterialTheme.typography.titleMedium)
   OutlinedTextField(weight,{weight=it;prefs.edit().putString("weight",it).apply()},label={Text("Aktuelles Gewicht (kg)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth())
   OutlinedTextField(goal,{goal=it;prefs.edit().putString("goal",it).apply()},label={Text("Zielgewicht (kg)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth())
   val completed=(0 until sessions.length()).count{sessions.getJSONObject(it).optBoolean("done")};val planned=sessions.length();Text("Trainingseinheiten absolviert: $completed von $planned")
   Text("Kraftziele: 3 Klimmzüge → 6–8; 15 Liegestütze → schwierigere Varianten.")
   Text("Protein-Orientierung: 125–170 g/Tag bei 77,5 kg. Individuelle Kalorienziele benötigen Alter, Geschlecht, Aktivität und eine Verlaufskontrolle.")
   Text("Trainingskalorien werden derzeit bewusst nicht berechnet: RPE und Dauer allein liefern keine verlässliche individuelle Energiemessung.",style=MaterialTheme.typography.bodySmall)
   Text("Alle Daten werden lokal auf diesem Gerät gespeichert. Kein Cloud-Sync in dieser Version.",style=MaterialTheme.typography.bodySmall)
  }
 }
 }
 }
}
