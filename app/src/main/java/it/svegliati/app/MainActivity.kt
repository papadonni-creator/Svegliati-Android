package it.svegliati.app
import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.*
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.*
import android.widget.*
import java.io.File
import java.util.Calendar

class MainActivity:Activity(){
 val green=0xff169b43.toInt(); val red=0xffc62430.toInt(); val panel=0xff202020.toInt()
 lateinit var root:LinearLayout; var editing:Alarm?=null; var selectedSound=""; var selectedVoice=""; var recorder:MediaRecorder?=null
 fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
 fun bg(c:Int,r:Int=12)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
 fun label(s:String,size:Float=16f)=TextView(this).apply{text=s;textSize=size;setTextColor(Color.WHITE);gravity=Gravity.CENTER_VERTICAL}
 fun button(s:String,c:Int=panel,action:()->Unit)=Button(this).apply{text=s;setTextColor(Color.WHITE);typeface=Typeface.DEFAULT_BOLD;isAllCaps=false;background=bg(c);setOnClickListener{action()}}
 fun add(v:View,h:Int=-2){root.addView(v,LinearLayout.LayoutParams(-1,if(h<0) h else dp(h)).apply{bottomMargin=dp(10)})}
 fun page(title:String){window.statusBarColor=Color.BLACK;window.navigationBarColor=Color.BLACK; val scroll=ScrollView(this).apply{setBackgroundColor(Color.BLACK);fitsSystemWindows=true};root=LinearLayout(this).apply{orientation=1;setPadding(dp(18),dp(18),dp(18),dp(24))};scroll.addView(root);setContentView(scroll);val row=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL};row.addView(label(title,25f).apply{typeface=Typeface.DEFAULT_BOLD},LinearLayout.LayoutParams(0,dp(52),1f));row.addView(button("⚙"){settings()},LinearLayout.LayoutParams(dp(60),dp(48)));add(row)}
 override fun onCreate(b:Bundle?){super.onCreate(b); if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),1); home()}
 override fun onResume(){super.onResume(); if(Schedule.exact(this))Store.all(this).forEach{Schedule.next(this,it)}}
 fun fresh():Alarm{val n=Calendar.getInstance();val list=Store.all(this);return Alarm((list.maxOfOrNull{it.id}?:0)+1,n.get(Calendar.HOUR_OF_DAY),n.get(Calendar.MINUTE),0,true,"","","","","")}
 fun home(){val list=Store.all(this);edit(list.firstOrNull()?:fresh())}
 fun edit(a:Alarm){editing=a;selectedSound=a.sound;selectedVoice=a.voice;page("SVEGLIATI");val list=Store.all(this);if(list.size>1){val tabs=HorizontalScrollView(this);val row=LinearLayout(this);list.forEach{item->row.addView(button(item.name.ifBlank{"SVEGLIA ${item.id}"},if(item.id==a.id)green else panel){edit(item)})};tabs.addView(row);add(tabs)}
 val wheels=LinearLayout(this).apply{gravity=Gravity.CENTER; background=bg(panel)};fun wheel(limit:Int,initial:Int)=NumberPicker(this).apply{minValue=0;maxValue=limit;setFormatter{String.format("%02d",it)};this.value=initial;wrapSelectorWheel=true;descendantFocusability=ViewGroup.FOCUS_BLOCK_DESCENDANTS}
 val hours=wheel(23,a.h);val mins=wheel(59,a.m); wheels.addView(hours,LinearLayout.LayoutParams(0,dp(180),1f));wheels.addView(label(":",38f).apply{setTextColor(green)});wheels.addView(mins,LinearLayout.LayoutParams(0,dp(180),1f));add(wheels)
 var days=a.days;val week=LinearLayout(this).apply{gravity=Gravity.CENTER};listOf("Lu","Ma","Me","Gi","Ve","Sa","Do").forEachIndexed{i,s->val v=label(s,14f).apply{gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD; background=bg(if(days and (1 shl i)!=0)green else panel,40);setOnClickListener{days=days xor (1 shl i);background=bg(if(days and (1 shl i)!=0)green else panel,40)}};week.addView(v,LinearLayout.LayoutParams(0,dp(42),1f).apply{setMargins(dp(2),0,dp(2),0)})};add(week)
 val toggle=Switch(this).apply{text="Sveglia ON / OFF";setTextColor(Color.WHITE);isChecked=a.enabled};add(toggle)
 fun field(hint:String,value:String)=EditText(this).apply{this.hint=hint;setText(value);setSingleLine();setTextColor(Color.WHITE);setHintTextColor(Color.GRAY)}
 val name=field("SVEGLIA ${a.id}",a.name);val g=field("SVEGLIATI !",a.green);val r=field("TI SEI ALZATO ?",a.red)
 add(button("Salva Sveglia",green){val updated=a.copy(h=hours.value,m=mins.value,days=days,enabled=toggle.isChecked,name=name.text.toString(),green=g.text.toString(),red=r.text.toString(),sound=selectedSound,voice=selectedVoice);Store.put(this,updated);Schedule.cancel(this,a.id,true);Schedule.next(this,updated);if(updated.enabled&&!Schedule.exact(this)){AlertDialog.Builder(this).setMessage("Consenti sveglie esatte per attivare la sveglia.").setPositiveButton("Impostazioni"){_,_->if(Build.VERSION.SDK_INT>=31)startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:$packageName")))}.show()}else Toast.makeText(this,"Sveglia salvata",Toast.LENGTH_SHORT).show();editing=updated},54)
 add(button("Elimina Sveglia",red){Schedule.cancel(this,a.id);Schedule.cancel(this,a.id,true);Store.save(this,Store.all(this).filter{it.id!=a.id});home()},54)
 add(label("Nome della sveglia"));add(name);add(label("Testo pulsante verde"));add(g);add(label("Testo pulsante rosso"));add(r)
 val soundRow=LinearLayout(this);val soundButton=button("▣  Suoneria"){startActivityForResult(Intent(RingtoneManager.ACTION_RINGTONE_PICKER).putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM).putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,false),20)};soundRow.addView(soundButton,LinearLayout.LayoutParams(0,dp(64),1f));soundRow.addView(button("👄  Voce"){recordDialog()},LinearLayout.LayoutParams(0,dp(64),1f));add(soundRow);add(button("＋",green){edit(fresh())},52)
 }
 override fun onActivityResult(req:Int,res:Int,data:Intent?){super.onActivityResult(req,res,data);if(req==20&&res==RESULT_OK){val uri=data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);selectedSound=uri?.toString().orEmpty();selectedVoice="";Toast.makeText(this,"Suoneria selezionata",Toast.LENGTH_SHORT).show()}}
 fun recordDialog(){if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),21);return};val f=File(filesDir,"voice_${editing?.id}_${System.currentTimeMillis()}.m4a");try{recorder=MediaRecorder().apply{setAudioSource(MediaRecorder.AudioSource.MIC);setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);setAudioEncoder(MediaRecorder.AudioEncoder.AAC);setOutputFile(f.absolutePath);prepare();start()};AlertDialog.Builder(this).setTitle("Registrazione in corso").setMessage("Pronuncia il tuo messaggio vocale.").setCancelable(false).setPositiveButton("Ferma e usa"){_,_->try{recorder?.stop();selectedVoice=f.absolutePath}catch(e:Exception){f.delete()}finally{recorder?.release();recorder=null}}.setNegativeButton("Annulla"){_,_->recorder?.release();recorder=null;f.delete()}.show()}catch(e:Exception){recorder?.release();recorder=null;Toast.makeText(this,"Registrazione non disponibile",Toast.LENGTH_LONG).show()}}
 fun settings(){page("Impostazioni");val p=Store.prefs(this);val preview=label("07:30\nMer 02 MAR",34f).apply{gravity=Gravity.CENTER};add(preview,120);val colors=listOf("Nero","Bianco","Verde","Rosso","Blu","Giallo","Grigio");val values=listOf(Color.BLACK,Color.WHITE,0xff35d369.toInt(),Color.RED,0xff448aff.toInt(),Color.YELLOW,Color.GRAY);fun color(key:String,title:String,def:Int){add(label(title));val s=Spinner(this);s.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,colors);s.setSelection(values.indexOf(p.getInt(key,def)).coerceAtLeast(0));s.onItemSelectedListener=object:android.widget.AdapterView.OnItemSelectedListener{override fun onNothingSelected(parent:AdapterView<*>?){};override fun onItemSelected(parent:AdapterView<*>?,v:View?,pos:Int,id:Long){p.edit().putInt(key,values[pos]).apply();preview.setBackgroundColor(p.getInt("bg",Color.BLACK));preview.setTextColor(p.getInt("clockColor",Color.WHITE))}};add(s)}
 fun slider(key:String,title:String,min:Int,limit:Int,def:Int){val l=label("$title: ${p.getInt(key,def)}");add(l);val s=SeekBar(this).apply{this.max=limit-min;progress=p.getInt(key,def)-min;setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{override fun onStartTrackingTouch(s:SeekBar){};override fun onStopTrackingTouch(s:SeekBar){};override fun onProgressChanged(s:SeekBar,v:Int,user:Boolean){p.edit().putInt(key,v+min).apply();l.text="$title: ${v+min}";if(key=="clockSize")preview.textSize=(v+min).toFloat()}})};add(s)}
 slider("clockSize","Dimensione ora",20,80,38);slider("dateSize","Dimensione data",10,32,14);color("clockColor","Colore ora",Color.WHITE);color("dateColor","Colore data",Color.WHITE);color("bg","Sfondo",Color.BLACK);slider("snooze","Snooze (minuti)",1,60,5);add(label("Carattere"));val fonts=Spinner(this);fonts.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("Moderno","Classico","Digitale"));fonts.setSelection(p.getInt("font",0));add(fonts);add(button("SALVA IMPOSTAZIONI",green){p.edit().putInt("font",fonts.selectedItemPosition).apply();SvegliatiWidgetProvider.refresh(this);home()},54);add(button("Indietro"){home()})}
}
