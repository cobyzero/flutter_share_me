package zhuoyuan.li.fluttershareme;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterShareMePlugin
 */
public class FlutterShareMePlugin implements MethodCallHandler, FlutterPlugin, ActivityAware {

    final private static String _methodWhatsApp = "whatsapp_share";
    final private static String _methodWhatsAppPersonal = "whatsapp_personal";
    final private static String _methodWhatsAppBusiness = "whatsapp_business_share";
     
    private Activity activity;
    private MethodChannel methodChannel;
 
    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        onAttachedToEngine(binding.getBinaryMessenger());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
        methodChannel = null;
        activity = null;
    }

    private void onAttachedToEngine(BinaryMessenger messenger) {
        methodChannel = new MethodChannel(messenger, "flutter_share_me");
        methodChannel.setMethodCallHandler(this);
         
    }

    /**
     * method
     *
     * @param call   methodCall
     * @param result Result
     */
    @Override
    public void onMethodCall(MethodCall call, @NonNull Result result) {
        String url, msg, fileType;
        switch (call.method) { 
            case _methodWhatsApp:
                msg = call.argument("msg");
                url = call.argument("url");
                shareWhatsApp(url, msg, result, false);
                break;
            case _methodWhatsAppBusiness:
                msg = call.argument("msg");
                url = call.argument("url");
                shareWhatsApp(url, msg, result, true);
                break;
            case _methodWhatsAppPersonal:
                msg = call.argument("msg");
                String phoneNumber = call.argument("phoneNumber");
                shareWhatsAppPersonal(msg, phoneNumber, result);
                break; 
            default:
                result.notImplemented();
                break;
        }
    }
 
 
    /**
     * share to whatsapp
     *
     * @param msg                String
     * @param result             Result
     * @param shareToWhatsAppBiz boolean
     */
    private void shareWhatsApp(String imagePath, String msg, Result result, boolean shareToWhatsAppBiz) {
        try {
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            
            whatsappIntent.setPackage(shareToWhatsAppBiz ? "com.whatsapp.w4b" : "com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, msg);
            // if the url is the not empty then get url of the file and share
            if (!TextUtils.isEmpty(imagePath)) {
                whatsappIntent.setType("*/*");
                System.out.print(imagePath+"url is not empty");
                File file = new File(imagePath);
                Uri fileUri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", file);
                whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                whatsappIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            else {
                whatsappIntent.setType("text/plain");
            }
            activity.startActivity(whatsappIntent);
            result.success("success");
        } catch (Exception var9) {
            result.error("error", var9.toString(), "");
        }
    }
    
    /**
     * share whatsapp message to personal number
     *
     * @param msg         String
     * @param phoneNumber String with country code
     * @param result
     */
    private void shareWhatsAppPersonal(String msg, String phoneNumber, Result result) {
        String url = null;
        try {
            url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + URLEncoder.encode(msg, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setPackage("com.whatsapp");
        i.setData(Uri.parse(url));
        activity.startActivity(i);
        result.success("success");
    }

    

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {

    } 
}
