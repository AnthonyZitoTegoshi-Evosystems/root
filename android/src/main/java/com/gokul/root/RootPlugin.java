package com.gokul.root;
import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import com.stericson.RootTools.RootTools;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ShellUtils;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** RootPlugin */
public class RootPlugin implements FlutterPlugin, MethodCallHandler {

  private MethodChannel methodChannel;
  private List<String> resultText;
  private String command;
  private StringBuilder stringBuilder;


  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    methodChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "root");
    methodChannel.setMethodCallHandler(this);
    Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_MOUNT_MASTER));
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call,@NonNull Result result) {
    if (call.method.equals("ExecuteCommand")) {
       command=call.argument("cmd");
       resultText=Shell.cmd(command).exec().getOut();
       stringBuilder=new StringBuilder();
      for(String data : resultText){
        stringBuilder.append(data);
        stringBuilder.append("\n");
       }
      result.success(String.format("%s",stringBuilder));
    } else if(call.method.equals("isRooted")){
      result.success(isRooted());
    } else if(call.method.equals("isRootAvailable")){
      result.success(isRootAvailable());
    } else{
      result.notImplemented();
    }


  }
  private boolean isRooted() {
    return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
  }
  private boolean isRootAvailable() {
    return RootTools.isRootAvailable();
  }

  private static boolean checkRootMethod1() {
      String buildTags = android.os.Build.TAGS;
      return buildTags != null && buildTags.contains("test-keys");
  }

  private static boolean checkRootMethod2() {
      String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
              "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
      for (String path : paths) {
          if (new File(path).exists()) return true;
      }
      return false;
  }

  private static boolean checkRootMethod3() {
      Process process = null;
      try {
          process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
          BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
          if (in.readLine() != null) return true;
          return false;
      } catch (Throwable t) {
          return false;
      } finally {
          if (process != null) process.destroy();
      }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    methodChannel.setMethodCallHandler(null);
  }
}
