package com.fitweek.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import org.json.JSONObject
import org.json.JSONArray
import kotlin.math.roundToInt

private data class Move(val name:String,val sets:Int,val reps:String,val rest:Int,val cue:String)
private data class Workout(val title:String,val duration:Int,val detail:String,val moves:List<Move> = emptyList())
private data class Ingredient(val name:String,val grams:Int)
private data class Recipe(val title:String,val minutes:Int,val ingredients:List<Ingredient>,val directions:String)
// Approximate per-100g nutrient values for the stated raw/dry food; values vary by brand and preparation.
// kcal, protein, carbohydrate, fat, fibre, calcium mg, magnesium mg, iron mg, vitamin C mg.
private data class Nutrients(val kcal:Double,val p:Double,val c:Double,val f:Double,val fibre:Double,val ca:Double,val mg:Double,val fe:Double,val vitC:Double)
private val nutrition=mapOf(
 "Magerquark" to Nutrients(67.0,12.0,4.0,0.2,0.0,90.0,11.0,0.3,0.0),
 "Haferflocken" to Nutrients(372.0,13.5,59.0,7.0,10.0,54.0,130.0,4.4,0.0),
 "Heidelbeeren" to Nutrients(57.0,0.7,12.0,0.3,2.4,6.0,6.0,0.3,9.7),
 "Walnüsse" to Nutrients(654.0,15.0,7.0,65.0,6.7,98.0,158.0,2.9,1.3),
 "Banane" to Nutrients(89.0,1.1,20.0,0.3,2.6,5.0,27.0,0.3,8.7),
 "Hüttenkäse" to Nutrients(98.0,12.0,3.0,4.0,0.0,83.0,8.0,0.1,0.0),
 "Harzer Käse" to Nutrients(126.0,30.0,0.0,0.5,0.0,125.0,20.0,0.4,0.0),
 "Ei" to Nutrients(143.0,12.6,0.7,9.5,0.0,56.0,12.0,1.8,0.0),
 "Vollkornbrot" to Nutrients(230.0,8.0,40.0,2.0,7.0,70.0,80.0,2.5,0.0),
 "Tomaten" to Nutrients(18.0,0.9,2.6,0.2,1.2,10.0,11.0,0.3,14.0),
 "Gurke" to Nutrients(12.0,0.6,1.8,0.1,0.5,16.0,13.0,0.3,2.8),
 "Paprika rot" to Nutrients(31.0,1.0,6.0,0.3,2.1,7.0,12.0,0.4,127.0),
 "Brokkoli" to Nutrients(34.0,2.8,4.0,0.4,2.6,47.0,21.0,0.7,89.0),
 "Spinat" to Nutrients(23.0,2.9,1.4,0.4,2.2,99.0,79.0,2.7,28.0),
 "Karotten" to Nutrients(41.0,0.9,7.0,0.2,2.8,33.0,12.0,0.3,5.9),
 "Kartoffeln" to Nutrients(77.0,2.0,17.0,0.1,2.2,12.0,23.0,0.8,20.0),
 "Reis trocken" to Nutrients(350.0,7.0,77.0,0.6,1.3,10.0,35.0,0.8,0.0),
 "Vollkornnudeln trocken" to Nutrients(350.0,13.0,65.0,2.5,8.0,21.0,110.0,3.0,0.0),
 "Hähnchenbrust roh" to Nutrients(110.0,23.0,0.0,1.5,0.0,5.0,29.0,0.4,0.0),
 "Lachs roh" to Nutrients(208.0,20.0,0.0,13.0,0.0,9.0,27.0,0.3,0.0),
 "Tofu natur" to Nutrients(125.0,13.0,2.0,7.0,1.0,200.0,35.0,2.7,0.0),
 "Kichererbsen gekocht" to Nutrients(164.0,8.9,20.0,2.6,7.6,49.0,48.0,2.9,1.3),
 "Linsen gekocht" to Nutrients(116.0,9.0,20.0,0.4,7.9,19.0,36.0,3.3,1.5),
 "Olivenöl" to Nutrients(884.0,0.0,0.0,100.0,0.0,0.0,0.0,0.0,0.0),
 "Milch 1,5 %" to Nutrients(47.0,3.4,4.9,1.5,0.0,120.0,11.0,0.1,0.0),
 "Apfel" to Nutrients(52.0,0.3,12.0,0.2,2.4,6.0,5.0,0.1,4.6)
)
private val breakfasts=listOf(
 Recipe("Quark-Bowl mit Haferflocken",5,listOf(Ingredient("Magerquark",300),Ingredient("Haferflocken",75),Ingredient("Heidelbeeren",120),Ingredient("Walnüsse",20)),"Alles in einer Schüssel mischen."),
 Recipe("Hüttenkäse-Brot und Ei",10,listOf(Ingredient("Hüttenkäse",250),Ingredient("Vollkornbrot",120),Ingredient("Ei",110),Ingredient("Tomaten",150)),"Eier kochen, Brot mit Hüttenkäse und Tomaten servieren."),
 Recipe("Quark mit Banane und Hafer",5,listOf(Ingredient("Magerquark",300),Ingredient("Haferflocken",70),Ingredient("Banane",130),Ingredient("Walnüsse",15)),"Banane schneiden und alles vermischen.")
)
private val lunches=listOf(
 Recipe("Hähnchen-Reis-Brokkoli",20,listOf(Ingredient("Hähnchenbrust roh",200),Ingredient("Reis trocken",100),Ingredient("Brokkoli",250),Ingredient("Olivenöl",15)),"Reis kochen; Hähnchen durchgaren; Brokkoli dünsten, Öl ergänzen."),
 Recipe("Linsen-Kartoffel-Pfanne",20,listOf(Ingredient("Linsen gekocht",250),Ingredient("Kartoffeln",300),Ingredient("Paprika rot",150),Ingredient("Olivenöl",15)),"Kartoffeln klein schneiden und garen, mit Linsen und Paprika anbraten."),
 Recipe("Tofu-Vollkornnudeln",20,listOf(Ingredient("Tofu natur",220),Ingredient("Vollkornnudeln trocken",110),Ingredient("Spinat",150),Ingredient("Olivenöl",12)),"Nudeln kochen, Tofu anbraten und Spinat kurz garen."),
 Recipe("Lachs mit Kartoffeln",20,listOf(Ingredient("Lachs roh",180),Ingredient("Kartoffeln",350),Ingredient("Brokkoli",250),Ingredient("Olivenöl",10)),"Kartoffeln und Brokkoli garen, Lachs vollständig durchgaren.")
)
private val dinners=listOf(
 Recipe("Harzer Brotzeit",8,listOf(Ingredient("Harzer Käse",150),Ingredient("Vollkornbrot",140),Ingredient("Tomaten",200),Ingredient("Gurke",150),Ingredient("Olivenöl",10)),"Gemüse schneiden und mit Käse und Brot servieren."),
 Recipe("Hüttenkäse-Kichererbsen-Salat",10,listOf(Ingredient("Hüttenkäse",250),Ingredient("Kichererbsen gekocht",200),Ingredient("Paprika rot",150),Ingredient("Gurke",150),Ingredient("Olivenöl",10)),"Abgetropfte Kichererbsen mit den übrigen Zutaten vermischen."),
 Recipe("Eier mit Kartoffeln und Spinat",15,listOf(Ingredient("Ei",165),Ingredient("Kartoffeln",300),Ingredient("Spinat",200),Ingredient("Olivenöl",10)),"Kartoffeln vorkochen oder klein schneiden, mit Eiern und Spinat fertig garen.")
)
private val snacks=listOf(
 Recipe("Quark und Apfel",3,listOf(Ingredient("Magerquark",200),Ingredient("Apfel",180)),"Apfel schneiden, dazu Quark."),
 Recipe("Hüttenkäse und Banane",3,listOf(Ingredient("Hüttenkäse",200),Ingredient("Banane",130)),"Zusammen servieren."),
 Recipe("Quark und Walnüsse",3,listOf(Ingredient("Magerquark",200),Ingredient("Walnüsse",20)),"Vermischen.")
)
private fun strength(kind:Int,week:Int):Workout {
 val block=(week-1)/4; val deload=week%4==0
 val sets=if(deload)2 else 3+if(block>=2)1 else 0
 val reps=if(deload)"6–8, locker" else when(block){0->"8–10";1->"9–12";else->"10–12"}
 fun m(n:String,s:Int=sets,r:String=reps,rest:Int=90,cue:String="2 Wiederholungen in Reserve; kontrollierte Technik")=Move(n,s,r,rest,cue)
 val moves=when(kind){
 0->listOf(m("Klimmzüge",if(deload)2 else 4,if(deload)"1" else if(block==0)"1–2" else "2–3 (ggf. Band)",150,"Bei Bedarf Bandunterstützung; nicht bis zum Versagen"),m("TRX Rudern"),m("Liegestütze",r=if(deload)"6–8" else "8–12"),m("TRX Chest Press"),m("Band Face Pull",r="12–15"))
 1->listOf(m("Bulgarian Split Squat",r="8–12 je Bein",rest=120),m("TRX Hamstring Curl"),m("Ausfallschritte",r="10 je Bein"),m("Wadenheben",r="12–20"),m("Side Plank",r="30–45 Sekunden je Seite"))
 else->listOf(m("Bandunterstützte Klimmzüge",r="6–10",rest=120),m("TRX Rudern"),m("Band Schulterdrücken"),m("TRX Squat",r="10–15"),m("Dead Bug",r="10 je Seite"))
 }
 return Workout(listOf("Kraft A · Oberkörper","Kraft B · Beine","Kraft C · Ganzkörper")[kind],if(deload)35 else 50+block*5,if(deload)"Entlastungswoche: bewusst weniger Umfang." else "Trainingsblock ${block+1}: Übungen sauber ausführen. Bei erfülltem Zielbereich in der nächsten Belastungsphase schwierigere Variante oder stärkeres Band.",moves)
}
private fun training(week:Int,day:Int):List<Workout>{
 val deload=week%4==0;val block=(week-1)/4;val extra=if(deload)0 else block*3
 return when(day){
 0->listOf(strength(0,week))
 1->listOf(Workout("Rudern · Intervalle",if(deload)30 else 40+extra,if(deload)"10 Min. locker · 4 × 2 Min. moderat, 2 Min. locker · 6 Min. ausrudern" else "10 Min. locker · ${4+block} × 4 Min. zügig (RPE 7–8), jeweils 2 Min. locker · 6 Min. ausrudern"))
 3->listOf(strength(1,week),Workout("Joggen · locker",if(deload)20 else 25+extra,"Gesprächstempo (RPE 3–4), hügelige Abschnitte ggf. gehen."))
 4->listOf(strength(2,week))
 5->listOf(Workout("Rudern · Grundlage",if(deload)30 else 40+week*2,"Gleichmäßig locker bis moderat (RPE 4–5), 5 Min. ein- und ausrudern."))
 else->emptyList()
 }
}
private fun recipes(day:Int):List<Recipe> = listOf(breakfasts[day%breakfasts.size],lunches[day%lunches.size],dinners[day%dinners.size],snacks[day%snacks.size])
private fun sums(recipe:Recipe):DoubleArray {val a=DoubleArray(9);recipe.ingredients.forEach {i->val n=nutrition[i.name]?:return@forEach;val q=i.grams/100.0;val v=doubleArrayOf(n.kcal,n.p,n.c,n.f,n.fibre,n.ca,n.mg,n.fe,n.vitC);v.indices.forEach{j->a[j]+=v[j]*q}};return a}
private fun fmt(d:Double)=String.format(java.util.Locale.GERMANY,"%.1f",d)
private fun key(date:LocalDate,index:Int)="${date}_$index"
private fun mondayNow()=LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent {MaterialTheme {FitWeekScreen()}}}}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun FitWeekScreen(){
 val context=androidx.compose.ui.platform.LocalContext.current
 val prefs=remember {context.getSharedPreferences("fitweek_v3",Context.MODE_PRIVATE)}
 var start by remember {mutableStateOf(prefs.getString("start",mondayNow().toString())?:mondayNow().toString())}
 val startDate=remember(start){runCatching{LocalDate.parse(start)}.getOrDefault(mondayNow())}
 var offset by remember{mutableIntStateOf(0)}
 var tab by remember{mutableIntStateOf(0)}
 var weight by remember{mutableStateOf(prefs.getString("weight","77.5")?:"77.5")}
 var revision by remember{mutableIntStateOf(0)}
 var selected by remember{mutableStateOf<LocalDate?>(null)}
 var expandedRecipe by remember{mutableStateOf<String?>(null)}
 val week=offset+1
 fun flag(k:String)=prefs.getBoolean(k,false)
 fun flip(k:String){prefs.edit().putBoolean(k,!flag(k)).apply();revision++}
 fun dateDay(d:Int)=startDate.plusDays(offset*7L+d)
 val today=LocalDate.now()
 val dayIndex=((today.toEpochDay()-startDate.toEpochDay()).toInt()).coerceIn(0,83)
 val displayDay=selected?:if(tab==0)today else dateDay(0)
 val actualDay=if(displayDay.isBefore(startDate))startDate else displayDay
 val relative=((actualDay.toEpochDay()-startDate.toEpochDay()).toInt()).coerceIn(0,83)
 val selectedWeek=relative/7+1
 val day=relative%7
 val workouts=training(selectedWeek,day)
 val dayRecipes=recipes(relative)
 val dayTotals=DoubleArray(9);dayRecipes.forEach{r->val v=sums(r);v.indices.forEach{i->dayTotals[i]+=v[i]}}
 Scaffold(topBar={TopAppBar(title={Text("FitWeek · 12-Wochen-Coach")})},bottomBar={NavigationBar {listOf("Heute","12 Wochen","Ernährung","Profil").forEachIndexed{i,label->NavigationBarItem(selected=tab==i,onClick={tab=i;selected=null},icon={Text(listOf("●","▦","◉","⚙")[i])},label={Text(label)})}}}){padding->
 LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal=14.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(vertical=12.dp)) {
 when(tab){
 0->{
 item{Text("Heute · $today",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}
 item{Text(if(today.isBefore(startDate)||today.isAfter(startDate.plusDays(83)))"Außerhalb des aktuellen 12-Wochen-Blocks. Über den Wochenplan kannst du die Einheiten ansehen." else "Trainingswoche $selectedWeek von 12 · Konkrete Vorgaben statt Protokollpflicht")}
 item{Text("Training",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)}
 if(workouts.isEmpty())item{Text("Ruhetag – Regeneration und lockere Bewegung.")} else items(workouts){w->WorkoutCard(w,flag("w_${actualDay}_${w.title}"),{flip("w_${actualDay}_${w.title}")})}
 item{Text("Mahlzeiten · geplant ${dayTotals[0].roundToInt()} kcal · ${fmt(dayTotals[1])} g Protein",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)}
 items(dayRecipes){r->RecipeCard(r,flag("m_${actualDay}_${r.title}"),{flip("m_${actualDay}_${r.title}")},expandedRecipe==r.title,{expandedRecipe=if(expandedRecipe==r.title)null else r.title})}
 }
 1->{
 item{Text("12-Wochen-Trainingsplan",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}
 item{Text("3 Kraftschwerpunkte, Intervallrudern, Grundlagenrudern und lockeres Laufen auf fünf Trainingstage verteilt. Wochen 4, 8 und 12 sind reduziert.")}
 item{Row(horizontalArrangement=Arrangement.SpaceBetween,modifier=Modifier.fillMaxWidth()){OutlinedButton(onClick={offset=(offset-1).coerceAtLeast(0)}){Text("← Vorherige")};Text("Woche $week/12",modifier=Modifier.padding(top=14.dp));OutlinedButton(onClick={offset=(offset+1).coerceAtMost(11)}){Text("Nächste →")}}}
 items((0..6).toList()){d->val date=dateDay(d);val ws=training(week,d);Card(Modifier.fillMaxWidth(),onClick={selected=date;tab=0}){Column(Modifier.padding(12.dp)){Text("${listOf("Mo","Di","Mi","Do","Fr","Sa","So")[d]} · $date",fontWeight=FontWeight.Bold);Text(if(ws.isEmpty())"Ruhetag" else ws.joinToString(" + "){it.title});ws.forEach{w->Text("${w.duration} Min. · ${if(flag("w_${date}_${w.title}"))"✓ absolviert" else "geplant"}",style=MaterialTheme.typography.bodySmall)};Text("Antippen für Tagesdetails",style=MaterialTheme.typography.labelSmall)}}}
 item{Text("Trainingssteigerungen sind geplante Vorgaben, keine gemessenen Leistungsverbesserungen. Bei Beschwerden, Erschöpfung oder nicht bewältigbaren Vorgaben Belastung reduzieren.",style=MaterialTheme.typography.bodySmall)}
 }
 2->{
 item{Text("Ernährungsplan",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}
 item{Text("Sieben-Tage-Rezeptzyklus für 12 Wochen · jede Zutat in Gramm · ungefähr 30 Minuten aktive Küchenzeit pro Tag durch Vorkochen von Reis/Kartoffeln.")}
 item{Row(horizontalArrangement=Arrangement.SpaceBetween,modifier=Modifier.fillMaxWidth()){OutlinedButton(onClick={offset=(offset-1).coerceAtLeast(0)}){Text("← Woche")};Text("Woche $week",modifier=Modifier.padding(top=12.dp));OutlinedButton(onClick={offset=(offset+1).coerceAtMost(11)}){Text("Woche →")}}}
 items((0..6).toList()){d->val date=dateDay(d);val rs=recipes(offset*7+d);val v=DoubleArray(9);rs.forEach{r->val n=sums(r);n.indices.forEach{i->v[i]+=n[i]}};Card(Modifier.fillMaxWidth(),onClick={selected=date;tab=0}){Column(Modifier.padding(12.dp)){Text("$date",fontWeight=FontWeight.Bold);Text("${v[0].roundToInt()} kcal · Protein ${fmt(v[1])} g · KH ${fmt(v[2])} g · Fett ${fmt(v[3])} g");Text(rs.joinToString(" · "){it.title},style=MaterialTheme.typography.bodySmall);Text("Antippen für Zutaten und Mikronährstoffe",style=MaterialTheme.typography.labelSmall)}}}
 item{Text("Nährwerte sind Richtwerte pro 100 g aus einer eingebauten Beispieltabelle, keine verifizierte Lebensmitteldatenbank. Marken, Zubereitung und Bioverfügbarkeit verändern die Werte. Die Tabelle deckt nur ausgewählte Mikronährstoffe ab.",style=MaterialTheme.typography.bodySmall)}
 }
 else->{
 item{Text("Dein Profil",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}
 item{Text("45 Jahre · 179 cm · Start 77,5 kg · Ziel 74 kg · 5 Trainingstage · TRX, Klimmzugstange, Bänder, Matte, WaterRower, Joggen")}
 item{OutlinedTextField(value=weight,onValueChange={weight=it},label={Text("Aktuelles Gewicht (kg)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),singleLine=true)}
 item{Button(onClick={prefs.edit().putString("weight",weight).apply()}){Text("Gewicht speichern")}}
 item{Text("Ernährungsorientierung: ca. 150 g Protein/Tag. Der Kalorienbedarf ist eine Schätzung; für ein individuelles Ziel sind Alltagsbewegung und Gewichtsverlauf erforderlich.")}
 item{Text("12-Wochen-Block beginnt: $startDate")}
 item{OutlinedButton(onClick={start=mondayNow().toString();prefs.edit().putString("start",start).apply();offset=0;selected=null}){Text("12-Wochen-Block ab dieser Woche neu starten")}}
 item{Text("Hinweis: Neustart ändert den Planbeginn; bereits gesetzte Häkchen bleiben datumsbezogen gespeichert.",style=MaterialTheme.typography.bodySmall)}
 }
 }
 }
 }
}
@Composable private fun WorkoutCard(w:Workout,done:Boolean,onDone:()->Unit){Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(w.title,fontWeight=FontWeight.Bold);Text("${w.duration} Minuten · ${w.detail}");w.moves.forEach{m->Text("• ${m.name}: ${m.sets} × ${m.reps}; Pause ${m.rest} s");Text(m.cue,style=MaterialTheme.typography.bodySmall)};Row{Checkbox(checked=done,onCheckedChange={onDone()});Text("Training durchgeführt",modifier=Modifier.padding(top=12.dp))}}}}
@Composable private fun RecipeCard(r:Recipe,eaten:Boolean,onEaten:()->Unit,expanded:Boolean,onExpand:()->Unit){val n=sums(r);Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(r.title,fontWeight=FontWeight.Bold);Text("${r.minutes} Min. aktiv · ${n[0].roundToInt()} kcal · Protein ${fmt(n[1])} g · KH ${fmt(n[2])} g · Fett ${fmt(n[3])} g");TextButton(onClick=onExpand){Text(if(expanded)"Details schließen" else "Zutaten, Zubereitung & Mikronährstoffe")};if(expanded){r.ingredients.forEach{Text("• ${it.name}: ${it.grams} g")};Text("Zubereitung: ${r.directions}");Text("Ballaststoffe ${fmt(n[4])} g · Calcium ${fmt(n[5])} mg · Magnesium ${fmt(n[6])} mg · Eisen ${fmt(n[7])} mg · Vitamin C ${fmt(n[8])} mg")};Row{Checkbox(checked=eaten,onCheckedChange={onEaten()});Text("Mahlzeit gegessen",modifier=Modifier.padding(top=12.dp))}}}}
