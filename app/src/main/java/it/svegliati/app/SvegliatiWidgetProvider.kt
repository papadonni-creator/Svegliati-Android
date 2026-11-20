package it.svegliati.app
import android.app.PendingIntent
import android.appwidget.*
import android.content.*
import android.graphics.Color
import android.widget.RemoteViews
class SvegliatiWidgetProvider:AppWidgetProvider(){
 override fun onUpdate(c:Context,m:AppWidgetManager,ids:IntArray){ids.forEach{update(c,m,it)}}
 companion object{
 fun refresh(c:Context){val m=AppWidgetManager.getInstance(c);m.getAppWidgetIds(ComponentName(c,SvegliatiWidgetProvider::class.java)).forEach{update(c,m,it)}}
 fun update(c:Context,m:AppWidgetManager,id:Int){val p=Store.prefs(c);val layout=when(p.getInt("font",0)){1->R.layout.widget_serif;2->R.layout.widget_mono;else->R.layout.svegliati_widget};val v=RemoteViews(c.packageName,layout);v.setTextColor(R.id.widget_time,p.getInt("clockColor",Color.WHITE));v.setTextColor(R.id.widget_date,p.getInt("dateColor",Color.WHITE));v.setTextViewTextSize(R.id.widget_time,2,p.getInt("clockSize",38).toFloat());v.setTextViewTextSize(R.id.widget_date,2,p.getInt("dateSize",14).toFloat());v.setInt(R.id.widget_root,"setBackgroundColor",p.getInt("bg",Color.BLACK));v.setOnClickPendingIntent(R.id.widget_root,PendingIntent.getActivity(c,0,Intent(c,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE));m.updateAppWidget(id,v)}
 }}
