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
 fun add(v:View,h:Int=-2){root.addView(v,LinearLayout.LayoutParams(-1,if(h<0) h else dp(h)).apply{bottomMargin=dp(5)})}
 fun page(title:String){window.statusBarColor=Color.BLACK;window.navigationBarColor=Color.BLACK; val scroll=ScrollView(this).apply{setBackgroundColor(Color.BLACK);fitsSystemWindows=true};root=LinearLayout(this).apply{orientation=1;setPadding(dp(12),dp(8),dp(12),dp(10))};scroll.addView(root);setContentView(scroll);val row=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL};row.addView(label(title,17f).apply{typeface=Typeface.create("sans-serif-light",Typeface.NORMAL);letterSpacing=0.12f},LinearLayout.LayoutParams(0,dp(42),1f));row.addView(button("⚙"){settings()},LinearLayout.LayoutParams(dp(48),dp(40)));add(row)}
 override fun onCreate(b:Bundle?){super.onCreate(b); if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),1); home()}
 override fun onResume(){super.onResume(); if(Schedule.exact(this))Store.all(this).forEach{Schedule.next(this,it)}}
 fun fresh():Alarm{val n=Calendar.getInstance();val list=Store.all(this);return Alarm((list.maxOfOrNull{it.id}?:0)+1,n.get(Calendar.HOUR_OF_DAY),n.get(Calendar.MINUTE),0,false,"","","","","")}
 fun home(){val list=Store.all(this);edit(list.firstOrNull()?:fresh())}
 fun edit(a:Alarm){editing=a;selectedSound=a.sound;selectedVoice=a.voice;page("SVEGLIATI");val list=Store.all(this);if(list.size>1){val tabs=HorizontalScrollView(this);val row=LinearLayout(this);list.forEach{item->row.addView(button(item.name.ifBlank{"SVEGLIA ${item.id}"},if(item.id==a.id)green else panel){edit(item)})};tabs.addView(row);add(tabs)}
 val wheels=LinearLayout(this).apply{gravity=Gravity.CENTER; background=bg(panel)}
 val digitSize=Store.prefs(this).getInt("pickerSize",72).toFloat()
 fun wheel(limit:Int,initial:Int,description:String)=ScrollDigit(limit,initial,description,digitSize)
 val hours=wheel(23,a.h,"Ore");val mins=wheel(59,a.m,"Minuti");val wheelHeight=maxOf(88,digitSize.toInt()+16)
 wheels.addView(hours,LinearLayout.LayoutParams(0,dp(wheelHeight),1f));wheels.addView(label(":",digitSize).apply{setTextColor(green)});wheels.addView(mins,LinearLayout.LayoutParams(0,dp(wheelHeight),1f));add(wheels)

 var days=a.days;val week=LinearLayout(this).apply{gravity=Gravity.CENTER};listOf("Lu","Ma","Me","Gi","Ve","Sa","Do").forEachIndexed{i,s->val v=label(s,14f).apply{gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD; background=bg(if(days and (1 shl i)!=0)green else panel,40);setOnClickListener{days=days xor (1 shl i);background=bg(if(days and (1 shl i)!=0)green else panel,40)}};week.addView(v,LinearLayout.LayoutParams(0,dp(42),1f).apply{setMargins(dp(2),0,dp(2),0)})};add(week)
 var enabled=a.enabled
 val toggle=button(if(enabled) "ON" else "OFF",if(enabled)green else red){}
 toggle.setOnClickListener{enabled=!enabled;toggle.text=if(enabled) "ON" else "OFF";toggle.background=bg(if(enabled)green else red)}
 add(toggle,40)
 fun field(hint:String,value:String)=EditText(this).apply{this.hint=hint;setText(value);setSingleLine();textSize=15f;setPadding(dp(8),0,dp(8),0);setTextColor(Color.WHITE);setHintTextColor(Color.GRAY)}
 val name=field("SVEGLIA ${a.id}",a.name);val g=field("SVEGLIATI !",a.green);val r=field("TI SEI ALZATO ?",a.red)
 val actions=LinearLayout(this)
 actions.addView(button("Salva",green){val updated=a.copy(h=hours.value,m=mins.value,days=days,enabled=enabled,name=name.text.toString(),green=g.text.toString(),red=r.text.toString(),sound=selectedSound,voice=selectedVoice);Store.put(this,updated);Schedule.cancel(this,a.id,true);Schedule.next(this,updated);if(updated.enabled&&!Schedule.exact(this)){AlertDialog.Builder(this).setMessage("Consenti sveglie esatte per attivare la sveglia.").setPositiveButton("Impostazioni"){_,_->if(Build.VERSION.SDK_INT>=31)startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:$packageName")))}.show()}else Toast.makeText(this,"Sveglia salvata",Toast.LENGTH_SHORT).show();editing=updated},LinearLayout.LayoutParams(0,dp(44),1f).apply{rightMargin=dp(4)})
 actions.addView(button("Elimina",red){Schedule.cancel(this,a.id);Schedule.cancel(this,a.id,true);Store.save(this,Store.all(this).filter{it.id!=a.id});home()},LinearLayout.LayoutParams(0,dp(44),1f).apply{leftMargin=dp(4)});add(actions)
 fun compactField(title:String,field:EditText){val row=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL};row.addView(label(title,12f),LinearLayout.LayoutParams(dp(108),dp(40)));row.addView(field,LinearLayout.LayoutParams(0,dp(40),1f));add(row)}
 compactField("Nome sveglia",name);compactField("Testo verde",g);compactField("Testo rosso",r)
 val soundRow=LinearLayout(this);val soundButton=button("▣  Suoneria"){startActivityForResult(Intent(RingtoneManager.ACTION_RINGTONE_PICKER).putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM).putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,false),20)};soundRow.addView(soundButton,LinearLayout.LayoutParams(0,dp(44),1f));soundRow.addView(button("👄  Voce"){recordDialog()},LinearLayout.LayoutParams(0,dp(44),1f));add(soundRow);add(button("＋",green){edit(fresh())},40)
 }
 inner class ScrollDigit(private val limit:Int,initial:Int,private val description:String,size:Float):TextView(this@MainActivity){
 var value=initial;private set
 private var anchor=0f
 init{gravity=Gravity.CENTER;textSize=size;setTextColor(Color.WHITE);typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL);includeFontPadding=false;isClickable=true;isFocusable=true;update()}
 private fun update(){text=String.format(java.util.Locale.ITALY,"%02d",value);contentDescription="$description: $text"}
 private fun step(delta:Int){value=(value+delta+limit+1)%(limit+1);update()}
 override fun onTouchEvent(event:MotionEvent):Boolean{when(event.actionMasked){
 MotionEvent.ACTION_DOWN->{anchor=event.y;parent.requestDisallowInterceptTouchEvent(true);return true}
 MotionEvent.ACTION_MOVE->{val distance=anchor-event.y;val steps=(distance/dp(22)).toInt();if(steps!=0){step(steps%(limit+1));anchor-=steps*dp(22)};return true}
 MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{parent.requestDisallowInterceptTouchEvent(false);return true}
 };return true}
 override fun onInitializeAccessibilityNodeInfo(info:android.view.accessibility.AccessibilityNodeInfo){super.onInitializeAccessibilityNodeInfo(info);info.isScrollable=true;info.addAction(4096);info.addAction(8192)}
 override fun performAccessibilityAction(action:Int,args:Bundle?):Boolean{when(action){4096->{step(1);return true};8192->{step(-1);return true}};return super.performAccessibilityAction(action,args)}
 override fun onKeyDown(keyCode:Int,event:KeyEvent):Boolean{when(keyCode){KeyEvent.KEYCODE_DPAD_UP->{step(1);return true};KeyEvent.KEYCODE_DPAD_DOWN->{step(-1);return true}};return super.onKeyDown(keyCode,event)}
 }
 override fun onActivityResult(req:Int,res:Int,data:Intent?){super.onActivityResult(req,res,data);if(req==20&&res==RESULT_OK){val uri=data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);selectedSound=uri?.toString().orEmpty();selectedVoice="";Toast.makeText(this,"Suoneria selezionata",Toast.LENGTH_SHORT).show()}}
 fun recordDialog(){if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),21);return};val f=File(filesDir,"voice_${editing?.id}_${System.currentTimeMillis()}.m4a");try{recorder=MediaRecorder().apply{setAudioSource(MediaRecorder.AudioSource.MIC);setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);setAudioEncoder(MediaRecorder.AudioEncoder.AAC);setOutputFile(f.absolutePath);prepare();start()};AlertDialog.Builder(this).setTitle("Registrazione in corso").setMessage("Pronuncia il tuo messaggio vocale.").setCancelable(false).setPositiveButton("Ferma e usa"){_,_->try{recorder?.stop();selectedVoice=f.absolutePath}catch(e:Exception){f.delete()}finally{recorder?.release();recorder=null}}.setNegativeButton("Annulla"){_,_->recorder?.release();recorder=null;f.delete()}.show()}catch(e:Exception){recorder?.release();recorder=null;Toast.makeText(this,"Registrazione non disponibile",Toast.LENGTH_LONG).show()}}
 fun settings(){page("Impostazioni");val p=Store.prefs(this);val preview=LinearLayout(this).apply{orientation=1;gravity=Gravity.CENTER;setBackgroundColor(p.getInt("bg",Color.BLACK))};val previewTime=label("07:30",p.getInt("clockSize",38).toFloat()).apply{gravity=Gravity.CENTER;setTextColor(p.getInt("clockColor",Color.WHITE))};val previewDate=label("Mer 02 MAR",p.getInt("dateSize",14).toFloat()).apply{gravity=Gravity.CENTER;setTextColor(p.getInt("dateColor",Color.WHITE))};preview.addView(previewTime);preview.addView(previewDate);add(preview);val colors=listOf("Nero","Bianco","Verde","Rosso","Blu","Giallo","Grigio");val values=listOf(Color.BLACK,Color.WHITE,0xff35d369.toInt(),Color.RED,0xff448aff.toInt(),Color.YELLOW,Color.GRAY);fun color(key:String,title:String,def:Int){add(label(title));val s=Spinner(this);s.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,colors);s.setSelection(values.indexOf(p.getInt(key,def)).coerceAtLeast(0));s.onItemSelectedListener=object:android.widget.AdapterView.OnItemSelectedListener{override fun onNothingSelected(parent:AdapterView<*>?){};override fun onItemSelected(parent:AdapterView<*>?,v:View?,pos:Int,id:Long){p.edit().putInt(key,values[pos]).apply();preview.setBackgroundColor(p.getInt("bg",Color.BLACK));previewTime.setTextColor(p.getInt("clockColor",Color.WHITE));previewDate.setTextColor(p.getInt("dateColor",Color.WHITE))}};add(s)}
 fun slider(key:String,title:String,min:Int,limit:Int,def:Int){val l=label("$title: ${p.getInt(key,def)}");add(l);val s=SeekBar(this).apply{this.max=limit-min;progress=p.getInt(key,def)-min;setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{override fun onStartTrackingTouch(s:SeekBar){};override fun onStopTrackingTouch(s:SeekBar){};override fun onProgressChanged(s:SeekBar,v:Int,user:Boolean){p.edit().putInt(key,v+min).apply();l.text="$title: ${v+min}";if(key=="clockSize")previewTime.textSize=(v+min).toFloat();if(key=="dateSize")previewDate.textSize=(v+min).toFloat()}})};add(s)}
 slider("clockSize","Dimensione ora",20,120,38);slider("dateSize","Dimensione data",10,60,14);slider("pickerSize","Dimensione numeri sveglia",36,100,72);color("clockColor","Colore ora",Color.WHITE);color("dateColor","Colore data",Color.WHITE);color("bg","Sfondo",Color.BLACK);slider("snooze","Snooze (minuti)",1,60,5);add(label("Carattere"));val fonts=Spinner(this);fonts.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("Moderno","Classico","Digitale"));fonts.setSelection(p.getInt("font",0));add(fonts);add(button("SALVA IMPOSTAZIONI",green){p.edit().putInt("font",fonts.selectedItemPosition).apply();SvegliatiWidgetProvider.refresh(this);home()},54);add(button("Indietro"){home()})}
}
