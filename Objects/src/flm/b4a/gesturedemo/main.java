package flm.b4a.gesturedemo;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;
    public static boolean dontPause;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mostCurrent = this;
		if (processBA == null) {
			processBA = new BA(this.getApplicationContext(), null, null, "flm.b4a.gesturedemo", "flm.b4a.gesturedemo.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.setActivityPaused(true);
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(this, processBA, wl, true))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "flm.b4a.gesturedemo", "flm.b4a.gesturedemo.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "flm.b4a.gesturedemo.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEventFromUI(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null)
            return;
        if (this != mostCurrent)
			return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        if (!dontPause)
            BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        else
            BA.LogInfo("** Activity (main) Pause event (activity is not paused). **");
        if (mostCurrent != null)
            processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        if (!dontPause) {
            processBA.setActivityPaused(true);
            mostCurrent = null;
        }

        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
            main mc = mostCurrent;
			if (mc == null || mc != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
            if (mc != mostCurrent)
                return;
		    processBA.raiseEvent(mc._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        for (int i = 0;i < permissions.length;i++) {
            Object[] o = new Object[] {permissions[i], grantResults[i] == 0};
            processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
        }
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public flm.b4a.gesturedetector.GestureDetectorForB4A _gd = null;
public anywheresoftware.b4a.objects.drawable.CanvasWrapper _c = null;
public static int _newx = 0;
public static int _newy = 0;
public anywheresoftware.b4a.objects.collections.List _painters = null;
public static int[] _color = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static class _typpainter{
public boolean IsInitialized;
public int LastX;
public int LastY;
public int Color;
public void Initialize() {
IsInitialized = true;
LastX = 0;
LastY = 0;
Color = 0;
}
@Override
		public String toString() {
			return BA.TypeToString(this, false);
		}}
public static String  _activity_create(boolean _firsttime) throws Exception{
anywheresoftware.b4a.objects.LabelWrapper _lbl = null;
anywheresoftware.b4a.objects.ButtonWrapper _btn = null;
 //BA.debugLineNum = 26;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 28;BA.debugLine="Dim lbl As Label";
_lbl = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 29;BA.debugLine="lbl.Initialize(\"\")";
_lbl.Initialize(mostCurrent.activityBA,"");
 //BA.debugLineNum = 30;BA.debugLine="lbl.Text = \"Paint on the screen with one or many";
_lbl.setText(BA.ObjectToCharSequence("Paint on the screen with one or many fingers."));
 //BA.debugLineNum = 31;BA.debugLine="lbl.TextColor = Colors.White";
_lbl.setTextColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 32;BA.debugLine="lbl.TextSize = 18";
_lbl.setTextSize((float) (18));
 //BA.debugLineNum = 33;BA.debugLine="Activity.AddView(lbl, 15dip, 15dip, 100%x-30dip,";
mostCurrent._activity.AddView((android.view.View)(_lbl.getObject()),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (15)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (15)),(int) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (30))),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60)));
 //BA.debugLineNum = 34;BA.debugLine="Dim btn As Button";
_btn = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 35;BA.debugLine="btn.Initialize(\"Clear\")";
_btn.Initialize(mostCurrent.activityBA,"Clear");
 //BA.debugLineNum = 36;BA.debugLine="btn.Text = \"Clear\"";
_btn.setText(BA.ObjectToCharSequence("Clear"));
 //BA.debugLineNum = 37;BA.debugLine="btn.TextSize = 18";
_btn.setTextSize((float) (18));
 //BA.debugLineNum = 38;BA.debugLine="Activity.AddView(btn, 100%x - 100dip, 100%y - 60d";
mostCurrent._activity.AddView((android.view.View)(_btn.getObject()),(int) (anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (100))),(int) (anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (100),mostCurrent.activityBA)-anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60))),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (100)),anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (60)));
 //BA.debugLineNum = 41;BA.debugLine="C.Initialize(Activity)";
mostCurrent._c.Initialize((android.view.View)(mostCurrent._activity.getObject()));
 //BA.debugLineNum = 42;BA.debugLine="C.DrawColor(Colors.Black)";
mostCurrent._c.DrawColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 45;BA.debugLine="Painters.Initialize";
mostCurrent._painters.Initialize();
 //BA.debugLineNum = 48;BA.debugLine="GD.SetOnGestureListener(Activity, \"Gesture\")";
mostCurrent._gd.SetOnGestureListener(processBA,(android.view.View)(mostCurrent._activity.getObject()),"Gesture");
 //BA.debugLineNum = 49;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 54;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 55;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 51;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 52;BA.debugLine="End Sub";
return "";
}
public static String  _clear_click() throws Exception{
 //BA.debugLineNum = 108;BA.debugLine="Sub Clear_Click";
 //BA.debugLineNum = 110;BA.debugLine="C.DrawColor(Colors.Black)";
mostCurrent._c.DrawColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 111;BA.debugLine="Activity.Invalidate";
mostCurrent._activity.Invalidate();
 //BA.debugLineNum = 112;BA.debugLine="End Sub";
return "";
}
public static String  _gesture_ondown(float _x,float _y,Object _motionevent) throws Exception{
 //BA.debugLineNum = 57;BA.debugLine="Sub Gesture_onDown(X As Float, Y As Float, MotionE";
 //BA.debugLineNum = 59;BA.debugLine="Gesture_onPointerDown(0, 0, MotionEvent)";
_gesture_onpointerdown((int) (0),(int) (0),_motionevent);
 //BA.debugLineNum = 60;BA.debugLine="End Sub";
return "";
}
public static String  _gesture_ondrag(float _deltax,float _deltay,Object _motionevent) throws Exception{
int _p = 0;
flm.b4a.gesturedemo.main._typpainter _painter = null;
 //BA.debugLineNum = 89;BA.debugLine="Sub Gesture_onDrag(deltaX As Float, deltaY As Floa";
 //BA.debugLineNum = 91;BA.debugLine="For p = 0 To Painters.Size - 1";
{
final int step1 = 1;
final int limit1 = (int) (mostCurrent._painters.getSize()-1);
_p = (int) (0) ;
for (;_p <= limit1 ;_p = _p + step1 ) {
 //BA.debugLineNum = 92;BA.debugLine="Try";
try { //BA.debugLineNum = 93;BA.debugLine="Dim Painter As typPainter";
_painter = new flm.b4a.gesturedemo.main._typpainter();
 //BA.debugLineNum = 94;BA.debugLine="Painter = Painters.Get(p)";
_painter = (flm.b4a.gesturedemo.main._typpainter)(mostCurrent._painters.Get(_p));
 //BA.debugLineNum = 95;BA.debugLine="newX = GD.getX(MotionEvent, p)";
_newx = (int) (mostCurrent._gd.getX((android.view.MotionEvent)(_motionevent),_p));
 //BA.debugLineNum = 96;BA.debugLine="newY = GD.getY(MotionEvent, p)";
_newy = (int) (mostCurrent._gd.getY((android.view.MotionEvent)(_motionevent),_p));
 //BA.debugLineNum = 97;BA.debugLine="C.DrawLine(Painter.LastX, Painter.LastY, newX,";
mostCurrent._c.DrawLine((float) (_painter.LastX /*int*/ ),(float) (_painter.LastY /*int*/ ),(float) (_newx),(float) (_newy),_painter.Color /*int*/ ,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (20))));
 //BA.debugLineNum = 98;BA.debugLine="C.DrawCircle(newX, newY, 10dip, Painter.Color,";
mostCurrent._c.DrawCircle((float) (_newx),(float) (_newy),(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))),_painter.Color /*int*/ ,anywheresoftware.b4a.keywords.Common.True,(float) (0));
 //BA.debugLineNum = 99;BA.debugLine="Painter.LastX = newX";
_painter.LastX /*int*/  = _newx;
 //BA.debugLineNum = 100;BA.debugLine="Painter.LastY = newY";
_painter.LastY /*int*/  = _newy;
 //BA.debugLineNum = 101;BA.debugLine="Activity.Invalidate";
mostCurrent._activity.Invalidate();
 } 
       catch (Exception e13) {
			processBA.setLastException(e13); //BA.debugLineNum = 103;BA.debugLine="Log(LastException)";
anywheresoftware.b4a.keywords.Common.LogImpl("4589838",BA.ObjectToString(anywheresoftware.b4a.keywords.Common.LastException(mostCurrent.activityBA)),0);
 };
 }
};
 //BA.debugLineNum = 106;BA.debugLine="End Sub";
return "";
}
public static String  _gesture_onpointerdown(int _ptrindex,int _pid,Object _motionevent) throws Exception{
flm.b4a.gesturedemo.main._typpainter _newpainter = null;
 //BA.debugLineNum = 62;BA.debugLine="Sub Gesture_onPointerDown(ptrIndex As Int, PID As";
 //BA.debugLineNum = 64;BA.debugLine="Log(\"Pointer DOWN = \" & ptrIndex & \" PID = \" & PI";
anywheresoftware.b4a.keywords.Common.LogImpl("4393218","Pointer DOWN = "+BA.NumberToString(_ptrindex)+" PID = "+BA.NumberToString(_pid),0);
 //BA.debugLineNum = 65;BA.debugLine="Dim NewPainter As typPainter";
_newpainter = new flm.b4a.gesturedemo.main._typpainter();
 //BA.debugLineNum = 66;BA.debugLine="NewPainter.Initialize";
_newpainter.Initialize();
 //BA.debugLineNum = 67;BA.debugLine="NewPainter.LastX = GD.getX(MotionEvent, ptrIndex)";
_newpainter.LastX /*int*/  = (int) (mostCurrent._gd.getX((android.view.MotionEvent)(_motionevent),_ptrindex));
 //BA.debugLineNum = 68;BA.debugLine="NewPainter.LastY = GD.getY(MotionEvent, ptrIndex)";
_newpainter.LastY /*int*/  = (int) (mostCurrent._gd.getY((android.view.MotionEvent)(_motionevent),_ptrindex));
 //BA.debugLineNum = 69;BA.debugLine="NewPainter.Color = Color(Min(ptrIndex, 8))";
_newpainter.Color /*int*/  = _color[(int) (anywheresoftware.b4a.keywords.Common.Min(_ptrindex,8))];
 //BA.debugLineNum = 70;BA.debugLine="Painters.InsertAt(ptrIndex, NewPainter)";
mostCurrent._painters.InsertAt(_ptrindex,(Object)(_newpainter));
 //BA.debugLineNum = 71;BA.debugLine="C.DrawCircle(NewPainter.LastX, NewPainter.LastY,";
mostCurrent._c.DrawCircle((float) (_newpainter.LastX /*int*/ ),(float) (_newpainter.LastY /*int*/ ),(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10))),_newpainter.Color /*int*/ ,anywheresoftware.b4a.keywords.Common.True,(float) (0));
 //BA.debugLineNum = 72;BA.debugLine="Activity.Invalidate";
mostCurrent._activity.Invalidate();
 //BA.debugLineNum = 73;BA.debugLine="End Sub";
return "";
}
public static String  _gesture_onpointerup(int _ptrindex,int _pid,Object _motionevent) throws Exception{
 //BA.debugLineNum = 83;BA.debugLine="Sub Gesture_onPointerUp(ptrIndex As Int, PID As In";
 //BA.debugLineNum = 85;BA.debugLine="Log(\"Pointer UP = \" & ptrIndex & \" PID = \" & PID)";
anywheresoftware.b4a.keywords.Common.LogImpl("4524290","Pointer UP = "+BA.NumberToString(_ptrindex)+" PID = "+BA.NumberToString(_pid),0);
 //BA.debugLineNum = 86;BA.debugLine="Painters.RemoveAt(ptrIndex)";
mostCurrent._painters.RemoveAt(_ptrindex);
 //BA.debugLineNum = 87;BA.debugLine="End Sub";
return "";
}
public static boolean  _gesture_ontouch(int _action,float _x,float _y,Object _motionevent) throws Exception{
 //BA.debugLineNum = 75;BA.debugLine="Sub Gesture_onTouch(Action As Int, X As Float, Y A";
 //BA.debugLineNum = 76;BA.debugLine="If Action = GD.ACTION_UP Then";
if (_action==mostCurrent._gd.ACTION_UP) { 
 //BA.debugLineNum = 78;BA.debugLine="Gesture_onPointerUp(0, 0, MotionEvent)";
_gesture_onpointerup((int) (0),(int) (0),_motionevent);
 };
 //BA.debugLineNum = 80;BA.debugLine="Return True";
if (true) return anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 81;BA.debugLine="End Sub";
return false;
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 18;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 19;BA.debugLine="Dim GD As GestureDetector";
mostCurrent._gd = new flm.b4a.gesturedetector.GestureDetectorForB4A();
 //BA.debugLineNum = 20;BA.debugLine="Dim C As Canvas";
mostCurrent._c = new anywheresoftware.b4a.objects.drawable.CanvasWrapper();
 //BA.debugLineNum = 21;BA.debugLine="Dim newX, newY As Int";
_newx = 0;
_newy = 0;
 //BA.debugLineNum = 22;BA.debugLine="Dim Painters As List";
mostCurrent._painters = new anywheresoftware.b4a.objects.collections.List();
 //BA.debugLineNum = 23;BA.debugLine="Dim Color(8) As Int = Array As Int(Colors.Red, Co";
_color = new int[]{anywheresoftware.b4a.keywords.Common.Colors.Red,anywheresoftware.b4a.keywords.Common.Colors.Cyan,anywheresoftware.b4a.keywords.Common.Colors.Green,anywheresoftware.b4a.keywords.Common.Colors.Yellow,anywheresoftware.b4a.keywords.Common.Colors.Magenta,anywheresoftware.b4a.keywords.Common.Colors.Blue,anywheresoftware.b4a.keywords.Common.Colors.Gray,anywheresoftware.b4a.keywords.Common.Colors.White};
 //BA.debugLineNum = 24;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 12;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 15;BA.debugLine="Type typPainter(LastX As Int, LastY As Int, Color";
;
 //BA.debugLineNum = 16;BA.debugLine="End Sub";
return "";
}
}
